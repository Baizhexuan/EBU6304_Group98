/**
 * Regression checks for CSV-backed lookup helpers and deterministic ID allocation.
 */
public class FileStorageLookupTest {
    private FileStorageLookupTest() {
    }

    /**
     * Runs lookup checks against isolated CSV data.
     *
     * @param args command-line arguments, not used
     * @throws Exception if isolated data setup fails
     */
    public static void main(String[] args) throws Exception {
        TestSupport.withIsolatedData(new TestSupport.CheckedRunnable() {
            @Override
            public void run() {
                FileStorage.initialise();

                User admin = FileStorage.findUserByUsername("ADMIN");
                TestSupport.assertTrue(admin != null && "ADMIN".equals(admin.role),
                        "Username lookup should be case-insensitive.");

                User displayUser = FileStorage.findUserByDisplayName(admin.displayName);
                TestSupport.assertTrue(displayUser != null && displayUser.id == admin.id,
                        "Display-name lookup should return the matching user.");

                TestSupport.assertTrue(FileStorage.findUserById(admin.id) != null,
                        "User lookup by ID should find seeded users.");
                TestSupport.assertTrue(FileStorage.findProfileByUserId(2) != null,
                        "Profile lookup should find seeded TA profiles.");
                TestSupport.assertTrue(FileStorage.findJobById(1) != null,
                        "Job lookup should find seeded jobs.");

                int nextUserId = FileStorage.nextUserId();
                TestSupport.assertIntEquals(maxUserId() + 1, nextUserId,
                        "Next user ID should follow the current maximum user ID.");
                TestSupport.assertIntEquals(nextUserId + 1, FileStorage.nextUserId(),
                        "Consecutive user ID allocation should not return a duplicate ID.");
                TestSupport.assertIntEquals(maxProfileId() + 1, FileStorage.nextProfileId(),
                        "Next profile ID should follow the current maximum profile ID.");
                TestSupport.assertIntEquals(maxJobId() + 1, FileStorage.nextJobId(),
                        "Next job ID should follow the current maximum job ID.");
                TestSupport.assertIntEquals(maxApplicationId() + 1, FileStorage.nextApplicationId(),
                        "Next application ID should follow the current maximum application ID.");
                TestSupport.assertIntEquals(maxNotificationId() + 1, FileStorage.nextNotificationId(),
                        "Next notification ID should follow the current maximum notification ID.");

                System.out.println("FileStorageLookupTest passed.");
            }
        });
    }

    private static int maxUserId() {
        int max = 0;
        for (User user : FileStorage.loadUsers()) {
            max = Math.max(max, user.id);
        }
        return max;
    }

    private static int maxProfileId() {
        int max = 0;
        for (TAProfile profile : FileStorage.loadProfiles()) {
            max = Math.max(max, profile.id);
        }
        return max;
    }

    private static int maxJobId() {
        int max = 0;
        for (Job job : FileStorage.loadJobs()) {
            max = Math.max(max, job.id);
        }
        return max;
    }

    private static int maxApplicationId() {
        int max = 0;
        for (Application application : FileStorage.loadApplications()) {
            max = Math.max(max, application.id);
        }
        return max;
    }

    private static int maxNotificationId() {
        int max = 0;
        for (Notification notification : FileStorage.loadNotifications()) {
            max = Math.max(max, notification.id);
        }
        return max;
    }
}
