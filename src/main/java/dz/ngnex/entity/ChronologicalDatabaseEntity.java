package dz.ngnex.entity;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface ChronologicalDatabaseEntity extends DatabaseEntity {

  Long getEpoch();

  Long getPriority();

  static <T extends ChronologicalDatabaseEntity> List<T> mergeOrdered(List<T> list1, List<T> list2) {
    if (list1 == null || list1.isEmpty())
      return list2 == null ? Collections.emptyList() : list2;
    else if (list2 == null || list2.isEmpty())
      return list1;
    else {
      ArrayList<T> result = new ArrayList<>(list1.size() + list2.size());
      Iterator<T> iterator1 = list1.iterator();
      Iterator<T> iterator2 = list2.iterator();
      T currentItem1 = getNext(iterator1);
      T currentItem2 = getNext(iterator2);
      while (currentItem1 != null && currentItem2 != null) {
        long priorityDiff = diff(currentItem1.getPriority(), currentItem2.getPriority());

        if (priorityDiff > 0 || (priorityDiff == 0 && diff(currentItem1.getEpoch(), currentItem2.getEpoch()) >= 0))
          currentItem1 = addAndGetNext(result, iterator1, currentItem1);
        else
          currentItem2 = addAndGetNext(result, iterator2, currentItem2);

      }

      if (currentItem1 != null)
        do
          currentItem1 = addAndGetNext(result, iterator1, currentItem1);
        while (currentItem1 != null);

      if (currentItem2 != null)
        do
          currentItem2 = addAndGetNext(result, iterator2, currentItem2);
        while (currentItem2 != null);

      return result;
    }
  }

  static long diff(Long t1, Long t2) {
    if (t1 == null && t2 == null)
      return 0L;

    if (t1 == null)
      return -1;
    else if (t2 == null)
      return 1;
    else
      return t1 - t2;
  }

  @Nullable
  static <T extends ChronologicalDatabaseEntity> T addAndGetNext(Collection<T> result, Iterator<T> iterator, T current) {
    result.add(current);
    return getNext(iterator);
  }

  static <T> T getNext(Iterator<T> iterator) {
    if (iterator != null)
      while (iterator.hasNext()) {
        T next = iterator.next();
        if (next != null)
          return next;
      }

    return null;
  }
}
