package dz.ngnex.entity;

public class YouthAssociationReference extends EntityReference<YouthAssociationEntity> {
  private static final long serialVersionUID = -7638033532239000491L;

  public YouthAssociationReference(Integer id, String name) {
    super(id, name, YouthAssociationEntity.class);
  }

  @Override
  public String toString() {
    return "YouthAssociationReference{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        '}';
  }
}
