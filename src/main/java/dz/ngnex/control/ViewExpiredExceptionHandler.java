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

import dz.ngnex.util.WebKit;
import dz.ngnex.view.LoginView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.util.Constants;

import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ViewExpiredExceptionHandler extends ExceptionHandlerWrapper {

  private static Logger log = LogManager.getLogger(ViewExpiredExceptionHandler.class);

  public ViewExpiredExceptionHandler(ExceptionHandler wrapped) {
    super(wrapped);
  }

  @Override
  public void handle() throws FacesException {
    FacesContext ctx = FacesContext.getCurrentInstance();

    if (ctx == null || ctx.getResponseComplete()) {
      return;
    }

    Map<String, String> parameters = ctx.getExternalContext().getRequestParameterMap();
    if (parameters.get(Constants.DialogFramework.CONVERSATION_PARAM) != null) {
      PrimeFaces.current().dialog().closeDynamic(null);
    }

    Iterator<ExceptionQueuedEvent> iterator = getUnhandledExceptionQueuedEvents().iterator();
    while (iterator.hasNext()) {
      ExceptionQueuedEvent event = iterator.next();
      ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();

      if (context != null) {
        Throwable t = context.getException();

        if (t instanceof ViewExpiredException) {
          try {
            handleViewExpiredException((ViewExpiredException) t);
          } finally {
            iterator.remove();
          }
        }

      }
    }
    // At this point, the queue will not contain any ViewExpiredEvents.
    // Therefore, let the parent handle them.
    getWrapped().handle();
  }

  private void handleViewExpiredException(ViewExpiredException vee) {
    if (vee != null) {
      log.warn("handling view expired exception for: " + vee.getViewId(), vee);
      WebKit.logout();

      Map<String, String[]> parameters = new HashMap<>();
      parameters.put(LoginView.NEXT_OUTCOME_PARAM, new String[]{vee.getViewId()});
      WebKit.redirect(LoginView.LOGIN_VIEW_ID, parameters);
    }
  }
}