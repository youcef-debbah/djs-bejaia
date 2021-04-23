package dz.ngnex.view;

import dz.ngnex.entity.BasicAssociationEntity;
import dz.ngnex.entity.DatabaseEntity;
import dz.ngnex.entity.EntityReference;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

public class RefreshAccountEvent implements Serializable {
  private static final long serialVersionUID = 3219223340349680014L;

  private BasicAssociationEntity association;
  private final EntityReference<? extends BasicAssociationEntity> associationReference;
  private final Integer contractTemplateID;
  private final Integer sectionID;

  public static RefreshAccountEvent refreshAll() {
    return new RefreshAccountEvent(null, null, null);
  }

  public RefreshAccountEvent(BasicAssociationEntity association) {
    setAssociation(DatabaseEntity.requireID(association));
    this.associationReference = association.getReference();
    this.contractTemplateID = null;
    this.sectionID = null;
  }

  public RefreshAccountEvent(EntityReference<? extends BasicAssociationEntity> associationReference,
                             Integer contractTemplateID,
                             Integer sectionID) {
    this(associationReference, contractTemplateID, sectionID, true);
  }

  public RefreshAccountEvent(EntityReference<? extends BasicAssociationEntity> associationReference,
                             Integer contractTemplateID,
                             Integer sectionID,
                             boolean stateful) {
    setAssociation(null);
    this.associationReference = associationReference;
    this.contractTemplateID = contractTemplateID;
    this.sectionID = sectionID;
  }

  public BasicAssociationEntity getAssociation() {
    return association;
  }

  public BasicAssociationEntity getAssociation(Supplier<? extends BasicAssociationEntity> supplier) {
    Objects.requireNonNull(supplier);
    if (association != null)
      return association;
    else {
        association = supplier.get();
      return association;
    }
  }

  public void setAssociation(BasicAssociationEntity basicAssociationEntity) {
    this.association = basicAssociationEntity;
  }

  public EntityReference<? extends BasicAssociationEntity> getAssociationReference() {
    return associationReference;
  }

  public Integer getContractTemplateID() {
    return contractTemplateID;
  }

  public Integer getSectionID() {
    return sectionID;
  }

  @Override
  public String toString() {
    return "RefreshAccountEvent{" +
        "contractTemplateName='" + getContractTemplateID() + '\'' +
        ", sectionID=" + getSectionID() +
        ", association=" + associationReference +
        '}';
  }
}
