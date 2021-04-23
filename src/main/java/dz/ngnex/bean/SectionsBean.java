package dz.ngnex.bean;

import dz.ngnex.entity.BudgetEntity;
import dz.ngnex.entity.EntityReference;
import dz.ngnex.entity.SectionEntity;

import javax.ejb.Local;
import java.util.List;
import java.util.Map;

@Local
public interface SectionsBean {
  List<EntityReference> getAvailableSectionNames();

  void delete(int sectionID);

  SectionEntity add(Integer assoID, String section) throws IntegrityException;

  void updateSection(SectionEntity section) throws IntegrityException;

  SectionEntity findSection(Integer id);

  void moveSectionUp(Integer associationID, Integer selectedSection);

  void moveSectionDown(Integer associationID, Integer selectedSectionID);

  void updateBudgets(Integer associationID,
                     Integer contractInstanceID,
                     Map<Integer, List<BudgetEntity>> potentialBudgetsMap);

  List<SectionEntity> getSections(Integer associationID);

  void clear();
}
