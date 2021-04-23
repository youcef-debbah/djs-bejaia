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

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Map;

public final class RequestParamsProducer implements Serializable {

  private static final long serialVersionUID = -4260202951977249652L;

  @Inject
  private FacesContext facesContext;

  @Produces
  @StringRequestParam
  public String getStringRequestParameter(InjectionPoint injectionPoint) {
    final String paramName = injectionPoint.getAnnotated().getAnnotation(StringRequestParam.class).value();
    if (paramName.isEmpty())
      return getRequestParameter(injectionPoint.getMember().getName());
    else
      return getRequestParameter(paramName);
  }

  @Produces
  @IntegerRequestParam
  public Integer getIntegerRequestParameter(InjectionPoint injectionPoint) {
    final String paramName = injectionPoint.getAnnotated().getAnnotation(IntegerRequestParam.class).value();
    if (paramName.isEmpty())
      return asInteger(getRequestParameter(injectionPoint.getMember().getName()));
    else
      return asInteger(getRequestParameter(paramName));
  }

  private String getRequestParameter(String name) {
    final Map<String, String> parameterMap = facesContext.getExternalContext().getRequestParameterMap();
    return parameterMap.get(name);
  }

  private Integer asInteger(String number) {
    if (number == null || number.isEmpty())
      return null;
    else
      try {
        return Integer.parseInt(number);
      } catch (NumberFormatException e) {
        return null;
      }
  }
}