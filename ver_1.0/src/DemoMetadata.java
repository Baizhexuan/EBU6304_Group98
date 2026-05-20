/**
 * Centralises version labels and iteration notes for the demo build.
 *
 * <p>Constants in this class are displayed in the UI title bar, the "About"
 * dialog, and generated reports. Update {@link #VERSION_LABEL} and
 * {@link #ITERATION_NOTE} at the start of each release iteration.
 * The class is not intended to be instantiated.</p>
 */
public final class DemoMetadata {
    /** Short application title used in window headings. */
    public static final String APP_TITLE = "BUPT TA Recruitment System";
    /** Full subtitle shown in the about dialog. */
    public static final String APP_SUBTITLE = "Teaching Assistant Recruitment Demo";
    /** Current version string shown in the menu bar. */
    public static final String VERSION_LABEL = "ver_1.11";
    /** One-line summary of what changed in this iteration. */
    public static final String ITERATION_NOTE = "Current focus: faster page refresh, cleaner search interactions, board-level AI matching support, and smoother final-demo responsiveness.";
    /** One-line summary of planned next steps. */
    public static final String NEXT_STEP_NOTE = "Planned next steps: package the final report, capture polished screenshots, and submit the final software bundle with documentation and tests.";

    private DemoMetadata() {
    }

    /**
     * Builds the multi-line message shown in the "About This Build" dialog.
     *
     * @return formatted about-message string
     */
    public static String buildAboutMessage() {
        return APP_TITLE + "\n"
                + VERSION_LABEL + "\n\n"
                + ITERATION_NOTE + "\n"
                + NEXT_STEP_NOTE + "\n\n"
                + AIIntegrationPlan.buildReadinessSummary();
    }
}
