package dz.ngnex.entity;

import dz.ngnex.util.Check;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "global_budget", uniqueConstraints = {@UniqueConstraint(name = "unique_global_budget_for_activity_per_contract_instance", columnNames = {"contract_instance", "activity"})})
public class GlobalBudgetEntity implements BudgetDatabaseEntity {
  private static final long serialVersionUID = -3135322090159846081L;

  private Integer id;
  private BigDecimal budget = BigDecimal.ZERO;
  private ActivityEntity activity;
  private String header;
  private Integer version = 0;
  private ContractInstanceEntity contract;

  public GlobalBudgetEntity() {
  }

  public GlobalBudgetEntity(ActivityEntity activity) {
    this.activity = Objects.requireNonNull(activity);
  }

  public GlobalBudgetEntity(ActivityEntity activity, String header) {
    this.activity = Objects.requireNonNull(activity);
    this.header = header;
  }

  public GlobalBudgetEntity(ActivityEntity activity, String header, BigDecimal budget) {
    this(activity, header);
    this.budget = Objects.requireNonNull(budget);
  }

  @Transient
  public void copyBudget(GlobalBudgetEntity source) {
    setBudget(source.getBudget());
    setHeader(source.getHeader());
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "global_budget_id", updatable = false, nullable = false)
  @Override
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
  @JoinColumn(name = "activity", foreignKey = @ForeignKey(name = "fk_global_budget_to_activity"))
  public ActivityEntity getActivity() {
    return activity;
  }

  public void setActivity(ActivityEntity activity) {
    this.activity = activity;
  }

  @Override
  @Transient
  public SectionEntity getSection() {
    return null;
  }

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "contract_instance", foreignKey = @ForeignKey(name = "fk_global_budget_to_contract_instance"))
  public ContractInstanceEntity getContract() {
    return contract;
  }

  public void setContract(ContractInstanceEntity contract) {
    this.contract = contract;
  }

  @Transient
  @Override
  public boolean isNull() {
    return (budget == null || BigDecimal.ZERO.compareTo(budget) == 0) && StringUtils.isBlank(header);
  }

  @Size(max = Constrains.MAX_TEXT_LENGTH)
  @Column(columnDefinition = "TEXT", length = Constrains.MAX_TEXT_LENGTH)
  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  public GlobalBudgetEntity persist(EntityManager em, ContractInstanceEntity parent) {
    Check.argNotNull(em);
    DatabaseEntity.requireID(parent);
    this.setContract(parent);
    em.persist(this);
    if (Hibernate.isInitialized(parent))
      DatabaseEntity.addLazily(this, parent.getGlobalBudgets());
    return this;
  }

  public static void removeAll(EntityManager em, ContractInstanceEntity parent, Set<GlobalBudgetEntity> children) {
    Check.argNotNull(em, children);
    DatabaseEntity.requireID(parent);

    if (!children.isEmpty())
      em.createQuery("delete from GlobalBudgetEntity b where b in :children")
          .setParameter("children", children)
          .executeUpdate();

    if (Hibernate.isInitialized(parent))
      DatabaseEntity.removeAllLazily(children, parent.getGlobalBudgets());
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, GlobalBudgetEntity.class);
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
        ", activity=" + DatabaseEntity.getID(activity) +
        '}';
  }
}
