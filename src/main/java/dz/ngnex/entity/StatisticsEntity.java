
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

package dz.ngnex.entity;

import javax.inject.Singleton;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Date;
import java.util.Objects;

/**
 * The persistent class for the statistic database table.
 */
@Entity
@Table(name = "statistic")
public class StatisticsEntity implements Serializable {

  private static final long serialVersionUID = 928504791039114923L;

  private Date statisticID;

  private Integer visitorsCount = 0;

  @Id
  @Column(name = "statistic_id", updatable = false)
  public Date getStatisticID() {
    return statisticID;
  }

  public void setStatisticID(Date statisticID) {
    this.statisticID = statisticID;
    }

  @NotNull
  @Column(nullable = false)
  public Integer getVisitorsCount() {
    return visitorsCount;
  }

  public void setVisitorsCount(Integer visitorsCount) {
    this.visitorsCount = visitorsCount;
    }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof StatisticsEntity))
      return false;
    StatisticsEntity that = (StatisticsEntity) o;
    return getStatisticID().equals(Objects.requireNonNull(that.getStatisticID()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(getStatisticID());
  }

  @Override
  public String toString() {
    return "StatisticsEntity [statisticID=" + statisticID + ", visitorsCount=" + visitorsCount + "]";
  }
}
