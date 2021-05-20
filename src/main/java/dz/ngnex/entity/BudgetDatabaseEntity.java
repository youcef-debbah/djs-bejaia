package dz.ngnex.entity;

import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Objects;

public interface BudgetDatabaseEntity extends DatabaseEntity {

  @Transient
  boolean isPresent();

  @Transient
  boolean isNull();

  BigDecimal getBudget();

  SectionEntity getSection();

  ActivityEntity getActivity();

  static <T extends BudgetDatabaseEntity> T extractBudget(BudgetDatabaseEntity target, Iterable<T> budgets) {
    return extractBudget(target.getSection(), target.getActivity(), budgets);
  }

  static <T extends BudgetDatabaseEntity> T extractBudget(SectionEntity section, ActivityEntity activity, Iterable<T> budgets) {
    Integer targetSectionID = DatabaseEntity.getID(section);
    Integer targetActivityID = DatabaseEntity.getID(activity);
    if (targetActivityID != null && budgets != null)
      for (T budget : budgets)
        if (DatabaseEntity.equalsID(budget.getActivity(), targetActivityID)
            && Objects.equals(DatabaseEntity.getID(budget.getSection()), targetSectionID))
          return budget;

    return null;
  }
}
