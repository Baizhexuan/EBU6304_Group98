import java.util.List;

/**
 * Lightweight authentication and registration regression checks for final delivery.
 */
public class AuthFlowTest {
    private AuthFlowTest() {
    }

    /**
     * Runs the authentication flow regression test.
     *
     * @param args command-line arguments, not used
     * @throws Exception if the isolated test data setup fails
     */
    public static void main(String[] args) throws Exception {
        TestSupport.withIsolatedData(new TestSupport.CheckedRunnable() {
            @Override
            public void run() {
                FileStorage.initialise();

                TestSupport.assertTrue(authenticate("admin", "admin123") != null,
                        "Seeded admin account should authenticate.");
                TestSupport.assertTrue(authenticate("admin", "wrong") == null,
                        "Wrong passwords should not authenticate.");
                TestSupport.assertTrue(ValidationUtils.isBlank("   "),
                        "Blank username input should be treated as invalid.");
                TestSupport.assertTrue(FileStorage.findUserByUsername("ta1") != null,
                        "Seeded demo account should exist.");
                TestSupport.assertTrue(FileStorage.findUserByUsername("TA1") != null,
                        "Duplicate username checks should be case-insensitive.");
                TestSupport.assertTrue(FileStorage.findUserByUsername("new_ta") == null,
                        "Fresh username should be available before registration.");

                List<User> users = FileStorage.loadUsers();
                users.add(new User(FileStorage.nextUserId(), "new_ta", "safePass1", "TA", "New TA"));
                FileStorage.saveUsers(users);

                TestSupport.assertTrue(authenticate("new_ta", "safePass1") != null,
                        "Newly registered account should authenticate.");
                TestSupport.assertTrue(FileStorage.findUserByUsername("NEW_TA") != null,
                        "Saved usernames should still be found case-insensitively.");

                System.out.println("AuthFlowTest passed.");
            }
        });
    }

    private static User authenticate(String username, String password) {
        if (ValidationUtils.isBlank(username) || ValidationUtils.isBlank(password)) {
            return null;
        }
        User user = FileStorage.findUserByUsername(username.trim());
        if (user == null) {
            return null;
        }
        return password.equals(user.password) ? user : null;
    }
}
