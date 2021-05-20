package dz.ngnex.entity;

public class SectionTemplateReference extends EntityReference<SectionTemplateEntity> {
  private static final long serialVersionUID = 8624827155277782294L;

  public SectionTemplateReference(Integer id, String name) {
    super(id, name, SectionTemplateEntity.class);
  }

  @Override
  public String toString() {
    return "SectionTemplateReference{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        '}';
  }
}
