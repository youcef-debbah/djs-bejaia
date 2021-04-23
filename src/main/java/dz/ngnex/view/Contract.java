/*
 * Handcrafted with love by Youcef DEBBAH
 * Copyright 2019 youcef-debbah@hotmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dz.ngnex.view;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import dz.ngnex.control.LocaleManager;
import dz.ngnex.entity.*;
import dz.ngnex.util.TemplatedContent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static dz.ngnex.view.Contract.GeneralVars.*;

public final class Contract {

  private static final Logger log = LogManager.getLogger(Contract.class);
  private static final int INDENT = 20;
  private static final int FONT = 12;
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  public static final String NEW_LINE = "<br />";

  public enum GeneralVars {
    USERNAME("username"),
    CREATION_DATE("creation_date"),
    ADRESSE("adresse"),
    AGREMENT("agrement"),
    BANQUE("banque"),
    AGENCE("agence"),
    COMPTE("compte"),
    DESCRIPTION("description"),
    PRESIDENT("president"),
    PHONE("phone"),
    BUDGET_GLOBAL("budget_global");

    private final String varName;

    GeneralVars(String varName) {
      this.varName = varName;
    }

    public String getRawName() {
      return varName;
    }

    public String getVariableName() {
      return TemplatedContent.getAsVariableName(varName);
    }

    @Override
    public java.lang.String toString() {
      return varName;
    }
  }

  public static final String NOM_SECTION_VAR_ACTIVITY = "nom_section";
  public static final String BUDGET_SECTION_VAR_ACTIVITY = "budget_section";

  public static final String HEADER_VAR_PROPERTY = "entete_propriete";
  public static final String VALUE_VAR_PROPERTY = "valeur_propriete";

  private static final String NAME_PLACEHOLDER = "....................................";
  private static final String BUDGET_PLACEHOLDER = "....................... DA";

  private String name;
  private int length;
  private InputStream inputStream;
  private final List<byte[]> content = new LinkedList<>();

  private final BasicAssociationEntity association;
  private final ContractInstanceEntity contractInstance;
  private final LocaleManager localeManager;

  public Contract(BasicAssociationEntity association, ContractInstanceEntity contractInstance, LocaleManager localeManager) {
    this.association = Objects.requireNonNull(association);
    this.contractInstance = Objects.requireNonNull(contractInstance);
    this.localeManager = Objects.requireNonNull(localeManager);
    this.name = "Le contrat de l'association " + association.getName() + ".pdf";
  }

  public void generate() {
    try {
      generateContent();

      ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
      PdfDocument fullDocument = new PdfDocument(new PdfWriter(out));
      PdfMerger merger = new PdfMerger(fullDocument);
      merger.setCloseSourceDocuments(true);

      for (byte[] bytes : content) {
        PdfDocument documentFragment = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)));
        merger.merge(documentFragment, 1, documentFragment.getNumberOfPages());
      }

      addPageNumbers(fullDocument);
      merger.close();
      byte[] data = out.toByteArray();

      inputStream = new ByteArrayInputStream(data);
      length = data.length;
    } catch (Exception e) {
      log.error("contract generation failed for association: " + association.getName(), e);
      generateError();
    }
  }

  private void generateContent() throws Exception {
    String contentAsHTML = getContentAsHTML();
    try (ByteArrayOutputStream out = new ByteArrayOutputStream(128)) {
      HtmlConverter.convertToPdf(contentAsHTML, out);
      content.add(out.toByteArray());
    }
  }

  private String getContentAsHTML() {
    TemplatedContent content = new TemplatedContent(getContentTemplate());
    replaceGeneralVars(content);
    replaceDynamicVars(content);
    replaceActivityVars(content);
    return content.toString();
  }

  private String getContentTemplate() {
    String template = contractInstance.getContractTemplate().getTemplate();
    if (template == null || template.isEmpty())
      return "";
    else
      return template.replace("<hr />", "<div style='page-break-before:always'>&nbsp;</div>");
  }

  private void replaceGeneralVars(TemplatedContent content) {
    BasicAssociationEntity association = this.association;
    content.replace(USERNAME.getRawName(), association.getName())
        .replace(CREATION_DATE.getRawName(), association.getCreationDate())
        .replace(ADRESSE.getRawName(), association.getAdresse())
        .replace(AGREMENT.getRawName(), association.getAgrement())
        .replace(BANQUE.getRawName(), association.getBanque())
        .replace(AGENCE.getRawName(), association.getAgence())
        .replace(COMPTE.getRawName(), association.getCompte())
        .replace(DESCRIPTION.getRawName(), association.getDescription())
        .replace(PRESIDENT.getRawName(), association.getPresident())
        .replace(PHONE.getRawName(), association.getPhone())
        .replace(BUDGET_GLOBAL.getRawName(), localeManager.formatAsDA_HTML(contractInstance.getGlobalMontant()));
  }

  private void replaceDynamicVars(TemplatedContent content) {
    List<PropertyValueEntity> propertyValues = new ArrayList<>(contractInstance.getPropertyValues());
    PropertiesView.addMissingValues(contractInstance.getContractTemplate().getProperties(), propertyValues);
    propertyValues.removeIf(value -> value.getProperty().getType() == InputType.NOTE);

    for (PropertyValueEntity propertyValue : propertyValues) {
      TemplatedContent inputContent = new TemplatedContent(propertyValue.getProperty().getPrototype());
      replaceGeneralVars(inputContent);

      inputContent.replace(VALUE_VAR_PROPERTY, getFormattedValue(propertyValue));
      inputContent.replace(HEADER_VAR_PROPERTY, propertyValue.getActualHeader());

      content.replace(propertyValue.getProperty().getName(), inputContent.toString());
    }
  }

  private String getFormattedValue(PropertyValueEntity propertyValue) {
    if (propertyValue.isNullValue())
      return propertyValue.getProperty().getDefaultValue();
    else {
      try {
        switch (propertyValue.getProperty().getType()) {
          case BUDGET:
          case MONETARY_AMOUNT:
            return localeManager.formatAsDA_HTML(propertyValue.getValueAsDecimal());
          case DATE:
            return localeManager.formatAsLocalDate(propertyValue.getValueAsLong());
          default:
            return propertyValue.getValueAsHtml();
        }
      } catch (Exception e) {
        log.error("could not format value: " + propertyValue, e);
        return propertyValue.getValue();
      }
    }
  }

  private void replaceActivityVars(TemplatedContent content) {
    Map<ActivityEntity, BigDecimal> totalBudgets = new HashMap<>();
    Map<ActivityEntity, String> activityLabels = new HashMap<>();

    for (GlobalBudgetEntity globalBudget : contractInstance.getGlobalBudgets()) {
      ActivityEntity activity = globalBudget.getActivity();
      totalBudgets.put(activity, globalBudget.getBudget());
      if (!StringUtils.isBlank(globalBudget.getHeader()))
        activityLabels.put(activity, globalBudget.getHeader());
    }

    for (ActivityEntity activity : contractInstance.getContractTemplate().getActivities())
      totalBudgets.putIfAbsent(activity, BigDecimal.ZERO);

    Set<SectionEntity> sections = association.getVirtualSections();
    for (SectionEntity section : sections) {
      for (BudgetEntity budget : contractInstance.getSectionBudgets(section.getId())) {
        ActivityEntity activity = budget.getActivity();
        BigDecimal totalBudget = totalBudgets.get(activity);
        if (totalBudget != null)
          totalBudgets.put(activity, totalBudget.add(budget.getBudget()));
      }
    }

    for (Map.Entry<ActivityEntity, BigDecimal> entry : totalBudgets.entrySet()) {
      StringBuilder ventilationTemplate = new StringBuilder();
      ActivityEntity activity = entry.getKey();
      BigDecimal totalBudget = entry.getValue();
      Integer minVentilation = activity.getMinVentilationCount();
      String style = content.extractStyleEnclosing(TemplatedContent.getAsVariableName(activity.getName()));

      ventilationTemplate.append(activityLabels.getOrDefault(activity, activity.getLabel()))
          .append(NEW_LINE)
          .append("pour un montant de ")
          .append(localeManager.formatAsDA_HTML(totalBudget))
          .append(NEW_LINE);

      if (!sections.isEmpty() || (minVentilation != null && minVentilation > 0)) {
        String header = activity.getHeader();
        if (header != null && !header.isEmpty()) {
          ventilationTemplate
              .append("<p style='").append(style).append("'><u>")
              .append(header)
              .append("</u></p>");
        }

        ventilationTemplate.append("<ul>");

        int i = 0;
        for (SectionEntity section : sections) {
          BudgetEntity budgetEntity = BudgetDatabaseEntity.extractBudget(section, activity, contractInstance.getBudgets());

          if (budgetEntity != null && budgetEntity.isPresent()) {
            if (i++ > 0)
              ventilationTemplate.append(NEW_LINE);

            String budgetAsString = localeManager.formatAsDA_HTML(budgetEntity.getBudget());
            appendSection(ventilationTemplate, activity, style, section.getName(), budgetAsString);
          }
        }

        while (i < minVentilation) {
          if (i++ > 0)
            ventilationTemplate.append(NEW_LINE);
          appendSection(ventilationTemplate, activity, style, NAME_PLACEHOLDER, BUDGET_PLACEHOLDER);
        }

        ventilationTemplate.append("</ul>");
      }

      TemplatedContent ventilationContent = new TemplatedContent(ventilationTemplate.toString());
      replaceGeneralVars(ventilationContent);
      content.replace(activity.getName(), ventilationContent.toString());
    }
  }

  private void appendSection(StringBuilder ventilationTemplate,
                             ActivityEntity activity,
                             String style,
                             String sectionName,
                             String sectionActivityBudget) {
    TemplatedContent activitySectionContent = new TemplatedContent(activity.getSectionPrototype());
    activitySectionContent.replace(NOM_SECTION_VAR_ACTIVITY, sectionName);
    activitySectionContent.replace(BUDGET_SECTION_VAR_ACTIVITY, sectionActivityBudget);
    ventilationTemplate
        .append("<li style='").append(style).append("'>")
        .append(activitySectionContent.toString())
        .append("</li>");
  }

  private void addPageNumbers(PdfDocument pdfDoc) {
    try (Document doc = new Document(pdfDoc)) {
      int totalPagesNumber = pdfDoc.getNumberOfPages();

      PageSize pageSize = pdfDoc.getDefaultPageSize();
      float x = pageSize.getWidth() / 2;
      float y = 15;

      for (int i = 1; i <= totalPagesNumber; i++) {
        doc.showTextAligned(new Paragraph(String.format("Page %s sur %s", i, totalPagesNumber)), x, y, i, TextAlignment.CENTER, VerticalAlignment.BOTTOM, 0);
      }
    }
  }

  private void generateError() {
    ByteArrayOutputStream out = new ByteArrayOutputStream(64);
    PdfDocument pdfDocument = new PdfDocument(new PdfWriter(out));

    try (Document document = new Document(pdfDocument, PageSize.A4, false)) {
      document.add(new Paragraph("Error"));
      document.close();

      byte[] data = out.toByteArray();
      name = "error.pdf";
      inputStream = new ByteArrayInputStream(data);
      length = data.length;
    }
  }

  @SuppressWarnings("unused")
  private Table newFooterTable(PdfDocument pdfDocument, Document document, PdfFont oblique) {
    Table table = new Table(2);

    float tableWidth = pdfDocument.getDefaultPageSize().getWidth() - document.getLeftMargin() - document.getRightMargin();
    table.setFixedPosition(document.getLeftMargin(), document.getBottomMargin(), tableWidth);

    float cellWidth = tableWidth / 2;
    table.addCell(new Cell()
        .setBorder(Border.NO_BORDER)
        .setBorderTop(new SolidBorder(1))
        .setWidth(cellWidth)
        .setVerticalAlignment(VerticalAlignment.TOP)
        .add(new Paragraph(localeManager.getCurrentLocalDateTime())
            .setFont(oblique)
            .setFontSize(FONT - 4)
            .setTextAlignment(TextAlignment.LEFT)));

    table.addCell(new Cell()
        .setBorder(Border.NO_BORDER)
        .setBorderTop(new SolidBorder(1))
        .setWidth(cellWidth)
        .setVerticalAlignment(VerticalAlignment.TOP)
        .add(new Paragraph("Génie Logiciel - NGNEX")
            .setFont(oblique)
            .setFontSize(FONT - 4)
            .setTextAlignment(TextAlignment.RIGHT)));

    return table;
  }

  public InputStream asInputStream() {
    return inputStream;
  }

  public String getMimeType() {
    return "application/pdf";
  }

  public int getLength() {
    return length;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Contract{" +
        "name='" + getName() + '\'' +
        ", length=" + getLength() +
        '}';
  }
}
