package dz.ngnex.bean;

import dz.ngnex.entity.ContractTemplateEntity;
import dz.ngnex.entity.SeasonEntity;
import dz.ngnex.entity.SeasonStats;
import dz.ngnex.util.Check;
import dz.ngnex.util.TestWithTransaction;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import java.util.List;

@Stateless
@TestWithTransaction
public class SeasonBeanImpl implements SeasonBean {

  @Inject
  private EntityManager em;

  @Inject
  private Logger log;

  @EJB
  private ContractBean contractBean;

  @EJB
  private PrincipalBean principalBean;

  @Override
  public SeasonStats getSeasonStats(Integer seasonID) {
    if (seasonID == null)
      return null;

    List<Integer> contractTemplateIDs = em.createQuery("select t.id from ContractTemplateEntity t where t.season.id = :seasonID", Integer.class)
        .setParameter("seasonID", seasonID)
        .getResultList();

    if (contractTemplateIDs.isEmpty())
      return SeasonStats.EMPTY_STATS;

    Long contractInstancesCount = em.createQuery("select count(c.id) from ContractInstanceEntity c where c.contractTemplate.id in :templateIDs", Long.class)
        .setParameter("templateIDs", contractTemplateIDs)
        .getSingleResult();

    return new SeasonStats(contractTemplateIDs.size(), contractInstancesCount);
  }

  @Override
  public List<SeasonEntity> getAllSeasons() {
    return em.createQuery("select s from SeasonEntity s order by s.name", SeasonEntity.class)
        .getResultList();
  }

  @Override
  public SeasonEntity addSeason(String name) throws IntegrityException {
    if (StringUtils.isBlank(name))
      throw new IllegalArgumentException("season name can't be blank: '" + name + "'");

    ensureSeasonNameNotUsed(name);

    SeasonEntity seasonEntity = new SeasonEntity(name);

    boolean currentSeasonChanged = setBiggestIndex(seasonEntity);
    em.persist(seasonEntity);
    if (currentSeasonChanged)
      updateContractsState(seasonEntity.getId());

    return seasonEntity;
  }

  private void ensureSeasonNameNotUsed(String name) throws IntegrityException {
    Long similarNamesCount = em.createQuery("select count(s.id) from SeasonEntity s where s.name = :name", Long.class)
        .setParameter("name", name)
        .getSingleResult();

    if (similarNamesCount > 0)
      throw new IntegrityException("season name already used: " + name, "identifierUsed");
  }

  @Override
  public void deleteSeason(Integer seasonID) {
    Check.argNotNull(seasonID);
    List<ContractTemplateEntity> templates = contractBean.getTemplates(seasonID);
    for (ContractTemplateEntity template : templates)
      principalBean.deleteTemplate(template.getId());

    em.createQuery("delete from SeasonEntity s where s.id = :seasonID")
        .setParameter("seasonID", seasonID)
        .executeUpdate();

    List<SeasonEntity> remainingSeasons = em.createQuery("select s from SeasonEntity s order by s.index desc", SeasonEntity.class)
        .setMaxResults(1)
        .getResultList();

    if (remainingSeasons.isEmpty())
      updateContractsState(null);
    else
      updateContractsState(remainingSeasons.get(0).getId());
  }

  @Override
  public void updateCurrentSeason(Integer newSeasonID) {
    SeasonEntity newSeason = em.find(SeasonEntity.class, newSeasonID);
    boolean seasonChanged = setBiggestIndex(newSeason);
    if (seasonChanged)
      updateContractsState(newSeasonID);
  }

  private boolean setBiggestIndex(SeasonEntity newSeason) {
    if (newSeason != null) {
      List<SeasonEntity> result = em.createQuery("select s from SeasonEntity s order by s.index desc", SeasonEntity.class)
          .setMaxResults(1)
          .getResultList();

      if (result.isEmpty()) {
        newSeason.setIndex(1);
        return true;
      } else {
        SeasonEntity currentSeason = result.get(0);
        if (!currentSeason.getName().equals(newSeason.getName())) {
          newSeason.setIndex(currentSeason.getIndex() + 1);
          return true;
        } else
          return false;
      }
    } else
      return false;
  }

  private void updateContractsState(Integer currentSeasonID) {
    contractBean.resetAllContractInstancesState(currentSeasonID);
    contractBean.recalcAllContractDownloadState();
  }

  @Override
  @TestWithTransaction(traceSQL = false)
  public void clear() {
    BeanUtil.clearCache(em);
  }

  @AroundInvoke
  public Object benchmarkCalls(InvocationContext ctx) throws Exception {
    return BeanUtil.benchmarkCall(log, ctx);
  }
}
