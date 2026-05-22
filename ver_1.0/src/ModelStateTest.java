/**
 * Regression checks for simple model helper methods used by the dashboards.
 */
public class ModelStateTest {
    private ModelStateTest() {
    }

    /**
     * Runs model state checks.
     *
     * @param args command-line arguments, not used
     */
    public static void main(String[] args) {
        TAProfile profile = new TAProfile();
        profile.fullName = "Demo TA";
        profile.email = "demo.ta@bupt.edu.cn";
        profile.studentId = "20260001";
        profile.skills = "Java";
        TestSupport.assertTrue(profile.isComplete(),
                "Profile should be complete when all mandatory fields are present.");

        profile.skills = "   ";
        TestSupport.assertTrue(!profile.isComplete(),
                "Profile should be incomplete when mandatory skills are blank.");

        Job openJob = new Job();
        openJob.status = "open";
        TestSupport.assertTrue(openJob.isOpen(),
                "Job status should be case-insensitive when checking OPEN.");

        Job closedJob = new Job();
        closedJob.status = "CLOSED";
        TestSupport.assertTrue(!closedJob.isOpen(),
                "Closed jobs should not be treated as open.");

        Notification unread = new Notification();
        unread.status = "UNREAD";
        TestSupport.assertTrue(unread.isUnread(),
                "UNREAD notifications should be reported as unread.");

        Notification read = new Notification();
        read.status = "read";
        TestSupport.assertTrue(!read.isUnread(),
                "READ notifications should be reported as read case-insensitively.");

        User namedUser = new User(1, "ta_demo", "hashed", "TA", " Demo User ");
        TestSupport.assertEquals("Demo User", namedUser.getSafeDisplayName(),
                "Display names should be trimmed before rendering.");

        User fallbackUser = new User(2, "mo_demo", "hashed", "MO", " ");
        TestSupport.assertEquals("mo_demo", fallbackUser.getSafeDisplayName(),
                "Blank display names should fall back to username.");

        TestSupport.assertContains(namedUser.toString(), "TA",
                "User toString should include the role for list rendering.");

        System.out.println("ModelStateTest passed.");
    }
}
