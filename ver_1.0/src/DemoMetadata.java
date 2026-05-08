public final class DemoMetadata {
    public static final String APP_TITLE = "BUPT TA Recruitment System";
    public static final String APP_SUBTITLE = "Teaching Assistant Recruitment Demo";
    public static final String VERSION_LABEL = "ver_1.6";
    public static final String ITERATION_NOTE = "Current focus: complete the in-app notification flow, expose clearer AI-assisted explanations, and polish the final demo experience for presentation and walkthroughs.";
    public static final String NEXT_STEP_NOTE = "Planned next steps: separate more business logic into dedicated services, expand automated tests, and harden the production AI contract.";

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
