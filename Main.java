import javax.swing.*;

/**
 * Main: Application entry point.
 * Initialises data files (creates them if first run) and launches the login window.
 *
 * <p>Version2 — L1 update: seed data now generates SHA-256 hashed passwords
 * via {@link PasswordUtil}. No plaintext passwords are written to CSV.
 *
 * @version 2.0
 * @since 2026-04-08
 */
public class Main {
    public static void main(String[] args) {
        // Initialise CSV data files on first run
        FileStorage.initDataFiles();

        // Launch GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to default look-and-feel
            }
            new LoginFrame();
        });
    }
}
