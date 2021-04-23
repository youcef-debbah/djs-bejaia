package dz.ngnex.entity;

import dz.ngnex.util.Check;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "budget", uniqueConstraints = {@UniqueConstraint(columnNames = {"contract_instance", "section", "activity"}, name = "unique_budget_for_section_per_contract_instance")})
public class BudgetEntity implements BudgetDatabaseEntity {
  private static final long serialVersionUID = -3135322090159846081L;

  private Integer id;
  private Integer version = 0;
  private ContractInstanceEntity contract;

  private BigDecimal budget = BigDecimal.ZERO;
  private ActivityEntity activity;
  private SectionEntity section;

  public BudgetEntity() {
  }

  public BudgetEntity(SectionEntity section, ActivityEntity activity) {
    this.section = Objects.requireNonNull(section);
    this.activity = Objects.requireNonNull(activity);
  }

  public BudgetEntity(SectionEntity section, ActivityEntity activity, BigDecimal budget) {
    this(section, activity);
    this.budget = Objects.requireNonNull(budget);
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "budget_id", updatable = false, nullable = false)
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

  @Override
  @Transient
  public Long getEntityIndex() {
    return getActivity().getEntityIndex();
  }

  @NotNull
  @Override
  @DecimalMin("0")
  @DecimalMax(Constrains.MAX_BUDGET)
  @Column(nullable = false)
  public BigDecimal getBudget() {
    return budget;
  }

  @Transient
  @Override
  public boolean isPresent() {
    return budget != null && budget.compareTo(BigDecimal.ZERO) > 0;
  }

  public void setBudget(BigDecimal budget) {
    this.budget = budget;
  }

  @NotNull
  @Override
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "activity", foreignKey = @ForeignKey(name = "fk_budget_to_activity"))
  public ActivityEntity getActivity() {
    return activity;
  }

  public void setActivity(ActivityEntity activity) {
    this.activity = activity;
  }

  @NotNull
  @Override
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "section", foreignKey = @ForeignKey(name = "fk_budget_to_section"))
  public SectionEntity getSection() {
    return section;
  }

  public void setSection(SectionEntity section) {
    this.section = section;
  }

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "contract_instance", foreignKey = @ForeignKey(name = "fk_budget_to_contract_instance"))
  public ContractInstanceEntity getContract() {
    return contract;
  }

  public void setContract(ContractInstanceEntity contract) {
    this.contract = contract;
  }

  public BudgetEntity persist(EntityManager em, ContractInstanceEntity parent) {
    Check.argNotNull(em);
    DatabaseEntity.requireID(parent);
    setContract(parent);
    em.persist(this);
    if (Hibernate.isInitialized(parent))
      DatabaseEntity.addLazily(this, parent.getBudgets());
    return this;
  }

  public static void removeAll(EntityManager em, ContractInstanceEntity parent, Set<BudgetEntity> children) {
    Check.argNotNull(em, children);
    DatabaseEntity.requireID(parent);

    if (!children.isEmpty())
      em.createQuery("delete from BudgetEntity b where b in :children")
          .setParameter("children", children)
          .executeUpdate();

    if (Hibernate.isInitialized(parent))
      DatabaseEntity.removeAllLazily(children, parent.getBudgets());
  }

  @Transient
  @Override
  public boolean isNull() {
    return budget == null || BigDecimal.ZERO.compareTo(budget) == 0;
  }

  @Transient
  public void copyBudget(BudgetEntity source) {
    setBudget(source.getBudget());
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, BudgetEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }

  @Override
  public String toString() {
    return "ActivityBudgetEntity{" +
        "id=" + getId() +
        ", budget=" + getBudget() +
        ", activityID=" + DatabaseEntity.getID(activity) +
        ", sectionID=" + DatabaseEntity.getID(section) +
        '}';
  }
}
