import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared helpers for lightweight console-based tests.
 *
 * <p>The tests in this demo intentionally avoid JUnit to keep the build simple. This helper backs
 * up the mutable CSV files, resets deterministic seed data, and restores the original workspace
 * state after each test run.</p>
 */
public final class TestSupport {
    private static final String[] CORE_DATA_FILES = {
            "users.csv",
            "profiles.csv",
            "jobs.csv",
            "applications.csv",
            "notifications.csv"
    };

    private TestSupport() {
    }

    /**
     * Callback that can throw checked exceptions while running isolated tests.
     */
    public interface CheckedRunnable {
        /**
         * Executes the test body.
         *
         * @throws Exception when the test body fails
         */
        void run() throws Exception;
    }

    /**
     * Runs a test action against temporary seed data and restores original CSV files afterwards.
     *
     * @param action test body to execute
     * @throws Exception when setup, execution, or restore fails
     */
    public static void withIsolatedData(CheckedRunnable action) throws Exception {
        Map<String, String> backup = backupCoreData();
        try {
            resetToSeedData();
            action.run();
        } finally {
            restoreCoreData(backup);
        }
    }

    /**
     * Asserts that a condition is true.
     *
     * @param condition condition to check
     * @param message   failure message
     */
    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Asserts that two strings are equal.
     *
     * @param expected expected value
     * @param actual   actual value
     * @param message  failure message prefix
     */
    public static void assertEquals(String expected, String actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new IllegalStateException(message + " Expected: " + expected + " Actual: " + actual);
        }
    }

    /**
     * Asserts that two integer values are equal.
     *
     * @param expected expected integer
     * @param actual   actual integer
     * @param message  failure message prefix
     */
    public static void assertIntEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new IllegalStateException(message + " Expected: " + expected + " Actual: " + actual);
        }
    }

    /**
     * Asserts that a text value contains a required snippet.
     *
     * @param text    text to inspect
     * @param snippet snippet expected to appear in {@code text}
     * @param message failure message prefix
     */
    public static void assertContains(String text, String snippet, String message) {
        if (text == null || !text.contains(snippet)) {
            throw new IllegalStateException(message + " Missing snippet: " + snippet);
        }
    }

    private static Map<String, String> backupCoreData() throws IOException {
        Map<String, String> backup = new LinkedHashMap<String, String>();
        Path dataDir = Paths.get("data");
        Files.createDirectories(dataDir);
        for (String name : CORE_DATA_FILES) {
            Path file = dataDir.resolve(name);
            backup.put(name, Files.exists(file)
                    ? new String(Files.readAllBytes(file), StandardCharsets.UTF_8)
                    : null);
        }
        return backup;
    }

    private static void restoreCoreData(Map<String, String> backup) throws IOException {
        Path dataDir = Paths.get("data");
        Files.createDirectories(dataDir);
        for (Map.Entry<String, String> entry : backup.entrySet()) {
            Path file = dataDir.resolve(entry.getKey());
            if (entry.getValue() == null) {
                Files.deleteIfExists(file);
            } else {
                Files.write(file, entry.getValue().getBytes(StandardCharsets.UTF_8));
            }
        }
        deleteGeneratedReports();
    }

    private static void resetToSeedData() throws IOException {
        Path dataDir = Paths.get("data");
        Files.createDirectories(dataDir);
        for (String name : CORE_DATA_FILES) {
            Files.deleteIfExists(dataDir.resolve(name));
        }
        deleteGeneratedReports();
        FileStorage.initialise();
    }

    private static void deleteGeneratedReports() throws IOException {
        Path dataDir = Paths.get("data");
        if (!Files.exists(dataDir)) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir, "admin_workload_report*.csv")) {
            for (Path file : stream) {
                Files.deleteIfExists(file);
            }
        }
    }
}
