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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Entry screen for the stand-alone recruitment system.
 *
 * <p>The portal uses the same compact visual structure as the submitted
 * prototype: a school header bar, a neutral workspace, and a focused
 * sign-in panel for TA, MO, and Admin users.</p>
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    /**
     * Constructs and displays the login frame.
     */
    public LoginFrame() {
        setTitle(DemoMetadata.APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 520));
        setSize(860, 580);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BaseDashboard.APP_BACKGROUND);
        root.add(BaseDashboard.buildPortalHeader("TA Portal  |  MO Dashboard  |  Admin Workload",
                this::applyCurrentLanguage),
                BorderLayout.NORTH);

        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setBackground(BaseDashboard.APP_BACKGROUND);
        workspace.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel card = new JPanel(new BorderLayout(0, 18));
        card.setBackground(BaseDashboard.SURFACE_COLOR);
        card.setPreferredSize(new Dimension(620, 410));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BaseDashboard.BORDER_COLOR),
                BorderFactory.createEmptyBorder(28, 30, 28, 30)));
        card.add(buildHeader(), BorderLayout.NORTH);
        card.add(buildSignInPanel(), BorderLayout.CENTER);

        GridBagConstraints place = new GridBagConstraints();
        place.gridx = 0;
        place.gridy = 0;
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

        JLabel eyebrow = new JLabel("BUPT TA Recruitment System");
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 12));
        eyebrow.setForeground(BaseDashboard.ACCENT_COLOR);
        eyebrow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("Sign In");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(27, 45, 65));
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel summary = new JLabel("Access the applicant, module organiser, or administrator workspace.");
        summary.setFont(BaseDashboard.UI_BODY_FONT);
        summary.setForeground(BaseDashboard.TEXT_MUTED);
        summary.setAlignmentX(LEFT_ALIGNMENT);

        header.add(eyebrow);
        header.add(Box.createVerticalStrut(7));
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(summary);
        return header;
    }

    private JPanel buildSignInPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 0, 7, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        usernameField = new JTextField(22);
        passwordField = new JPasswordField(22);
        usernameField.setPreferredSize(new Dimension(260, 34));
        passwordField.setPreferredSize(new Dimension(260, 34));

        addFormRow(form, gbc, 0, "Username", usernameField);
        addFormRow(form, gbc, 1, "Password", passwordField);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton loginButton = new JButton("Log In");
        JButton registerButton = new JButton("Register");
        JButton aboutButton = new JButton("About");
        BaseDashboard.applyButtonStyle(loginButton, BaseDashboard.ACCENT_COLOR, Color.WHITE);
        BaseDashboard.applyButtonStyle(registerButton, BaseDashboard.SOFT_ACCENT, BaseDashboard.ACCENT_COLOR);
        BaseDashboard.applyButtonStyle(aboutButton, BaseDashboard.SECONDARY_SURFACE, BaseDashboard.ACCENT_COLOR);
        actions.add(loginButton);
        actions.add(registerButton);
        actions.add(aboutButton);

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        JLabel hint = new JLabel("Sign in with your registered account or create a TA/MO account.");
        hint.setFont(BaseDashboard.UI_BODY_FONT);
        hint.setForeground(BaseDashboard.TEXT_MUTED);
        hint.setAlignmentX(LEFT_ALIGNMENT);
        footer.add(actions);
        footer.add(Box.createVerticalStrut(10));
        footer.add(hint);

        panel.add(form, BorderLayout.NORTH);
        panel.add(footer, BorderLayout.CENTER);

        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> new RegisterFrame(this));
        aboutButton.addActionListener(e -> JOptionPane.showMessageDialog(this,
                I18n.t(DemoMetadata.buildAboutMessage()),
                I18n.t(DemoMetadata.AI_MATCH_HELP_TITLE),
                JOptionPane.INFORMATION_MESSAGE));
        passwordField.addActionListener(e -> attemptLogin());
        return panel;
    }

    private void applyCurrentLanguage() {
        setTitle(I18n.t(DemoMetadata.APP_TITLE));
        I18n.applyTo(this);
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
    }

    /**
     * Pre-fills the username after a successful registration.
     *
     * @param username registered username
     */
    public void prefillCredentials(String username) {
        usernameField.setText(username);
        passwordField.setText("");
        usernameField.requestFocusInWindow();
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (ValidationUtils.isBlank(username) && ValidationUtils.isBlank(password)) {
            JOptionPane.showMessageDialog(this, I18n.t("Please enter both username and password."),
                    I18n.t("Missing Input"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ValidationUtils.isBlank(username)) {
            JOptionPane.showMessageDialog(this, I18n.t("Username cannot be empty."), I18n.t("Missing Username"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ValidationUtils.isBlank(password)) {
            JOptionPane.showMessageDialog(this, I18n.t("Password cannot be empty."), I18n.t("Missing Password"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<User> users = FileStorage.loadUsers();
        User matched = null;
        for (User user : users) {
            if (user.username.equalsIgnoreCase(username)) {
                matched = user;
                break;
            }
        }

        if (matched == null) {
            JOptionPane.showMessageDialog(this, I18n.t("Username not found."), I18n.t("Login Failed"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!PasswordService.verifyPassword(password, matched.password)) {
            JOptionPane.showMessageDialog(this, I18n.t("Password is incorrect."), I18n.t("Login Failed"),
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            return;
        }

        if (!PasswordService.isHashed(matched.password)) {
            for (User user : users) {
                if (user.id == matched.id) {
                    user.password = PasswordService.hashPassword(password);
                    matched.password = user.password;
                    break;
                }
            }
            FileStorage.saveUsers(users);
        }

        dispose();
        openDashboard(matched);
    }

    private void openDashboard(User user) {
        if ("TA".equalsIgnoreCase(user.role)) {
            new TADashboard(user);
            return;
        }
        if ("MO".equalsIgnoreCase(user.role)) {
            new MODashboard(user);
            return;
        }
        if ("ADMIN".equalsIgnoreCase(user.role)) {
            new AdminDashboard(user);
            return;
        }
        JOptionPane.showMessageDialog(this, I18n.t("Unknown role:") + " " + user.role, I18n.t("Error"),
                JOptionPane.ERROR_MESSAGE);
        new LoginFrame();
    }
}
