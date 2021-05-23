package dz.ngnex.view;

import dz.ngnex.bean.ContractBean;
import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.bean.SeasonBean;
import dz.ngnex.control.LocaleManager;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.*;
import dz.ngnex.security.WriteAccess;
import dz.ngnex.util.Messages;
import dz.ngnex.util.TemplatedContent;
import dz.ngnex.util.ViewModel;
import dz.ngnex.util.WebKit;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.*;

@ViewModel
public class TemplateView implements Serializable {
  private static final long serialVersionUID = 6361241281842611738L;

  @EJB
  private ContractBean contractBean;

  @EJB
  private PrincipalBean principalBean;

  private final List<PropertyEntity> properties = new ArrayList<>();
  private PropertyEntity currentProperty;

  private final List<ActivityEntity> activities = new ArrayList<>();
  private ActivityEntity currentActivity;

  @Size(min = Constrains.Min_IDENTIFIER_SIZE, max = Constrains.MAX_IDENTIFIER_SIZE)
  private String newPropertyName;
  private InputType newPropertyType = InputType.TEXT;

  @Size(min = Constrains.Min_IDENTIFIER_SIZE, max = Constrains.MAX_IDENTIFIER_SIZE)
  private String newActivityName;

  @Size(min = Constrains.Min_IDENTIFIER_SIZE, max = Constrains.MAX_IDENTIFIER_SIZE)
  private String propertyName;
  private InputType propertyType = InputType.TEXT;
  private WriteAccess propertyAccess;
  private RequiredLevel propertyRequiredLevel;
  @Size(min = Constrains.Min_IDENTIFIER_SIZE, max = Constrains.MAX_PHRASE_LENGTH)
  private String propertyLabel;
  @Size(max = Constrains.MAX_TEXT_LENGTH)
  private String propertyDefaultValue;
  @Size(max = Constrains.MAX_TEXT_LENGTH)
  private String propertyDefaultHeader;
  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  private String propertyPrototype;

  @Size(min = Constrains.Min_IDENTIFIER_SIZE, max = Constrains.MAX_IDENTIFIER_SIZE)
  private String activityName;
  @Size(max = Constrains.MAX_TEXT_LENGTH)
  private String activityLabel;
  @Size(max = Constrains.MAX_TEXT_LENGTH)
  private String activityHeader;
  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  private String activitySectionPrototype;
  @NotNull
  @Max(10)
  @Min(0)
  private Integer activityMinVentilation;

  private ContractTemplateEntity currentTemplate;
  private List<ContractTemplateEntity> templates;
  private List<TemplateDetails> templatesSources;

  @Size(min = Constrains.Min_IDENTIFIER_SIZE, max = Constrains.MAX_IDENTIFIER_SIZE)
  private String updatedTemplateName;

  @Size(min = Constrains.Min_IDENTIFIER_SIZE, max = Constrains.MAX_IDENTIFIER_SIZE)
  private String newTemplateName;
  private Integer sourceTemplateID;

  @Size(max = Constrains.MAX_TEXT_LENGTH)
  private String template;

  @FutureOrPresent
  private Date downloadDate;

  private String currentSeasonName;
  private Integer currentSeasonID;

  @Inject
  Meta meta;

  @Inject
  LocaleManager localeManager;

  @EJB
  SeasonBean seasonManager;

  @Inject
  private Messages messages;

  @PostConstruct
  private void init() {
    initCurrentSeason();
    initAccordingToCurrentYear();
  }

  private void initCurrentSeason() {
    SeasonEntity currentSeason = contractBean.getCurrentSeason();
    if (currentSeason != null) {
      currentSeasonName = currentSeason.getName();
      currentSeasonID = currentSeason.getId();
    } else {
      currentSeasonName = messages.get("nothing");
      currentSeasonID = null;
    }
  }

  public void initAccordingToCurrentYear() {
    fetchTemplates();
    selectFirstTemplate();

    initNewTemplate();
    initNewProperty();
    initNewActivity();
  }

  private void selectFirstTemplate() {
    List<ContractTemplateEntity> templates = getTemplates();
    if (templates.isEmpty())
      setCurrentTemplateID(null);
    else
      setCurrentTemplateID(templates.get(0).getId());
  }

  private void fetchTemplates() {
    try {
      templates = contractBean.getTemplates(getCurrentSeasonID());
      templatesSources = contractBean.getSources();
    } catch (Exception e) {
      meta.handleException(e);
      templates = Collections.emptyList();
      templatesSources = Collections.emptyList();
    }
  }

  private void initNewTemplate() {
    newTemplateName = null;
    sourceTemplateID = contractBean.getEmptyTemplateDetails().getId();
  }

  private void initNewActivity() {
    newActivityName = "ventilation de decomposition " + (activities.size() + 1);
  }

  private void initNewProperty() {
    newPropertyName = "decomposition de budget " + (properties.size() + 1);
  }

  public ContractTemplateEntity getCurrentTemplate() {
    return currentTemplate;
  }

  public Integer getCurrentTemplateID() {
    if (currentTemplate != null && currentTemplate.getId() != null)
      return currentTemplate.getId();
    else
      return null;
  }

  public void setCurrentTemplateID(Integer id) {
    if (id == null) {
      currentTemplate = null;
      updatedTemplateName = null;
      template = null;
      downloadDate = null;

      properties.clear();
      activities.clear();
      setCurrentProperty(null);
      setCurrentActivity(null);
    } else
      try {
        ContractTemplateEntity template = contractBean.getSelectedTemplate(id);
        setCurrentTemplateHelper(template);
        setCurrentProperty(null);
        setCurrentActivity(null);
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

  private void setCurrentTemplateHelper(ContractTemplateEntity newTemplate) {
    currentTemplate = newTemplate;
    updatedTemplateName = newTemplate.getName();
    template = newTemplate.getTemplate();
    downloadDate = meta.asDate(newTemplate.getDownloadDate());

    properties.clear();
    properties.addAll(newTemplate.getProperties());
    Collections.sort(properties);

    activities.clear();
    activities.addAll(newTemplate.getActivities());
    Collections.sort(activities);
  }

  public void saveCurrentTemplate() {
    if (getCurrentTemplateID() != null) {
      try {
        currentTemplate.setTemplate(template);
        currentTemplate.setDownloadDate(meta.asEpoch(downloadDate));
        contractBean.updateTemplate(currentTemplate, WebKit.getUserPrincipalName());
        meta.dataUpdatedSuccessfully();
      } catch (Exception e) {
        meta.handleException(e);
      }
    }
  }

  public void renameTemplate() {
    if (getCurrentTemplateID() != null)
      try {
        ContractTemplateEntity renamedTemplate = contractBean.renameTemplate(currentTemplate, updatedTemplateName, WebKit.getUserPrincipalName());
        fetchTemplates();
        setCurrentTemplateHelper(renamedTemplate);
        meta.dataUpdatedSuccessfully();
        PrimeFaces.current().executeScript("PF('edit_template_dialog_widget').hide()");
      } catch (Exception e) {
        ContractTemplateEntity currentTemplate = getCurrentTemplate();
        updatedTemplateName = currentTemplate != null ? currentTemplate.getName() : null;
        meta.handleException(e);
      }
  }

  public void addNewTemplate() {
    try {
      ContractTemplateEntity newTemplate = contractBean.addNewTemplate(newTemplateName, sourceTemplateID, WebKit.getUserPrincipalName());
      fetchTemplates();
      setCurrentTemplateHelper(newTemplate);
      setCurrentProperty(null);
      setCurrentActivity(null);
      initNewTemplate();
      meta.dataUpdatedSuccessfully();
      PrimeFaces.current().executeScript("PF('add_template_dialog_widget').hide()");
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  public void deleteCurrentTemplate() {
    Integer currentTemplateId = getCurrentTemplateID();
    if (currentTemplateId != null)
      try {
        principalBean.deleteTemplate(currentTemplate.getId());
        contractBean.recalcAllContractDownloadState();
        meta.dataUpdatedSuccessfully();
      } catch (Exception e) {
        meta.handleException(e);
      }
    else
      meta.addGlobalError("noTemplateSelected", "selectTemplateThenTryAgain");

    fetchTemplates();
    selectFirstTemplate();
  }

  public long getUsageCountOfCurrentTemplate() {
    Integer currentTemplateID = getCurrentTemplateID();
    if (currentTemplateID != null)
      return contractBean.countContractInstances(currentTemplateID);
    else
      return 0;
  }

  public PropertyEntity getCurrentProperty() {
    return currentProperty;
  }

  public ActivityEntity getCurrentActivity() {
    return currentActivity;
  }

  public boolean isCurrentPropertyIsFirst() {
    if (currentProperty == null || currentProperty.getIndex() == null)
      return true;
    else {
      Integer firstIndex = currentProperty.getIndex();
      for (PropertyEntity property : properties)
        if (property.getIndex() < firstIndex)
          firstIndex = property.getIndex();
      return firstIndex.equals(currentProperty.getIndex());
    }
  }

  public boolean isCurrentPropertyIsLast() {
    if (currentProperty == null || currentProperty.getIndex() == null)
      return true;
    else {
      Integer lastIndex = currentProperty.getIndex();
      for (PropertyEntity property : properties)
        if (property.getIndex() > lastIndex)
          lastIndex = property.getIndex();
      return lastIndex.equals(currentProperty.getIndex());
    }
  }

  private void setCurrentPropertyByName(String name) {
    if (name != null && !name.isEmpty())
      for (PropertyEntity property : properties)
        if (name.equals(property.getName())) {
          setCurrentProperty(property);
          return;
        }

    setCurrentProperty(null);
  }

  private void setCurrentActivityByName(String name) {
    if (name != null && !name.isEmpty())
      for (ActivityEntity activity : activities)
        if (name.equals(activity.getName())) {
          setCurrentActivity(activity);
          return;
        }

    setCurrentProperty(null);
  }

  public void setCurrentProperty(PropertyEntity property) {
    if (property == null) {
      currentProperty = null;

      propertyName = null;
      propertyType = InputType.TEXT;
      propertyAccess = null;
      propertyRequiredLevel = RequiredLevel.NOT_REQUIRED;
      propertyLabel = null;
      propertyDefaultValue = null;
      propertyDefaultHeader = null;
      propertyPrototype = null;
    } else {
      currentProperty = property;

      propertyName = property.getName();
      propertyType = property.getType();
      propertyAccess = property.getAccess();
      propertyRequiredLevel = property.getRequired();
      propertyLabel = property.getLabel();
      propertyDefaultValue = property.getDefaultValue();
      propertyDefaultHeader = property.getDefaultHeader();
      propertyPrototype = TemplatedContent.brElementsToNewLines(property.getPrototype());
    }
  }

  public void setCurrentActivity(ActivityEntity activity) {
    if (activity == null) {
      currentActivity = null;
      activityName = null;
      activityLabel = null;
      activityHeader = null;
      activitySectionPrototype = null;
      activityMinVentilation = 0;
    } else {
      currentActivity = activity;
      activityName = activity.getName();
      activityLabel = activity.getLabel();
      activityHeader = activity.getHeader();
      activityMinVentilation = activity.getMinVentilationCount();
      activitySectionPrototype = TemplatedContent.brElementsToNewLines(activity.getSectionPrototype());
    }
  }

  public void saveCurrentProperty() {
    if (currentTemplate != null && currentProperty != null)
      try {
        currentProperty.setAccess(propertyAccess);
        currentProperty.setName(propertyName);
        currentProperty.setRequired(propertyRequiredLevel);
        currentProperty.setType(propertyType);
        currentProperty.setLabel(propertyLabel);
        currentProperty.setDefaultValue(propertyDefaultValue);
        currentProperty.setDefaultHeader(propertyDefaultHeader);
        currentProperty.setPrototype(propertyPrototype);
        PropertyEntity updatedProperty = contractBean.updateProperty(currentProperty);
        properties.remove(currentProperty);
        properties.add(updatedProperty);
        Collections.sort(properties);
        meta.dataUpdatedSuccessfully();
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

  public void saveCurrentActivity() {
    if (currentTemplate != null && currentActivity != null)
      try {
        currentActivity.setName(activityName);
        currentActivity.setLabel(activityLabel);
        currentActivity.setHeader(activityHeader);
        currentActivity.setSectionPrototype(activitySectionPrototype);
        currentActivity.setMinVentilationCount(activityMinVentilation);
        ActivityEntity updatedActivity = contractBean.updateActivity(currentActivity);
        activities.remove(currentActivity);
        activities.add(updatedActivity);
        Collections.sort(activities);
        meta.dataUpdatedSuccessfully();
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

  public void addNewProperty() {
    Integer currentTemplateID = getCurrentTemplateID();
    if (currentTemplateID != null)
      try {
        contractBean.addProperty(currentTemplateID, newPropertyName, newPropertyType);
        setCurrentTemplateHelper(contractBean.getSelectedTemplate(currentTemplateID));
        setCurrentPropertyByName(newPropertyName);
        meta.dataUpdatedSuccessfully();
        initNewProperty();
        PrimeFaces.current().executeScript("PF('add-property-dialog').hide()");
      } catch (Exception e) {
        meta.handleException(e);
      }
    else
      meta.addGlobalError("noTemplateSelected", "selectTemplateThenTryAgain");
  }

  public void addNewActivity() {
    Integer currentTemplateID = getCurrentTemplateID();
    if (currentTemplateID != null)
      try {
        contractBean.addActivity(currentTemplateID, newActivityName);
        setCurrentTemplateHelper(contractBean.getSelectedTemplate(currentTemplateID));
        setCurrentPropertyByName(newActivityName);
        meta.dataUpdatedSuccessfully();
        initNewActivity();
        PrimeFaces.current().executeScript("PF('add-activity-dialog').hide()");
      } catch (Exception e) {
        meta.handleException(e);
      }
    else
      meta.addGlobalError("noTemplateSelected", "selectTemplateThenTryAgain");
  }

  public void deleteCurrentProperty() {
    Integer currentTemplateID = getCurrentTemplateID();
    if (currentTemplateID != null)
      try {
        contractBean.deleteProperty(currentProperty);
        meta.dataUpdatedSuccessfully();
        setCurrentTemplateID(currentTemplateID);
      } catch (Exception e) {
        meta.handleException(e);
      }
    else
      meta.addGlobalError("noTemplateSelected", "selectTemplateThenTryAgain");
  }

  public void deleteCurrentActivity() {
    Integer currentTemplateID = getCurrentTemplateID();
    if (currentTemplateID != null)
      try {
        contractBean.deleteActivity(currentActivity);
        meta.dataUpdatedSuccessfully();
        setCurrentTemplateID(currentTemplateID);
      } catch (Exception e) {
        meta.handleException(e);
      }
    else
      meta.noSelectionError();
  }

  public void moveCurrentPropertyUp() {
    Integer currentTemplateID = getCurrentTemplateID();
    PropertyEntity selectedProperty = getCurrentProperty();
    if (currentTemplateID != null && selectedProperty.getId() != null)
      try {
        ContractTemplateEntity template = contractBean.movePropertyUp(currentTemplateID, selectedProperty.getId());
        setCurrentTemplateHelper(template);
        setCurrentPropertyByName(selectedProperty.getName());
        meta.dataUpdatedSuccessfully();
      } catch (Exception e) {
        meta.handleException(e);
      }
    else
      meta.noSelectionError();
  }

  public void moveCurrentPropertyDown() {
    Integer currentTemplateID = getCurrentTemplateID();
    PropertyEntity selectedProperty = getCurrentProperty();
    if (currentTemplateID != null && selectedProperty.getId() != null)
      try {
        ContractTemplateEntity template = contractBean.movePropertyDown(currentTemplateID, selectedProperty.getId());
        setCurrentTemplateHelper(template);
        setCurrentPropertyByName(selectedProperty.getName());
        meta.dataUpdatedSuccessfully();
      } catch (Exception e) {
        meta.handleException(e);
      }
    else
      meta.noSelectionError();
  }

  public String getUpdatedTemplateName() {
    return updatedTemplateName;
  }

  public void setUpdatedTemplateName(String updatedTemplateName) {
    this.updatedTemplateName = updatedTemplateName;
  }

  public String getNewTemplateName() {
    return newTemplateName;
  }

  public List<TemplateDetails> getAvailableTemplateSources() {
    return templatesSources;
  }

  public void setNewTemplateName(String newTemplateName) {
    this.newTemplateName = newTemplateName;
  }

  public Integer getSourceTemplateID() {
    return sourceTemplateID;
  }

  public void setSourceTemplateID(Integer sourceTemplateID) {
    this.sourceTemplateID = sourceTemplateID;
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public Date getDownloadDate() {
    return downloadDate;
  }

  public void setDownloadDate(Date downloadDate) {
    this.downloadDate = downloadDate;
  }

  public List<PropertyEntity> getProperties() {
    return properties;
  }

  public List<ActivityEntity> getActivities() {
    return activities;
  }

  public InputType[] getFieldTypes() {
    return InputType.values();
  }

  public String getNewPropertyName() {
    return newPropertyName;
  }

  public void setNewPropertyName(String newPropertyName) {
    this.newPropertyName = newPropertyName;
  }

  public InputType getNewPropertyType() {
    return newPropertyType;
  }

  public void setNewPropertyType(InputType newPropertyType) {
    this.newPropertyType = newPropertyType;
  }

  public String getNewActivityName() {
    return newActivityName;
  }

  public void setNewActivityName(String newActivityName) {
    this.newActivityName = newActivityName;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public InputType getPropertyType() {
    return propertyType;
  }

  public void setPropertyType(InputType propertyType) {
    this.propertyType = propertyType;
  }

  public WriteAccess getPropertyAccess() {
    return propertyAccess;
  }

  public void setPropertyAccess(WriteAccess propertyAccess) {
    this.propertyAccess = propertyAccess;
  }

  public RequiredLevel getPropertyRequiredLevel() {
    return propertyRequiredLevel;
  }

  public void setPropertyRequiredLevel(RequiredLevel propertyRequiredLevel) {
    this.propertyRequiredLevel = propertyRequiredLevel;
  }

  public WriteAccess[] getAccessLevels() {
    return WriteAccess.values();
  }

  public String getPropertyLabel() {
    return propertyLabel;
  }

  public void setPropertyLabel(String propertyLabel) {
    this.propertyLabel = propertyLabel;
  }

  public List<ContractTemplateEntity> getTemplates() {
    return templates;
  }

  public String getActivityName() {
    return activityName;
  }

  public void setActivityName(String activityName) {
    this.activityName = activityName;
  }

  public String getActivityLabel() {
    return activityLabel;
  }

  public void setActivityLabel(String activityLabel) {
    this.activityLabel = activityLabel;
  }

  public String getActivityHeader() {
    return activityHeader;
  }

  public void setActivityHeader(String activityHeader) {
    this.activityHeader = activityHeader;
  }

  public String getActivitySectionPrototype() {
    return activitySectionPrototype;
  }

  public void setActivitySectionPrototype(String activitySectionPrototype) {
    this.activitySectionPrototype = activitySectionPrototype;
  }

  public Integer getActivityMinVentilation() {
    return activityMinVentilation;
  }

  public void setActivityMinVentilation(Integer activityMinVentilation) {
    this.activityMinVentilation = activityMinVentilation;
  }

  public String getPropertyDefaultValue() {
    return propertyDefaultValue;
  }

  public void setPropertyDefaultValue(String propertyDefaultValue) {
    this.propertyDefaultValue = propertyDefaultValue;
  }

  public String getPropertyDefaultHeader() {
    return propertyDefaultHeader;
  }

  public void setPropertyDefaultHeader(String propertyDefaultHeader) {
    this.propertyDefaultHeader = propertyDefaultHeader;
  }

  public String getPropertyPrototype() {
    return propertyPrototype;
  }

  public void setPropertyPrototype(String propertyPrototype) {
    this.propertyPrototype = propertyPrototype;
  }

  public RequiredLevel[] getRequiredLevels() {
    return RequiredLevel.values();
  }

  public Contract.GeneralVars[] getGeneralVars() {
    return Contract.GeneralVars.values();
  }

  public String getCurrentSeasonName() {
    return currentSeasonName;
  }

  public Integer getCurrentSeasonID() {
    return currentSeasonID;
  }
}
