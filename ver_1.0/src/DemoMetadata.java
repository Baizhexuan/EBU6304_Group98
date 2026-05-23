/**
 * Centralises version labels and user-facing help text for the demo build.
 *
 * <p>Constants in this class are displayed in the UI title bar, help dialog,
 * and generated reports. The class is not intended to be instantiated.</p>
 */
public final class DemoMetadata {
    /** Short application title used in window headings. */
    public static final String APP_TITLE = "BUPT TA Recruitment System";
    /** Full subtitle shown in the about dialog. */
    public static final String APP_SUBTITLE = "Teaching Assistant Recruitment Demo";
    /** Current version string shown in the menu bar. */
    public static final String VERSION_LABEL = "ver_1.11";
    /** User-facing title for the AI matching help dialog. */
    public static final String AI_MATCH_HELP_TITLE = "AI Match Guide";
    /** User-facing instruction text for the AI matching help dialog. */
    public static final String AI_MATCH_HELP_BODY = "AI Match compares a TA profile with each open job by reading the TA skills and the job's required skills.\n"
            + "Use the match score to see overall fit, then read the explanation to check matched skills and missing skills.\n"
            + "TA users can review the ranking before applying. MO users can compare applicants for a job. Admin users can inspect workload and replacement recommendations.\n"
            + "If no external AI key is configured, the system uses the local explainable rule-based scorer, so the matching feature still works offline.";

    private DemoMetadata() {
    }

    /**
     * Builds the multi-line message shown in the AI matching help dialog.
     *
     * @return formatted AI matching help string
     */
    public static String buildAboutMessage() {
        return APP_TITLE + "\n"
                + VERSION_LABEL + "\n\n"
                + AI_MATCH_HELP_BODY + "\n\n"
                + "Current AI Match status: " + AIIntegrationPlan.buildReadinessSummary();
    }
}
