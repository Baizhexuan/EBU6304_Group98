public final class AIIntegrationPlan {
    private AIIntegrationPlan() {
    }

    public static String buildReadinessSummary() {
        SkillScoringProvider provider = ScoringService.getActiveProvider();
        return "Scoring provider: " + provider.getProviderName()
                + " | Mode: " + ScoringService.getProviderMode()
                + " | Provider ready: " + (provider.isReady() ? "Yes" : "No")
                + " | " + provider.getStatusDescription();
    }
}
