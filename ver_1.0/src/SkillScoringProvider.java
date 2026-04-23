public interface SkillScoringProvider {
    MatchResult evaluate(TAProfile profile, Job job);

    String getProviderName();

    boolean isExternalModel();

    boolean isReady();

    String getStatusDescription();
}
