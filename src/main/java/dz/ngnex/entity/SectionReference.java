package dz.ngnex.entity;

public class SectionReference<T extends DatabaseEntity> extends EntityReference<SectionEntity> {
  private static final long serialVersionUID = 8624827155277782294L;

  public SectionReference(Integer id, String name) {
    super(id, name, SectionEntity.class);
  }

  @Override
  public String toString() {
    return "SectionReference{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        '}';
  }
}
