import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Provides configuration values for the optional AI integration.
 *
 * <p>Values are resolved in priority order:
 * <ol>
 *   <li>Environment variable (highest priority)</li>
 *   <li>{@code config/ai.properties} file</li>
 *   <li>{@code ai.properties} in the working directory</li>
 *   <li>Empty string — callers treat this as "not configured" (lowest priority)</li>
 * </ol>
 * The class is not intended to be instantiated.
 */
public final class AIConfig {
    private static final Properties LOCAL_PROPERTIES = loadLocalProperties();

    private AIConfig() {
    }

    /**
     * Returns the configuration value for {@code key}, or an empty string when not set.
     *
     * @param key the configuration key (e.g. {@code OPENAI_API_KEY})
     * @return trimmed value string, never {@code null}
     */
    public static String get(String key) {
        String envValue = System.getenv(key);
        if (ValidationUtils.notBlank(envValue)) {
            return envValue.trim();
        }
        String localValue = LOCAL_PROPERTIES.getProperty(key);
        return ValidationUtils.notBlank(localValue) ? localValue.trim() : "";
    }

    private static Properties loadLocalProperties() {
        Properties properties = new Properties();
        File file = new File("config/ai.properties");
        if (!file.exists()) {
            file = new File("ai.properties");
        }
        if (!file.exists()) {
            return properties;
        }
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        } catch (IOException ignored) {
            // Keep the demo usable even when a local config file is malformed or unavailable.
        }
        return properties;
    }
}
