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
 * created on 2016/12/08
 * @header
 */

package dz.ngnex.control;

import dz.ngnex.bean.IntegrityException;
import dz.ngnex.security.ReadAccess;
import dz.ngnex.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static dz.ngnex.util.Config.GLOBAL_MSG;

@Named
@RequestScoped
@InjectableByTests
public class Meta {

  private static final boolean UNDER_MAINTENANCE = false;

  @Inject
  private Logger log;

  @Inject
  private NavigationHistory navigationHistory;

  public void pageNotFound() {
    try {
      FacesContext.getCurrentInstance().getExternalContext().dispatch(Config.ERROR_404_PAGE);
    } catch (IOException e) {
      handleException(e);
    }
  }

  public void noSelectionError() {
    addGlobalError("noSelection", "selectRowThenTryAgain");
  }

  public void inputMissing() {
    addGlobalMessage("inputMissing", "checkYourInputThenTryAgain");
  }

  public boolean isUnderMaintenance() {
    return UNDER_MAINTENANCE;
  }

  public String logout() {
    try {
      FacesContext context = FacesContext.getCurrentInstance();
      HttpServletRequest request = WebKit.getFacesRequest();
      Principal principal = request.getUserPrincipal();

      if (principal != null) {
        log.info("disconnecting: " + principal.getName());
        request.logout();
        Messages msgs = ResourcesProvider.getMessagesBundle();
        FacesMessage message = new FacesMessage(msgs.get("userDisconnected"),
            msgs.get("seeYouAgain"));
        context.addMessage(GLOBAL_MSG, message);
        context.getExternalContext().getFlash().setKeepMessages(true);
      }

      context.getExternalContext().invalidateSession();

      return "homePage";
    } catch (Exception e) {
      log.error("couldn't logout", e);
      return null;
    }

  }

  private void handleIntegrityException(IntegrityException e) {
    log.warn("IntegrityException: " + e.getMessage());
    Messages msgs = ResourcesProvider.getMessagesBundle();
    FacesContext context = FacesContext.getCurrentInstance();
    String detail = msgs.get(e.getUserMessageKey());

    if (e.getParam() != null)
      detail = MessageFormat.format(detail, e.getParam());

    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, msgs.get("invalidInput"),
        detail);
    context.addMessage(GLOBAL_MSG, message);
    log.debug(e);
  }

  public void handleException(Exception e) {
    if (e instanceof IntegrityException)
      handleIntegrityException((IntegrityException) e);
    else if (e instanceof OptimisticLockException || e.getCause() instanceof OptimisticLockException)
      handleConcurrentUpdate(e);
    else
      handleInternalError(e);
  }

  private void handleConcurrentUpdate(Exception e) {
    Messages msgs = ResourcesProvider.getMessagesBundle();
    FacesContext context = FacesContext.getCurrentInstance();
    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, msgs.get("concurrentUpdate"),
        msgs.get("concurrentUpdateDetails"));
    context.addMessage(GLOBAL_MSG, message);
  }

  public void addGlobalError(String summary, String detail) {
    log.error(summary + " -> " + detail);
    Messages messages = ResourcesProvider.getMessagesBundle();
    FacesContext.getCurrentInstance().addMessage(GLOBAL_MSG, new FacesMessage(FacesMessage.SEVERITY_ERROR,
        messages.get(summary), messages.get(detail)));
  }

  public void workDoneSuccessfully(String msg) {
    Messages messages = ResourcesProvider.getMessagesBundle();
    FacesContext.getCurrentInstance().addMessage(GLOBAL_MSG, new FacesMessage(FacesMessage.SEVERITY_INFO,
        messages.get(msg), messages.get("successfully")));
  }

  public void dataUpdatedSuccessfully() {
    Messages messages = ResourcesProvider.getMessagesBundle();
    FacesContext.getCurrentInstance().addMessage(GLOBAL_MSG, new FacesMessage(FacesMessage.SEVERITY_INFO,
        messages.get("dataUpdated"), messages.get("successfully")));
  }

  public void dataUpdated(String detail) {
    Messages messages = ResourcesProvider.getMessagesBundle();
    FacesContext.getCurrentInstance().addMessage(GLOBAL_MSG, new FacesMessage(FacesMessage.SEVERITY_INFO,
        messages.get("dataUpdated"), messages.get(detail)));
  }

  public void addGlobalMessage(String summary, String detail) {
    Messages messages = ResourcesProvider.getMessagesBundle();
    FacesContext.getCurrentInstance().addMessage(GLOBAL_MSG, new FacesMessage(FacesMessage.SEVERITY_INFO,
        messages.get(summary), messages.get(detail)));
  }

  public void addGlobalLog(String detail) {
    Messages messages = ResourcesProvider.getMessagesBundle();
    FacesContext.getCurrentInstance().addMessage(GLOBAL_MSG, new FacesMessage(FacesMessage.SEVERITY_INFO,
        "Log", detail));
  }

  private void handleInternalError(Exception e) {
    handleInternalError();
    log.error("internal error " + navigationHistory, e);
  }

  public void handleInternalError() {
    Messages msgs = ResourcesProvider.getMessagesBundle();
    FacesContext context = FacesContext.getCurrentInstance();
    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msgs.get("internErrorTitle"),
        msgs.get("internError"));
    context.addMessage(GLOBAL_MSG, message);
  }

  public <T> void broadcastOverViewScop(Class<T> type, Consumer<T> consumer) {
    UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
    Map<String, Object> viewMap = viewRoot.getViewMap();

    for (Object bean : viewMap.values()) {
      if (type.isInstance(bean)) {
        consumer.accept(type.cast(bean));
      }
    }
  }

  public String icon(ReadAccess accessType) {
    switch (accessType) {
      case ADMINS_ONLY:
        return "ui-icon-lock important";
      case ADMINS_AND_ASSOCIATIONS:
        return "ui-icon-vpn-lock";
      case ANYONE:
        return "ui-icon-public";
      default:
        return "";
    }
  }

  public String getCkEditorRichToolbar() {
    return "[['Undo', 'Redo', '-', " +
        "'Cut', 'Copy', 'Paste', 'PasteText', 'PasteFromWord', '-', " +
        "'Bold', 'Italic', 'Underline', 'Strike', 'Subscript', 'Superscript', '-', " +
        "'TextColor', 'BGColor', 'RemoveFormat', " +
        "'Styles', 'Format', 'Font', 'FontSize', " +
        "'NumberedList', 'BulletedList', '-', " +
        "'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock', '-', " +
        "'Outdent', 'Indent', '-', 'BidiLtr', 'BidiRtl', '-', " +
        "'Smiley', 'SpecialChar', 'Image', 'Table', 'CreateDiv', '-', " +
        "'Blockquote', 'HorizontalRule', '-', " +
        "'SelectAll', 'Replace', 'Find', 'Maximize', 'Source']]";
  }

  public String safeUrl(String url) {
    if (StringUtils.isBlank(url))
      return "#";
    else
      return url;
  }

  public Date asDate(Long epoch) {
    if (epoch == null)
      return null;
    else
      return new Date(epoch);
  }

  public Long asEpoch(Date date) {
    if (date == null)
      return null;
    else
      return date.getTime();
  }

  public String imagePath(String key) {
    FacesContext context = FacesContext.getCurrentInstance();
    Optional<String> resource = context.getApplication().getResourceHandler().getViewResources(context, key).findFirst();
    return resource.orElse("");
  }

  public String attachmentUrl(Integer id) {
    return "/attachment/file?attachment=true&id=" + id;
  }

  public void keepMessages() {
    FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
  }
}
