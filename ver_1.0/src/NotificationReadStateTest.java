import java.util.List;

/**
 * Regression checks for notification read-state operations.
 */
public class NotificationReadStateTest {
    private NotificationReadStateTest() {
    }

    /**
     * Runs notification read-state checks against isolated CSV data.
     *
     * @param args command-line arguments, not used
     * @throws Exception if isolated data setup fails
     */
    public static void main(String[] args) throws Exception {
        TestSupport.withIsolatedData(new TestSupport.CheckedRunnable() {
            @Override
            public void run() {
                FileStorage.initialise();

                User ta = FileStorage.findUserById(2);
                TestSupport.assertTrue(ta != null, "Seeded TA user should exist.");

                NotificationService.notifyProfileRequired(ta);
                TestSupport.assertTrue(NotificationService.countUnreadForUser(ta.id) > 0,
                        "Profile reminder should create an unread notification.");

                NotificationService.markProfileReminderResolved(ta);
                TestSupport.assertIntEquals(0, NotificationService.countUnreadForUser(ta.id),
                        "Resolving a profile reminder should mark it as read.");

                NotificationService.notifyProfileRequired(ta);
                List<Notification> notifications = NotificationService.getNotificationsForUser(ta.id);
                Notification latest = notifications.get(notifications.size() - 1);
                TestSupport.assertTrue(latest.isUnread(),
                        "A fresh profile reminder should be unread.");

                NotificationService.markAsRead(latest.id);
                notifications = NotificationService.getNotificationsForUser(ta.id);
                Notification reloaded = null;
                for (Notification notification : notifications) {
                    if (notification.id == latest.id) {
                        reloaded = notification;
                    }
                }
                TestSupport.assertTrue(reloaded != null && !reloaded.isUnread(),
                        "markAsRead should persist read state for the selected notification.");

                System.out.println("NotificationReadStateTest passed.");
            }
        });
    }
}
