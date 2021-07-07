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
import dz.ngnex.control.LocaleManager;
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

    @Inject
    private LocaleManager localeManager;

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
            log.info("\"#\"login-history\"#\" - trying to authenticate: " + currentUser(request) + " as: " + getUsername());
            request.login(getUsername(), getPassword());
            Principal principal = request.getUserPrincipal();
            String principalName = principal.getName();
            Objects.requireNonNull(principalName);
            currentPrincipal.refreshState(principal, request);
            log.info("\"#\"login-history\"#\" - succeeded to authenticate: " + currentUser(request) + "@" + localeManager.getCurrentLocalDateTime());

            String welcomeBack = MessageFormat.format(messages.getString("welcomeBack"), principalName);
            context.addMessage(GLOBAL_MSG, new FacesMessage(messages.getString("userConnected"), welcomeBack));

            if (nextOutcome != null)
                return nextOutcome;

            AccessType principalType = CurrentPrincipal.getPrincipalType(request);
            if (principalType.isAdmin()) {
                request.getSession().setMaxInactiveInterval(WebKit.ADMIN_INACTIVE_INTERVAL);
                if (lastUrl != null && lastUrl.contains("/admin/"))
                    WebKit.redirect(lastUrl);
                else
                    WebKit.redirect(Config.ADMIN_HOME);
            } else if (principalType.isAssociation()) {
                request.getSession().setMaxInactiveInterval(WebKit.ASSOCIATION_INACTIVE_INTERVAL);
                if (lastUrl != null && lastUrl.contains("/asso/"))
                    WebKit.redirect(lastUrl);
                else
                    WebKit.redirect(Config.ASSO_HOME);
            } else {
                request.getSession().setMaxInactiveInterval(WebKit.GUEST_INACTIVE_INTERVAL);
                WebKit.redirect(Config.HOME_PAGE);
            }

            return null;
        } catch (Exception e) {
            log.info("#loggin - failed to authenticate: '" + getUsername(), e);
            context.addMessage(GLOBAL_MSG, new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("loginFailed"),
                    messages.getString("loginFailedDetail")));
            return null;
        }
    }

    private String currentUser(HttpServletRequest request) {
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null)
            return "principal: " + userPrincipal.getName() + ", currentPrincipal: " + currentPrincipal.getName();
        else
            return "principal: null , currentPrincipal: " + currentPrincipal.getName();
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
