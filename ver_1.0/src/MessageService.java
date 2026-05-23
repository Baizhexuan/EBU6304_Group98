import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Handles TA-MO in-app messages with a three-message limit before consent.
 */
public final class MessageService {
    private static final int MAX_MESSAGES_WITHOUT_CONSENT = 3;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private MessageService() {
    }

    public static MessageSendResult sendMessage(User sender, int toUserId, int jobId, String body) {
        if (sender == null || ValidationUtils.isBlank(body)) {
            return new MessageSendResult(false, "Message text is required.");
        }
        User recipient = FileStorage.findUserById(toUserId);
        if (recipient == null) {
            return new MessageSendResult(false, "Recipient not found.");
        }
        boolean approved = hasApprovedConsent(sender.id, toUserId, jobId);
        if (!approved) {
            ensureConsentRequest(sender.id, toUserId, jobId);
            int sentCount = countSentMessages(sender.id, toUserId, jobId);
            if (sentCount >= MAX_MESSAGES_WITHOUT_CONSENT) {
                return new MessageSendResult(false,
                        "Conversation consent required. You cannot send more than 3 messages before the other side approves.");
            }
        }

        List<MessageRecord> messages = FileStorage.loadMessages();
        MessageRecord message = new MessageRecord();
        message.id = FileStorage.nextMessageId();
        message.fromUserId = sender.id;
        message.toUserId = toUserId;
        message.jobId = jobId;
        message.body = body.trim();
        message.status = "UNREAD";
        message.createdAt = LocalDateTime.now().format(FORMATTER);
        messages.add(message);
        FileStorage.saveMessages(messages);
        NotificationService.notifyDirectMessage(recipient, sender, FileStorage.findJobById(jobId));
        if (approved) {
            return new MessageSendResult(true,
                    "Message sent. Conversation already approved, so the three-message limit is lifted.");
        }
        int remaining = getRemainingMessagesBeforeConsent(sender.id, toUserId, jobId);
        return new MessageSendResult(true, "Message sent. Pre-approval messages left: " + remaining + ".");
    }

    public static boolean approveConversation(int approverId, int otherUserId, int jobId) {
        User approver = FileStorage.findUserById(approverId);
        if (!canApproveConversation(approver, otherUserId, jobId)) {
            return false;
        }
        List<MessageConsent> consents = FileStorage.loadMessageConsents();
        MessageConsent consent = findConsent(consents, approverId, otherUserId, jobId);
        if (consent == null) {
            consent = new MessageConsent();
            consent.id = FileStorage.nextMessageConsentId();
            consent.userAId = Math.min(approverId, otherUserId);
            consent.userBId = Math.max(approverId, otherUserId);
            consent.jobId = jobId;
            consent.requestedBy = otherUserId;
            consents.add(consent);
        }
        consent.approved = true;
        consent.updatedAt = LocalDateTime.now().format(FORMATTER);
        FileStorage.saveMessageConsents(consents);
        return true;
    }

    public static boolean canApproveConversation(User approver, int otherUserId, int jobId) {
        if (approver == null || !"MO".equalsIgnoreCase(approver.role)) {
            return false;
        }
        Job job = FileStorage.findJobById(jobId);
        return job != null && job.moId == approver.id && otherUserId != approver.id;
    }

    public static boolean hasApprovedConsent(int userA, int userB, int jobId) {
        MessageConsent consent = findConsent(FileStorage.loadMessageConsents(), userA, userB, jobId);
        return consent != null && consent.approved;
    }

    public static int countSentMessagesBeforeConsent(int fromUserId, int toUserId, int jobId) {
        return countSentMessages(fromUserId, toUserId, jobId);
    }

    public static int getRemainingMessagesBeforeConsent(int fromUserId, int toUserId, int jobId) {
        if (hasApprovedConsent(fromUserId, toUserId, jobId)) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, MAX_MESSAGES_WITHOUT_CONSENT - countSentMessages(fromUserId, toUserId, jobId));
    }

    public static int countUnreadMessagesForUser(int userId) {
        int unread = 0;
        for (MessageRecord message : FileStorage.loadMessages()) {
            if (message.isUnreadFor(userId)) {
                unread++;
            }
        }
        return unread;
    }

    public static void markMessagesReadForUser(int userId) {
        List<MessageRecord> messages = FileStorage.loadMessages();
        for (MessageRecord message : messages) {
            if (message.toUserId == userId) {
                message.status = "READ";
            }
        }
        FileStorage.saveMessages(messages);
    }

    private static int countSentMessages(int fromUserId, int toUserId, int jobId) {
        int count = 0;
        for (MessageRecord message : FileStorage.loadMessages()) {
            if (message.fromUserId == fromUserId && message.toUserId == toUserId && message.jobId == jobId) {
                count++;
            }
        }
        return count;
    }

    private static void ensureConsentRequest(int senderId, int toUserId, int jobId) {
        List<MessageConsent> consents = FileStorage.loadMessageConsents();
        if (findConsent(consents, senderId, toUserId, jobId) != null) {
            return;
        }
        MessageConsent consent = new MessageConsent();
        consent.id = FileStorage.nextMessageConsentId();
        consent.userAId = Math.min(senderId, toUserId);
        consent.userBId = Math.max(senderId, toUserId);
        consent.jobId = jobId;
        consent.approved = false;
        consent.requestedBy = senderId;
        consent.updatedAt = LocalDateTime.now().format(FORMATTER);
        consents.add(consent);
        FileStorage.saveMessageConsents(consents);
    }

    private static MessageConsent findConsent(List<MessageConsent> consents, int userA, int userB, int jobId) {
        int first = Math.min(userA, userB);
        int second = Math.max(userA, userB);
        for (MessageConsent consent : consents) {
            if (consent.userAId == first && consent.userBId == second && consent.jobId == jobId) {
                return consent;
            }
        }
        return null;
    }
}
