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

package dz.ngnex.control;

import dz.ngnex.bean.StatisticManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.EJB;
import javax.faces.application.ResourceHandler;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class FacesFilter implements Filter {

  public static final String PROJECT_STAGE_INIT_PARAM = "javax.faces.PROJECT_STAGE";
  public static final String PRODUCTION = "Production";
  public static final String DEVELOPMENT = "Development";
  public static final String CACHE_CONTROL = "Cache-Control";
  public static final String PRAGMA = "Pragma";
  public static final String EXPIRES = "Expires";
  private static final int CACHE_SECONDS = (int) TimeUnit.DAYS.toSeconds(7);

  private static Logger log = LogManager.getLogger(FacesFilter.class);

  private static final String VISITOR = "visitor";
  private static final int VISITOR_COOKIE_MAX_AGE = 24 * 60 * 60;

  @EJB
  private StatisticManager statisticManager;

  @Inject
  private NavigationHistory navigationHistory;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
      HttpServletRequest req = (HttpServletRequest) request;
      HttpServletResponse res = (HttpServletResponse) response;

      // Skip JSF resources (CSS/JS/Images/etc)
      if (!req.getRequestURI().startsWith(req.getContextPath() + ResourceHandler.RESOURCE_IDENTIFIER)) {
        // count this visitor
        countVisitor(req, res);

        String projectState = req.getServletContext().getInitParameter(PROJECT_STAGE_INIT_PARAM);
        if (PRODUCTION.equalsIgnoreCase(projectState))
          addCacheHeaders(res);

        if (DEVELOPMENT.equalsIgnoreCase(projectState))
          addNoCacheHeaders(res);

        if (req.getHeader("X-Requested-With") == null && req.getHeader("Faces-Request") == null)
          navigationHistory.refresh(req, res);
      }
    }

    chain.doFilter(request, response);
  }

  private void countVisitor(HttpServletRequest request, HttpServletResponse response) {
    ZonedDateTime currentTime = ZonedDateTime.now(LocaleManager.ADMIN_ZONE);
    LocalDate today = currentTime.toLocalDate();
    if (newVisitor(request, today)) {
      log.info("new visitor: " + request.getRemoteAddr() + " (" + currentTime + ")");

      try {
        statisticManager.countVisitor(today);
      } catch (Exception e) {
        log.error("could not count visitor", e);
      }

      Cookie cookie = new Cookie(VISITOR, today.toString());
      cookie.setMaxAge(VISITOR_COOKIE_MAX_AGE);
      response.addCookie(cookie);
    }
  }

  private boolean newVisitor(HttpServletRequest request, LocalDate today) {
    Cookie[] cookies = request.getCookies();

    if (cookies == null) {
      return true;
    } else {
      for (Cookie cookie : cookies) {
        if (VISITOR.equals(cookie.getName())) {
          String visitDate = cookie.getValue();

          try {
            LocalDate visitLocalDate = LocalDate.parse(visitDate);
            return visitLocalDate.isBefore(today);
          } catch (RuntimeException e) {
            log.error("could not parse visit date: " + visitDate);
            return true;
          }

        }
      }
      return true;
    }
  }

  private void addCacheHeaders(HttpServletResponse response) {
    response.setHeader(CACHE_CONTROL, "max-age=" + CACHE_SECONDS + ", public");
    response.setHeader(PRAGMA, null);
    response.setDateHeader(EXPIRES, getCacheExpireTime());
  }

  private long getCacheExpireTime() {
    Calendar calendar = Calendar.getInstance(LocaleManager.ADMIN_TIME_ZONE);
    calendar.add(Calendar.SECOND, CACHE_SECONDS);
    return calendar.getTimeInMillis();
  }

  private void addNoCacheHeaders(HttpServletResponse response) {
    response.setHeader(CACHE_CONTROL, "no-cache, no-store, must-revalidate");
    response.setHeader(PRAGMA, "no-cache");
    response.setDateHeader(EXPIRES, 0);
  }

  @Override
  public void init(FilterConfig filterConfig) {
    log.info("initializing FacesFilter");
  }

  @Override
  public void destroy() {
    log.info("destroying FacesFilter");
  }

}
