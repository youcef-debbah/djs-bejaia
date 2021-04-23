package dz.ngnex.entity;

import java.io.Serializable;
import java.util.Objects;

public final class SeasonStats implements Serializable {
  private static final long serialVersionUID = -8144388462851496551L;
  public static final SeasonStats EMPTY_STATS = new SeasonStats(0, 0);

  private final int contractTemplatesCount;
  private final long contractInstancesCount;

  public SeasonStats(int contractTemplatesCount, long contractInstancesCount) {
    this.contractTemplatesCount = contractTemplatesCount;
    this.contractInstancesCount = contractInstancesCount;
  }

  public int getContractTemplatesCount() {
    return contractTemplatesCount;
  }

  public long getContractInstancesCount() {
    return contractInstancesCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof SeasonStats))
      return false;
    SeasonStats that = (SeasonStats) o;
    return getContractTemplatesCount() == that.getContractTemplatesCount() &&
        getContractInstancesCount() == that.getContractInstancesCount();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getContractTemplatesCount(), getContractInstancesCount());
  }

  @Override
  public String toString() {
    return "SeasonStats{" +
        "contractTemplatesCount=" + getContractTemplatesCount() +
        ", contractInstancesCount=" + getContractInstancesCount() +
        '}';
  }
}
