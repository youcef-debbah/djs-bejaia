package dz.ngnex.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "admin")
public class AdminEntity extends BasicPrincipalEntity {

  private static final long serialVersionUID = -6041641242883434769L;

  private String role;

  protected AdminEntity() {
  }

  public AdminEntity(String name, String password, String role) {
    super(name, password);
    setRole(role);
  }

  @NotNull
  @Size(max = 32)
  @Column(nullable = false, length = 32)
  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  @Override
  @Transient
  public AccessType getAccessType() {
    return AccessType.ofSecurityRole(role);
  }

  @Override
  @Transient
  public EntityReference<AdminEntity> getReference() {
    return new EntityReference<>(getId(), getName(), AdminEntity.class);
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, AdminEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }

  @Override
  public String toString() {
    return "PrincipalEntity{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        ", role='" + getRole() + '\'' +
        '}';
  }
}
