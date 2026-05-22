/**
 * Regression checks for scoring-provider selection and provider metadata.
 */
public class ScoringServiceTest {
    private ScoringServiceTest() {
    }

    /**
     * Runs scoring service checks.
     *
     * @param args command-line arguments, not used
     */
    public static void main(String[] args) {
        SkillScoringProvider originalProvider = ScoringService.getActiveProvider();
        try {
            RuleBasedSkillScoringProvider ruleProvider = new RuleBasedSkillScoringProvider();
            ScoringService.setActiveProvider(ruleProvider);

            TestSupport.assertEquals("RULE", ScoringService.getProviderMode(),
                    "Rule-based provider should expose RULE mode.");
            TestSupport.assertTrue(ScoringService.getActiveProvider().isReady(),
                    "Rule-based provider should always be ready for offline demo use.");
            TestSupport.assertContains(AIIntegrationPlan.buildReadinessSummary(), "RuleBasedSkillScoringProvider",
                    "Readiness summary should include the active provider name.");

            TAProfile profile = new TAProfile();
            profile.skills = "Java; Communication";
            Job job = new Job();
            job.requiredSkills = "Java; Python";
            MatchResult result = ScoringService.evaluate(profile, job);
            TestSupport.assertIntEquals(50, result.score,
                    "ScoringService should delegate scoring to the active provider.");
            TestSupport.assertContains(result.summary, "Source: local rule-based scorer",
                    "Rule-based provider should label its explanation source.");

            ScoringService.setActiveProvider(null);
            TestSupport.assertTrue(ScoringService.getActiveProvider() == ruleProvider,
                    "Setting a null provider should leave the active provider unchanged.");
        } finally {
            ScoringService.setActiveProvider(originalProvider);
        }

        System.out.println("ScoringServiceTest passed.");
    }
}
