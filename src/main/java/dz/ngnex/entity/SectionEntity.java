package dz.ngnex.entity;

import dz.ngnex.bean.IntegrityException;
import dz.ngnex.util.Check;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

@Entity
@Table(name = "section", uniqueConstraints = @UniqueConstraint(columnNames = {"sport_association", "name"}, name = "unique_section_name_per_association"))
@NamedQueries({
    @NamedQuery(name = "SectionEntity.delete", query = "delete SectionEntity s where s.id = :id")
})
public class SectionEntity implements DatabaseEntity {
  private static final long serialVersionUID = -685648569475910266L;

  private Integer id;

  private Integer index;

  private String name;

  private ProcessingState processingState = ProcessingState.NOT_STARTED;
  private Integer version = 0;

  private SportAssociationEntity association;

  public SectionEntity() {
  }

  public SectionEntity(SectionTemplateEntity template) {
    this.name = Objects.requireNonNull(template.getName());
  }

  public SectionEntity(String section) {
    this.name = Objects.requireNonNull(section);
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "section_id", updatable = false, nullable = false)
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

  @Override
  @Transient
  public Long getEntityIndex() {
    return getIndex().longValue();
  }

  @NotNull
  @Column(nullable = false, length = Constrains.MAX_IDENTIFIER_SIZE)
  @Size(min = Constrains.Min_IDENTIFIER_SIZE, max = Constrains.MAX_IDENTIFIER_SIZE)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @NotNull
  @Enumerated
  @Column(nullable = false)
  public ProcessingState getProcessingState() {
    return processingState;
  }

  public void setProcessingState(ProcessingState processingState) {
    this.processingState = processingState;
  }

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "sport_association", foreignKey = @ForeignKey(name = "fk_section_to_sport_association"))
  public SportAssociationEntity getAssociation() {
    return association;
  }

  public void setAssociation(SportAssociationEntity association) {
    this.association = association;
  }

  public SectionEntity persist(EntityManager em, SportAssociationEntity parent) throws IntegrityException {
    Check.argNotNull(em);
    DatabaseEntity.requireID(parent);
    setAssociation(parent);
    em.persist(this);
    if (Hibernate.isInitialized(parent))
      DatabaseEntity.addLazily(this, parent.getSections());
    return this;
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, SectionEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }

  @Override
  public String toString() {
    return name;
  }
}
