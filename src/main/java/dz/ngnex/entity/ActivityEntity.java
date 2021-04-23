package dz.ngnex.entity;

import dz.ngnex.bean.IntegrityException;
import dz.ngnex.util.Check;
import dz.ngnex.util.TemplatedContent;
import dz.ngnex.view.Contract;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "activity", uniqueConstraints = @UniqueConstraint(name = "unique_activity_name_per_contract", columnNames = {"contract_template", "name"}))
public class ActivityEntity implements DatabaseEntity {
  private static final long serialVersionUID = 4796919728684497247L;

  public static final String DEFAULT_HEADER = "Ventil\u00e9 comme suit";
  public static final String DEFAULT_SECTION_PROTOTYPE =
      "Pour la section " + TemplatedContent.getAsVariableName(Contract.NOM_SECTION_VAR_ACTIVITY) + Contract.NEW_LINE
          + "pour un montant de " + TemplatedContent.getAsVariableName(Contract.BUDGET_SECTION_VAR_ACTIVITY);

  private Integer id;
  private String name;
  private String label;
  private String header = DEFAULT_HEADER;
  private String sectionPrototype = DEFAULT_SECTION_PROTOTYPE;
  private Integer minVentilationCount = 3;
  private Integer version = 0;

  private ContractTemplateEntity contract;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "activity_id", updatable = false, nullable = false)
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
  @Size(min = Constrains.Min_IDENTIFIER_SIZE, max = Constrains.MAX_IDENTIFIER_SIZE)
  @Column(updatable = false, nullable = false)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    }

  @Size(max = Constrains.MAX_TEXT_LENGTH)
  @Column(columnDefinition = "TEXT")
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
    }

  @Size(max = Constrains.MAX_TEXT_LENGTH)
  @Column(columnDefinition = "TEXT", length = Constrains.MAX_TEXT_LENGTH)
  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
    }

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  @Column(length = Constrains.MAX_PHRASE_LENGTH)
  public String getSectionPrototype() {
    return sectionPrototype;
  }

  public void setSectionPrototype(String sectionPrototype) {
    this.sectionPrototype = sectionPrototype;
    }

  @NotNull
  @Max(10)
  @Min(0)
  @Column(nullable = false)
  public Integer getMinVentilationCount() {
    return minVentilationCount;
  }

  public void setMinVentilationCount(Integer minVentilationCount) {
    this.minVentilationCount = minVentilationCount;
    }

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "contract_template", foreignKey = @ForeignKey(name = "fk_activity_to_contract_template"))
  public ContractTemplateEntity getContract() {
    return contract;
  }

  public void setContract(ContractTemplateEntity contract) {
    this.contract = contract;
    }

  public ActivityEntity persist(EntityManager em, ContractTemplateEntity parent) throws IntegrityException {
    Check.argNotNull(em);
    DatabaseEntity.requireID(parent);
    setContract(parent);
    em.persist(this);
    if (Hibernate.isInitialized(parent))
      DatabaseEntity.addLazily(this, parent.getActivities());
    return this;
  }

  @Transient
  public String getVariableName() {
    return TemplatedContent.getAsVariableName(name);
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, ActivityEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }

  @Override
  public String toString() {
    return "ActivityEntity{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        '}';
  }
}
