import javax.swing.*;

/**
 * Main: Application entry point.
 * Initialises data files (creates them if first run) and launches the login window.
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
