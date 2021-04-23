package dz.ngnex.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Comparator;
import java.util.Objects;

@Entity
@Table(name = "season", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name", name = "unique_season_name"),
    @UniqueConstraint(columnNames = "index", name = "unique_season_index"),
})
public class SeasonEntity implements DatabaseEntity {
  private static final long serialVersionUID = 4274632266272217316L;

  public static final Comparator<? super SeasonEntity> COMPARE_BY_INDEX = Comparator.comparingInt(SeasonEntity::getIndex);

  private Integer id;
  private String name;
  private Integer index;
  private Integer version = 0;

  protected SeasonEntity() {
  }

  public SeasonEntity(String name) {
    this.name = Objects.requireNonNull(name);
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "season_id", updatable = false, nullable = false)
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
  @Column(updatable = false, nullable = false, length = Constrains.MAX_IDENTIFIER_SIZE)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
    Integer index = getIndex();
    if (index != null)
      return index.longValue();
    else
      return Long.MAX_VALUE;
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, SeasonEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }

  @Override
  public String toString() {
    return "SeasonEntity{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        ", index=" + getIndex() +
        '}';
  }
}
