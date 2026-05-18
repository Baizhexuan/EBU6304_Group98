/**
 * Default offline scorer used by the demo when no external model is configured.
 *
 * <p>It wraps the token-based {@link MatchingService} result and labels the output clearly so the
 * user can see that the recommendation came from local explainable logic.</p>
 */
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
