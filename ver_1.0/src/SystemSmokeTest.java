public class SystemSmokeTest {
    public static void main(String[] args) {
        FileStorage.initialise();
        ScoringService.resetProviderFromEnvironment();

        if (FileStorage.loadUsers().size() < 5) {
            throw new IllegalStateException("Expected seeded demo users.");
        }
        if (FileStorage.loadJobs().isEmpty()) {
            throw new IllegalStateException("Expected seeded jobs.");
        }
        if (FileStorage.loadNotifications().isEmpty()) {
            throw new IllegalStateException("Expected seeded notifications.");
        }

        TAProfile profile = FileStorage.findProfileByUserId(2);
        Job job = FileStorage.findJobById(1);
        MatchResult result = ScoringService.evaluate(profile, job);
        NotificationService.notifyProfileRequired(null);
        NotificationService.notifyJobClosed(null, null);

        if (result.score <= 0) {
            throw new IllegalStateException("Expected a positive match score.");
        }

        System.out.println("Smoke test passed.");
        System.out.println("Users: " + FileStorage.loadUsers().size());
        System.out.println("Jobs: " + FileStorage.loadJobs().size());
        System.out.println("Applications: " + FileStorage.loadApplications().size());
        System.out.println("Notifications: " + FileStorage.loadNotifications().size());
        System.out.println("Unread notifications for TA 2: " + NotificationService.countUnreadForUser(2));
        System.out.println("US-8 triggers: application decisions, profile reminders, and job closure alerts");
        System.out.println("Sample match: " + result.score + "% - " + result.summary);
        System.out.println(AIIntegrationPlan.buildReadinessSummary());
        System.out.println("Admin alert preview: " + AdminRecommendationService.buildGlobalAlertSummary());
    }
}
