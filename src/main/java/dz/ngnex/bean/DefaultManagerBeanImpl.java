
/*
 * Handcrafted with love by Youcef DEBBAH
 * Copyright 2019 youcef-debbah@hotmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dz.ngnex.bean;

import dz.ngnex.entity.*;
import dz.ngnex.util.Check;
import dz.ngnex.util.TestWithTransaction;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Stateless
@TestWithTransaction
public class DefaultManagerBeanImpl implements StatisticManager {

  @Inject
  private Logger log;

  @Inject
  private EntityManager em;

  @EJB
  private PrincipalBean principalBean;

  @EJB
  private SeasonBean seasonBean;

  @EJB
  private ContractBean contractBean;

  @Override
  public void countVisitor(LocalDate date) {
    Date currentDate = Date.valueOf(date);
    try {
      StatisticsEntity statisticsEntity = em.find(StatisticsEntity.class, currentDate);
      if (statisticsEntity == null) {
        statisticsEntity = new StatisticsEntity();
        statisticsEntity.setStatisticID(currentDate);
      }

      statisticsEntity.setVisitorsCount(statisticsEntity.getVisitorsCount() + 1);
      em.merge(statisticsEntity);

    } catch (Exception e) {
      throw new EJBException("Failed to increment visitors count for date: " + currentDate, e);
    }
  }

  @Override
  public SortedMap<LocalDate, Integer> getVisitorsStatistic(LocalDate from, LocalDate to) {
    Objects.requireNonNull(from);
    Objects.requireNonNull(to);

    if (from.isAfter(to))
      return Collections.emptySortedMap();

    try {
      List<StatisticsEntity> result = em.createQuery("SELECT s FROM StatisticsEntity s WHERE s.statisticID BETWEEN :from AND :to", StatisticsEntity.class)
          .setParameter("from", Date.valueOf(from))
          .setParameter("to", Date.valueOf(to))
          .getResultList();

      SortedMap<LocalDate, Integer> data = new TreeMap<>();
      for (StatisticsEntity state : result)
        data.put(state.getStatisticID().toLocalDate(), state.getVisitorsCount());

      return data;
    } catch (Exception e) {
      throw new EJBException("Can not retreive Statistics from " + from + " to " + to, e);
    }
  }

  @Override
  public long getTotalVisitors() {
    try {
      return BeanUtil.getCountResult(em.createQuery("SELECT SUM(s.visitorsCount) FROM StatisticsEntity s", Long.class));
    } catch (Exception e) {
      throw new EJBException("Error during retreiving Total Visitors Count ", e);
    }
  }

  @Override
  public void countDownload(Integer contractInstanceId) {
    Check.argNotNull(contractInstanceId);
    ContractInstanceEntity contractInstance = em.find(ContractInstanceEntity.class, contractInstanceId);
    Integer downloads = contractInstance.getRetrait();
    if (downloads < 1)
      contractInstance.setRetrait(1);
    else
      contractInstance.setRetrait(downloads + 1);

    contractInstance.setLastDownload(System.currentTimeMillis());
    BasicAssociationEntity association = contractInstance.getAssociation();
    principalBean.recalcContractDownloadState(association.getReference());
  }

  @Override
  public Map<ContractDownloadState, Long> getDownloadsStatics() {
    try {
      HashMap<ContractDownloadState, Long> map = new HashMap<>();

      long noContractAssignedYet = 0L;
      long allAssignedContractsDownloaded = 0L;
      long assignedContractsPartiallyDownloaded = 0L;
      long noAssignedContractDownloaded = 0L;

      List<BasicAssociationEntity> allUsers = principalBean.getAllAssociations();
      for (BasicAssociationEntity association : allUsers)
        switch (association.getContractDownloadState()) {
          case NO_CONTRACT_ASSIGNED_YET:
            noContractAssignedYet++;
            break;
          case NO_ASSIGNED_CONTRACT_DOWNLOADED:
            noAssignedContractDownloaded++;
            break;
          case ALL_ASSIGNED_CONTRACTS_DOWNLOADED:
            allAssignedContractsDownloaded++;
            break;
          case ASSIGNED_CONTRACTS_PARTIALLY_DOWNLOADED:
            assignedContractsPartiallyDownloaded++;
            break;
        }

      map.put(ContractDownloadState.NO_CONTRACT_ASSIGNED_YET, noContractAssignedYet);
      map.put(ContractDownloadState.ALL_ASSIGNED_CONTRACTS_DOWNLOADED, allAssignedContractsDownloaded);
      map.put(ContractDownloadState.ASSIGNED_CONTRACTS_PARTIALLY_DOWNLOADED, assignedContractsPartiallyDownloaded);
      map.put(ContractDownloadState.NO_ASSIGNED_CONTRACT_DOWNLOADED, noAssignedContractDownloaded);

      return map;
    } catch (Exception e) {
      throw new EJBException(e);
    }
  }

  @Override
  public Collection<Progress> getDownloadsProgressForSportAssociations() {
    return getProgresses(principalBean.getAllSportAssociations());
  }

  @Override
  public Collection<Progress> getDownloadsProgressForYouthAssociations() {
    return getProgresses(principalBean.getAllYouthAssociations());
  }

  @NotNull
  private Collection<Progress> getProgresses(List<? extends BasicAssociationEntity> associations) {
    Map<Integer, Progress> progresses = new TreeMap<>();
    SeasonEntity currentSeason = contractBean.getCurrentSeason();
    if (currentSeason != null)
      for (BasicAssociationEntity association : associations)
        for (ContractInstanceEntity contractInstance : association.getActiveContractInstances(currentSeason.getId()))
          countContractProgress(progresses, contractInstance);

    return progresses.values();
  }

  private void countContractProgress(Map<Integer, Progress> progresses, ContractInstanceEntity contractInstance) {
    ContractTemplateEntity contractTemplate = contractInstance.getContractTemplate();
    if (contractTemplate != null && contractTemplate.getId() != null) {
      String contractTemplateName = contractTemplate.getName();
      Progress progress = progresses.get(contractTemplate.getId());
      if (progress == null) {
        progress = new Progress(contractTemplateName);
        progresses.put(contractTemplate.getId(), progress);
      }

      if (contractInstance.getRetrait() > 0) {
        progress.countAsDone();
        progress.addDoneBudget(contractInstance.getGlobalMontant());
      } else {
        progress.countAsNotYet();
        progress.addNotYetBudget(contractInstance.getGlobalMontant());
      }
    }
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
