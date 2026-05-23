package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.PasswordUtil;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class RegisterFrame extends JFrame {
    private final LoginFrame loginFrame;
    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JPasswordField confirmField = new JPasswordField(16);
    private final JComboBox<User.UserRole> roleComboBox = new JComboBox<>(new User.UserRole[] {User.UserRole.TA, User.UserRole.MO});
    private final CsvStorage<User> userStorage = new CsvStorage<>("data/users.csv", User::fromCsvRow);

    public RegisterFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
        setTitle("Register New Account");
        setSize(420, 260);
        setLocationRelativeTo(loginFrame);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gbc, 0, "Username:", usernameField);
        addRow(form, gbc, 1, "Password:", passwordField);
        addRow(form, gbc, 2, "Confirm Password:", confirmField);
        addRow(form, gbc, 3, "Role:", roleComboBox);

        JButton createButton = new JButton("Create Account");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        form.add(createButton, gbc);
        createButton.addActionListener(e -> register());

        root.add(form, BorderLayout.CENTER);
        add(root);
        setVisible(true);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirm = new String(confirmField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showWarning("All fields are required.");
            return;
        }
        if (!password.equals(confirm)) {
            showWarning("Password confirmation does not match.");
            return;
        }

        List<User> users = userStorage.loadAll();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                showWarning("Username already exists.");
                return;
            }
        }

        String salt = PasswordUtil.generateSalt();
        User newUser = new User(java.util.UUID.randomUUID().toString(), username,
                PasswordUtil.hashPassword(password, salt), salt,
                (User.UserRole) roleComboBox.getSelectedItem());
        users.add(newUser);
        userStorage.saveAll(users);

        JOptionPane.showMessageDialog(this, "Registration successful. You can log in immediately.", "Success", JOptionPane.INFORMATION_MESSAGE);
        loginFrame.prefillUsername(username);
        dispose();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Registration Error", JOptionPane.WARNING_MESSAGE);
    }
}
