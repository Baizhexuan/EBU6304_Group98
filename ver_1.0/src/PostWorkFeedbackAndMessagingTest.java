import java.util.List;

/**
 * Regression checks for post-work reputation penalties and TA-MO message limits.
 */
public class PostWorkFeedbackAndMessagingTest {
    public static void main(String[] args) throws Exception {
        TestSupport.withIsolatedData(new TestSupport.CheckedRunnable() {
            @Override
            public void run() {
                FileStorage.initialise();
                ScoringService.setActiveProvider(new RuleBasedSkillScoringProvider());

                Application selectedApplication = findApplication(2, 1);
                TestSupport.assertEquals("SELECTED", selectedApplication.status,
                        "Seed application should be selected for post-work evaluation.");
                TestSupport.assertTrue(selectedApplication.matchScore >= 80,
                        "Seed application should have a high original match score.");

                WorkEvaluation evaluation = new WorkEvaluation();
                evaluation.id = FileStorage.nextWorkEvaluationId();
                evaluation.applicationId = selectedApplication.id;
                evaluation.taId = selectedApplication.taId;
                evaluation.moId = 4;
                evaluation.jobId = selectedApplication.jobId;
                evaluation.rating = 1;
                evaluation.comment = "Low delivery quality despite strong claimed skills.";
                evaluation.evaluatedAt = "2026-05-20 12:00";
                boolean penaltyApplied = ReputationService.applyCompletedWorkEvaluation(evaluation, selectedApplication);
                List<WorkEvaluation> evaluations = FileStorage.loadWorkEvaluations();
                evaluations.add(evaluation);
                FileStorage.saveWorkEvaluations(evaluations);

                TestSupport.assertTrue(penaltyApplied,
                        "High match plus low completion rating should trigger a reputation penalty.");
                TestSupport.assertIntEquals(70, ReputationService.getScoreForTa(2),
                        "First high-match low-rating penalty should reduce reputation by 30 points.");

                MatchResult adjusted = ScoringService.evaluate(FileStorage.findProfileByUserId(2), FileStorage.findJobById(1));
                TestSupport.assertIntEquals(70, adjusted.score,
                        "Future match score should be adjusted by the lowered reputation score.");
                TestSupport.assertContains(adjusted.summary, "Reputation penalty",
                        "Adjusted summary should explain the reputation penalty.");

                User ta = FileStorage.findUserById(2);
                MessageSendResult first = MessageService.sendMessage(ta, 4, 1, "Could we discuss the Java lab role?");
                MessageSendResult second = MessageService.sendMessage(ta, 4, 1, "I have a question about timing.");
                MessageSendResult third = MessageService.sendMessage(ta, 4, 1, "Please confirm the expected workload.");
                boolean taApproval = MessageService.approveConversation(2, 4, 1);
                MessageSendResult fourth = MessageService.sendMessage(ta, 4, 1, "Fourth message before approval.");
                TestSupport.assertTrue(first.success && second.success && third.success,
                        "First three messages should be allowed before conversation consent.");
                TestSupport.assertTrue(!taApproval,
                        "TA should not be allowed to approve the conversation; approval belongs to the MO.");
                TestSupport.assertTrue(!fourth.success,
                        "Fourth message should be blocked until the recipient approves the conversation.");
                MessageService.approveConversation(4, 2, 1);
                MessageSendResult afterApproval = MessageService.sendMessage(ta, 4, 1, "Thanks for approving the chat.");
                TestSupport.assertTrue(afterApproval.success,
                        "Messages should be allowed after conversation approval.");
                TestSupport.assertTrue(hasApprovalNotificationForTa(2),
                        "MO approval should create a notification for the TA.");
                TestSupport.assertTrue(MessageService.countUnreadMessagesForUser(4) >= 4,
                        "MO should have unread incoming messages from the TA.");

                System.out.println("PostWorkFeedbackAndMessagingTest passed.");
            }
        });
    }

    private static Application findApplication(int taId, int jobId) {
        for (Application application : FileStorage.loadApplications()) {
            if (application.taId == taId && application.jobId == jobId) {
                return application;
            }
        }
        throw new IllegalStateException("Expected application not found.");
    }

    private static boolean hasApprovalNotificationForTa(int taId) {
        for (Notification notification : NotificationService.getNotificationsForUser(taId)) {
            if (notification.title != null && notification.title.startsWith("Conversation approved by ")) {
                return true;
            }
        }
        return false;
    }
}
