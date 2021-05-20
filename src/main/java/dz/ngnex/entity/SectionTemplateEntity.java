package dz.ngnex.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "section_template", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "unique_section_template_name"))
public class SectionTemplateEntity implements DatabaseEntity {
  private static final long serialVersionUID = -7539235897931597776L;
  private Integer id;
  private String name;
  private Integer version = 0;

  public SectionTemplateEntity() {
  }

  public SectionTemplateEntity(String name) {
    this.name = name;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "section_template_id", updatable = false, nullable = false)
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Version
  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  @NotNull
  @Column(nullable = false, length = Constrains.MAX_IDENTIFIER_SIZE)
  @Size(min = Constrains.Min_IDENTIFIER_SIZE, max = Constrains.MAX_IDENTIFIER_SIZE)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Transient
  public SectionTemplateReference getReference() {
    return new SectionTemplateReference(getId(), getName());
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, SectionTemplateEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }

  @Override
  public String toString() {
    return "SectionTemplateEntity{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        '}';
  }
}
