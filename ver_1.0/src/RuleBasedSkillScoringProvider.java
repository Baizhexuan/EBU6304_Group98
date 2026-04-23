public class RuleBasedSkillScoringProvider implements SkillScoringProvider {
    @Override
    public MatchResult evaluate(TAProfile profile, Job job) {
        MatchResult result = MatchingService.evaluate(profile, job);
        return new MatchResult(result.score, result.summary + " | Source: local rule-based scorer");
    }

    @Override
    public String getProviderName() {
        return "RuleBasedSkillScoringProvider";
    }

    @Override
    public boolean isExternalModel() {
        return false;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public String getStatusDescription() {
        return "Local rule-based scoring is active. No network access or API key is required.";
    }
}
