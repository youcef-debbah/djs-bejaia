package dz.ngnex.view;

import dz.ngnex.util.HasName;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;

public class ReceiverItem implements HasName, Serializable, Comparable<ReceiverItem> {

  private final String name;
  private final String phone;
  private final String agrement;

  public ReceiverItem(String name, String phone, String agrement) {
    this.name = normalize(Objects.requireNonNull(name));
    this.phone = normalize(phone);
    this.agrement = normalize(agrement);
  }

  @Override
  public String getName() {
    return name;
  }

  public String getPhone() {
    return phone;
  }

  public String getAgrement() {
    return agrement;
  }

  @Override
  public int compareTo(@NotNull ReceiverItem other) {
    return name.compareTo(other.name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ReceiverItem that = (ReceiverItem) o;
    return name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return "ReceiverItem{" +
        "name='" + name + '\'' +
        ", agrement='" + agrement + '\'' +
        ", phone='" + phone + '\'' +
        '}';
  }

  public static Collection<String> getNames(Collection<ReceiverItem> items) {
    if (items == null)
      return Collections.emptyList();
    else {
      ArrayList<String> result = new ArrayList<>(items.size());
      for (ReceiverItem item : items)
        result.add(item.getName());
      return result;
    }
  }

  public static List<ReceiverItem> filter(List<ReceiverItem> allReceivers, String input) {
    if (allReceivers == null || allReceivers.isEmpty())
      return Collections.emptyList();
    else if (StringUtils.isBlank(input))
      return allReceivers;
    else {
      String normalizedInput = normalize(input);
      List<ReceiverItem> filtered = new ArrayList<>(allReceivers.size());

      for (ReceiverItem receiver : allReceivers)
        if (receiver.name.contains(normalizedInput))
          filtered.add(receiver);

      return filtered;
    }
  }

  public static String normalize(String value) {
    if (StringUtils.isBlank(value))
      return "";
    else
      return value.trim().toLowerCase();
  }
}
