/**
 * Reports which scoring provider is active for the demo build.
 *
 * <p>The summary is used by tests and UI support text to show whether the
 * system is using the local rule-based scorer or an optional external model.</p>
 */
public final class AIIntegrationPlan {
    private AIIntegrationPlan() {
    }

    /**
     * Builds a concise readiness summary for the active scoring provider.
     *
     * @return provider name, mode, readiness, and explanatory status text
     */
    public static String buildReadinessSummary() {
        SkillScoringProvider provider = ScoringService.getActiveProvider();
        return "Scoring provider: " + provider.getProviderName()
                + " | Mode: " + ScoringService.getProviderMode()
                + " | Provider ready: " + (provider.isReady() ? "Yes" : "No")
                + " | " + provider.getStatusDescription();
    }
}
