package dz.ngnex.bean;

import dz.ngnex.entity.*;

import javax.ejb.Local;
import java.util.List;
import java.util.Map;

@Local
public interface SectionsBean {
  List<SectionTemplateReference> getAvailableSectionNames();

  void delete(int sectionID);

  SectionTemplateEntity addTemplate(String name) throws IntegrityException;

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
