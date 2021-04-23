package dz.ngnex.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedEntityGraphs({
    @NamedEntityGraph(name = "loadSelectedAssociation", includeAllAttributes = true,
        attributeNodes = @NamedAttributeNode(value = "contractInstances", subgraph = "contractInstances"),
        subgraphs = @NamedSubgraph(name = "contractInstances", attributeNodes = {
            @NamedAttributeNode(value = "contractTemplate"),
            @NamedAttributeNode("propertyValues"),
            @NamedAttributeNode("budgets"),
            @NamedAttributeNode("globalBudgets")
        }))
})
public abstract class BasicAssociationEntity extends AbstractAssociationEntity {
  private static final long serialVersionUID = -7376940710219754941L;

  private Set<ContractInstanceEntity> contractInstances = new TreeSet<>();

  @Transient
  private Map<Integer, Map<Integer, ContractInstanceEntity>> filteredContractInstances;

  protected BasicAssociationEntity() {
  }

  protected BasicAssociationEntity(String name, String password, String description) {
    super(name, password, description);
  }

  @OneToMany(mappedBy = "association")
  public Set<ContractInstanceEntity> getContractInstances() {
    return contractInstances;
  }

  public void setContractInstances(Set<ContractInstanceEntity> contractInstanceEntities) {
    this.contractInstances = contractInstanceEntities;
  }

  @Transient
  @Nullable
  public ContractInstanceEntity getActiveContractInstance(Integer seasonID, Integer templateID) {
    Map<Integer, ContractInstanceEntity> contracts = getActiveContractInstances().get(seasonID);
    return contracts != null ? contracts.get(templateID) : null;
  }

  @Transient
  @NotNull
  public Collection<ContractInstanceEntity> getActiveContractInstances(Integer seasonID) {
    Map<Integer, ContractInstanceEntity> contracts = getActiveContractInstances().get(seasonID);
    return contracts != null ? contracts.values() : Collections.emptyList();
  }

  @Transient
  @NotNull
  public Map<Integer, Map<Integer, ContractInstanceEntity>> getActiveContractInstances() {
    if (filteredContractInstances == null) {
      Map<Integer, Map<Integer, ContractInstanceEntity>> result = new HashMap<>(8);

      for (ContractInstanceEntity contractInstance : getContractInstances())
        if (ContractInstanceState.ACTIVE.equals(contractInstance.getState())) {
          ContractTemplateEntity template = contractInstance.getContractTemplate();
          result.computeIfAbsent(template.getSeason().getId(), id -> new TreeMap<>())
              .put(template.getId(), contractInstance);
        }

      filteredContractInstances = Collections.unmodifiableMap(result);
    }

    return filteredContractInstances;
  }

  @Transient
  public abstract Set<SectionEntity> getVirtualSections();

  @Transient
  @Override
  public abstract EntityReference<? extends BasicAssociationEntity> getReference();

  @Transient
  @Override
  public abstract Class<? extends BasicAssociationEntity> getType();
}
