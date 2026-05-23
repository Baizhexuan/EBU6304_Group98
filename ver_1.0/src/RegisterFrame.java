import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

/**
 * Registration frame for creating additional TA or MO demo accounts.
 */
public class RegisterFrame extends JFrame {
    private final LoginFrame loginFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField displayNameField;
    private JComboBox<String> roleBox;

    /**
     * Creates the registration portal.
     *
     * @param loginFrame login frame to update after registration
     */
    public RegisterFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
        setTitle("Create Demo Account");
        setMinimumSize(new Dimension(760, 560));
        setSize(840, 610);
        setLocationRelativeTo(loginFrame);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BaseDashboard.APP_BACKGROUND);
        root.add(BaseDashboard.buildPortalHeader("Account Registration", this::applyCurrentLanguage),
                BorderLayout.NORTH);

        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setBackground(BaseDashboard.APP_BACKGROUND);
        workspace.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JPanel card = new JPanel(new BorderLayout(0, 18));
        card.setBackground(BaseDashboard.SURFACE_COLOR);
        card.setPreferredSize(new Dimension(620, 470));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BaseDashboard.BORDER_COLOR),
                BorderFactory.createEmptyBorder(26, 28, 26, 28)));
        card.add(buildHeader(), BorderLayout.NORTH);
        card.add(buildForm(), BorderLayout.CENTER);

        GridBagConstraints place = new GridBagConstraints();
        workspace.add(card, place);
        root.add(workspace, BorderLayout.CENTER);
        add(root);
        applyCurrentLanguage();
        setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("SansSerif", Font.BOLD, 21));
        title.setForeground(new Color(27, 45, 65));
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Register a TA or MO account for the recruitment portal.");
        subtitle.setFont(BaseDashboard.UI_BODY_FONT);
        subtitle.setForeground(BaseDashboard.TEXT_MUTED);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(7));
        header.add(subtitle);
        return header;
    }

    private JPanel buildForm() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 0, 7, 10);
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
        roleBox.setPreferredSize(new Dimension(280, 34));

        addRow(form, gbc, 0, "Username", usernameField);
        addRow(form, gbc, 1, "Password", passwordField);
        addRow(form, gbc, 2, "Confirm Password", confirmPasswordField);
        addRow(form, gbc, 3, "Display Name", displayNameField);
        addRow(form, gbc, 4, "Role", roleBox);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton registerButton = new JButton("Create Account");
        JButton cancelButton = new JButton("Cancel");
        BaseDashboard.applyButtonStyle(registerButton, BaseDashboard.ACCENT_COLOR, Color.WHITE);
        BaseDashboard.applyButtonStyle(cancelButton, BaseDashboard.SECONDARY_SURFACE, BaseDashboard.ACCENT_COLOR);
        actions.add(cancelButton);
        actions.add(registerButton);

        JLabel footer = new JLabel("After registration, sign in and continue from the role dashboard.");
        footer.setFont(BaseDashboard.UI_BODY_FONT);
        footer.setForeground(BaseDashboard.TEXT_MUTED);

        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        bottom.add(footer, BorderLayout.NORTH);
        bottom.add(actions, BorderLayout.SOUTH);
        wrapper.add(form, BorderLayout.CENTER);
        wrapper.add(bottom, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> registerUser());
        cancelButton.addActionListener(e -> dispose());
        return wrapper;
    }

    private void applyCurrentLanguage() {
        setTitle(I18n.t("Create Demo Account"));
        I18n.applyTo(this);
    }

    private void configureField(java.awt.Component field) {
        field.setPreferredSize(new Dimension(280, 34));
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
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
            JOptionPane.showMessageDialog(this, I18n.t("All fields are required."), I18n.t("Validation"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, I18n.t("Password confirmation does not match."), I18n.t("Validation"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<User> users = FileStorage.loadUsers();
        for (User user : users) {
            if (user.username.equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(this, I18n.t("Username already exists."), I18n.t("Validation"),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        User user = new User(FileStorage.nextUserId(), username, password, role, displayName);
        users.add(user);
        FileStorage.saveUsers(users);

        JOptionPane.showMessageDialog(this,
                I18n.t("Account created. Please sign in and complete the remaining workflow in the dashboard."),
                I18n.t("Registration Complete"), JOptionPane.INFORMATION_MESSAGE);
        loginFrame.prefillCredentials(username);
        dispose();
    }
}
