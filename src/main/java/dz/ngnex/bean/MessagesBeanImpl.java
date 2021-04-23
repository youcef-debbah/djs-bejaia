package dz.ngnex.bean;

import dz.ngnex.entity.*;
import dz.ngnex.util.HtmlCleaner;
import dz.ngnex.util.TestWithTransaction;
import org.apache.logging.log4j.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Objects;

@Stateless
@TestWithTransaction
public class MessagesBeanImpl implements MessagesBean {

  @Inject
  private Logger log;

  @Inject
  private EntityManager em;

  @EJB
  private PrincipalBean principalBean;

  @EJB
  private AttachmentsBean attachmentsBean;

  @Override
  public void sendGlobalAdminMessage(String adminName, String content) {
    AdminMessageEntity message = new AdminMessageEntity();
    message.setAdminName(adminName);
    message.setContent(HtmlCleaner.secureHtml(content));
    message.setState(MessageState.READ);
    sendAdminMessageHelper(message, null);
  }

  @Override
  public void sendAdminMessage(String adminName, String content, String receiverUsername, AttachmentContentEntity attachment) {
    AdminMessageEntity adminMessage = new AdminMessageEntity();
    adminMessage.setAdminName(adminName);
    adminMessage.setContent(HtmlCleaner.secureHtml(content));
    adminMessage.setReceiverName(receiverUsername);
    sendAdminMessageHelper(adminMessage, attachment);
  }

  private void sendAdminMessageHelper(AdminMessageEntity message, AttachmentContentEntity attachment) {
    Objects.requireNonNull(message.getAdminName());
    Objects.requireNonNull(message.getContent());

    message.setEpoch(System.currentTimeMillis());

    if (attachment != null)
      message.setAdminAttachment(em.getReference(AttachmentInfoEntity.class, attachment.getId()));

    if (message.getReceiverName() != null)
      setupSnippetRepresentation(AdminMessageEntity.class, message, message.getReceiverName());

    em.persist(message);
  }

  @Override
  public void sendMessage(Service destination, String title, String content, String senderName, AttachmentContentEntity attachment) {
    Objects.requireNonNull(destination);
    Objects.requireNonNull(content);
    long now = System.currentTimeMillis();

    MessageEntity message = new MessageEntity();

    if (attachment != null) {
      message.setAttachment(em.getReference(AttachmentInfoEntity.class, attachment.getId()));
    }

    message.setSenderName(senderName);
    message.setDestination(destination);
    message.setTitle(title);
    message.setContent(HtmlCleaner.secureHtml(content));
    message.setEpoch(now);

    setupSnippetRepresentation(MessageEntity.class, message, senderName);
    em.persist(message);
    updateMessageState(senderName);
  }

  private <T extends BasicMessageEntity> void setupSnippetRepresentation(Class<T> messageType, T newMessage, String username) {
    Objects.requireNonNull(messageType);
    Objects.requireNonNull(newMessage);
    Objects.requireNonNull(username);

    BasicMessageEntity latestMessage = getLatestMessage(username);
    if (latestMessage != null) {
      long millieSecondsSinceLastMessage = newMessage.getEpoch() - latestMessage.getEpoch();

      if (messageType.isInstance(latestMessage) &&
          millieSecondsSinceLastMessage < DURATION_TO_CHAIN_WITH_PREVIEW &&
          !BasicMessageEntity.GLOBAL.equals(latestMessage.getSnippetSourceName())) {
        latestMessage.setRepresentation(Snippet.chainWithNext(latestMessage.getRepresentation()));
        newMessage.setRepresentation(Snippet.chainWithPreviews(newMessage.getRepresentation()));
      }

      if (millieSecondsSinceLastMessage > DURATION_TO_SHOW_DATE)
        latestMessage.setRepresentation(Snippet.markAsConversationEnd(latestMessage.getRepresentation()));
    }
  }

  @Override
  public void markMessageAsRead(int id) {
    MessageEntity message = em.find(MessageEntity.class, id);
    if (message != null) {
      message.setState(MessageState.READ);
      updateMessageState(message.getSenderName());
    }
  }

  @Override
  public void deleteMessage(int id) {
    MessageEntity messageEntity = em.find(MessageEntity.class, id);
    if (messageEntity != null) {
      em.remove(messageEntity);
      updateMessageState(messageEntity.getSenderName());
    }
  }

  @Override
  public void deleteAdminMessage(int id) {
    AdminMessageEntity adminMessageEntity = em.find(AdminMessageEntity.class, id);
    if (adminMessageEntity != null)
      em.remove(adminMessageEntity);
  }

  @Override
  public Long countUnreadAdminMessagesReceivedBy(String receiverName) {
    return em.createQuery("select count(msg.id) from AdminMessageEntity msg where (msg.receiverName = :receiverName or msg.receiverName is null) and msg.state = :state", Long.class)
        .setParameter("receiverName", receiverName)
        .setParameter("state", MessageState.NOT_READ_YET)
        .getSingleResult();
  }

  @Override
  public Long countUnreadMessagesReceivedBy(Service destination) {
    if (destination == null)
      return em.createQuery("select count(msg.id) from MessageEntity msg where msg.state = :state", Long.class)
          .setParameter("state", MessageState.NOT_READ_YET)
          .getSingleResult();
    else
      return em.createQuery("select count(msg.id) from MessageEntity msg where msg.destination = :destination and msg.state = :state", Long.class)
          .setParameter("destination", destination)
          .setParameter("state", MessageState.NOT_READ_YET)
          .getSingleResult();
  }

  @Override
  public List<MessageEntity> getAllUnReadMessagesReceivedBy(Service destination) {
    if (destination == null)
      return em.createQuery("select msg from MessageEntity msg order by msg.state, msg.epoch desc", MessageEntity.class)
          .setMaxResults(100)
          .getResultList();
    else
      return em.createQuery("select msg from MessageEntity msg where msg.destination = :destination order by msg.state, msg.epoch desc", MessageEntity.class)
          .setParameter("destination", destination)
          .setMaxResults(100)
          .getResultList();
  }

  @Override
  public List<Snippet> getConversationWithAdministration(String username, boolean markReadByAdmin) {
    Objects.requireNonNull(username);

    if (markReadByAdmin) {
      em.createQuery("update MessageEntity m set m.state = :newState where m.senderName = :name")
          .setParameter("newState", MessageState.READ)
          .setParameter("name", username)
          .executeUpdate();

      updateMessageState(username);
    } else {
      em.createQuery("update AdminMessageEntity m set m.state = :newState where m.receiverName = :name")
          .setParameter("newState", MessageState.READ)
          .setParameter("name", username)
          .executeUpdate();
    }

    return em.createNamedQuery("BasicMessageEntity.getConversation", Snippet.class)
        .setParameter("username", username)
        .getResultList();
  }

  private BasicMessageEntity getLatestMessage(String username) {
    List<MessageEntity> latestMessages = em.createQuery("select m from MessageEntity m where m.senderName = :username order by m.epoch desc", MessageEntity.class)
        .setParameter("username", username)
        .setMaxResults(1)
        .getResultList();

    List<AdminMessageEntity> latestAdminMessages = em.createQuery("select m from AdminMessageEntity m where m.receiverName = :username or m.receiverName is null order by m.epoch desc", AdminMessageEntity.class)
        .setParameter("username", username)
        .setMaxResults(1)
        .getResultList();

    if (latestMessages.isEmpty() && latestAdminMessages.isEmpty())
      return null;

    if (latestMessages.isEmpty())
      return latestAdminMessages.get(0);

    if (latestAdminMessages.isEmpty())
      return latestMessages.get(0);

    MessageEntity latestMessage = latestMessages.get(0);
    AdminMessageEntity latestAdminMessage = latestAdminMessages.get(0);
    if (latestMessage.getEpoch() > latestAdminMessage.getEpoch())
      return latestMessage;
    else
      return latestAdminMessage;
  }

  @Override
  public void updateMessageState(String username) {
    if (username != null) {
      int updatedSoFar = em.createQuery("update SportAssociationEntity a set a.unreadMessagesCount = (select count(m.id) from MessageEntity m where m.senderName = :name and m.state = :state) where a.name = :name")
          .setParameter("name", username)
          .setParameter("state", MessageState.NOT_READ_YET)
          .executeUpdate();

      if (updatedSoFar == 0)
        em.createQuery("update YouthAssociationEntity a set a.unreadMessagesCount = (select count(m.id) from MessageEntity m where m.senderName = :name and m.state = :state) where a.name = :name")
            .setParameter("name", username)
            .setParameter("state", MessageState.NOT_READ_YET)
            .executeUpdate();
    }
  }

  @AroundInvoke
  public Object benchmarkCalls(InvocationContext ctx) throws Exception {
    return BeanUtil.benchmarkCall(log, ctx);
  }

  @Override
  @TestWithTransaction(traceSQL = false)
  public void clear() {
    BeanUtil.clearCache(em);
  }
}
