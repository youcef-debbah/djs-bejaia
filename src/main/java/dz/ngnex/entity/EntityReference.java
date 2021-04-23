package dz.ngnex.entity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.util.Objects;

public class EntityReference<T extends DatabaseEntity> implements Serializable {

  private static final long serialVersionUID = 9164365648958640863L;

  private final Integer id;
  private final String name;
  private final Class<T> type;

  public EntityReference(Integer id, String name, Class<T> type) {
    this.id = Objects.requireNonNull(id);
    this.name = Objects.requireNonNull(name);
    this.type = Objects.requireNonNull(type);
  }

  @NotNull
  public Integer getId() {
    return id;
  }

  @NotNull
  public String getName() {
    return name;
  }

  @NotNull
  public Class<T> getType() {
    return type;
  }

  @Override
  public String toString() {
    return "EntityReference{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        ", type=" + getType() +
        '}';
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof EntityReference))
      return false;
    EntityReference that = (EntityReference) o;
    return getId().equals(that.getId()) &&
        getName().equals(that.getName()) &&
        getType().equals(that.getType());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(getId(), getName(), getType());
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    Objects.requireNonNull(id);
    Objects.requireNonNull(name);
    Objects.requireNonNull(type);
  }

  private void readObjectNoData() throws InvalidObjectException {
    throw new InvalidObjectException("Stream data required");
  }
}
