package dz.ngnex.control;

import dz.ngnex.bean.DossierBean;
import dz.ngnex.entity.BinaryContent;
import dz.ngnex.entity.SimpleBinaryContent;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@WebServlet(name = "DossierServlet", urlPatterns = DossierServlet.DOSSIER_SERVLET_PATTERN)
public class DossierServlet extends BinaryFilesServlet {
  private static final long serialVersionUID = -2405587618232453814L;

  public static final String DOSSIER_SERVLET_NAMESPACE = "/dossier/";
  public static final String DOSSIER_SERVLET_PATTERN = DOSSIER_SERVLET_NAMESPACE + "*";

  public static final String USERNAME_PARAM = "username";
  public static final String ZIP_MIME = "application/zip";

  @Inject
  private Logger log;

  @EJB
  private DossierBean dossierBean;

  @Inject
  private LocaleManager localeManager;

  @Nullable
  @Override
  protected BinaryContent getLoadedFileEntity(HttpServletRequest request, String file) {
    String usernameParam = request.getParameter(USERNAME_PARAM);
    if (usernameParam == null || usernameParam.isEmpty())
      try {
        log.info("serving dossier file for: " + file);
        return dossierBean.getBinaryContent(file);
      } catch (Exception e) {
        log.warn("could not retrieve dossier file: " + file, e);
        return null;
      }
    else {
      try {
        List<? extends BinaryContent> contents = dossierBean.getBinaryContentsFor(usernameParam);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Long latestUpload = null;

        try (ZipOutputStream zip = new ZipOutputStream(out)) {
          for (BinaryContent content : contents) {
            zip.putNextEntry(new ZipEntry(content.getName()));
            zip.write(content.getContents());
            zip.closeEntry();

            if (latestUpload == null || content.getUploadTime() > latestUpload)
              latestUpload = content.getUploadTime();
          }
        }

        if (latestUpload == null)
          latestUpload = System.currentTimeMillis();

        System.out.println("serving a zip with last upload of: " + localeManager.formatAsLocalDateTime(latestUpload));
        return new SimpleBinaryContent(out.toByteArray(), latestUpload, getZipName(usernameParam), ZIP_MIME);
      } catch (IOException e) {
        log.warn("could not creat zip dossier for: " + usernameParam, e);
        return null;
      }
    }
  }

  @Override
  protected long getExpireTime(HttpServletRequest request, BinaryContent file) {
    return 0;
  }

  @NotNull
  private String getZipName(String usernameParam) {
    return "dossier_" + usernameParam + ".zip";
  }

  public static String getUrl(@NotNull String filename) {
    return DOSSIER_SERVLET_NAMESPACE + Objects.requireNonNull(filename);
  }

  public static String getUrlAsAttachment(@NotNull String filename) {
    return DOSSIER_SERVLET_NAMESPACE + Objects.requireNonNull(filename) + "?" + ATTACHMENT_PARAM + "=true";
  }

  public static String getDossierUrlAsAttachment(@NotNull String username) {
    return DOSSIER_SERVLET_NAMESPACE + "zip?" + ATTACHMENT_PARAM + "=true&" + USERNAME_PARAM + "=" + Objects.requireNonNull(username);
  }
}
