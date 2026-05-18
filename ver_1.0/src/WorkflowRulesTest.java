import java.util.List;

/**
 * Workflow-focused regression checks for TA, MO, Admin, notifications, and explainable scoring.
 */
public class WorkflowRulesTest {
    public static void main(String[] args) throws Exception {
        TestSupport.withIsolatedData(new TestSupport.CheckedRunnable() {
            @Override
            public void run() {
                FileStorage.initialise();
                ScoringService.setActiveProvider(new RuleBasedSkillScoringProvider());

                TAProfile existingProfile = FileStorage.findProfileByUserId(2);
                TestSupport.assertTrue(existingProfile != null && existingProfile.isComplete(),
                        "Seeded TA profile should be complete.");

                TAProfile incompleteProfile = new TAProfile();
                incompleteProfile.fullName = "Incomplete User";
                incompleteProfile.email = "incomplete@bupt.edu.cn";
                TestSupport.assertTrue(!incompleteProfile.isComplete(),
                        "Incomplete profiles should not pass application readiness checks.");

                int openJobs = 0;
                for (Job job : FileStorage.loadJobs()) {
                    if (job.isOpen()) {
                        openJobs++;
                    }
                }
                TestSupport.assertTrue(openJobs >= 4, "Seed data should expose open jobs for browsing.");

                TAProfile ta2Profile = FileStorage.findProfileByUserId(3);
                Job job2 = FileStorage.findJobById(2);
                TestSupport.assertTrue(canApply(3, 2), "TA 2 should be allowed to apply to job 2 initially.");
                MatchResult result = ScoringService.evaluate(ta2Profile, job2);
                TestSupport.assertContains(result.summary, "Source: local rule-based scorer",
                        "Explainable fallback scoring should label its source.");

                List<Application> applications = FileStorage.loadApplications();
                Application newApplication = new Application();
                newApplication.id = FileStorage.nextApplicationId();
                newApplication.taId = 3;
                newApplication.jobId = 2;
                newApplication.status = "PENDING";
                newApplication.appliedAt = "2026-05-18 10:00";
                newApplication.matchScore = result.score;
                newApplication.matchSummary = result.summary;
                newApplication.reviewerNote = "Awaiting review";
                applications.add(newApplication);
                FileStorage.saveApplications(applications);

                TestSupport.assertTrue(!canApply(3, 2),
                        "Duplicate applications for the same active job should be prevented.");

                User reviewer = FileStorage.findUserById(4);
                NotificationService.notifyApplicationDecision(newApplication, reviewer, job2, "REJECTED");
                TestSupport.assertTrue(NotificationService.countUnreadForUser(3) > 0,
                        "A decision update should generate an unread notification for the TA.");
                List<Notification> taNotifications = NotificationService.getNotificationsForUser(3);
                Notification latest = taNotifications.get(taNotifications.size() - 1);
                TestSupport.assertContains(latest.title, job2.title,
                        "Decision notification should mention the reviewed job.");

                applications = FileStorage.loadApplications();
                for (Application application : applications) {
                    if (application.taId == 3 && (application.jobId == 2 || application.jobId == 3)) {
                        application.status = "SELECTED";
                    }
                }
                Application overloadApp = new Application();
                overloadApp.id = FileStorage.nextApplicationId();
                overloadApp.taId = 3;
                overloadApp.jobId = 4;
                overloadApp.status = "SELECTED";
                overloadApp.appliedAt = "2026-05-18 10:30";
                overloadApp.matchScore = 71;
                overloadApp.matchSummary = "Promoted to selected for workload test";
                overloadApp.reviewerNote = "Used for overload verification";
                applications.add(overloadApp);
                FileStorage.saveApplications(applications);

                String alertSummary = AdminRecommendationService.buildGlobalAlertSummary();
                String recommendation = AdminRecommendationService.buildRecommendationReportForTa(3);
                TestSupport.assertContains(alertSummary, "High-risk overview",
                        "Admin summary should surface overload risk when selected hours exceed the limit.");
                TestSupport.assertContains(recommendation, "OVERLOAD",
                        "TA-specific admin recommendation should expose overload status.");

                System.out.println("WorkflowRulesTest passed.");
            }
        });
    }

    private static boolean canApply(int taId, int jobId) {
        TAProfile profile = FileStorage.findProfileByUserId(taId);
        if (profile == null || !profile.isComplete()) {
            return false;
        }
        for (Application application : FileStorage.loadApplications()) {
            if (application.taId == taId && application.jobId == jobId
                    && !"WITHDRAWN".equalsIgnoreCase(application.status)
                    && !"REJECTED".equalsIgnoreCase(application.status)) {
                return false;
            }
        }
        return true;
    }

    private static Application findApplication(int taId, int jobId) {
        for (Application application : FileStorage.loadApplications()) {
            if (application.taId == taId && application.jobId == jobId) {
                return application;
            }
        }
        throw new IllegalStateException("Expected application not found for taId=" + taId + " jobId=" + jobId);
    }
}
