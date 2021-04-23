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

/*
 * Copyright (c) 2016 youcef debbah (youcef-kun@hotmail.fr)
 *
 * This file is part of cervex.
 *
 * cervex is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * cervex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cervex.  If not, see <http://www.gnu.org/licenses/>.
 *
 * created on 2017/03/17
 * @header
 */

package dz.ngnex.view;

import dz.ngnex.bean.ContractBean;
import dz.ngnex.bean.SeasonBean;
import dz.ngnex.bean.StatisticManager;
import dz.ngnex.control.CurrentVisitorsCounter;
import dz.ngnex.control.LocaleManager;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.ContractDownloadState;
import dz.ngnex.entity.SeasonEntity;
import dz.ngnex.util.Messages;
import dz.ngnex.util.ViewModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.chart.*;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.validation.constraints.Past;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author youcef debbah
 */
@ViewModel
public class CommandBoard implements Serializable {

  private static final long serialVersionUID = 2995181812740976709L;

  private static final Logger log = LogManager.getLogger(CommandBoard.class);

  @EJB
  private StatisticManager statisticManager;

  @EJB
  private ContractBean contractBean;

  @Inject
  private Messages messages;

  private BarChartModel visitorsChartModel;
  private PieChartModel downloadsCharsModel;

  private Long totalVisitors;

  private String chartStyle;

  @Past
  private Date visitorsStatFrom;
  @Past
  private Date visitorsStatTo;

  @Inject
  private Meta meta;

  @Inject
  private LocaleManager localeManager;

  @EJB
  private SeasonBean seasonBean;

  private String currentSeasonName;
  private boolean downloadsProgressEnabled;

  public CommandBoard() {
    chartStyle = "height: 300px; background: transparent; color: white;";

    setVisitorsStatTo(new Date());

    Calendar fromDate = Calendar.getInstance();
    fromDate.setTime(getVisitorsStatTo());
    fromDate.add(Calendar.DAY_OF_MONTH, -10);
    setVisitorsStatFrom(fromDate.getTime());
  }

  public String getCurrentSeasonName() {
    if (currentSeasonName == null)
      try {
        SeasonEntity currentSeason = contractBean.getCurrentSeason();
        if (currentSeason != null)
          currentSeasonName = currentSeason.getName();
        else
          currentSeasonName = messages.get("nothing");
      } catch (Exception e) {
        meta.handleException(e);
        return "";
      }

    return currentSeasonName;
  }

  public Long getTotalVisitors() {
    if (totalVisitors == null)
      try {
        totalVisitors = statisticManager.getTotalVisitors();
      } catch (Exception e) {
        meta.handleException(e);
        return 0L;
      }
    return totalVisitors;
  }

  private void initDownloadsChartModel() {
    downloadsCharsModel = new PieChartModel();
    downloadsCharsModel.setTitle(messages.get("downloadsProgress") + " (" + getCurrentSeasonName() + ")");
    downloadsCharsModel.setShowDataLabels(true);
    downloadsCharsModel.setLegendPosition("w");
    downloadsCharsModel.setShadow(true);

    Map<ContractDownloadState, Long> statics;

    try {
      statics = statisticManager.getDownloadsStatics();
    } catch (Exception e) {
      statics = null;
      meta.handleException(e);
    }

    if (statics != null) {
      for (Entry<ContractDownloadState, Long> entry : statics.entrySet()) {
        ContractDownloadState key = entry.getKey();
        String label = messages.get(key.name());
        downloadsCharsModel.set(label, entry.getValue());
      }
    }
  }

  private void initVisitorsChartModel() {
    visitorsChartModel = new BarChartModel();
    visitorsChartModel.setTitle(messages.get("visitorsPerDay"));
    visitorsChartModel.setAnimate(true);
    visitorsChartModel.setStacked(false);
    visitorsChartModel.setShowDatatip(false);
    visitorsChartModel.setShadow(true);
    visitorsChartModel.setLegendPlacement(LegendPlacement.INSIDE);

    Axis xAxis = visitorsChartModel.getAxis(AxisType.X);
    xAxis.setTickAngle(-50);

    updateVisitorsModel();
  }

  public void updateVisitorsModel() {
    Locale clientLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", clientLocale);

    Date to = getVisitorsStatTo();
    LocalDate toDate = (to == null) ? localeManager.getCurrentDate() : localeManager.toLocalDate(to);

    Date from = getVisitorsStatFrom();
    LocalDate fromDate = (from == null) ? toDate.minusDays(9) : localeManager.toLocalDate(from);

    visitorsChartModel.clear();

    if (fromDate.isAfter(toDate)) {
      FacesContext context = FacesContext.getCurrentInstance();
      context.addMessage("global", new FacesMessage(FacesMessage.SEVERITY_ERROR,
          messages.get("invalidInterval"), messages.get("fromMustBeBeforTO")));
    }

    Map<LocalDate, Integer> stats = statisticManager.getVisitorsStatistic(fromDate, toDate);
    try {
      ChartSeries visitors = new ChartSeries();
      Iterator<Entry<LocalDate, Integer>> data = stats.entrySet().iterator();
      LocalDate current = fromDate;
      while (data.hasNext()) {
        Entry<LocalDate, Integer> newData = data.next();
        while ((current = current.plusDays(1)).isBefore(newData.getKey())) {
          visitors.set(formatter.format(current), 0);
        }

        visitors.set(formatter.format(newData.getKey()), newData.getValue());
        current = newData.getKey();
      }

      LocalDate limit = toDate.plusDays(1);
      while ((current = current.plusDays(1)).isBefore(limit)) {
        visitors.set(formatter.format(current), 0);
      }

      visitorsChartModel.setShowPointLabels(visitors.getData() != null && visitors.getData().size() <= 32);
      visitorsChartModel.addSeries(visitors);
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  public BarChartModel getVisitorsChartModel() {
    if (visitorsChartModel == null)
      initVisitorsChartModel();
    return visitorsChartModel;
  }

  public PieChartModel getDownloadsCharsModel() {
    if (downloadsCharsModel == null)
      initDownloadsChartModel();
    return downloadsCharsModel;
  }

  public String getChartStyle() {
    return chartStyle;
  }

  public void setVisitorsStatFrom(Date visitorsStatFrom) {
    this.visitorsStatFrom = visitorsStatFrom;
  }

  public Date getVisitorsStatFrom() {
    return visitorsStatFrom;
  }

  public void setVisitorsStatTo(Date visitorsStatTo) {
    this.visitorsStatTo = visitorsStatTo;
  }

  public Date getVisitorsStatTo() {
    return visitorsStatTo;
  }

  public int getCurrentVisitors() {
    return CurrentVisitorsCounter.getCurrentVisitors();
  }

}
