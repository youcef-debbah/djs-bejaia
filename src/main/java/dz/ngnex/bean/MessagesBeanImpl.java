package dz.ngnex.bean;

import dz.ngnex.entity.*;
import dz.ngnex.util.HtmlCleaner;
import dz.ngnex.util.TestWithTransaction;
import dz.ngnex.view.MenuItem;
import org.apache.logging.log4j.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Stateless
@TestWithTransaction
public class MessagesBeanImpl implements MessagesBean {

    public static final int INBOX_SIZE = 100;
    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @EJB
    private PrincipalBean principalBean;

    @EJB
    private AttachmentsBean attachmentsBean;

    @Override
    public void sendGlobalAdminMessage(String adminName, String content, AttachmentContentEntity attachment) {
        AdminMessageEntity message = new AdminMessageEntity();
        message.setState(MessageState.READ);
        sendAdminMessageHelper(message, adminName, content, System.currentTimeMillis(), attachment);
    }

    @Override
    public void sendAdminMessage(String adminName, String content, String receiverUsername, AttachmentContentEntity attachment) {
        AdminMessageEntity adminMessage = new AdminMessageEntity();
        adminMessage.setReceiverName(Objects.requireNonNull(receiverUsername));
        sendAdminMessageHelper(adminMessage, adminName, content, System.currentTimeMillis(), attachment);
    }

    @Override
    public void sendAdminMessages(String adminName, String content, Collection<String> receivers, AttachmentContentEntity attachment) {
        for (String receiver : receivers) {
            AdminMessageEntity adminMessage = new AdminMessageEntity();
            adminMessage.setReceiverName(Objects.requireNonNull(receiver));
            sendAdminMessageHelper(adminMessage, adminName, content, System.currentTimeMillis(), attachment);
        }
    }

    private void sendAdminMessageHelper(AdminMessageEntity message, String adminName, String content, long epoch, AttachmentContentEntity attachment) {
        message.setAdminName(Objects.requireNonNull(adminName));
        message.setContent(HtmlCleaner.secureHtml(Objects.requireNonNull(content)));
        message.setEpoch(epoch);

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

    @Override
    public void sendMessage(GuestMessageEntity messageDraft, AttachmentContentEntity attachment) {
        GuestMessageEntity message = new GuestMessageEntity(messageDraft);

        if (attachment != null)
            message.setAttachment(em.getReference(AttachmentInfoEntity.class, attachment.getId()));

        message.setEpoch(System.currentTimeMillis());
        em.persist(message);
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
                    !BasicMessageEntity.GLOBAL_MSG.equals(latestMessage.getSnippetSourceName())) {
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
    public void markGuestMessageAsRead(int id) {
        GuestMessageEntity message = em.find(GuestMessageEntity.class, id);
        if (message != null)
            message.setState(MessageState.READ);
    }

    @Override
    public AttachmentInfoEntity getAttachment(Integer attachmentID) {
        return em.createQuery("select a from AttachmentInfoEntity a where a.id = :id", AttachmentInfoEntity.class)
                .setParameter("id", attachmentID)
                .getSingleResult();
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
    public void deleteGuestMessage(int id) {
        GuestMessageEntity messageEntity = em.find(GuestMessageEntity.class, id);
        if (messageEntity != null)
            em.remove(messageEntity);
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
        return countUnreadMessages(destination, MessageEntity.class)
                + countUnreadMessages(destination, GuestMessageEntity.class);
    }

    private Long countUnreadMessages(Service destination, Class<? extends ClientMessageEntity> messageType) {
        String entityName = messageType.getSimpleName();
        if (destination == null)
            return em.createQuery("select count(msg.id) from " + entityName + " msg where msg.state = :state", Long.class)
                    .setParameter("state", MessageState.NOT_READ_YET)
                    .getSingleResult();
        else
            return em.createQuery("select count(msg.id) from " + entityName + " msg where msg.destination = :destination and msg.state = :state", Long.class)
                    .setParameter("destination", destination)
                    .setParameter("state", MessageState.NOT_READ_YET)
                    .getSingleResult();
    }

    @Override
    public List<ClientMessageEntity> getInboxMessagesReceivedBy(Service destination) {
        List<ClientMessageEntity> usersMessges = getMessages(destination, MessageEntity.class, INBOX_SIZE);
        List<ClientMessageEntity> guestsMessges = getMessages(destination, GuestMessageEntity.class, Math.max(INBOX_SIZE - usersMessges.size(), 0));
        return ChronologicalDatabaseEntity.mergeOrdered(usersMessges, guestsMessges);
    }

    private List<ClientMessageEntity> getMessages(Service destination, Class<? extends ClientMessageEntity> messageType, int maxResults) {
        if (maxResults < 1)
            return Collections.emptyList();

        String entityName = messageType.getSimpleName();
        if (destination == null) {
            return em.createQuery("select msg from " + entityName + " msg order by msg.state, msg.epoch desc", ClientMessageEntity.class)
                    .setMaxResults(maxResults)
                    .getResultList();
        } else
            return em.createQuery("select msg from " + entityName + " msg where msg.destination = :destination order by msg.state, msg.epoch desc", ClientMessageEntity.class)
                    .setParameter("destination", destination)
                    .setMaxResults(100)
                    .getResultList();
    }

    @Override
    public List<MenuItem> getAllReceivers(Service destination) {
        if (destination == Service.SPORT_SERVICE)
            return em.createQuery("select new dz.ngnex.view.MenuItem(asso.name, asso.description) from SportAssociationEntity asso order by asso.lastUpdate, asso.name", MenuItem.class)
                    .getResultList();
        else if (destination == Service.YOUTH_SERVICE)
            return em.createQuery("select new dz.ngnex.view.MenuItem(asso.name, asso.description) from YouthAssociationEntity asso order by asso.lastUpdate, asso.name", MenuItem.class)
                    .getResultList();

        return em.createQuery("select new dz.ngnex.view.MenuItem(asso.name, asso.description) from BasicAssociationEntity asso order by asso.lastUpdate, asso.name", MenuItem.class)
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
