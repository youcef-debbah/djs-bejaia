package dz.ngnex.control;

import dz.ngnex.bean.AttachmentsBean;
import dz.ngnex.entity.BinaryContent;
import dz.ngnex.util.WebKit;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@WebServlet(name = "AttachmentServlet", urlPatterns = AttachmentServlet.ATTACHMENT_SERVLET_PATTERN)
public class AttachmentServlet extends BinaryFilesServlet {
  private static final long serialVersionUID = 234231616383585589L;

  public static final String ATTACHMENT_SERVLET_NAMESPACE = "/attachment/";
  public static final String ATTACHMENT_SERVLET_PATTERN = ATTACHMENT_SERVLET_NAMESPACE + '*';
  private static final String ID_PARAM = "id";

  @Inject
  private Logger log;

  @EJB
  private AttachmentsBean attachmentsBean;

  @Nullable
  @Override
  protected BinaryContent getLoadedFileEntity(HttpServletRequest request, String filename) {
    Integer id = WebKit.toInteger(request.getParameter(ID_PARAM));
    if (id != null)
      return getBinaryContentByID(id);
      else
      return getBinaryContentByName(filename);
  }

  @Nullable
  private BinaryContent getBinaryContentByID(Integer id) {
    try {
      log.info("serving attachment with id: " + id);
      return attachmentsBean.getBinaryContent(id);
    } catch (Exception e) {
      log.warn("could not retrieve attachment with id: " + id);
      return null;
    }
  }

  @Nullable
  private BinaryContent getBinaryContentByName(String filename) {
    try {
      log.info("serving attachment: " + filename);
      return attachmentsBean.getBinaryContent(filename);
    } catch (Exception e) {
      log.warn("could not retrieve attachment: " + filename, e);
      return null;
    }
  }

  public static String getUrl(@NotNull String filename) {
    return ATTACHMENT_SERVLET_NAMESPACE + Objects.requireNonNull(filename);
  }

  public static String getUrlAsAttachment(@NotNull String filename) {
    return ATTACHMENT_SERVLET_NAMESPACE + Objects.requireNonNull(filename) + "?" + ATTACHMENT_PARAM + "=true";
  }
}
