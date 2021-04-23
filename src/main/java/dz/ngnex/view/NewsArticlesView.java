package dz.ngnex.view;

import dz.ngnex.bean.ArticlesBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.ArticleEntity;
import dz.ngnex.util.ViewModel;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@ViewModel
public class NewsArticlesView implements Serializable {

  private static final long serialVersionUID = 4152272711526008762L;
  @EJB
  private ArticlesBean articlesBean;

  private List<ArticleEntity> publishedArticles;

  private boolean oldToNew = false;
  private boolean isSortedByLastUpdate = false;

  @Inject
  Meta meta;

  @Inject
  CurrentPrincipal currentPrincipal;

  @PostConstruct
  public void init() {
    refresh();
  }

  public void refresh() {
    if (isSortedByLastUpdate)
      publishedArticles = articlesBean.getAllArticlesByUpdateDate(currentPrincipal.getHighestReadAccess(), oldToNew);
    else
      publishedArticles = articlesBean.getAllArticlesByCreationDate(currentPrincipal.getHighestReadAccess(), oldToNew);
  }

  public List<ArticleEntity> getPublishedArticles() {
    return publishedArticles;
  }

  public boolean isOldToNew() {
    return oldToNew;
  }

  public void setOldToNew(boolean oldToNew) {
    this.oldToNew = oldToNew;
  }

  public boolean isSortedByLastUpdate() {
    return isSortedByLastUpdate;
  }

  public void setSortedByLastUpdate(boolean sortedByLastUpdate) {
    isSortedByLastUpdate = sortedByLastUpdate;
  }

  public void delete(Integer id) {
    if (id != null)
      try {
        articlesBean.delete(id);
        meta.dataUpdated("articleDeleted");
        refresh();
      } catch (Exception e) {
        meta.handleException(e);
      }
  }
}
