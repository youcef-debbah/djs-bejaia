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
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public class FacesFilter implements Filter {

  private static Logger log = LogManager.getLogger(FacesFilter.class);

  private static final String VISITOR = "visitor";
  private static final int VISITOR_COOKIE_MAX_AGE = 24 * 60 * 60;

  @EJB
  private StatisticManager statisticManager;

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
        // set no cache headers
        noCache(res);
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

  private void noCache(HttpServletResponse response) {
    // HTTP 1.1
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    // HTTP 1.0
    response.setHeader("Pragma", "no-cache");
    // Proxies
    response.setDateHeader("Expires", 0);
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
