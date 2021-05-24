package dz.ngnex.bean;

import dz.ngnex.entity.ArticleEntity;
import dz.ngnex.entity.CommentEntity;
import dz.ngnex.security.ReadAccess;

import javax.ejb.Local;
import java.util.List;

@Local
public interface ArticlesBean {

  List<ArticleEntity> getAllArticlesByCreationDate(ReadAccess access, boolean oldToNew);

  List<ArticleEntity> getAllArticlesByUpdateDate(ReadAccess access, boolean oldToNew);

  ArticleEntity getArticle(int id);

  void updateArticle(ArticleEntity article);

  void delete(Integer id);

  CommentEntity addComment(Integer articleID, String content, String author, boolean isAnonymous);

  void deleteComment(Integer commentID);

  int likeComment(Integer commentID);

  int dislikeComment(Integer commentID);

  void clear();

  List<CommentEntity> getInitialComments(Integer articleID);
}
