public final class DemoMetadata {
    public static final String APP_TITLE = "BUPT TA Recruitment System";
    public static final String APP_SUBTITLE = "Teaching Assistant Recruitment Demo";
    public static final String VERSION_LABEL = "ver_1.4";
    public static final String ITERATION_NOTE = "Current focus: add a live AI API placeholder path, strengthen admin reallocation decisions, and polish the demo into a presentation-ready build.";
    public static final String NEXT_STEP_NOTE = "Planned next steps: connect a production AI workflow, move recommendation logic into clearer service modules, and extend automated regression tests.";

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
