/**
 * Stateless utility methods for common input validation and type conversion.
 *
 * <p>All methods are {@code static} and the class is not intended to be
 * instantiated. Conversions return a caller-supplied fallback on failure
 * rather than throwing exceptions, keeping UI code concise.</p>
 */
public class ValidationUtils {

    /**
     * Returns {@code true} when the string is {@code null} or contains only whitespace.
     *
     * @param value the string to test
     * @return {@code true} if blank
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Returns {@code true} when the string is neither {@code null} nor blank.
     *
     * @param value the string to test
     * @return {@code true} if not blank
     */
    public static boolean notBlank(String value) {
        return !isBlank(value);
    }

    /**
     * Returns {@code true} when {@code value} matches a simple e-mail pattern.
     *
     * @param value the string to validate
     * @return {@code true} if the string looks like a valid e-mail address
     */
    public static boolean isEmail(String value) {
        return value != null && value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    /**
     * Parses an integer from a string, returning {@code fallback} on failure.
     *
     * @param value    the string to parse
     * @param fallback value returned when parsing fails
     * @return parsed integer or {@code fallback}
     */
    public static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    /**
     * Parses a double from a string, returning {@code fallback} on failure.
     *
     * @param value    the string to parse
     * @param fallback value returned when parsing fails
     * @return parsed double or {@code fallback}
     */
    public static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
