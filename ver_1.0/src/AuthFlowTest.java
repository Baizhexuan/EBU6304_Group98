import java.util.List;

/**
 * Lightweight authentication and registration regression checks for final delivery.
 */
public class AuthFlowTest {
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
                TestSupport.assertTrue(!canRegister("short_pw", "a", "a", "Short Password TA"),
                        "Registration should reject passwords shorter than six characters.");
                TestSupport.assertTrue(FileStorage.findUserByUsername("short_pw") == null,
                        "Rejected short-password registration should not create a user.");
                TestSupport.assertTrue(!canRegister("short_pw2", "abcde", "abcde", "Short Password TA"),
                        "Registration should reject five-character passwords.");

                List<User> users = FileStorage.loadUsers();
                users.add(new User(FileStorage.nextUserId(), "new_ta", FileStorage.hashPassword("safePass1"), "TA", "New TA"));
                FileStorage.saveUsers(users);

                TestSupport.assertTrue(authenticate("new_ta", "safePass1") != null,
                        "Newly registered account should authenticate.");
                User newUser = FileStorage.findUserByUsername("new_ta");
                TestSupport.assertTrue(newUser != null && !newUser.password.equals("safePass1"),
                        "Newly registered passwords should be stored as hashes.");
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
        return FileStorage.passwordMatches(password, user.password) ? user : null;
    }

    private static boolean canRegister(String username, String password, String confirmPassword, String displayName) {
        if (ValidationUtils.isBlank(username) || ValidationUtils.isBlank(password)
                || ValidationUtils.isBlank(confirmPassword) || ValidationUtils.isBlank(displayName)) {
            return false;
        }
        if (!password.equals(confirmPassword)) {
            return false;
        }
        if (!ValidationUtils.isValidRegistrationPassword(password)) {
            return false;
        }
        return FileStorage.findUserByUsername(username.trim()) == null;
    }
}
