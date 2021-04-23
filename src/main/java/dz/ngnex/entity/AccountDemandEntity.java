package dz.ngnex.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "account_demand", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "unique_account_demand_name"))
public class AccountDemandEntity implements DatabaseEntity {
  private static final long serialVersionUID = -2294803164432149332L;

  private Integer id;
  private String name;

  private String description = "";
  private String agrement = "";
  private String adresse = "";
  private String president = "";
  private String phone = "";
  private Long created;
  private Integer version = 0;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "demand_id", updatable = false, nullable = false)
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

  @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
  @Column(length = Constrains.MAX_IDENTIFIER_SIZE)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  @Column(length = Constrains.MAX_PHRASE_LENGTH)
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  @Column(length = Constrains.MAX_PHRASE_LENGTH)
  public String getAgrement() {
    return agrement;
  }

  public void setAgrement(String agrement) {
    this.agrement = agrement;
  }

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  @Column(length = Constrains.MAX_PHRASE_LENGTH)
  public String getAdresse() {
    return adresse;
  }

  public void setAdresse(String adresse) {
    this.adresse = adresse;
  }

  @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
  @Column(length = Constrains.MAX_IDENTIFIER_SIZE)
  public String getPresident() {
    return president;
  }

  public void setPresident(String president) {
    this.president = president;
    }

  @Size(max = Constrains.MAX_PHONE_NUMBER_SIZE)
  @Column(length = Constrains.MAX_PHONE_NUMBER_SIZE)
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
    }

  @NotNull
  @Column(nullable = false)
  public Long getCreated() {
    return created;
  }

  public void setCreated(Long created) {
    this.created = created;
    }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, AccountDemandEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }

  @Override
  public String toString() {
    return "AccountDemandEntity{" +
        "id=" + getId() +
        '}';
  }
}
