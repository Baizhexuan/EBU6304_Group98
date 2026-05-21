import java.util.List;

/**
 * Verifies that CSV persistence preserves commas, quotes, and empty values after save/load cycles.
 */
public class CsvPersistenceTest {
    private CsvPersistenceTest() {
    }

    /**
     * Runs the CSV persistence regression test.
     *
     * @param args command-line arguments, not used
     * @throws Exception if the isolated test data setup fails
     */
    public static void main(String[] args) throws Exception {
        TestSupport.withIsolatedData(new TestSupport.CheckedRunnable() {
            @Override
            public void run() {
                FileStorage.initialise();

                List<User> users = FileStorage.loadUsers();
                User csvUser = new User(FileStorage.nextUserId(), "csv_user", "csv123", "TA", "Doe, \"Jane\"");
                users.add(csvUser);
                FileStorage.saveUsers(users);
                User loadedUser = FileStorage.findUserByUsername("csv_user");
                TestSupport.assertEquals("Doe, \"Jane\"", loadedUser.displayName,
                        "User display names should preserve commas and quotes.");

                List<TAProfile> profiles = FileStorage.loadProfiles();
                TAProfile profile = new TAProfile();
                profile.id = FileStorage.nextProfileId();
                profile.userId = csvUser.id;
                profile.fullName = "Doe, Jane";
                profile.email = "jane.doe@bupt.edu.cn";
                profile.studentId = "2023999999";
                profile.skills = "Java, Git, Communication";
                profile.gpa = 3.5;
                profile.cvPath = "/tmp/jane \"cv\".pdf";
                profile.availability = "";
                profile.statement = "Interested in labs, mentoring, and reviewer feedback.";
                profiles.add(profile);
                FileStorage.saveProfiles(profiles);
                TAProfile loadedProfile = FileStorage.findProfileByUserId(csvUser.id);
                TestSupport.assertEquals(profile.skills, loadedProfile.skills,
                        "Profile skills should preserve embedded commas.");
                TestSupport.assertEquals("", loadedProfile.availability,
                        "Empty availability should survive a CSV round trip.");
                TestSupport.assertEquals(profile.statement, loadedProfile.statement,
                        "Profile statements should preserve commas.");

                List<Application> applications = FileStorage.loadApplications();
                Application application = new Application();
                application.id = FileStorage.nextApplicationId();
                application.taId = csvUser.id;
                application.jobId = 2;
                application.status = "PENDING";
                application.appliedAt = "2026-05-18 11:00";
                application.matchScore = 88;
                application.matchSummary = "Matched: Java, Git | Missing: Excel";
                application.reviewerNote = "";
                applications.add(application);
                FileStorage.saveApplications(applications);
                Application loadedApplication = findApplication(csvUser.id, 2);
                TestSupport.assertEquals(application.matchSummary, loadedApplication.matchSummary,
                        "Application match summaries should preserve commas.");
                TestSupport.assertEquals("", loadedApplication.reviewerNote,
                        "Empty reviewer notes should survive a CSV round trip.");

                List<Notification> notifications = FileStorage.loadNotifications();
                Notification notification = new Notification();
                notification.id = FileStorage.nextNotificationId();
                notification.userId = csvUser.id;
                notification.title = "Reminder, please check";
                notification.message = "Profile \"statement\" saved, but availability is still empty.";
                notification.status = "UNREAD";
                notification.createdAt = "2026-05-18 11:10";
                notification.actionHint = "Open My Profile, then update availability.";
                notifications.add(notification);
                FileStorage.saveNotifications(notifications);
                Notification loadedNotification = findNotification(csvUser.id, notification.title);
                TestSupport.assertEquals(notification.message, loadedNotification.message,
                        "Notification messages should preserve punctuation and quotes.");
                TestSupport.assertEquals(notification.actionHint, loadedNotification.actionHint,
                        "Notification action hints should preserve commas.");

                System.out.println("CsvPersistenceTest passed.");
            }
        });
    }

    private static Application findApplication(int taId, int jobId) {
        for (Application application : FileStorage.loadApplications()) {
            if (application.taId == taId && application.jobId == jobId) {
                return application;
            }
        }
        throw new IllegalStateException("Application not found for CSV test.");
    }

    private static Notification findNotification(int userId, String title) {
        for (Notification notification : FileStorage.loadNotifications()) {
            if (notification.userId == userId && title.equals(notification.title)) {
                return notification;
            }
        }
        throw new IllegalStateException("Notification not found for CSV test.");
    }
}
