package dz.ngnex.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "youth_association")
public class YouthAssociationEntity extends BasicAssociationEntity {
  private static final long serialVersionUID = -3819106747319574378L;

  protected YouthAssociationEntity() {

  }

  public YouthAssociationEntity(String name, String password, String description) {
    super(name, password, description);
  }

  @Override
  @Transient
  public AccessType getAccessType() {
    return AccessType.YOUTH_ASSOCIATION;
  }

  @Override
  @Transient
  public Set<SectionEntity> getVirtualSections() {
    return Collections.emptySet();
  }

  @Override
  @Transient
  public EntityReference<YouthAssociationEntity> getReference() {
    return new YouthAssociationReference(getId(), getName());
  }

  @Override
  @Transient
  public Class<YouthAssociationEntity> getType() {
    return YouthAssociationEntity.class;
  }

  @Override
  public String toString() {
    return "YouthAssociation{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        '}';
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, YouthAssociationEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }
}
