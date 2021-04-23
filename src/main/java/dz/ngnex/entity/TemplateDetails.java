package dz.ngnex.entity;

public class TemplateDetails extends EntityReference<ContractTemplateEntity> {
  private static final long serialVersionUID = 5848381890032775884L;

  private final Integer version;
  private Integer seasonID;
  private String seasonName;

  public TemplateDetails(Integer templateID, String templateName, Integer version, Integer seasonID, String seasonName) {
    super(templateID, templateName, ContractTemplateEntity.class);
    this.version = version;
    this.seasonID = seasonID;
    this.seasonName = seasonName;
  }

  public Integer getSeasonID() {
    return seasonID;
  }

  public String getSeasonName() {
    return seasonName;
  }

  public Integer getVersion() {
    return version;
  }

  public void setSeasonID(Integer seasonID) {
    this.seasonID = seasonID;
  }

  public void setSeasonName(String seasonName) {
    this.seasonName = seasonName;
  }

  @Override
  public String toString() {
    return "TemplateDetails{" +
        "templateID=" + getId() +
        ", templateName='" + getName() + '\'' +
        ", seasonID='" + getSeasonID() + '\'' +
        '}';
  }
}
