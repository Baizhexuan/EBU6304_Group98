import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Handles TA-MO in-app messages and conversation approval.
 *
 * <p>Viva explanation: this service contains the business rules behind the Bell Centre chat UI.
 * The UI only collects clicks and text; this class decides whether a message can be sent, whether
 * the three-message limit applies, and whether an MO is allowed to approve the conversation.</p>
 */
public final class MessageService {
    /** Before the MO approves a conversation, one sender can send at most three messages. */
    private static final int MAX_MESSAGES_WITHOUT_CONSENT = 3;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private MessageService() {
    }

    public static int getMaxMessagesWithoutConsent() {
        return MAX_MESSAGES_WITHOUT_CONSENT;
    }

    public static MessageSendResult sendMessage(User sender, int toUserId, int jobId, String body) {
        // Basic validation: empty messages are rejected before any CSV data is modified.
        if (sender == null || ValidationUtils.isBlank(body)) {
            return new MessageSendResult(false, "Message text is required.");
        }
        User recipient = FileStorage.findUserById(toUserId);
        if (recipient == null) {
            return new MessageSendResult(false, "Recipient not found.");
        }

        // Consent check:
        // If the MO has not approved this TA-MO-job conversation, enforce the three-message gate.
        boolean approved = hasApprovedConsent(sender.id, toUserId, jobId);
        if (!approved) {
            // A first pre-approval message also creates a pending consent row in
            // message_consents.csv, so the MO can later approve the same conversation.
            ensureConsentRequest(sender.id, toUserId, jobId);
            int sentCount = countSentMessages(sender.id, toUserId, jobId);
            if (sentCount >= MAX_MESSAGES_WITHOUT_CONSENT) {
                return new MessageSendResult(false,
                        "Conversation consent required. You cannot send more than 3 messages before the other side approves.");
            }
        }

        // Persist the actual message after all permission checks pass.
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

        // Every incoming message also creates a notification so the recipient's bell badge changes.
        NotificationService.notifyDirectMessage(recipient, sender, FileStorage.findJobById(jobId));
        if (approved) {
            return new MessageSendResult(true,
                    "Message sent. Conversation already approved, so the three-message limit is lifted.");
        }
        int remaining = getRemainingMessagesBeforeConsent(sender.id, toUserId, jobId);
        return new MessageSendResult(true, "Message sent. Pre-approval messages left: " + remaining + ".");
    }

    /**
     * Approves a conversation. Only the MO who owns the job can perform this action.
     */
    public static boolean approveConversation(int approverId, int otherUserId, int jobId) {
        User approver = FileStorage.findUserById(approverId);
        if (!canApproveConversation(approver, otherUserId, jobId)) {
            return false;
        }
        List<MessageConsent> consents = FileStorage.loadMessageConsents();
        MessageConsent consent = findConsent(consents, approverId, otherUserId, jobId);
        boolean alreadyApproved = consent != null && consent.approved;
        if (consent == null) {
            // If the MO approves before an explicit request row exists, create the row here.
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
        if (!alreadyApproved) {
            // Approval is a workflow event, so notify the TA once and avoid duplicate notifications.
            NotificationService.notifyConversationApproved(FileStorage.findUserById(otherUserId), approver,
                    FileStorage.findJobById(jobId));
        }
        return true;
    }

    /**
     * Checks whether the current user is the correct MO for this job.
     */
    public static boolean canApproveConversation(User approver, int otherUserId, int jobId) {
        if (approver == null || !"MO".equalsIgnoreCase(approver.role)) {
            return false;
        }
        Job job = FileStorage.findJobById(jobId);
        return job != null && job.moId == approver.id && otherUserId != approver.id;
    }

    /**
     * Consent rows are stored by unordered user pair plus job id, so either side can query the same
     * conversation without caring who started it.
     */
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

    /**
     * Counts unread messages for the top-right bell badge.
     */
    public static int countUnreadMessagesForUser(int userId) {
        int unread = 0;
        for (MessageRecord message : FileStorage.loadMessages()) {
            if (message.isUnreadFor(userId)) {
                unread++;
            }
        }
        return unread;
    }

    /**
     * Marks all incoming messages as read when the user opens/reads the message centre.
     */
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

    /**
     * Finds the consent record for a user pair and job. The smaller/larger id trick avoids two
     * duplicate records such as (TA, MO) and (MO, TA) for the same conversation.
     */
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
