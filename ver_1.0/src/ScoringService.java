public final class ScoringService {
    private static SkillScoringProvider activeProvider = new RuleBasedSkillScoringProvider();

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
}
