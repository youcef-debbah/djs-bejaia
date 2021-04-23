package dz.ngnex.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class TemplateInfo extends EntityReference<ContractTemplateEntity> {
  private static final long serialVersionUID = -3284961318696485400L;
  private Integer seasonID;

  public TemplateInfo(Integer templateID, String templateName, Integer seasonID) {
    super(templateID, templateName, ContractTemplateEntity.class);
    this.seasonID = seasonID;
  }

  @NotNull
  public Integer getTemplateID() {
    return getId();
  }

  @NotNull
  public String getTemplateName() {
    return getName();
  }

  @Nullable
  public Integer getSeasonID() {
    return seasonID;
  }

  public void setSeasonID(Integer seasonID) {
    this.seasonID = seasonID;
    ;
  }

  @Override
  public String toString() {
    return "TemplateInfo{" +
        "templateID=" + getTemplateID() +
        ", templateName='" + getTemplateName() + '\'' +
        ", seasonID='" + getSeasonID() + '\'' +
        '}';
  }
}
