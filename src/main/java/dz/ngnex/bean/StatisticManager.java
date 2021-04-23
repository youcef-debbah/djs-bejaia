
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

import dz.ngnex.entity.ContractDownloadState;

import javax.ejb.EJBException;
import javax.ejb.Local;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

/**
 * @author KratosPOP
 */
@Local
public interface StatisticManager {

  /**
   * Increments visitors count by 1
   */
  void countVisitor(LocalDate date);

  /**
   * Returns the total number of counted visitors for each day in the period
   * between {@code from} and {@code to}.
   * <p>
   * Map keys represent days and values are the total visitors count for
   * the corresponded day
   * <p>
   * days with 0 visitors are not included in the returned map
   *
   * @param from The first included day in this statistic
   * @param to   The last included day in this statistic
   * @return total number of visitor per days
   * @throws NullPointerException     if either {@code from} or {@code to} are {@code null}
   * @throws IllegalArgumentException if {@code from} represent a day later than {@code to} date
   * @throws EJBException             if any other unexpected error occurs
   */
  Map<LocalDate, Integer> getVisitorsStatistic(LocalDate from, LocalDate to);

  /**
   * Returns the total number of visitors (sum of visitors in all days)
   *
   * @return total visitors count
   */

  long getTotalVisitors();

  void countDownload(Integer contractInstanceID);

  Map<ContractDownloadState, Long> getDownloadsStatics();

  Collection<Progress> getDownloadsProgressForSportAssociations();

  Collection<Progress> getDownloadsProgressForYouthAssociations();

  void clear();
}
