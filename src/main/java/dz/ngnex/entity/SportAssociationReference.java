package dz.ngnex.entity;

public class SportAssociationReference extends EntityReference<SportAssociationEntity> {
  private static final long serialVersionUID = -2880468199074543606L;

  public SportAssociationReference(Integer id, String name) {
    super(id, name, SportAssociationEntity.class);
  }

  @Override
  public String toString() {
    return "SportAssociationReference{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        '}';
  }
}
