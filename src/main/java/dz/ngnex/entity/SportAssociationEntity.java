package dz.ngnex.entity;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Table(name = "sport_association")
public class SportAssociationEntity extends BasicAssociationEntity {
  private static final long serialVersionUID = -8865948653775840997L;

  private Set<SectionEntity> sections = new TreeSet<>();

  protected SportAssociationEntity() {

  }

  public SportAssociationEntity(String name, String password, String description) {
    super(name, password, description);
  }

  @OneToMany(mappedBy = "association")
  public Set<SectionEntity> getSections() {
    return sections;
  }

  public void setSections(Set<SectionEntity> sections) {
    this.sections = sections;
  }

  @Override
  @Transient
  public AccessType getAccessType() {
    return AccessType.SPORT_ASSOCIATION;
  }

  @Override
  @Transient
  public Set<SectionEntity> getVirtualSections() {
    return sections;
  }

  @Override
  @Transient
  public SportAssociationReference getReference() {
    return new SportAssociationReference(getId(), getName());
  }

  @Override
  @Transient
  public Class<SportAssociationEntity> getType() {
    return SportAssociationEntity.class;
  }

  @Override
  public String toString() {
    return "SportAssociation{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        '}';
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, SportAssociationEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }
}
