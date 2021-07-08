package dz.ngnex.entity;

import dz.ngnex.util.Check;
import dz.ngnex.util.TemplatedContent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "property_value", uniqueConstraints = {@UniqueConstraint(name = "unique_value_for_property_per_contract_instance", columnNames = {"contract_instance", "property"})})
public class PropertyValueEntity implements DatabaseEntity {
  private static final long serialVersionUID = -3787481429739409216L;
  private static Logger log = LogManager.getLogger(PropertyValueEntity.class);

  private Integer id;
  private PropertyEntity property;
  private String header;
  private String value;
  private Integer version = 0;
  private ContractInstanceEntity contract;

  public PropertyValueEntity() {
  }

  @PrePersist
  @PreUpdate
  public void preStore() {
    value = TemplatedContent.newLinesToBrElements(value);
  }

  @PostLoad
  @PostPersist
  @PostUpdate
  public void postLoad() {
    value = TemplatedContent.brElementsToNewLines(value);
  }

  public PropertyValueEntity(PropertyEntity property) {
    setProperty(Objects.requireNonNull(property));
  }

  public PropertyValueEntity(PropertyEntity property, String header) {
    this(property);
    setHeader(header);
  }

  public PropertyValueEntity(PropertyEntity property, String header, String value) {
    this(property, header);
    setValue(value);
  }

  @Transient
  public void copyValue(PropertyValueEntity source) {
    setValue(source.getValue());
    setHeader(source.getHeader());
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "property_value_id", updatable = false, nullable = false)
  public Integer getId() {
    return id;
  }

  @Version
  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  @Size(max = Constrains.MAX_TEXT_LENGTH)
  @Column(columnDefinition = "TEXT", length = Constrains.MAX_TEXT_LENGTH)
  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  @Size(max = Constrains.MAX_TEXT_LENGTH)
  @Column(columnDefinition = "TEXT", length = Constrains.MAX_TEXT_LENGTH)
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @NotNull
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "property", foreignKey = @ForeignKey(name = "fk_property_value_to_property"))
  public PropertyEntity getProperty() {
    return property;
  }

  public void setProperty(PropertyEntity property) {
    this.property = property;
  }

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "contract_instance", foreignKey = @ForeignKey(name = "fk_property_value_to_contract_instance"))
  public ContractInstanceEntity getContract() {
    return contract;
  }

  public PropertyValueEntity persist(EntityManager em, ContractInstanceEntity parent) {
    Check.argNotNull(em);
    DatabaseEntity.requireID(parent);
    this.setContract(parent);
    em.persist(this);
    if (Hibernate.isInitialized(parent))
      DatabaseEntity.addLazily(this, parent.getPropertyValues());
    return this;
  }

  public static void removeAll(EntityManager em, ContractInstanceEntity parent, Set<PropertyValueEntity> children) {
    Check.argNotNull(em, children);
    DatabaseEntity.requireID(parent);

    if (!children.isEmpty()) {
      em.createQuery("delete from PropertyValueEntity p where p in :children")
          .setParameter("children", children)
          .executeUpdate();

      if (Hibernate.isInitialized(parent))
        DatabaseEntity.removeAllLazily(children, parent.getPropertyValues());
    }
  }

  public void setContract(ContractInstanceEntity contract) {
    this.contract = contract;
  }

  @Transient
  public BigDecimal getValueAsDecimal() {
    if (isNullValue())
      return BigDecimal.ZERO;
    else
      try {
        return new BigDecimal(value);
      } catch (RuntimeException e) {
        throw new RuntimeException("invalid decimal value: " + value, e);
      }
  }

  @Transient
  public void setValueAsDecimal(BigDecimal decimal) {
    if (decimal == null || BigDecimal.ZERO.compareTo(decimal) == 0)
      value = null;
    else
      value = decimal.toString();
  }

  @Transient
  public Date getValueAsDate() {
    if (isNullValue())
      return null;
    else
      try {
        return new Date(getValueAsLong());
      } catch (RuntimeException e) {
        log.error("could not parse value as date: " + value, e);
        return null;
      }
  }

  @Transient
  public void setValueAsDate(Date date) {
    if (date == null)
      value = null;
    else
      value = String.valueOf(date.getTime());
  }

  @Transient
  public Long getValueAsLong() {
    try {
      if (isNullValue())
        return null;
      else
        return Long.parseLong(value);
    } catch (RuntimeException e) {
      log.error("could not parse value as long: " + value, e);
      return null;
    }
  }

  @Transient
  public void setValueAsLong(Long l) {
    if (l == null)
      value = null;
    else
      value = String.valueOf(l);
  }

  @Transient
  public String getActualHeader() {
    if (isNullHeader())
      return getProperty().getDefaultHeader();
    else
      return getHeader();
  }

  @Transient
  public String getValueAsHtml() {
    return TemplatedContent.newLinesToBrElements(getValue());
  }

  @Override
  @Transient
  public Long getEntityIndex() {
    return getProperty().getEntityIndex();
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, PropertyValueEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }

  @Transient
  public boolean isNull() {
    return isNullValue() && isNullHeader();
  }

  @Transient
  public boolean isNullValue() {
    return value == null || value.isEmpty();
  }

  @Transient
  public boolean isNullHeader() {
    return header == null || header.isEmpty();
  }

  @Override
  public String toString() {
    return "PropertyValueEntity{" +
        "id=" + getId() +
        ", value='" + getValue() + '\'' +
        ", propertyID=" + DatabaseEntity.getID(property) +
        '}';
  }

  public static PropertyValueEntity extractValue(PropertyEntity targetProperty, Iterable<PropertyValueEntity> values) {
    Integer targetPropertyID = targetProperty.getId();
    if (targetPropertyID != null && values != null)
      for (PropertyValueEntity value : values)
        if (DatabaseEntity.equalsID(value.getProperty(), targetPropertyID))
          return value;

    return null;
  }
}
