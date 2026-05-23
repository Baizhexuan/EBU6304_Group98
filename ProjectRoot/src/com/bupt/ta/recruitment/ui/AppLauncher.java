package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.util.DataSeeder;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class AppLauncher {
    private AppLauncher() {
    }

    public static void main(String[] args) {
        DataSeeder.seedIfNeeded();
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new LoginFrame();
        });
    }
}
