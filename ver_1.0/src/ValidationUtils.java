/**
 * Stateless utility methods for common input validation and type conversion.
 *
 * <p>All methods are {@code static} and the class is not intended to be
 * instantiated. Conversions return a caller-supplied fallback on failure
 * rather than throwing exceptions, keeping UI code concise.</p>
 */
public class ValidationUtils {
    /** Minimum password length accepted by the registration workflow. */
    public static final int MIN_PASSWORD_LENGTH = 6;

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
     * Returns {@code true} when {@code value} matches a valid e-mail address pattern.
     *
     * <p><b>邓博文修复（邮箱正则加固）：</b>
     * 原正则 {@code ^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$} 存在以下漏洞：
     * <ul>
     *   <li>允许 {@code a@b}（无顶级域名，裸主机名）通过验证；</li>
     *   <li>允许 {@code user@localhost} 等本地地址，在注册场景下不合法；</li>
     *   <li>域名部分 {@code [A-Za-z0-9.-]+} 可接受连续点号如 {@code a@b..c}。</li>
     * </ul>
     *
     * <p>修复后正则 {@code ^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-])*\.[A-Za-z]{2,}$}：
     * <ul>
     *   <li>{@code [A-Za-z0-9-]+} — 域名主体（不含点号，避免连续点）；</li>
     *   <li>{@code (\.[A-Za-z0-9-])*} — 零个或多个子域段（每段以点开头）；</li>
     *   <li>{@code \.[A-Za-z]{2,}} — 强制要求至少 2 个字母的顶级域名（如 .com、.uk）；</li>
     *   <li>拒绝 {@code a@b}、{@code user@localhost} 等非法格式。</li>
     * </ul>
     *
     * <p>注意：此正则不覆盖所有 RFC 5322 边缘情况（如带引号的本地部分），
     * 但对常见注册表单验证已足够严格。
     *
     * @param value the string to validate
     * @return {@code true} if the string looks like a valid e-mail address
     */
    public static boolean isEmail(String value) {
        // 加固后的正则：要求域名包含至少一个点且顶级域名长度 >= 2
        // 拒绝裸主机名（如 a@b）和本地地址（如 user@localhost）
        return value != null && value.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-])*\\.[A-Za-z]{2,}$");
    }

    /**
     * Returns {@code true} when a registration password meets the minimum length rule.
     *
     * @param value password text to validate
     * @return {@code true} if the password has at least {@link #MIN_PASSWORD_LENGTH} characters
     */
    public static boolean isValidRegistrationPassword(String value) {
        return value != null && value.trim().length() >= MIN_PASSWORD_LENGTH;
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
