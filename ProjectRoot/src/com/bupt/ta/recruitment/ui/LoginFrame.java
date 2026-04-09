package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.service.AuthService;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.DataSeeder;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {
    private final AuthService authService;
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);

    public LoginFrame() {
        seedDataIfNeeded();
        this.authService = new AuthService(new CsvStorage<>("data/users.csv", User::fromCsvRow));

        setTitle("TA Recruitment System - Login");
        setSize(420, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        add(panel);

        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
    }

    private void seedDataIfNeeded() {
        CsvStorage<User> storage = new CsvStorage<>("data/users.csv", User::fromCsvRow);
        if (storage.loadAll().isEmpty()) {
            DataSeeder.main(new String[0]);
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() && password.isEmpty()) {
            showError("Username and password cannot be empty.");
            return;
        }
        if (username.isEmpty()) {
            showError("Username cannot be empty.");
            return;
        }
        if (password.isEmpty()) {
            showError("Password cannot be empty.");
            return;
        }

        Optional<User> userOpt = authService.login(username, password);
        if (userOpt.isPresent()) {
            openDashboard(userOpt.get());
            dispose();
            return;
        }

        if (authService.findUserByUsername(username).isPresent()) {
            showError("Invalid password.");
        } else {
            showError("User not found.");
        }
    }

    private void openDashboard(User user) {
        switch (user.getRole()) {
            case ADMIN:
                new AdminDashboard(user).setVisible(true);
                break;
            case MO:
                new MODashboard(user).setVisible(true);
                break;
            case TA:
                new TADashboard(user).setVisible(true);
                break;
            default:
                showError("Unknown role. Cannot open dashboard.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }
}
