/**
 * Regression checks for shared validation and parsing helpers.
 */
public class ValidationUtilsTest {
    private ValidationUtilsTest() {
    }

    /**
     * Runs validation utility checks.
     *
     * @param args command-line arguments, not used
     */
    public static void main(String[] args) {
        TestSupport.assertTrue(ValidationUtils.isBlank(null),
                "Null input should be treated as blank.");
        TestSupport.assertTrue(ValidationUtils.isBlank("   "),
                "Whitespace-only input should be treated as blank.");
        TestSupport.assertTrue(ValidationUtils.notBlank("admin"),
                "Non-empty input should pass notBlank.");

        TestSupport.assertTrue(ValidationUtils.isEmail("ta.demo@bupt.edu.cn"),
                "Simple university email addresses should be accepted.");
        TestSupport.assertTrue(!ValidationUtils.isEmail("not-an-email"),
                "Malformed email addresses should be rejected.");
        TestSupport.assertTrue(!ValidationUtils.isValidRegistrationPassword("a"),
                "Single-character passwords should be rejected during registration.");
        TestSupport.assertTrue(!ValidationUtils.isValidRegistrationPassword("abcde"),
                "Five-character passwords should be rejected during registration.");
        TestSupport.assertTrue(ValidationUtils.isValidRegistrationPassword("abcdef"),
                "Six-character passwords should be accepted during registration.");
        String hashedPassword = FileStorage.hashPassword("abcdef");
        TestSupport.assertTrue(hashedPassword.length() == 64,
                "SHA-256 password hashes should contain 64 hex characters.");
        TestSupport.assertTrue(FileStorage.passwordMatches("abcdef", hashedPassword),
                "Password matcher should accept hashed registration passwords.");
        TestSupport.assertTrue(FileStorage.passwordMatches("ta123", "ta123"),
                "Password matcher should keep legacy demo accounts usable.");

        TestSupport.assertIntEquals(42, ValidationUtils.parseInt("42", -1),
                "parseInt should return the parsed value for valid integers.");
        TestSupport.assertIntEquals(-1, ValidationUtils.parseInt("missing", -1),
                "parseInt should return fallback for invalid integers.");

        double parsed = ValidationUtils.parseDouble("3.5", -1.0);
        TestSupport.assertTrue(Math.abs(parsed - 3.5) < 0.0001,
                "parseDouble should return the parsed value for valid decimals.");
        TestSupport.assertTrue(Math.abs(ValidationUtils.parseDouble("missing", -1.0) + 1.0) < 0.0001,
                "parseDouble should return fallback for invalid decimals.");

        System.out.println("ValidationUtilsTest passed.");
    }
}
