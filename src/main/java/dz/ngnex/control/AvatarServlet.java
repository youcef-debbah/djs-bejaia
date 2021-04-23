package dz.ngnex.control;

import dz.ngnex.bean.AvatarBean;
import dz.ngnex.entity.BinaryContent;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@WebServlet(name = "AvatarServlet", urlPatterns = AvatarServlet.AVATAR_SERVLET_PATTERN)
public class AvatarServlet extends BinaryFilesServlet {
  private static final long serialVersionUID = -107156894737158252L;

  public static final String AVATAR_SERVLET_NAMESPACE = "/avatar/";
  public static final String AVATAR_SERVLET_PATTERN = AVATAR_SERVLET_NAMESPACE + "*";

  @Inject
  private Logger log;

  @EJB
  private AvatarBean avatarBean;

  @Nullable
  @Override
  protected BinaryContent getLoadedFileEntity(HttpServletRequest request, String filename) {
    try {
      log.info("serving avatar: " + filename);
      return avatarBean.getBinaryContent(filename);
    } catch (Exception e) {
      log.warn("could not retrieve avatar: " + filename, e);
      return null;
    }
  }

  public static String getUrl(@NotNull String filename) {
    return AVATAR_SERVLET_NAMESPACE + Objects.requireNonNull(filename);
  }

  public static String getUrlAsAttachment(@NotNull String filename) {
    return AVATAR_SERVLET_NAMESPACE + Objects.requireNonNull(filename) + "?" + ATTACHMENT_PARAM + "=true";
  }
}
