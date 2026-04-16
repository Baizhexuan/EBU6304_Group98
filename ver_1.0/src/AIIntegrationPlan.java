public final class AIIntegrationPlan {
    private AIIntegrationPlan() {
    }

    public static String buildReadinessSummary() {
        SkillScoringProvider provider = ScoringService.getActiveProvider();
        return "Scoring provider: " + provider.getProviderName()
                + " | External model connected: " + (provider.isExternalModel() ? "Yes" : "No")
                + " | Integration path: replace the provider with an LLM-backed implementation while keeping dashboard flows unchanged.";
    }
}
