/**
 * Strategy interface for TA-job skill scoring providers.
 *
 * <p>This keeps the UI and workflow code independent from the scoring implementation so the demo
 * can switch between a local explainable matcher and an external AI-backed provider.</p>
 */
public interface SkillScoringProvider {
    /**
     * Evaluates a TA profile against a job and returns a score plus a human-readable summary.
     *
     * @param profile TA profile to evaluate
     * @param job     job posting to compare with the profile
     * @return match result containing a score and explanation
     */
    MatchResult evaluate(TAProfile profile, Job job);

    /**
     * Returns a short provider name suitable for admin/demo status displays.
     *
     * @return provider display name
     */
    String getProviderName();

    /**
     * Indicates whether this provider depends on an external model or network call.
     *
     * @return {@code true} when the provider uses an external AI model
     */
    boolean isExternalModel();

    /**
     * Indicates whether the provider has enough local configuration to run.
     *
     * @return {@code true} when the provider can evaluate requests
     */
    boolean isReady();

    /**
     * Returns an explainable status message for README, UI, and viva discussion.
     *
     * @return provider status summary
     */
    String getStatusDescription();
}
