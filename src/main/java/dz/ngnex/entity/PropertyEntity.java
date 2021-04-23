package dz.ngnex.entity;

import dz.ngnex.security.WritableResource;
import dz.ngnex.security.WriteAccess;
import dz.ngnex.util.Check;
import dz.ngnex.util.TemplatedContent;
import dz.ngnex.view.Contract;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

@Entity
@Table(name = "property", uniqueConstraints = @UniqueConstraint(columnNames = {"contract_template", "name"}, name = "unique_property_name_per_contract"))
public class PropertyEntity implements DatabaseEntity, WritableResource {
  private static final long serialVersionUID = 1176554614665663991L;
  public static final String DEFAULT_PROTOTYPE = TemplatedContent.getAsVariableName(Contract.HEADER_VAR_PROPERTY) + Contract.NEW_LINE
      + "pour un montant de " + TemplatedContent.getAsVariableName(Contract.VALUE_VAR_PROPERTY);

  private Integer id;

  private Integer index;
  private String name;
  private String label;

  private WriteAccess access;
  private InputType type;
  private String defaultValue;

  private String defaultHeader;
  private String prototype;
  private RequiredLevel required;

  private Integer version = 0;

  private ContractTemplateEntity contract;

  public PropertyEntity() {
    this(InputType.TEXT);
  }

  public PropertyEntity(InputType newPropertyType) {
    Objects.requireNonNull(newPropertyType);
    type = newPropertyType;

    if (newPropertyType == InputType.NOTE) {
      defaultValue = "";
      prototype = "";
    } else {
      defaultValue = "..........................";
      prototype = DEFAULT_PROTOTYPE;
    }

    defaultHeader = "";
    label = "";
    access = WriteAccess.SPORT_ADMIN_AND_ASSO;
    required = RequiredLevel.NOT_REQUIRED;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "property_id", updatable = false, nullable = false)
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
  @Column(nullable = false)
  public Integer getIndex() {
    return index;
  }

  public void setIndex(Integer index) {
    this.index = index;
    }

  @NotNull
  @Transient
  public Long getEntityIndex() {
    return getIndex().longValue();
  }

  @NotNull
  @Size(min = Constrains.Min_IDENTIFIER_SIZE, max = Constrains.MAX_IDENTIFIER_SIZE)
  @Column(updatable = false, nullable = false, length = Constrains.MAX_IDENTIFIER_SIZE)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    }

  @Transient
  public String getVariableName() {
    return TemplatedContent.getAsVariableName(name);
  }

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  @Column(length = Constrains.MAX_PHRASE_LENGTH)
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
    }

  @NotNull
  @Override
  @Enumerated
  @Column(nullable = false)
  public WriteAccess getAccess() {
    return access;
  }

  public void setAccess(WriteAccess access) {
    this.access = access;
    }

  @NotNull
  @Enumerated
  @Column(nullable = false, updatable = false)
  public InputType getType() {
    return type;
  }

  public void setType(InputType type) {
    this.type = type;
    }

  @Size(max = Constrains.MAX_TEXT_LENGTH)
  @Column(columnDefinition = "TEXT", length = Constrains.MAX_TEXT_LENGTH)
  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    }

  @Size(max = Constrains.MAX_TEXT_LENGTH)
  @Column(columnDefinition = "TEXT", length = Constrains.MAX_TEXT_LENGTH)
  public String getDefaultHeader() {
    return defaultHeader;
  }

  public void setDefaultHeader(String defaultHeader) {
    this.defaultHeader = defaultHeader;
    }

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  @Column(length = Constrains.MAX_PHRASE_LENGTH)
  public String getPrototype() {
    return prototype;
  }

  public void setPrototype(String prototype) {
    this.prototype = prototype;
    }

  @NotNull
  @Enumerated
  @Column(nullable = false)
  public RequiredLevel getRequired() {
    return required;
  }

  public void setRequired(RequiredLevel required) {
    this.required = required;
    }

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "contract_template", foreignKey = @ForeignKey(name = "fk_property_to_contract_template"))
  public ContractTemplateEntity getContract() {
    return contract;
  }

  public void setContract(ContractTemplateEntity contract) {
    this.contract = contract;
    }

  public PropertyEntity persist(EntityManager em, ContractTemplateEntity parent) {
    Check.argNotNull(em);
    DatabaseEntity.requireID(parent);
    setContract(parent);
    em.persist(this);
    if (Hibernate.isInitialized(parent))
      DatabaseEntity.addLazily(this, parent.getProperties());
    return this;
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, PropertyEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }

  @Override
  public String toString() {
    return "PropertyEntity{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        '}';
  }
}
