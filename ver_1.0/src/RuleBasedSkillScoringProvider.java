public class RuleBasedSkillScoringProvider implements SkillScoringProvider {
    @Override
    public MatchResult evaluate(TAProfile profile, Job job) {
        return MatchingService.evaluate(profile, job);
    }

    @Override
    public String getProviderName() {
        return "RuleBasedSkillScoringProvider";
    }

    @Override
    public boolean isExternalModel() {
        return false;
    }
}
