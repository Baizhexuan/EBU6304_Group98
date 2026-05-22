import java.util.List;

/**
 * Regression checks for notification generation, de-duplication, and read-state transitions.
 */
public class NotificationFlowTest {
    private NotificationFlowTest() {
    }

    /**
     * Runs notification flow checks against isolated CSV data.
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
                User reviewer = FileStorage.findUserById(4);
                Job job = FileStorage.findJobById(1);

                TestSupport.assertTrue(ta != null, "Seeded TA user should exist.");
                TestSupport.assertTrue(reviewer != null, "Seeded MO reviewer should exist.");
                TestSupport.assertTrue(job != null, "Seeded job should exist.");

                int baselineUnread = NotificationService.countUnreadForUser(ta.id);

                NotificationService.notifyProfileRequired(ta);
                int unreadAfterFirstReminder = NotificationService.countUnreadForUser(ta.id);
                TestSupport.assertTrue(unreadAfterFirstReminder >= baselineUnread + 1,
                        "Profile reminder should increase unread count.");

                NotificationService.notifyProfileRequired(ta);
                int unreadAfterDuplicateReminder = NotificationService.countUnreadForUser(ta.id);
                TestSupport.assertIntEquals(unreadAfterFirstReminder, unreadAfterDuplicateReminder,
                        "Duplicate unread profile reminders should be de-duplicated.");

                Application application = new Application();
                application.id = 9991;
                application.taId = ta.id;
                application.jobId = job.id;
                application.status = "PENDING";
                application.appliedAt = "2026-05-22 10:00";
                application.matchScore = 80;
                application.matchSummary = "Matched: java";
                application.reviewerNote = "Pending review";

                NotificationService.notifyApplicationDecision(application, reviewer, job, "SELECTED");
                List<Notification> notifications = NotificationService.getNotificationsForUser(ta.id);
                Notification latest = notifications.get(notifications.size() - 1);
                TestSupport.assertContains(latest.title, job.title,
                        "Decision notification title should include job title.");
                TestSupport.assertContains(latest.message, "selected",
                        "Decision notification message should include decision status.");

                NotificationService.markAllAsRead(ta.id);
                TestSupport.assertIntEquals(0, NotificationService.countUnreadForUser(ta.id),
                        "markAllAsRead should clear unread notification count.");

                int closureCount = NotificationService.notifyJobClosed(job, reviewer);
                TestSupport.assertTrue(closureCount >= 1,
                        "Closing a job with active applications should notify at least one TA.");

                System.out.println("NotificationFlowTest passed.");
            }
        });
    }
}
