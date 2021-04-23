package dz.ngnex.control;

import dz.ngnex.bean.ImagesBean;
import dz.ngnex.entity.BinaryContent;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@WebServlet(name = "ImageServlet", urlPatterns = ImagesServlet.IMAGE_SERVLET_PATTERN)
public class ImagesServlet extends BinaryFilesServlet {
  private static final long serialVersionUID = 4873845738048460610L;

  public static final String IMAGE_SERVLET_NAMESPACE = "/image/";
  public static final String IMAGE_SERVLET_PATTERN = IMAGE_SERVLET_NAMESPACE + '*';

  @Inject
  private Logger log;

  @EJB
  private ImagesBean imagesBean;

  @Nullable
  @Override
  protected BinaryContent getLoadedFileEntity(HttpServletRequest request, String filename) {
    try {
      log.info("serving image: " + filename);
      return imagesBean.getBinaryContent(filename);
    } catch (Exception e) {
      log.warn("could not retrieve image: " + filename, e);
      return null;
    }
  }

  public static String getUrl(@NotNull String filename) {
    return IMAGE_SERVLET_NAMESPACE + Objects.requireNonNull(filename);
  }

  public static String getUrlAsAttachment(@NotNull String filename) {
    return IMAGE_SERVLET_NAMESPACE + Objects.requireNonNull(filename) + "?" + ATTACHMENT_PARAM + "=true";
  }
}
