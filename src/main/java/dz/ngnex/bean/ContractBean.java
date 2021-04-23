package dz.ngnex.bean;

import dz.ngnex.entity.*;

import javax.ejb.Local;
import java.util.List;

@Local
public interface ContractBean {

  SeasonEntity getCurrentSeason();

  Integer getCurrentSeasonID() throws IntegrityException;

  Integer findCurrentSeasonID();

  TemplateDetails getEmptyTemplateDetails();

  List<TemplateDetails> getSources();

  List<ContractTemplateEntity> getTemplates();

  List<ContractTemplateEntity> getTemplates(Integer seasonID);

  ContractTemplateEntity getTemplate(Integer id);

  ContractTemplateEntity getSelectedTemplate(Integer id);

  PropertyEntity getProperty(Integer id);

  ContractTemplateEntity updateTemplate(ContractTemplateEntity currentTemplate) throws IntegrityException;

  ContractTemplateEntity updateTemplate(ContractTemplateEntity currentTemplate, String updater) throws IntegrityException;

  PropertyEntity updateProperty(PropertyEntity updatedProperty) throws IntegrityException;

  PropertyEntity addProperty(Integer templateID, String newPropertyName, InputType newPropertyType) throws IntegrityException;

  void deleteProperty(PropertyEntity property);

  ContractTemplateEntity addNewTemplate(String newTemplateName, Integer sourceTemplateID) throws IntegrityException;
  ContractTemplateEntity addNewTemplate(String newTemplateName, Integer sourceTemplateID, String updater) throws IntegrityException;

  long countContractInstances(Integer templateID);

  ContractTemplateEntity movePropertyUp(Integer templateID, Integer propertyID);

  ContractTemplateEntity movePropertyDown(Integer templateID, Integer propertyID);

  ActivityEntity updateActivity(ActivityEntity activity) throws IntegrityException;

  ActivityEntity addActivity(Integer templateID, String newActivityName) throws IntegrityException;

  void deleteActivity(ActivityEntity activity);

  void updateValues(Integer contractInstanceID,
                    List<PropertyValueEntity> potentialValues,
                    List<GlobalBudgetEntity> potentialBudgets);

  List<PropertyValueEntity> getPropertyValues(Integer contract);

  List<GlobalBudgetEntity> getGlobalBudgets(Integer contractInstanceId);

  ContractInstanceEntity findAssignedContract(Integer id);

  void updateAchievements(ContractInstanceEntity contract);

  void updateAchievementLevel(ContractInstanceEntity contract);

  List<ContractInstanceEntity> getContractsAssignedBetween(long date1, long date2);

  void updateContractsSeason(List<TemplateInfo> contracts, Integer currentSeasonID);

  void resetAllContractInstancesState(Integer currentSeasonID);

  void recalcAllContractDownloadState();

  List<TemplateInfo> getTemplatesInfo(Integer seasonID);

  List<TemplateInfo> getTemplatesInfo();

  List<TemplateDetails> getTemplatesDetails(Integer seasonID);

  List<TemplateDetails> getTemplatesDetails();

  void clear();
}
