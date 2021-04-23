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

package dz.ngnex.view;

import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.Meta;
import dz.ngnex.control.NavigationHistory;
import dz.ngnex.entity.AccessType;
import dz.ngnex.util.Messages;
import dz.ngnex.util.WebKit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.Objects;

/**
 * @author youcef debbah
 */
@Named
@RequestScoped
public class LoginView implements Serializable {
  private static final long serialVersionUID = -1791839482568436535L;
  private static final Logger log = LogManager.getLogger(LoginView.class);

  public static String LOGIN_VIEW_ID = "/login.xhtml";
  public static final String NEXT_OUTCOME_PARAM = "next";
  public static final String REDIRECT_QUERY_PARAM = "query";

  @EJB
  private PrincipalBean principalBean;

  @Inject
  private NavigationHistory navigationHistory;

  @Inject
  private Messages messages;

  @Size(min = 1, max = 45)
  private String username;
  @Size(min = 1, max = 45)
  private String password;

  @Inject
  private CurrentPrincipal currentPrincipal;

  @Inject
  private Meta meta;

  @Inject
  private ViewSetting viewSetting;

  @PostConstruct
  private void init() {
    HttpServletRequest request = WebKit.getFacesRequest();
    String nextOutcome = request.getParameter(NEXT_OUTCOME_PARAM);
    if (currentPrincipal.isGuest() && nextOutcome != null && !nextOutcome.isEmpty()) {
      viewSetting.setNextOutcome(nextOutcome);
      viewSetting.setRedirectQuery(request.getParameter(REDIRECT_QUERY_PARAM));
      Principal principal = request.getUserPrincipal();
      if (principal != null)
        tryRedirectToNex();
    }
  }

  private boolean tryRedirectToNex() {
    String nextOutcome = viewSetting.getNextOutcome();
    if (nextOutcome != null)
      try {
        FacesContext context = FacesContext.getCurrentInstance();
        NavigationHandler navigationHandler = context.getApplication().getNavigationHandler();
        String url = getViewID(context, navigationHandler, nextOutcome);
        if (url == null)
          url = nextOutcome;

        String query = viewSetting.getRedirectQuery();
        if (query != null && !query.isEmpty())
          url += "?faces-redirect=true&" + query;

        navigationHandler.handleNavigation(context, null, url);
        return true;
      } catch (Exception e) {
        meta.handleException(e);
      }

    return false;
  }

  private String getViewID(FacesContext context, NavigationHandler navigationHandler, String nextOutcome) {
    if (navigationHandler instanceof ConfigurableNavigationHandler) {
      ConfigurableNavigationHandler configurableNavigationHandler = (ConfigurableNavigationHandler) context.getApplication().getNavigationHandler();
      NavigationCase navigationCase = configurableNavigationHandler.getNavigationCase(context, null, nextOutcome);
      if (navigationCase != null) {
        return navigationCase.getToViewId(context);
      }
    }

    return null;
  }

  public String login() {
    setUsername(getUsername().toLowerCase());

    WebKit.logout();

    FacesContext context = FacesContext.getCurrentInstance();
    try {
      HttpServletRequest request = WebKit.getFacesRequest();

      request.login(getUsername(), getPassword());
      Principal principal = request.getUserPrincipal();
      String principalName = principal.getName();
      Objects.requireNonNull(principalName);
      currentPrincipal.refreshState(principal, request);
      log.info("succeeded to authenticate user: " + principalName);

      String welcomeBack = MessageFormat.format(messages.getString("welcomeBack"), principalName);
      context.addMessage("global", new FacesMessage(messages.getString("userConnected"), welcomeBack));

      if (tryRedirectToNex())
        return null;

      AccessType principalType = CurrentPrincipal.getPrincipalType(request);
      if (principalType.isAdmin())
        return "adminHome";
      else if (principalType.isAssociation())
        return "userHome";

      return "homePage";
    } catch (Exception e) {
      log.info("failed to authenticate user '" + getUsername() + "' (password '" + getPassword() + "')", e);
      context.addMessage("global", new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("loginFailed"),
          messages.getString("loginFailedDetail")));
      return null;
    }
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
