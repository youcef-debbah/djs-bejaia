package dz.ngnex.view;

import dz.ngnex.bean.ArticlesBean;
import dz.ngnex.bean.ImagesBean;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.ArticleEntity;
import dz.ngnex.entity.ImageInfoEntity;
import dz.ngnex.security.ReadAccess;
import dz.ngnex.util.ViewModel;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@ViewModel
public class LandingView implements Serializable {
  private static final long serialVersionUID = -3243410615372422000L;

  @EJB
  private ImagesBean imagesBean;

  @EJB
  private ArticlesBean articlesBean;

  private List<ImageInfoEntity> galleryFiles;
  private List<ArticleEntity> topNews;

  @Inject
  Meta meta;

  @PostConstruct
  private void init() {
    refresh();
  }

  public void refresh() {
    try {
      galleryFiles = imagesBean.getGalleryImages();
      List<ArticleEntity> articles = articlesBean.getAllArticlesByCreationDate(ReadAccess.ANYONE, false);
      if (articles.size() > 3)
        topNews = articles.subList(0, 3);
      else
        topNews = articles;
    } catch (Exception e) {
      galleryFiles = null;
      meta.handleException(e);
    }
  }

  public List<ImageInfoEntity> getGalleryFiles() {
    return galleryFiles;
  }

  public List<ArticleEntity> getTopNews() {
    return topNews;
  }
}
