package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.DataSeeder;
import com.bupt.ta.recruitment.util.PasswordUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final CsvStorage<User> userStorage = new CsvStorage<>("data/users.csv", User::fromCsvRow);

    public LoginFrame() {
        DataSeeder.seedIfNeeded();
        setTitle("BUPT TA Recruitment System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        JLabel title = new JLabel("BUPT International School", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        JLabel subtitle = new JLabel("L2 Authentication and Dashboard Framework", SwingConstants.CENTER);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        root.add(titlePanel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        form.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        JPanel buttonRow = new JPanel();
        buttonRow.add(loginButton);
        buttonRow.add(registerButton);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        form.add(buttonRow, gbc);
        root.add(form, BorderLayout.CENTER);

        JLabel hint = new JLabel("Demo accounts: admin/admin123, mo1/mo123, ta1/ta123", SwingConstants.CENTER);
        hint.setPreferredSize(new Dimension(400, 40));
        root.add(hint, BorderLayout.SOUTH);

        add(root);

        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> new RegisterFrame(this));
        passwordField.addActionListener(e -> attemptLogin());

        setVisible(true);
    }

    public void prefillUsername(String username) {
        usernameField.setText(username);
        passwordField.setText("");
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() && password.isEmpty()) {
            showError("Please enter username and password.");
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

        List<User> users = userStorage.loadAll();
        User matched = null;
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                matched = user;
                break;
            }
        }

        if (matched == null) {
            showError("User does not exist.");
            return;
        }
        if (!PasswordUtil.verifyPassword(password, matched.getSalt(), matched.getPasswordHash())) {
            showError("Password is incorrect.");
            passwordField.setText("");
            return;
        }

        dispose();
        openDashboard(matched);
    }

    private void openDashboard(User user) {
        switch (user.getRole()) {
            case TA:
                new TADashboard(user);
                break;
            case MO:
                new MODashboard(user);
                break;
            case ADMIN:
                new AdminDashboard(user);
                break;
            default:
                showError("Unsupported role.");
                new LoginFrame();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }
}
