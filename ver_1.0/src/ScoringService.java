/**
 * Chooses and exposes the active TA-job scoring provider for the whole demo.
 *
 * <p>The dashboards call this facade rather than talking directly to a concrete AI or rule-based
 * scorer. That keeps the application modular and lets the final demo fall back safely when no
 * external API key is configured.</p>
 */
public final class ScoringService {
    private static SkillScoringProvider activeProvider = buildProviderFromEnvironment();

    private ScoringService() {
    }

    /**
     * Delegates one scoring request to the currently active provider.
     *
     * @param profile TA profile to evaluate
     * @param job     job posting to compare with the profile
     * @return match result from the active provider
     */
    public static MatchResult evaluate(TAProfile profile, Job job) {
        return activeProvider.evaluate(profile, job);
    }

    /**
     * Returns the provider currently used by the UI and admin recommendation logic.
     *
     * @return active scoring provider
     */
    public static SkillScoringProvider getActiveProvider() {
        return activeProvider;
    }

    /**
     * Allows tests or future wiring code to override the provider explicitly.
     *
     * @param provider scoring provider to activate
     */
    public static void setActiveProvider(SkillScoringProvider provider) {
        if (provider != null) {
            activeProvider = provider;
        }
    }

    /**
     * Rebuilds the provider from environment variables or local AI config.
     */
    public static void resetProviderFromEnvironment() {
        activeProvider = buildProviderFromEnvironment();
    }

    /**
     * Returns a short mode label for admin screens and exported reports.
     *
     * @return provider mode label such as {@code RULE} or {@code AI}
     */
    public static String getProviderMode() {
        return activeProvider.isExternalModel() ? "AI" : "RULE";
    }

    private static SkillScoringProvider buildProviderFromEnvironment() {
        String providerMode = AIConfig.get("AI_SCORING_MODE");
        if (ValidationUtils.notBlank(providerMode) && "AI".equalsIgnoreCase(providerMode.trim())) {
            return new AIModelSkillScoringProvider();
        }
        if (ValidationUtils.notBlank(AIConfig.get("OPENAI_API_KEY"))) {
            return new AIModelSkillScoringProvider();
        }
        return new RuleBasedSkillScoringProvider();
    }
}
