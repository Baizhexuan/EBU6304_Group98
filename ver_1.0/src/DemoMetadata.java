public final class DemoMetadata {
    public static final String APP_TITLE = "BUPT TA Recruitment System";
    public static final String APP_SUBTITLE = "Teaching Assistant Recruitment Demo";
    public static final String VERSION_LABEL = "ver_1.11";
    public static final String ITERATION_NOTE = "Current focus: faster page refresh, cleaner search interactions, board-level AI matching support, and smoother final-demo responsiveness.";
    public static final String NEXT_STEP_NOTE = "Planned next steps: package the final report, capture polished screenshots, and submit the final software bundle with documentation and tests.";

    private DemoMetadata() {
    }

    public static String buildAboutMessage() {
        return APP_TITLE + "\n"
                + VERSION_LABEL + "\n\n"
                + ITERATION_NOTE + "\n"
                + NEXT_STEP_NOTE + "\n\n"
                + AIIntegrationPlan.buildReadinessSummary();
    }
}
