package dz.ngnex.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Table(name = "contract_template", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name", name = "unique_contract_template_name"),
    @UniqueConstraint(columnNames = {"lastUpdate", "name"}, name = "unique_contract_template_last_update_and_name")
})
@NamedEntityGraph(name = "loadSelectedTemplate", includeAllAttributes = true)
public class ContractTemplateEntity implements DatabaseEntity {
  private static final long serialVersionUID = -63734141413426846L;

  private Integer id;
  private String name;
  private String template;
  private Set<PropertyEntity> properties = new TreeSet<>();
  private Set<ActivityEntity> activities = new TreeSet<>();
  private SeasonEntity season;
  private Integer version = 0;
  private Long lastUpdate;
  private Long creationDate;
  private Long downloadDate;
  private String lastUpdater;

  public ContractTemplateEntity() {
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "contract_template_id", updatable = false, nullable = false)
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
  @Column(nullable = false, length = Constrains.MAX_IDENTIFIER_SIZE)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    }

  @Size(max = Constrains.MAX_TEXT_LENGTH)
  @Column(columnDefinition = "TEXT", length = Constrains.MAX_TEXT_LENGTH)
  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
    }

  @OneToMany(mappedBy = "contract")
  public Set<PropertyEntity> getProperties() {
    return properties;
  }

  public void setProperties(Set<PropertyEntity> properties) {
    this.properties = Objects.requireNonNull(properties);
    }

  @OneToMany(mappedBy = "contract")
  public Set<ActivityEntity> getActivities() {
    return activities;
  }

  public void setActivities(Set<ActivityEntity> activities) {
    this.activities = activities;
    }

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "season", foreignKey = @ForeignKey(name = "fk_contract_template_to_season"))
  public SeasonEntity getSeason() {
    return season;
  }

  public void setSeason(SeasonEntity season) {
    this.season = season;
    }

  public Long getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(Long lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  public Long getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Long creationDate) {
    this.creationDate = creationDate;
  }

  public Long getDownloadDate() {
    return downloadDate;
  }

  public void setDownloadDate(Long downloadDate) {
    this.downloadDate = downloadDate;
  }

  @Transient
  public boolean canBeDownloadedAt(long epoch) {
    return downloadDate == null || epoch >= downloadDate;
  }

  public String getLastUpdater() {
    return lastUpdater;
  }

  public void setLastUpdater(String lastUpdater) {
    this.lastUpdater = lastUpdater;
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, ContractTemplateEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }

  @Override
  public String toString() {
    return "ContractTemplateEntity{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        '}';
  }
}
