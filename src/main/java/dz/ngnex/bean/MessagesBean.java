package dz.ngnex.bean;

import dz.ngnex.entity.*;
import dz.ngnex.view.MenuItem;

import javax.ejb.Local;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Local
public interface MessagesBean {

  long DURATION_TO_CHAIN_WITH_PREVIEW = TimeUnit.MINUTES.toMillis(4);
  long DURATION_TO_SHOW_DATE = TimeUnit.MINUTES.toMillis(45);

  void sendGlobalAdminMessage(String adminName, String content) throws IntegrityException;

  void sendAdminMessage(String adminName, String content, String receiverUsername, AttachmentContentEntity attachment) throws IntegrityException;

  void sendAdminMessages(String adminName, String content, Collection<String> receivers, AttachmentContentEntity attachment);

  void sendMessage(Service destination, String title, String content, String senderName, AttachmentContentEntity attachment) throws IntegrityException;

  void sendMessage(GuestMessageEntity messageDraft, AttachmentContentEntity attachment);

  void markMessageAsRead(int id);

  void markGuestMessageAsRead(int id);

  AttachmentInfoEntity getAttachment(Integer id);

  void deleteMessage(int id);

  void deleteGuestMessage(int id);

  void deleteAdminMessage(int id);

  Long countUnreadAdminMessagesReceivedBy(String receiverName);

  Long countUnreadMessagesReceivedBy(Service destination);

  List<ClientMessageEntity> getInboxMessagesReceivedBy(Service destination);

  List<MenuItem> getAllReceivers(Service service);

  List<Snippet> getConversationWithAdministration(String username, boolean markReadByAdmin) throws IntegrityException;

  void updateMessageState(String username);

  void clear();
}
