import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class NotificationService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private NotificationService() {
    }

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

    public static int countUnreadForUser(int userId) {
        int unread = 0;
        for (Notification notification : getNotificationsForUser(userId)) {
            if (notification.isUnread()) {
                unread++;
            }
        }
        return unread;
    }

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

    public static void markAllAsRead(int userId) {
        List<Notification> notifications = FileStorage.loadNotifications();
        for (Notification notification : notifications) {
            if (notification.userId == userId) {
                notification.status = "READ";
            }
        }
        FileStorage.saveNotifications(notifications);
    }
}
