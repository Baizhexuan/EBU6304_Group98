import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicButtonUI;

public class RegisterFrame extends JFrame {
    private final LoginFrame loginFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField displayNameField;
    private JComboBox<String> roleBox;

    public RegisterFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
        setTitle("Create Demo Account");
        setMinimumSize(new Dimension(820, 580));
        setSize(860, 640);
        setLocationRelativeTo(loginFrame);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 242, 235));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel card = new JPanel(new BorderLayout(16, 16));
        card.setBackground(new Color(255, 252, 247));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 228)),
                BorderFactory.createEmptyBorder(28, 30, 28, 30)));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel(
                "Register a TA or MO account, then complete the remaining workflow inside the dashboard.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(88, 96, 102));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);
        card.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        usernameField = new JTextField(24);
        passwordField = new JPasswordField(24);
        confirmPasswordField = new JPasswordField(24);
        displayNameField = new JTextField(24);
        roleBox = new JComboBox<String>(new String[] {"TA", "MO"});

        configureField(usernameField);
        configureField(passwordField);
        configureField(confirmPasswordField);
        configureField(displayNameField);
        roleBox.setPreferredSize(new Dimension(280, 40));

        addRow(form, gbc, 0, "Username", usernameField);
        addRow(form, gbc, 1, "Password", passwordField);
        addRow(form, gbc, 2, "Confirm Password", confirmPasswordField);
        addRow(form, gbc, 3, "Display Name", displayNameField);
        addRow(form, gbc, 4, "Role", roleBox);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        JButton registerButton = new JButton("Create Account");
        JButton cancelButton = new JButton("Cancel");
        styleButton(registerButton, new Color(33, 76, 95), Color.WHITE);
        styleButton(cancelButton, new Color(225, 234, 238), new Color(33, 76, 95));
        actions.add(registerButton);
        actions.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        form.add(actions, gbc);
        card.add(form, BorderLayout.CENTER);

        JLabel footer = new JLabel("After registration, sign in and complete the profile or job workflow.");
        footer.setFont(new Font("SansSerif", Font.PLAIN, 13));
        footer.setForeground(new Color(88, 96, 102));
        card.add(footer, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> registerUser());
        cancelButton.addActionListener(e -> dispose());
        root.add(card, BorderLayout.CENTER);
        add(root);
        setVisible(true);
    }

    private void configureField(java.awt.Component field) {
        field.setPreferredSize(new Dimension(280, 40));
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setUI(new BasicButtonUI());
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(foreground.equals(Color.WHITE) ? new Color(23, 55, 69) : new Color(154, 170, 178)),
                BorderFactory.createEmptyBorder(11, 14, 11, 14)));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
        String displayName = displayNameField.getText().trim();
        String role = String.valueOf(roleBox.getSelectedItem());

        if (ValidationUtils.isBlank(username) || ValidationUtils.isBlank(password)
                || ValidationUtils.isBlank(confirmPassword) || ValidationUtils.isBlank(displayName)) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Password confirmation does not match.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<User> users = FileStorage.loadUsers();
        for (User user : users) {
            if (user.username.equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        User user = new User(FileStorage.nextUserId(), username, password, role, displayName);
        users.add(user);
        FileStorage.saveUsers(users);

        JOptionPane.showMessageDialog(this,
                "Account created. Please sign in and complete the remaining workflow in the dashboard.",
                "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
        loginFrame.prefillCredentials(username);
        dispose();
    }
}
