public final class DemoMetadata {
    public static final String APP_TITLE = "BUPT TA Recruitment System";
    public static final String APP_SUBTITLE = "Teaching Assistant Recruitment Demo";
    public static final String VERSION_LABEL = "ver_1.3";
    public static final String ITERATION_NOTE = "Current focus: strengthen admin operations, centralise scoring behind an AI-ready provider layer, and keep the demo close to a near-final integration build.";
    public static final String NEXT_STEP_NOTE = "Planned next steps: connect an external AI model through the scoring provider, add recommendation workflows, and expand automated tests around admin and matching scenarios.";

    private DemoMetadata() {
    }

    public static String buildAboutMessage() {
        return APP_TITLE + "\n"
                + VERSION_LABEL + "\n\n"
                + ITERATION_NOTE + "\n"
                + NEXT_STEP_NOTE;
    }
}
