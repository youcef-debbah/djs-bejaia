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

import dz.ngnex.util.Messages;
import dz.ngnex.util.ResourcesProvider;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@FacesValidator("dz.ngnex.LocalDate")
public class DateInputValidator<T> implements Validator<String> {

  private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  @Override
  public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
    try {
      LocalDate.parse(value, formatter);
    } catch (Exception e) {
      Messages messages = ResourcesProvider.getMessagesBundle();
      throw new ValidatorException(new FacesMessage(
          FacesMessage.SEVERITY_ERROR, messages.get("invalidDate"), value));
    }
  }
}
