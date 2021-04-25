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
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

public final class WebKit {

  public static final Charset CONTENT_ENCODING = StandardCharsets.UTF_8;
  private static final Logger log = LogManager.getLogger(WebKit.class);

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

  @NotNull
  public static String getURL(@NotNull String contextPath,
                              @Nullable String path,
                              @Nullable Map<String, String[]> parameters, Charset encoding) throws UnsupportedEncodingException {
    if (parameters == null || parameters.isEmpty())
      return buildURL(contextPath, path, Collections.emptyList());
    else {
      List<String> params = new ArrayList<>(parameters.size());
      for (Map.Entry<String, String[]> param : parameters.entrySet()) {
        String[] values = param.getValue();
        if (values != null && values.length > 0) {
          String key = param.getKey();
          StringBuilder paramWithValues = new StringBuilder(key + "=" + URLEncoder.encode(values[0], encoding.name()));
          for (int i = 1; i < values.length; i++)
            paramWithValues.append('&').append(key).append('=').append(URLEncoder.encode(values[i], encoding.name()));
          params.add(paramWithValues.toString());
        }
      }
      return buildURL(contextPath, path, params);
    }
  }

  @NotNull
  private static String buildURL(@NotNull String contextPath,
                                 @Nullable String path,
                                 @Nullable List<String> parameters) {
    StringBuilder url = new StringBuilder(Objects.requireNonNull(contextPath));

    if (path != null)
      url.append(path);

    if (parameters != null && !parameters.isEmpty()) {
      url.append("?");
      url.append(parameters.get(0));
      for (int i = 1; i < parameters.size(); i++) {
        url.append("&");
        url.append(parameters.get(i));
      }
    }

    return url.toString();
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

  public static void redirectToHome() {
    redirect("/djs", "/landing.xhtml", null);
  }

  public static void redirectToLogin() {
    redirect("/djs", "/login.xhtml", null);
  }

  public static void redirect(@Nullable String context, @Nullable String viewId, @Nullable Map<String, String[]> parameters) {
    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
    try {
      String url = getURL(context != null ? context : externalContext.getRequestContextPath(), viewId, parameters, CONTENT_ENCODING);
      log.info("redirect to: " + url);
      externalContext.redirect(url);
    } catch (final IOException e) {
      log.error("failed to redirect to: " + viewId, e);
    }
  }

  public static void redirect(@Nullable String viewId, @Nullable Map<String, String[]> parameters) {
    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
    try {
      String url = getURL(externalContext.getRequestContextPath(), viewId, parameters, CONTENT_ENCODING);
      log.info("redirect to: " + url);
      externalContext.redirect(url);
    } catch (final IOException e) {
      log.error("failed to redirect to: " + viewId, e);
    }
  }

  public static void logout() {
    HttpServletRequest request = getFacesRequest();
    Principal principal = request.getUserPrincipal();

    String name = principal != null ? principal.getName() : null;
    if (name != null) {
      log.info("logout for principal named: " + name);
    }

    HttpServletResponse response = getResponse();
    Cookie jsessionid = new Cookie("JSESSIONID", "");
    jsessionid.setMaxAge(0);
    response.addCookie(jsessionid);

    try {
      request.logout();
    } catch (ServletException e) {
      log.warn("logout failed for principal named: " + name, e);
    }
  }
}
