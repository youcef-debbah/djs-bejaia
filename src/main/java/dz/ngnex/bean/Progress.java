package dz.ngnex.bean;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

public final class Progress implements Comparable<Progress> {

  private final String title;
  private long done;
  private BigDecimal doneBudget = BigDecimal.ZERO;
  private long notYet;
  private BigDecimal notYetBudget = BigDecimal.ZERO;

  public Progress(String title) {
    this.title = Objects.requireNonNull(title);
  }

  public long getCurrentPercentage() {
    if (getTotal() == 0)
      return 100;
    else {
      long result = (long) ((100.0 * done) / getTotal());
      if (result > 100)
        return 100;
      else
        return result;
    }
  }

  public void countAsDone() {
    done++;
  }

  public void countAsNotYet() {
    notYet++;
  }

  public void addDoneBudget(BigDecimal budget) {
    if (budget != null)
      doneBudget = doneBudget.add(budget);
  }

  public void addNotYetBudget(BigDecimal budget) {
    if (budget != null)
      notYetBudget = notYetBudget.add(budget);
  }

  @NotNull
  public String getTitle() {
    return title;
  }

  public long getTotal() {
    return done + notYet;
  }

  public BigDecimal getTotalBudget() {
    return doneBudget.add(notYetBudget);
  }

  public long getDone() {
    return done;
  }

  public BigDecimal getDoneBudget() {
    return doneBudget;
  }

  public long getNotYet() {
    return notYet;
  }

  public BigDecimal getNotYetBudget() {
    return notYetBudget;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Progress))
      return false;
    Progress progress = (Progress) o;
    return getTitle().equals(progress.getTitle());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTitle());
  }

  @Override
  public int compareTo(@NotNull Progress other) {
    return String.CASE_INSENSITIVE_ORDER.compare(getTitle(), other.getTitle());
  }
}
