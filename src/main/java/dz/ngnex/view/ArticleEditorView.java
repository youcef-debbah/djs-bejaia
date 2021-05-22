package dz.ngnex.view;

import dz.ngnex.bean.ArticlesBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.LocaleManager;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.ArticleEntity;
import dz.ngnex.entity.Constrains;
import dz.ngnex.entity.DatabaseEntity;
import dz.ngnex.security.ReadAccess;
import dz.ngnex.util.ViewModel;
import dz.ngnex.util.WebKit;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@ViewModel
public class ArticleEditorView implements Serializable {
  private static final long serialVersionUID = 7585162799570993082L;
  public static final String ID_PARAM = "id";

  @EJB
  private ArticlesBean articlesBean;

  private ArticleEntity article;

  @Size(max = Constrains.MAX_URL_LENGTH)
  private String thumbnails;

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  private String title_en;
  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  private String title_fr;

  @Size(max = Constrains.MAX_TEXT_LENGTH)
  private String summary_en;
  @Size(max = Constrains.MAX_TEXT_LENGTH)
  private String summary_fr;

  @Size(max = Constrains.MAX_TEXT_LENGTH)
  private String content_en;
  @Size(max = Constrains.MAX_TEXT_LENGTH)
  private String content_fr;

  @Inject
  private Meta meta;

  @Inject
  private LocaleManager localeManager;

  @Inject
  private CurrentPrincipal currentPrincipal;

  private ReadAccess access;

  @PostConstruct
  private void init() {
    Integer id = WebKit.getRequestParamAsInt(ID_PARAM);
    if (id != null)
      article = getArticle(id);

    loadData();
    if (article == null)
      article = newArticle();
  }

  public void deleteArticle() {
    ArticleEntity article = this.article;
    if (article != null && article.getId() != null) {
      try {
        articlesBean.delete(article.getId());
        meta.dataUpdated("articleDeleted");
        PrimeFaces.current().executeScript("updateArticleID(null)");
      } catch (Exception e) {
        meta.handleException(e);
      }
    }

    this.article = newArticle();
  }

  public ArticleEntity getArticle(Integer id) {
    ArticleEntity article = articlesBean.getArticle(id);
    if (article == null) {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, null, "articleNotFound");
    }
    return article;
  }

  private ArticleEntity newArticle() {
    return new ArticleEntity();
  }

  public String getCurrentID() {
    if (article != null)
      return String.valueOf(article.getId());
    else
      return "null";
  }

  public Date getArticleLastSaveDate() {
    ArticleEntity article = this.article;
    if (article != null) {
      Long lastUpdate = article.getLastUpdate();
      if (lastUpdate != null)
        return new Date(lastUpdate);
    }

    return null;
  }

  public String getArticleLastSaveFormatted() {
    ArticleEntity article = this.article;
    if (article != null) {
      Long lastUpdate = article.getLastUpdate();
      if (lastUpdate != null)
        return localeManager.formatAsLocalDateTime(lastUpdate);
    }
    return null;
  }

  public Date getArticleCreationDate() {
    ArticleEntity article = this.article;
    if (article != null) {
      Long createdEpoch = article.getCreated();
      if (createdEpoch != null)
        return new Date(createdEpoch);
    }

    return null;
  }

  public String getArticleCreationFormatted() {
    ArticleEntity article = this.article;
    if (article != null) {
      Long createdEpoch = article.getCreated();
      if (createdEpoch != null)
        return localeManager.formatAsLocalDateTime(createdEpoch);
    }

    return null;
  }

  public void loadData() {
    ArticleEntity article = this.article;
    if (article != null) {
      thumbnails = article.getThumbnails();
      title_en = article.getTitle_en();
      title_fr = article.getTitle_fr();
      summary_en = article.getSummary_en();
      summary_fr = article.getSummary_fr();
      content_en = article.getContent_en();
      content_fr = article.getContent_fr();
      access = article.getAccess();
    } else {
      thumbnails = "";
      title_en = "";
      title_fr = "";
      summary_en = "";
      summary_fr = "";
      content_en = "";
      content_fr = "";
      access = ReadAccess.ANYONE;
    }
  }

  public void saveData() {
    ArticleEntity article = this.article;
    if (article != null) {
      article.setAuthor(currentPrincipal.getName());
      article.setThumbnails(getThumbnails());
      article.setTitle_en(getTitle_en());
      article.setTitle_fr(getTitle_fr());
      article.setSummary_en(getSummary_en());
      article.setSummary_fr(getSummary_fr());
      article.setContent_en(getContent_en());
      article.setContent_fr(getContent_fr());
      article.setAccess(getAccess());
      try {
        articlesBean.updateArticle(article);
        meta.dataUpdatedSuccessfully();
        PrimeFaces.current().executeScript("updateArticleID(" + article.getId() + ")");
      } catch (Exception e) {
        meta.handleException(e);
      }
    } else
      meta.addGlobalError("updateFailed", "articleNotFound");
  }

  public String getThumbnails() {
    return thumbnails;
  }

  public void setThumbnails(String thumbnails) {
    this.thumbnails = thumbnails;
  }

  public String getTitle_en() {
    return title_en;
  }

  public void setTitle_en(String title_en) {
    this.title_en = title_en;
  }

  public String getTitle_fr() {
    return title_fr;
  }

  public void setTitle_fr(String title_fr) {
    this.title_fr = title_fr;
  }

  public String getSummary_en() {
    return summary_en;
  }

  public void setSummary_en(String summary_en) {
    this.summary_en = summary_en;
  }

  public String getSummary_fr() {
    return summary_fr;
  }

  public void setSummary_fr(String summary_fr) {
    this.summary_fr = summary_fr;
  }

  public String getContent_en() {
    return content_en;
  }

  public void setContent_en(String content_en) {
    this.content_en = content_en;
  }

  public String getContent_fr() {
    return content_fr;
  }

  public void setContent_fr(String content_fr) {
    this.content_fr = content_fr;
  }

  public ReadAccess getAccess() {
    return access;
  }

  public ReadAccess[] getAvailableAccessLevels() {
    return ReadAccess.values();
  }

  public void setAccess(ReadAccess access) {
    this.access = access;
  }

  private String currentLanguage() {
    return FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
  }

  public String getTitle() {
    return "fr".equalsIgnoreCase(currentLanguage()) ? getTitle_fr() : getTitle_en();
  }

  @Override
  public String toString() {
    return "ArticleEditorView{" +
        "articleID=" + DatabaseEntity.getID(article) +
        ", thumbnails='" + getThumbnails() + '\'' +
        ", title_en='" + getTitle_en() + '\'' +
        ", title_fr='" + getTitle_fr() + '\'' +
        ", summary_en='" + getSummary_en() + '\'' +
        ", summary_fr='" + getSummary_fr() + '\'' +
        ", access=" + getAccess() +
        '}';
  }
}
