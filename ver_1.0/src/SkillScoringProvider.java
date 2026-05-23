/**
 * Strategy interface for TA-job skill scoring providers.
 *
 * <p>This keeps the UI and workflow code independent from the scoring implementation so the demo
 * can switch between a local explainable matcher and an external AI-backed provider.</p>
 */
public interface SkillScoringProvider {
    /**
     * Evaluates a TA profile against a job and returns a score plus a human-readable summary.
     */
    MatchResult evaluate(TAProfile profile, Job job);

    /**
     * Returns a short provider name suitable for admin/demo status displays.
     */
    String getProviderName();

    /**
     * Indicates whether this provider depends on an external model or network call.
     */
    boolean isExternalModel();

    /**
     * Indicates whether the provider has enough local configuration to run.
     */
    boolean isReady();

    /**
     * Returns an explainable status message for README, UI, and viva discussion.
     */
    String getStatusDescription();
}
