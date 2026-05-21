import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application entry point for the BUPT TA Recruitment System demo.
 *
 * <p>Bootstraps persistent storage, initialises the active scoring provider
 * from the current environment configuration, then launches the login window
 * on the Swing event-dispatch thread.</p>
 */
public class Main {
    private Main() {
    }

    /**
     * Starts the application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        FileStorage.initialise();
        ScoringService.resetProviderFromEnvironment();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new LoginFrame();
        });
    }
}
