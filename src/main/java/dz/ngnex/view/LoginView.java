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
import dz.ngnex.control.NavigationHistory;
import dz.ngnex.entity.AccessType;
import dz.ngnex.util.Config;
import dz.ngnex.util.Messages;
import dz.ngnex.util.ViewModel;
import dz.ngnex.util.WebKit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.Objects;

import static dz.ngnex.util.Config.GLOBAL_MSG;

/**
 * @author youcef debbah
 */
@ViewModel
public class LoginView implements Serializable {
  private static final long serialVersionUID = -1791839482568436535L;
  private static final Logger log = LogManager.getLogger(LoginView.class);

  public static final String NEXT_OUTCOME = "outcome";

  @EJB
  private PrincipalBean principalBean;

  @Inject
  private Messages messages;

  @Size(min = 1, max = 45)
  private String username;
  @Size(min = 1, max = 45)
  private String password;

  @Inject
  private CurrentPrincipal currentPrincipal;

  private String nextOutcome;
  private String lastUrl;

  @PostConstruct
  private void init() {
    nextOutcome = WebKit.getRequestParam(NEXT_OUTCOME);
    lastUrl = WebKit.getCookie(NavigationHistory.LAST_URL_VISITED);
  }

  public String login() {
    setUsername(getUsername().toLowerCase());

    WebKit.logout();

    FacesContext context = FacesContext.getCurrentInstance();
    try {
      HttpServletRequest request = WebKit.getFacesRequest();

      System.out.println("@@@ before login principal name: " + currentPrincipal.getName());
      request.login(getUsername(), getPassword());
      Principal principal = request.getUserPrincipal();
      String principalName = principal.getName();
      Objects.requireNonNull(principalName);
      currentPrincipal.refreshState(principal, request);
      log.info("succeeded to authenticate user: " + principalName);
      System.out.println("@@@ after login principal name: " + currentPrincipal.getName());

      String welcomeBack = MessageFormat.format(messages.getString("welcomeBack"), principalName);
      context.addMessage(GLOBAL_MSG, new FacesMessage(messages.getString("userConnected"), welcomeBack));

      if (nextOutcome != null)
        return nextOutcome;

      AccessType principalType = CurrentPrincipal.getPrincipalType(request);
      System.out.println("@@@ later principal name: " + currentPrincipal.getName());
      if (principalType.isAdmin()) {
        if (lastUrl != null && lastUrl.contains("/admin/")) {
          WebKit.redirect(lastUrl);
          return null;
        } else {
          WebKit.redirect(Config.ADMIN_CHAT_PAGE);
          return null;
        }
      } else if (principalType.isAssociation()) {
        if (lastUrl != null && lastUrl.contains("/asso/")) {
          WebKit.redirect(lastUrl);
          return null;
        } else {
          WebKit.redirect(Config.ASSO_HOME);
          return null;
        }
      }

      WebKit.redirect(Config.HOME_PAGE);
      return null;
    } catch (Exception e) {
      log.info("failed to authenticate user '" + getUsername() + "' (password '" + getPassword() + "')", e);
      context.addMessage(GLOBAL_MSG, new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("loginFailed"),
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
