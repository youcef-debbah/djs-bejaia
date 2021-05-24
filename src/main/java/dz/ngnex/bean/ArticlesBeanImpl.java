package dz.ngnex.bean;

import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.entity.ArticleEntity;
import dz.ngnex.entity.CommentEntity;
import dz.ngnex.security.ReadAccess;
import dz.ngnex.util.HtmlCleaner;
import dz.ngnex.util.TemplatedContent;
import dz.ngnex.util.TestWithTransaction;
import org.apache.logging.log4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Stateless
@TestWithTransaction
public class ArticlesBeanImpl implements ArticlesBean {

  public static final int MAX_INITIAL_COMMENTS_COUNT = 32;

  @Inject
  private Logger log;

  @Inject
  private EntityManager em;

  @Inject
  CurrentPrincipal currentPrincipal;

  @Override
  public List<ArticleEntity> getAllArticlesByCreationDate(ReadAccess access, boolean oldToNew) {
    if (oldToNew)
      return em.createNamedQuery("ArticleEntity.findAllOrderedByCreationAsc", ArticleEntity.class)
          .setParameter("access", access)
          .getResultList();
    else
      return em.createNamedQuery("ArticleEntity.findAllOrderedByCreationDesc", ArticleEntity.class)
          .setParameter("access", access)
          .getResultList();
  }

  @Override
  public List<ArticleEntity> getAllArticlesByUpdateDate(ReadAccess access, boolean oldToNew) {
    if (oldToNew)
      return em.createNamedQuery("ArticleEntity.findAllOrderedByLastUpdateAsc", ArticleEntity.class)
          .setParameter("access", access)
          .getResultList();
    else
      return em.createNamedQuery("ArticleEntity.findAllOrderedByLastUpdateDesc", ArticleEntity.class)
          .setParameter("access", access)
          .getResultList();
  }

  @Override
  public ArticleEntity getArticle(int id) {
    return em.find(ArticleEntity.class, id);
  }

  @Override
  public void updateArticle(ArticleEntity article) {
    long now = System.currentTimeMillis();
    article.setLastUpdate(now);
    if (article.getCreated() == null)
      article.setCreated(now);

    article.setContent_en(HtmlCleaner.secureHtml(article.getContent_en()));
    article.setContent_fr(HtmlCleaner.secureHtml(article.getContent_fr()));

    em.merge(article);
  }

  @Override
  public void delete(Integer id) {
    if (id != null) {
      em.createQuery("delete from CommentEntity c where c.articleID = :id")
          .setParameter("id", id)
          .executeUpdate();

      em.createQuery("delete from ArticleEntity a where a.id = :id")
          .setParameter("id", id)
          .executeUpdate();
    }
  }

  @Override
  public CommentEntity addComment(Integer articleID, String content, String author, boolean isAnonymous) {
    Objects.requireNonNull(author);
    CommentEntity newComment = new CommentEntity();
    newComment.setContent(TemplatedContent.newLinesToBrElements(content));
    newComment.setAuthor(author);
    newComment.setPosted(System.currentTimeMillis());
    newComment.setArticleID(articleID);
    newComment.setType(isAnonymous ? CommentEntity.Type.ANONYMOUS.ordinal() : CommentEntity.Type.PLAIN.ordinal());
    em.persist(newComment);
    return newComment;
  }

  @Override
  public void deleteComment(Integer commentID) {
    if (commentID != null)
      em.createQuery("delete from CommentEntity c where c.id = :commentID")
          .setParameter("commentID", commentID)
          .executeUpdate();
  }

  @Override
  public int likeComment(Integer commentID) {
    CommentEntity commentEntity = em.find(CommentEntity.class, commentID);
    if (commentEntity != null) {
      int likes = commentEntity.getLikes() + 1;
      commentEntity.setLikes(likes);
      return likes;
    }

    return 0;
  }

  @Override
  public int dislikeComment(Integer commentID) {
    CommentEntity commentEntity = em.find(CommentEntity.class, commentID);
    if (commentEntity != null) {
      int likes = commentEntity.getLikes() - 1;
      commentEntity.setLikes(likes);
      return likes;
    }

    return 0;
  }

  @Override
  public List<CommentEntity> getInitialComments(Integer articleID) {
    if (articleID == null)
      return Collections.emptyList();
    else {
      return em.createQuery("select c from CommentEntity c where c.articleID = :articleID order by c.posted desc", CommentEntity.class)
          .setParameter("articleID", articleID)
          .setMaxResults(MAX_INITIAL_COMMENTS_COUNT)
          .getResultList();
    }
  }

  @Override
  @TestWithTransaction(traceSQL = false)
  public void clear() {
    BeanUtil.clearCache(em);
  }

  @AroundInvoke
  public Object benchmarkCalls(InvocationContext ctx) throws Exception {
    return BeanUtil.benchmarkCall(log, ctx);
  }
}
