public final class ScoringService {
    private static SkillScoringProvider activeProvider = buildProviderFromEnvironment();

    private ScoringService() {
    }

    public static MatchResult evaluate(TAProfile profile, Job job) {
        return activeProvider.evaluate(profile, job);
    }

    public static SkillScoringProvider getActiveProvider() {
        return activeProvider;
    }

    public static void setActiveProvider(SkillScoringProvider provider) {
        if (provider != null) {
            activeProvider = provider;
        }
    }

    public static void resetProviderFromEnvironment() {
        activeProvider = buildProviderFromEnvironment();
    }

    public static String getProviderMode() {
        return activeProvider.isExternalModel() ? "AI" : "RULE";
    }

    private static SkillScoringProvider buildProviderFromEnvironment() {
        String providerMode = System.getenv("AI_SCORING_MODE");
        if (ValidationUtils.notBlank(providerMode) && "AI".equalsIgnoreCase(providerMode.trim())) {
            return new AIModelSkillScoringProvider();
        }
        if (ValidationUtils.notBlank(System.getenv("OPENAI_API_KEY"))) {
            return new AIModelSkillScoringProvider();
        }
        return new RuleBasedSkillScoringProvider();
    }
}
