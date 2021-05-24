package dz.ngnex.view;

import dz.ngnex.bean.ArticlesBean;
import dz.ngnex.bean.AvatarBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.Meta;
import dz.ngnex.control.PrincipalState;
import dz.ngnex.entity.*;
import dz.ngnex.util.ViewModel;
import dz.ngnex.util.WebKit;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@ViewModel
public class ArticleView implements Serializable {
  private static final long serialVersionUID = 7719144389046552950L;
  public static final String ID_PARAM = "id";

  @EJB
  private ArticlesBean articlesBean;

  private ArticleEntity article;
  private LinkedList<CommentEntity> comments = new LinkedList<>();

  @Size(max = Constrains.MAX_TEXT_LENGTH)
  @NotNull
  private String newComment;

  @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
  private String author;

  @Inject
  private Meta meta;

  @EJB
  private AvatarBean avatarBean;

  @Inject
  private CurrentPrincipal currentPrincipal;

  @Inject
  private PrincipalState principalState;

  private Map<String, FileInfo> cache = new HashMap<>();

  @PostConstruct
  private void init() {
    Integer id = WebKit.getRequestParamAsInt(ID_PARAM);
    if (id != null)
      article = getArticle(id);

    if (article == null)
      article = newArticle();

    comments.clear();
    addComments(articlesBean.getInitialComments(article.getId()));

    author = currentPrincipal.getName();
  }

  private void addComments(List<CommentEntity> newComments) {
    for (CommentEntity comment : newComments) {
      setCommentThumbnails(comment);
      comments.add(comment);
    }
  }

  private void setCommentThumbnails(CommentEntity comment) {
    FileInfo thumbnails = cache.computeIfAbsent(comment.getAuthor(), name -> avatarBean.getInfoByUploader(name));
    comment.setThumbnails(thumbnails);
  }

  public ArticleEntity getArticle(Integer id) {
    ArticleEntity article = articlesBean.getArticle(id);
    if (article == null) {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, null, "articleNotFound");
    }
    return article;
  }

  public List<CommentEntity> getComments() {
    return comments;
  }

  private ArticleEntity newArticle() {
    return new ArticleEntity();
  }

  public ArticleEntity getArticle() {
    return article;
  }

  public String getNewComment() {
    return newComment;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setNewComment(String newComment) {
    this.newComment = newComment;
  }

  public void addComment() {
    try {
      Integer articleID = getArticle().getId();
      CommentEntity addedComment = articlesBean.addComment(articleID, newComment, author, currentPrincipal.isGuest());
      setCommentThumbnails(addedComment);
      comments.addFirst(addedComment);
      newComment = null;
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  public void removeComment(Integer commentID) {
    if (commentID != null)
      try {
        articlesBean.deleteComment(commentID);
        DatabaseEntity.remove(commentID, comments);
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

  public void likeComment(Integer commentID) {
    if (commentID != null && !principalState.getLikedComments().contains(commentID)) {
      try {
        int likes = articlesBean.likeComment(commentID);
        principalState.getLikedComments().add(commentID);
        CommentEntity comment = DatabaseEntity.get(commentID, comments);
        if (comment != null)
          comment.setLikes(likes);
      } catch (Exception e) {
        meta.handleException(e);
      }
    }
  }

  public void dislikeComment(Integer commentID) {
    if (commentID != null && !principalState.getLikedComments().contains(commentID)) {
      try {
        int likes = articlesBean.dislikeComment(commentID);
        principalState.getLikedComments().add(commentID);
        CommentEntity comment = DatabaseEntity.get(commentID, comments);
        if (comment != null)
          comment.setLikes(likes);
      } catch (Exception e) {
        meta.handleException(e);
      }
    }
  }
}
