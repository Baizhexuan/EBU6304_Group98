import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class AIConfig {
    private static final Properties LOCAL_PROPERTIES = loadLocalProperties();

    private AIConfig() {
    }

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
