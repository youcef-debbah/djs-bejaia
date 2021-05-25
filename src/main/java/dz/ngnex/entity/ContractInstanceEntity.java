package dz.ngnex.entity;

import dz.ngnex.util.Check;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "contract_instance", uniqueConstraints = @UniqueConstraint(columnNames = {"association", "contract_template"}, name = "unique_contract_template_per_association"),
    indexes = @Index(columnList = "assignmentDate", name = "index_contract_assignment_date"))
@NamedEntityGraphs({
    @NamedEntityGraph(name = "loadBudgetGraph", attributeNodes = {@NamedAttributeNode("contractTemplate")})
})
public class ContractInstanceEntity implements DatabaseEntity {
  private static final long serialVersionUID = 4492591575710011356L;

  private Integer id;
  private ContractTemplateEntity contractTemplate;
  private Integer retrait = 0;
  private Long assignmentDate;
  private Long lastDownload;
  private Long lastUpdate;
  private ContractInstanceState state = ContractInstanceState.ACTIVE;
  private AchievementLevel achievementLevel;
  private String achievement;
  private Integer version = 0;

  private Set<PropertyValueEntity> propertyValues = new TreeSet<>();
  private Set<BudgetEntity> budgets = new HashSet<>();
  private Set<GlobalBudgetEntity> globalBudgets = new HashSet<>();

  private BasicAssociationEntity association;

  @Transient
  private BigDecimal cachedGlobalMontant;

  @Transient
  private Set<PropertyValueEntity> cachedAchievements;

  private Map<Integer, Collection<BudgetEntity>> cachedSectionBudgets;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "contract_instance_id", updatable = false, nullable = false)
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

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "contract_template", foreignKey = @ForeignKey(name = "fk_contract_instance_to_contract_template"))
  public ContractTemplateEntity getContractTemplate() {
    return contractTemplate;
  }

  public void setContractTemplate(ContractTemplateEntity contractTemplate) {
    this.contractTemplate = contractTemplate;
  }

  @NotNull
  @Column(nullable = false)
  public Integer getRetrait() {
    return retrait;
  }

  public void setRetrait(Integer retrait) {
    this.retrait = retrait;
  }

  public Long getAssignmentDate() {
    return assignmentDate;
  }

  public void setAssignmentDate(Long assignmentDate) {
    this.assignmentDate = assignmentDate;
  }

  public Long getLastDownload() {
    return lastDownload;
  }

  public void setLastDownload(Long lastDownload) {
    this.lastDownload = lastDownload;
  }

  public Long getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(Long lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  @NotNull
  @Enumerated
  @Column(nullable = false)
  public ContractInstanceState getState() {
    return state;
  }

  public void setState(ContractInstanceState state) {
    this.state = state;
  }

  @OneToMany(mappedBy = "contract")
  public Set<PropertyValueEntity> getPropertyValues() {
    return propertyValues;
  }

  public void setPropertyValues(Set<PropertyValueEntity> propertyValues) {
    this.propertyValues = propertyValues;
  }

  @OneToMany(mappedBy = "contract")
  public Set<BudgetEntity> getBudgets() {
    return budgets;
  }

  public void setBudgets(Set<BudgetEntity> budgets) {
    this.budgets = budgets;
  }

  @OneToMany(mappedBy = "contract")
  public Set<GlobalBudgetEntity> getGlobalBudgets() {
    return globalBudgets;
  }

  public void setGlobalBudgets(Set<GlobalBudgetEntity> globalBudgets) {
    this.globalBudgets = globalBudgets;
  }

  @Enumerated
  public AchievementLevel getAchievementLevel() {
    return achievementLevel;
  }

  public void setAchievementLevel(AchievementLevel achievementLevel) {
    this.achievementLevel = achievementLevel;
  }

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  @Column(length = Constrains.MAX_PHRASE_LENGTH)
  public String getAchievement() {
    return achievement;
  }

  public void setAchievement(String achievement) {
    this.achievement = achievement;
  }

  @Transient
  public boolean getValid() {
    return state == ContractInstanceState.ACTIVE;
  }

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "association", foreignKey = @ForeignKey(name = "fk_contract_instance_to_association"))
  public BasicAssociationEntity getAssociation() {
    return association;
  }

  public void setAssociation(BasicAssociationEntity association) {
    this.association = association;
  }

  ////////////// start of cached state operations //////////////

  @Transient
  public BigDecimal getGlobalMontant() {
    if (cachedGlobalMontant == null) {
      BigDecimal result = BigDecimal.ZERO;

      for (GlobalBudgetEntity globalBudget : getGlobalBudgets())
        result = result.add(globalBudget.getBudget());

      for (BudgetEntity budget : getBudgets())
        result = result.add(budget.getBudget());

      for (PropertyValueEntity propertyValue : propertyValues)
        if (InputType.BUDGET.equals(propertyValue.getProperty().getType()))
          result = result.add(propertyValue.getValueAsDecimal());

      cachedGlobalMontant = result;
    }
    return cachedGlobalMontant;
  }

  @Transient
  public Set<PropertyValueEntity> getAchievements() {
    if (cachedAchievements == null) {
      Set<PropertyValueEntity> achievements = new TreeSet<>();

      for (PropertyValueEntity propertyValue : getPropertyValues())
        if (propertyValue.getProperty().getType() == InputType.GOAL)
          achievements.add(propertyValue);

      cachedAchievements = Collections.unmodifiableSet(achievements);
    }

    return cachedAchievements;
  }

  @Transient
  public Collection<BudgetEntity> getSectionBudgets(Integer sectionID) {
    if (cachedSectionBudgets == null) {
      Map<Integer, Collection<BudgetEntity>> sectionsBudgets = new HashMap<>();

      for (BudgetEntity budget : getBudgets())
        sectionsBudgets.computeIfAbsent(budget.getSection().getId(), id -> new TreeSet<>())
            .add(budget);

      cachedSectionBudgets = Collections.unmodifiableMap(sectionsBudgets);
    }

    Collection<BudgetEntity> budgetEntities = cachedSectionBudgets.get(sectionID);
    if (budgetEntities != null)
      return budgetEntities;
    else
      return Collections.emptySet();
  }

  ////////////// end of cached state operations //////////////

  public ContractInstanceEntity persist(EntityManager em, BasicAssociationEntity parent) {
    Check.argNotNull(em);
    DatabaseEntity.requireID(parent);
    setAssociation(parent);
    em.persist(this);
    if (Hibernate.isInitialized(parent))
      DatabaseEntity.addLazily(this, parent.getContractInstances());
    return this;
  }

  public ContractInstanceEntity remove(EntityManager em, BasicAssociationEntity parent) {
    Check.argNotNull(em);
    DatabaseEntity.requireID(this, parent);
    em.remove(this);
    if (Hibernate.isInitialized(parent))
      DatabaseEntity.removeLazily(this, parent.getContractInstances());
    return this;
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, ContractInstanceEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }

  @Override
  public String toString() {
    return "ContractInstanceEntity{" +
        "id=" + getId() +
        ", retrait=" + getRetrait() +
        ", contractTemplateID=" + DatabaseEntity.getID(contractTemplate) +
        '}';
  }
}
