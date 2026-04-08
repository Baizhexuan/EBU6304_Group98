public final class DemoMetadata {
    public static final String APP_TITLE = "BUPT TA Recruitment System";
    public static final String APP_SUBTITLE = "Teaching Assistant Recruitment Demo";
    public static final String VERSION_LABEL = "ver_1.1";
    public static final String ITERATION_NOTE = "Current focus: improve discoverability with search/filter tools and make admin monitoring clearer for demos.";
    public static final String NEXT_STEP_NOTE = "Planned next steps: stronger validation, richer analytics, and smoother integration with later project iterations.";

    private DemoMetadata() {
    }

    public static String buildAboutMessage() {
        return APP_TITLE + "\n"
                + VERSION_LABEL + "\n\n"
                + ITERATION_NOTE + "\n"
                + NEXT_STEP_NOTE;
    }
}
