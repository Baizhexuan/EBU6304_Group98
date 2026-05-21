import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for CSV-backed in-app notifications.
 *
 * <p>The demo uses notifications to make important workflow state visible without introducing a
 * heavier messaging system. Messages are generated for MO decisions, missing profiles, and job
 * closure events, then persisted through {@link FileStorage} so the behaviour is easy to
 * demonstrate and test.</p>
 */
public final class NotificationService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private NotificationService() {
    }

    /**
     * Creates one notification for a TA after an MO updates an application decision.
     *
     * @param application application whose decision changed
     * @param reviewer    user who performed the review action
     * @param job         job associated with the application
     * @param decision    new application decision
     */
    public static void notifyApplicationDecision(Application application, User reviewer, Job job, String decision) {
        if (application == null || reviewer == null || job == null || ValidationUtils.isBlank(decision)) {
            return;
        }

        List<Notification> notifications = FileStorage.loadNotifications();
        Notification notification = new Notification();
        notification.id = FileStorage.nextNotificationId();
        notification.userId = application.taId;
        notification.status = "UNREAD";
        notification.createdAt = LocalDateTime.now().format(FORMATTER);
        notification.title = "Application update for " + job.title;
        notification.actionHint = "Open My Applications to review the latest status and note.";

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
     *
     * @param taUser TA user who needs to complete a profile
     */
    public static void notifyProfileRequired(User taUser) {
        if (taUser == null) {
            return;
        }
        addNotificationIfNotUnread(
                taUser.id,
                "Profile completion required",
                "Please complete your TA profile before applying for jobs. The system needs your skills, GPA, availability and CV path for fair screening.",
                "Open My Profile and complete all required fields before submitting applications.");
    }

    /**
     * Sends closure alerts to TAs with active applications when a job is closed.
     *
     * @param job   closed job
     * @param actor user who closed the job, or {@code null} for a system actor
     * @return number of new notifications created
     */
    public static int notifyJobClosed(Job job, User actor) {
        if (job == null) {
            return 0;
        }

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
     * Returns all notifications for one user in stored order.
     *
     * @param userId user identifier to filter by
     * @return notifications belonging to the selected user
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
     *
     * @param userId user identifier to count notifications for
     * @return unread notification count
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
     *
     * @param taUser TA user whose profile reminder should be marked read
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
     *
     * @param notificationId notification identifier to update
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
     *
     * @param userId user identifier whose notifications should be updated
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
}
