package dz.ngnex.entity;

import dz.ngnex.bean.IntegrityException;
import dz.ngnex.util.Check;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.*;

public interface DatabaseEntity extends Serializable, Comparable<DatabaseEntity> {

  static void requireID(DatabaseEntity... entities) {
    for (DatabaseEntity entity : entities)
      requireID(entity);
  }

  static <T extends DatabaseEntity> T requireID(T entity) {
    if (entity == null)
      throw new IllegalArgumentException("null entity is not permitted as argument");
    else if (entity.getId() == null)
      throw new IllegalArgumentException("an entity with null ID is not permitted as argument");
    return entity;
  }

  static Integer getID(DatabaseEntity entity) {
    if (entity == null)
      return null;
    else
      return entity.getId();
  }

  static void checkIdentifierSyntax(String identifier) throws IntegrityException {
    Check.argNotNull(identifier);
    if (!Constrains.IDENTIFIER_PATTERN.matcher(identifier).matches())
      throw new IntegrityException("illegal identifier: " + identifier, "identifierWithIllegalChars", identifier);
  }

  static <T extends DatabaseEntity> void addLazily(T entityToAdd, Collection<? super T> collection) {
    if (Hibernate.isInitialized(collection))
      collection.add(entityToAdd);
  }

  static <T extends DatabaseEntity> void removeLazily(T entityToAdd, Collection<? super T> collection) {
    if (Hibernate.isInitialized(collection))
      collection.remove(entityToAdd);
  }

  static <T extends DatabaseEntity> void removeAllLazily(Collection<T> entitiesToRemove, Collection<? super T> collection) {
    if (Hibernate.isInitialized(collection))
      collection.removeAll(entitiesToRemove);
  }

  static boolean containsName(String name, Collection<? extends EntityReference<?>> availableSectionNames) {
    if (name != null && availableSectionNames != null && !availableSectionNames.isEmpty())
      for (EntityReference<?> reference : availableSectionNames)
        if (reference != null && name.equalsIgnoreCase(reference.getName()))
          return true;

    return false;
  }

  Integer getId();

  Integer getVersion();

  @Transient
  default Long getEntityIndex() {
    Integer id = getId();
    if (id != null)
      return id.longValue();
    else
      return Long.MAX_VALUE;
  }

  @Override
  default int compareTo(DatabaseEntity other) {
    return Long.compare(getEntityIndex(), other.getEntityIndex());
  }

  default boolean hasSameId(DatabaseEntity otherEntity) {
    Integer id = Objects.requireNonNull(getEntityId());
    Integer otherId = Objects.requireNonNull(otherEntity.getEntityId());
    return id.equals(otherId);
  }

  default boolean isEqualsById(Object other, Class<? extends DatabaseEntity> type) {
    if (this == other)
      return true;
    else
      return type.isInstance(other) && hasSameId((DatabaseEntity) other);
  }

  default int getIdHashcode() {
    return getEntityId().hashCode();
  }

  @NotNull
  default Integer getEntityId() {
    return Objects.requireNonNull(getId(), "entity id not set: " + toString());
  }

  static boolean equalsID(Integer id1, Integer id2) {
    return id1 != null && id1.equals(id2);
  }

  static boolean equalsID(DatabaseEntity entity, Integer id) {
    return entity != null && equalsID(entity.getId(), id);
  }

  static boolean equalsID(Integer id, DatabaseEntity entity) {
    return equalsID(entity, id);
  }

  static boolean equalsID(DatabaseEntity entity1, DatabaseEntity entity2) {
    return entity1 != null && equalsID(entity2, entity1.getId());
  }

  static boolean notEqualsID(Integer id1, Integer id2) {
    return !equalsID(id1, id2);
  }

  static boolean notEqualsID(DatabaseEntity entity, Integer id) {
    return !equalsID(entity, id);
  }

  static boolean notEqualsID(DatabaseEntity entity1, DatabaseEntity entity2) {
    return !equalsID(entity1, entity2);
  }

  static <E extends DatabaseEntity> E get(final @Nullable Integer id, final @Nullable Collection<E> entities) {
    if (id != null && entities != null)
      for (E entity : entities)
        if (entity != null && id.equals(entity.getId()))
          return entity;

    return null;
  }

  static <E extends DatabaseEntity> int indexOf(final @Nullable EntityReference<?> reference, final @Nullable List<E> entities) {
    if (reference != null)
      return indexOf(reference.getId(), entities);
    else
      return -1;
  }

  static <E extends DatabaseEntity> int indexOf(final @Nullable DatabaseEntity entity, final @Nullable List<E> entities) {
    if (entity != null)
      return indexOf(entity.getId(), entities);
    else
      return -1;
  }

  static <E extends DatabaseEntity> int indexOf(final @Nullable Integer id, final @Nullable List<E> entities) {
    if (id != null && entities != null)
      for (int index = 0; index < entities.size(); index++) {
        E entity = entities.get(index);
        if (entity != null && id.equals(entity.getId()))
          return index;
      }

    return -1;
  }

  static boolean contains(final @Nullable DatabaseEntity entity, final @Nullable Iterable<? extends DatabaseEntity> entities) {
    if (entity != null)
      return contains(entity.getId(), entities);
    else
      return false;
  }

  static boolean contains(final @Nullable Integer id, final @Nullable Iterable<? extends DatabaseEntity> entities) {
    if (id != null && entities != null)
      for (DatabaseEntity entity : entities)
        if (entity != null && id.equals(entity.getId()))
          return true;

    return false;
  }

  static <T extends DatabaseEntity> T remove(final @Nullable DatabaseEntity entity, final @Nullable Collection<T> entities) {
    return entity != null ? remove(entity.getId(), entities) : null;
  }

  static <T extends DatabaseEntity> T remove(final @Nullable Integer id, final @Nullable Collection<T> entities) {
    if (id != null && entities != null) {
      final Iterator<T> iterator = entities.iterator();
      while (iterator.hasNext()) {
        T currentEntity = iterator.next();
        if (currentEntity != null && id.equals(currentEntity.getId())) {
          iterator.remove();
          return currentEntity;
        }
      }
    }

    return null;
  }
}
