public final class DemoMetadata {
    public static final String APP_TITLE = "BUPT TA Recruitment System";
    public static final String APP_SUBTITLE = "Teaching Assistant Recruitment Demo";
    public static final String VERSION_LABEL = "ver_1.0";
    public static final String ITERATION_NOTE = "Current focus: polish demo flow, improve usability, and prepare integration for later iterations.";
    public static final String NEXT_STEP_NOTE = "Planned next steps: stronger validation, better filtering, and more complete admin reporting.";

    private DemoMetadata() {
    }

    public static String buildAboutMessage() {
        return APP_TITLE + "\n"
                + VERSION_LABEL + "\n\n"
                + ITERATION_NOTE + "\n"
                + NEXT_STEP_NOTE;
    }
}
