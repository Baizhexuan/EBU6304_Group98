import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for CSV-backed in-app notifications.
 *
 * <p>Viva explanation: dashboards should not silently change state. Whenever an important
 * workflow event happens, this class writes a notification row to notifications.csv so the
 * affected user can see the update in the Bell Centre.</p>
 */
public final class NotificationService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private NotificationService() {
    }

    /**
     * Creates one notification for a TA after an MO updates an application decision.
     */
    public static void notifyApplicationDecision(Application application, User reviewer, Job job, String decision) {
        if (application == null || reviewer == null || job == null || ValidationUtils.isBlank(decision)) {
            return;
        }

        // This notification belongs to the TA, because the MO/Admin decision changes the TA's
        // application outcome.
        List<Notification> notifications = FileStorage.loadNotifications();
        Notification notification = new Notification();
        notification.id = FileStorage.nextNotificationId();
        notification.userId = application.taId;
        notification.status = "UNREAD";
        notification.createdAt = LocalDateTime.now().format(FORMATTER);
        notification.title = "Application update for " + job.title;
        notification.actionHint = "Open My Applications to review the latest status and note.";

        // Keep messages human-readable rather than just storing SELECTED/REJECTED codes.
        if ("SELECTED".equalsIgnoreCase(decision)) {
            notification.message = "You have been selected for " + job.title + " (" + job.module + ") by "
                    + reviewer.getSafeDisplayName() + ".";
        } else if ("REJECTED".equalsIgnoreCase(decision)) {
            notification.message = "Your application for " + job.title + " (" + job.module + ") was not selected by "
                    + reviewer.getSafeDisplayName() + ".";
        } else {
            notification.message = "Your application for " + job.title + " (" + job.module + ") was updated to "
                    + decision + ".";
        }

        notifications.add(notification);
        FileStorage.saveNotifications(notifications);
    }

    /**
     * Adds a deduplicated reminder when a TA profile is missing or incomplete.
     */
    public static void notifyProfileRequired(User taUser) {
        if (taUser == null) {
            return;
        }
        // Deduplicated reminder: if an unread profile reminder already exists, do not spam another.
        addNotificationIfNotUnread(
                taUser.id,
                "Profile completion required",
                "Please complete your TA profile before applying for jobs. The system needs your skills, GPA, availability and CV path for fair screening.",
                "Open My Profile and complete all required fields before submitting applications.");
    }

    /**
     * Sends closure alerts to TAs with active applications when a job is closed.
     */
    public static int notifyJobClosed(Job job, User actor) {
        if (job == null) {
            return 0;
        }

        // Only TAs with active/non-withdrawn applications need to know that a job has closed.
        Set<Integer> affectedTaIds = new HashSet<Integer>();
        for (Application application : FileStorage.loadApplications()) {
            if (application.jobId == job.id && !"WITHDRAWN".equalsIgnoreCase(application.status)) {
                affectedTaIds.add(application.taId);
            }
        }

        int created = 0;
        String actorName = actor == null ? "the recruitment team" : actor.getSafeDisplayName();
        for (Integer taId : affectedTaIds) {
            boolean added = addNotificationIfNotUnread(
                    taId,
                    "Job closed: " + job.title,
                    job.title + " (" + job.module + ") has been closed by " + actorName
                            + ". Check My Applications for the current status before planning further applications.",
                    "Open My Applications and review other open jobs if needed.");
            if (added) {
                created++;
            }
        }
        return created;
    }

    /**
     * Marks old closure notices as read when a closed job is reopened.
     */
    public static int markJobClosureNotificationsRead(Job job) {
        if (job == null) {
            return 0;
        }
        // If a job is reopened, old "job closed" alerts are no longer actionable, so mark them read.
        List<Notification> notifications = FileStorage.loadNotifications();
        int updated = 0;
        String title = "Job closed: " + job.title;
        for (Notification notification : notifications) {
            if (notification.isUnread() && title.equalsIgnoreCase(notification.title)) {
                notification.status = "READ";
                updated++;
            }
        }
        if (updated > 0) {
            FileStorage.saveNotifications(notifications);
        }
        return updated;
    }

    public static void notifyDirectMessage(User recipient, User sender, Job job) {
        if (recipient == null || sender == null) {
            return;
        }
        // Direct message notifications are what make the bell badge react to new chat messages.
        String jobLabel = job == null ? "general recruitment conversation" : job.title + " (" + job.module + ")";
        addNotification(
                recipient.id,
                "New message from " + sender.getSafeDisplayName(),
                sender.getSafeDisplayName() + " sent you a message about " + jobLabel + ".",
                "Open the bell centre to read the message and reply.");
    }

    public static void notifyConversationApproved(User recipient, User approver, Job job) {
        if (recipient == null || approver == null) {
            return;
        }
        // Approval changes the chat permission state, so the TA should be told that the
        // three-message limit has been lifted.
        String jobLabel = job == null ? "general recruitment conversation" : job.title + " (" + job.module + ")";
        addNotification(
                recipient.id,
                "Conversation approved by " + approver.getSafeDisplayName(),
                approver.getSafeDisplayName() + " approved your conversation about " + jobLabel
                        + ". You can now continue the discussion without the three-message limit.",
                "Open the bell centre to continue the conversation.");
    }

    public static void notifyWorkEvaluation(Application application, User reviewer, Job job, int rating,
            boolean penaltyApplied, int reputationScore) {
        if (application == null || reviewer == null || job == null) {
            return;
        }
        // The rating notification closes the loop after a selected TA has completed work.
        String message = reviewer.getSafeDisplayName() + " rated your completed work for " + job.title
                + " as " + rating + "/5.";
        if (penaltyApplied) {
            // If a reputation penalty was applied, explain it directly in the notification so the
            // TA can understand why future match scores may be lower.
            message += " Because the original match score was high but the completion rating was low, your reputation score is now "
                    + reputationScore + "/100. This is a review signal for future matching, not an automatic misconduct decision.";
        }
        addNotification(
                application.taId,
                "Work evaluation for " + job.title,
                message,
                "Open My Applications or the bell centre to review the update.");
    }

    /**
     * Returns all notifications for one user in stored order.
     */
    public static List<Notification> getNotificationsForUser(int userId) {
        List<Notification> all = FileStorage.loadNotifications();
        List<Notification> result = new ArrayList<Notification>();
        for (Notification notification : all) {
            if (notification.userId == userId) {
                result.add(notification);
            }
        }
        return result;
    }

    /**
     * Counts unread notifications for badge and summary displays.
     */
    public static int countUnreadForUser(int userId) {
        int unread = 0;
        for (Notification notification : getNotificationsForUser(userId)) {
            if (notification.isUnread()) {
                unread++;
            }
        }
        return unread;
    }

    /**
     * Marks outstanding profile reminders as resolved after a complete profile is saved.
     */
    public static void markProfileReminderResolved(User taUser) {
        if (taUser == null) {
            return;
        }
        List<Notification> notifications = FileStorage.loadNotifications();
        for (Notification notification : notifications) {
            if (notification.userId == taUser.id
                    && notification.isUnread()
                    && "Profile completion required".equalsIgnoreCase(notification.title)) {
                notification.status = "READ";
            }
        }
        FileStorage.saveNotifications(notifications);
    }

    /**
     * Marks a single notification as read.
     */
    public static void markAsRead(int notificationId) {
        List<Notification> notifications = FileStorage.loadNotifications();
        for (Notification notification : notifications) {
            if (notification.id == notificationId) {
                notification.status = "READ";
                break;
            }
        }
        FileStorage.saveNotifications(notifications);
    }

    /**
     * Marks every stored notification for one user as read.
     */
    public static void markAllAsRead(int userId) {
        List<Notification> notifications = FileStorage.loadNotifications();
        for (Notification notification : notifications) {
            if (notification.userId == userId) {
                notification.status = "READ";
            }
        }
        FileStorage.saveNotifications(notifications);
    }

    private static boolean addNotificationIfNotUnread(int userId, String title, String message, String actionHint) {
        List<Notification> notifications = FileStorage.loadNotifications();
        for (Notification notification : notifications) {
            if (notification.userId == userId && notification.isUnread()
                    && title.equalsIgnoreCase(notification.title)) {
                return false;
            }
        }

        Notification notification = new Notification();
        notification.id = FileStorage.nextNotificationId();
        notification.userId = userId;
        notification.title = title;
        notification.message = message;
        notification.status = "UNREAD";
        notification.createdAt = LocalDateTime.now().format(FORMATTER);
        notification.actionHint = actionHint;
        notifications.add(notification);
        FileStorage.saveNotifications(notifications);
        return true;
    }

    private static void addNotification(int userId, String title, String message, String actionHint) {
        List<Notification> notifications = FileStorage.loadNotifications();
        Notification notification = new Notification();
        notification.id = FileStorage.nextNotificationId();
        notification.userId = userId;
        notification.title = title;
        notification.message = message;
        notification.status = "UNREAD";
        notification.createdAt = LocalDateTime.now().format(FORMATTER);
        notification.actionHint = actionHint;
        notifications.add(notification);
        FileStorage.saveNotifications(notifications);
    }
}
