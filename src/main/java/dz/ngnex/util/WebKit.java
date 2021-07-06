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

package dz.ngnex.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class WebKit {

  public static final Charset CONTENT_ENCODING = StandardCharsets.UTF_8;
  private static final Logger log = LogManager.getLogger(WebKit.class);

  public static final int GUEST_INACTIVE_INTERVAL = (int) TimeUnit.MINUTES.toSeconds(10);
  public static final int ADMIN_INACTIVE_INTERVAL = (int) TimeUnit.HOURS.toSeconds(3);
  public static final int ASSOCIATION_INACTIVE_INTERVAL = (int) TimeUnit.MINUTES.toSeconds(30);


  @Nullable
  public static String getPrincipalName() {
    Principal userPrincipal = getFacesRequest().getUserPrincipal();
    return userPrincipal != null ? userPrincipal.getName() : null;
  }

  @NotNull
  public static HttpServletRequest getFacesRequest() {
    Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
    if (request instanceof HttpServletRequest) {
      return (HttpServletRequest) request;
    } else {
      throw new RuntimeException("could not parse request as an HTTP request: " + request);
    }
  }

  @Nullable
  public static HttpServletRequest findFacesRequest() {
    FacesContext currentInstance = FacesContext.getCurrentInstance();
    if (currentInstance == null)
      return null;

    ExternalContext externalContext = currentInstance.getExternalContext();
    if (externalContext == null)
      return null;

    Object request = externalContext.getRequest();
    if (request instanceof HttpServletRequest)
      return (HttpServletRequest) request;
    else
      return null;
  }

  @NotNull
  public static HttpServletResponse getResponse() {
    Object response = FacesContext.getCurrentInstance().getExternalContext().getResponse();

    if (response instanceof HttpServletResponse) {
      return (HttpServletResponse) response;
    } else {
      throw new RuntimeException("could not parse request as an HTTP request: " + response);
    }
  }

  @Nullable
  public static String getUserPrincipalName() {
    Principal userPrincipal = getFacesRequest().getUserPrincipal();
    return userPrincipal != null ? userPrincipal.getName() : null;
  }

  @Nullable
  public static String getRequestParam(@NotNull final String name) {
    Objects.requireNonNull(name);
    try {
      return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(name);
    } catch (RuntimeException e) {
      log.warn("could not get request param: " + name, e);
      return null;
    }
  }

  @Nullable
  public static Integer getRequestParamAsInt(@NotNull final String name) {
    return toInteger(getRequestParam(name));
  }

  @Nullable
  public static Integer toInteger(String value) {
    if (null == value || "null".equalsIgnoreCase(value))
      return null;
    else {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        log.warn("bad int param value: " + value);
        return null;
      }
    }
  }

  public static void logout() {
    HttpServletRequest request = getFacesRequest();
    Principal principal = request.getUserPrincipal();

    String name = principal != null ? principal.getName() : null;
    if (name != null) {
      log.info("logout for principal named: " + name);
    }

    try {
      request.logout();
      request.getSession().setMaxInactiveInterval(GUEST_INACTIVE_INTERVAL);
    } catch (ServletException e) {
      log.warn("logout failed for principal named: " + name, e);
    }
  }

  public static void removeSessionID() {
    HttpServletResponse response = getResponse();
    Cookie jsessionid = new Cookie("JSESSIONID", "");
    jsessionid.setMaxAge(0);
    response.addCookie(jsessionid);
  }

  public static String requireNotBlank(String value) {
    if (StringUtils.isBlank(value))
      throw new IllegalArgumentException("illegal blank argument: '" + value + "'");
    return value;
  }

  public synchronized static String getFromSession(String key) {
    Object value = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(key);
    if (value instanceof String)
      return (String) value;
    else
      return null;
  }

  public synchronized static void putToSession(String key, String value) {
    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(key, value);
  }

  public static void redirect(String url) {
    try {
      log.info("webkit redirecting to: " + url);
      FacesContext.getCurrentInstance().getExternalContext().redirect(url);
    } catch (IOException e) {
      log.error("could not redirect to: " + url);
    }
  }

  public static boolean isAjaxRequest() {
    return FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest();
  }

  public static boolean isDeepUrl(String url) {
    return !StringUtils.isBlank(url) && !url.equals("/") && !url.equals("null");
  }

  public static String getCookie(String key) {
    Object cookie = FacesContext.getCurrentInstance().getExternalContext().getRequestCookieMap().get(Objects.requireNonNull(key));
    return cookie instanceof Cookie ? decode(((Cookie) cookie).getValue()) : null;
  }

  private static String decode(String value) {
    if (value != null)
      try {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
      } catch (UnsupportedEncodingException e) {
        log.error("could not decode value: " + value, e);
      }
    return null;
  }
}
