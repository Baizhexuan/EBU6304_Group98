/**
 * Regression checks for demo metadata displayed in UI support text.
 */
public class DemoMetadataTest {
    private DemoMetadataTest() {
    }

    /**
     * Runs metadata formatting checks.
     *
     * @param args command-line arguments, not used
     */
    public static void main(String[] args) {
        TestSupport.assertTrue(ValidationUtils.notBlank(DemoMetadata.APP_TITLE),
                "Application title should be present.");
        TestSupport.assertTrue(DemoMetadata.VERSION_LABEL.startsWith("ver_"),
                "Version label should use the ver_ prefix shown in the README.");

        String about = DemoMetadata.buildAboutMessage();
        TestSupport.assertContains(about, DemoMetadata.APP_TITLE,
                "About message should include the application title.");
        TestSupport.assertContains(about, DemoMetadata.VERSION_LABEL,
                "About message should include the current version label.");
        TestSupport.assertContains(about, "AI Match compares",
                "Help message should explain how AI Match works.");
        TestSupport.assertContains(about, "missing skills",
                "Help message should mention missing skill explanations.");
        TestSupport.assertContains(about, "Current AI Match status:",
                "Help message should include AI Match readiness.");

        System.out.println("DemoMetadataTest passed.");
    }
}
