package dz.ngnex.view;

import dz.ngnex.control.FacesFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "ReceiverItemConverter", managed = true)
public class ReceiverItemConverter implements Converter<ReceiverItem> {

  private static Logger log = LogManager.getLogger(FacesFilter.class);

  private static final String SEPARATOR = "";

  @Override
  public ReceiverItem getAsObject(FacesContext context, UIComponent component, String value) {
    if (StringUtils.isBlank(value))
      return null;
    else
      try {
        String[] tokens = value.split(SEPARATOR, 3);
        return new ReceiverItem(parse(tokens[0]), parse(tokens[1]), parse(tokens[2]));
      } catch (RuntimeException e) {
        log.error("could not parse string into ReceiverItem: " + value);
        return null;
      }
  }

  private String parse(String token) {
    if ("null".equals(token))
      return null;
    else
      return token;
  }

  @Override
  public String getAsString(FacesContext context, UIComponent component, ReceiverItem value) {
    if (value == null)
      return null;
    else {
      return value.getName() + SEPARATOR + value.getPhone() + SEPARATOR + value.getAgrement();
    }
  }
}
