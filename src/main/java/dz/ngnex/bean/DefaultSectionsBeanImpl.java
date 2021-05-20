package dz.ngnex.bean;

import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.entity.*;
import dz.ngnex.util.Check;
import dz.ngnex.util.TestWithTransaction;
import org.apache.logging.log4j.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import java.util.*;
import java.util.Map.Entry;

@Stateless
@TestWithTransaction
public class DefaultSectionsBeanImpl implements SectionsBean {

  @Inject
  private Logger log;

  @Inject
  private EntityManager em;

  @EJB
  private PrincipalBean principalBean;

  @Inject
  CurrentPrincipal currentPrincipal;

  @Override
  public List<SectionTemplateReference> getAvailableSectionNames() {
    return em.createQuery("select new dz.ngnex.entity.SectionTemplateReference(t.id, t.name) from SectionTemplateEntity t order by t.name", SectionTemplateReference.class)
        .getResultList();
  }

  @Override
  public SectionTemplateEntity addTemplate(String name) throws IntegrityException {
    DatabaseEntity.checkIdentifierSyntax(name);
    SectionTemplateEntity templateEntity = new SectionTemplateEntity(name);
    em.persist(templateEntity);
    return templateEntity;
  }

  @Override
  public SectionEntity add(Integer assoID, String sectionName) throws IntegrityException {
    Check.argNotNull(assoID, sectionName);
    DatabaseEntity.checkIdentifierSyntax(sectionName);
    checkDuplicatedSectionName(em, assoID, sectionName);

    SectionEntity newSection = new SectionEntity(sectionName);
    newSection.setIndex(getNewSectionIndex(em, assoID));
    newSection.persist(em, em.getReference(SportAssociationEntity.class, Check.argNotNull(assoID)));
    return newSection;
  }

  private int getNewSectionIndex(EntityManager em, Integer assoID) {
    Integer maxIndex = em.createQuery("select max(s.index) from SectionEntity s where s.association.id = :assoID", Integer.class)
        .setParameter("assoID", assoID)
        .getSingleResult();
    return maxIndex == null ? 1 : (maxIndex + 1);
  }

  private void checkDuplicatedSectionName(EntityManager em, Integer associationID, String name) throws IntegrityException {
    Long similarNames = em.createQuery("select count(s.id) from SectionEntity s where s.association.id = :associationID and s.name = :name", Long.class)
        .setParameter("associationID", associationID)
        .setParameter("name", name)
        .getSingleResult();

    if (similarNames > 0)
      throw new IntegrityException("section name already used: " + name, "identifierUsed");
  }

  @Override
  public void updateSection(SectionEntity updatedSection) throws IntegrityException {
    DatabaseEntity.requireID(updatedSection);
    SectionEntity sectionEntity = em.find(SectionEntity.class, updatedSection.getId());
    sectionEntity.setIndex(updatedSection.getIndex());
    sectionEntity.setProcessingState(updatedSection.getProcessingState());

//    String updatedSectionName = updatedSection.getName();
//    if (!sectionEntity.getName().equals(updatedSectionName)) {
//      DatabaseEntity.checkIdentifierSyntax(updatedSectionName);
//      checkDuplicatedSectionName(em, sectionEntity.getAssociation().getId(), updatedSectionName);
//      sectionEntity.setName(updatedSectionName);
//    }
  }

  @Override
  public void updateBudgets(Integer associationID,
                            Integer contractInstanceID,
                            Map<Integer, List<BudgetEntity>> potentialBudgetsMap) {
    Check.argNotNull(associationID, contractInstanceID, potentialBudgetsMap);
    ContractInstanceEntity contractInstance = em.getReference(ContractInstanceEntity.class, contractInstanceID);
    Collection<BudgetEntity> persistedBudgets = getBudgets(contractInstanceID);
    if (persistedBudgets.isEmpty())
      for (Entry<Integer, List<BudgetEntity>> potentialBudgetEntry : potentialBudgetsMap.entrySet())
        for (BudgetEntity potentialBudget : potentialBudgetEntry.getValue())
          addNewBudget(contractInstance, potentialBudget);
    else {
      Set<BudgetEntity> toDelete = new HashSet<>();
      for (Entry<Integer, List<BudgetEntity>> potentialBudgetEntry : potentialBudgetsMap.entrySet())
        for (BudgetEntity potentialBudget : potentialBudgetEntry.getValue()) {
          BudgetEntity persistedBudget = BudgetDatabaseEntity.extractBudget(potentialBudget, persistedBudgets);
          if (persistedBudget != null) {
            if (potentialBudget.isNull())
              toDelete.add(persistedBudget);
            else
              persistedBudget.copyBudget(potentialBudget);
          } else
            addNewBudget(contractInstance, potentialBudget);
        }
      BudgetEntity.removeAll(em, contractInstance, toDelete);
    }
  }

  private void addNewBudget(ContractInstanceEntity contractInstance, BudgetEntity potentialBudget) {
    if (!potentialBudget.isNull()) {
      BudgetEntity budgetEntity = new BudgetEntity(potentialBudget.getSection(), potentialBudget.getActivity(), potentialBudget.getBudget());
      budgetEntity.persist(em, contractInstance);
    }
  }

  private List<BudgetEntity> getBudgets(Integer contractInstanceID) {
    return em.createQuery("select b from BudgetEntity b where b.contract.id = :contractInstanceID", BudgetEntity.class)
        .setParameter("contractInstanceID", contractInstanceID)
        .getResultList();
  }

  private List<BudgetEntity> getSectionBudgets(Integer contractInstanceID, Integer sectionID) {
    return em.createQuery("select b from BudgetEntity b where b.contract.id = :contractInstanceID and b.section.id = :sectionID", BudgetEntity.class)
        .setParameter("contractInstanceID", contractInstanceID)
        .setParameter("sectionID", sectionID)
        .getResultList();
  }

  @Override
  public SectionEntity findSection(Integer id) {
    return em.find(SectionEntity.class, id);
  }

  @Override
  public void moveSectionUp(Integer associationID, Integer selectedSectionID) {
    SportAssociationEntity association = em.find(SportAssociationEntity.class, associationID);
    SectionEntity selectedSection = em.find(SectionEntity.class, selectedSectionID);
    if (selectedSection != null) {
      Set<SectionEntity> rawSections = association.getSections();
      if (rawSections.size() > 1) {
        NavigableSet<SectionEntity> orderedSections = new TreeSet<>(rawSections);
        if (!Objects.equals(selectedSection.getId(), orderedSections.first().getId()))
          moveSectionTowardFirst(selectedSection, orderedSections);
      }
    }
  }

  @Override
  public void moveSectionDown(Integer associationID, Integer selectedSectionID) {
    SectionEntity selectedSection = em.find(SectionEntity.class, selectedSectionID);
    if (selectedSection != null) {
      List<SectionEntity> rawSections = getSections(associationID);
      if (rawSections.size() > 1) {
        NavigableSet<SectionEntity> orderedSections = new TreeSet<>(rawSections);
        if (!Objects.equals(selectedSection.getId(), orderedSections.last().getId()))
          moveSectionTowardLast(selectedSection, orderedSections);
      }
    }
  }

  @Override
  public List<SectionEntity> getSections(Integer associationID) {
    return em.createQuery("select s from SectionEntity s where s.association.id = :associationID", SectionEntity.class)
        .setParameter("associationID", associationID)
        .getResultList();
  }

  @Override
  public void delete(int sectionID) {
    em.createQuery("delete from BudgetEntity b where b.section.id = :sectionID")
        .setParameter("sectionID", sectionID)
        .executeUpdate();

    em.createQuery("delete from SectionEntity s where s.id = :sectionID")
        .setParameter("sectionID", sectionID)
        .executeUpdate();
  }

  private void moveSectionTowardFirst(SectionEntity target, NavigableSet<SectionEntity> elements) {
    SectionEntity beforeCurrent = Objects.requireNonNull(elements.pollFirst());
    while (!elements.isEmpty()) {
      SectionEntity current = Objects.requireNonNull(elements.pollFirst());
      if (DatabaseEntity.equalsID(current, target)) {
        swapIndex(beforeCurrent, current);
        return;
      }

      beforeCurrent = current;
    }
  }

  private void moveSectionTowardLast(SectionEntity target, NavigableSet<SectionEntity> elements) {
    SectionEntity beforeCurrent = Objects.requireNonNull(elements.pollLast());
    while (!elements.isEmpty()) {
      SectionEntity current = Objects.requireNonNull(elements.pollLast());
      if (DatabaseEntity.equalsID(current, target)) {
        swapIndex(beforeCurrent, current);
        return;
      }

      beforeCurrent = current;
    }
  }

  private void swapIndex(SectionEntity property1, SectionEntity property2) {
    Integer temp = property1.getIndex();
    property1.setIndex(property2.getIndex());
    property2.setIndex(temp);
  }

  @AroundInvoke
  public Object benchmarkCalls(InvocationContext ctx) throws Exception {
    return BeanUtil.benchmarkCall(log, ctx);
  }

  @Override
  @TestWithTransaction(traceSQL = false)
  public void clear() {
    BeanUtil.clearCache(em);
  }
}
