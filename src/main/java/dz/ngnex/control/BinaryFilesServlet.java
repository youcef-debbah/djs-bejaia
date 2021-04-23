package dz.ngnex.control;

import dz.ngnex.entity.BinaryContent;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;

public abstract class BinaryFilesServlet extends FileServlet {
  private static final long serialVersionUID = 7956438599416187126L;

  public static final String EXPIRE_TIME_PARAM = "expire_time";
  public static final String ATTACHMENT_PARAM = "attachment";
  private static final String ID_PARAM = "id";

  @Override
  protected BinaryContent getFile(HttpServletRequest request) {
    return usePathAsFilename(request);
  }

  private BinaryContent usePathAsFilename(HttpServletRequest request) {
    String filePath = request.getPathInfo();
    if (isEmptyPath(filePath))
      throw new IllegalArgumentException();
    else
      return getLoadedFileEntity(request, filePath.substring(1));
  }

  protected boolean isEmptyPath(String filePath) {
    if (filePath == null)
      return true;
    else {
      String trim = filePath.trim();
      return trim.isEmpty() || "/".equals(trim);
    }
  }

  @Nullable
  protected abstract BinaryContent getLoadedFileEntity(HttpServletRequest request, String file);

  @Override
  protected long getExpireTime(HttpServletRequest request, BinaryContent file) {
    String expireTimeParam = request.getParameter(EXPIRE_TIME_PARAM);
    if (expireTimeParam != null && !expireTimeParam.isEmpty())
      try {
        return Long.parseLong(expireTimeParam);
      } catch (NumberFormatException e) {
        return super.getExpireTime(request, file);
      }
    return super.getExpireTime(request, file);
  }

  @Override
  protected boolean isAttachment(HttpServletRequest request, String contentType) {
    String attachmentParameter = request.getParameter(ATTACHMENT_PARAM);
    if (attachmentParameter != null)
      return Boolean.parseBoolean(attachmentParameter);
    else
      return super.isAttachment(request, contentType);
  }
}
