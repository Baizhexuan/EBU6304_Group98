import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * LoginFrame: The application entry screen.
 * Users log in with username + password; the system routes them to the
 * appropriate dashboard based on their role (TA, MO, ADMIN).
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("TA Recruitment System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 280);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title banner
        JLabel title = new JLabel("  BUPT International School", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JLabel subtitle = new JLabel("Teaching Assistant Recruitment System", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(Color.DARK_GRAY);

        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(title);
        topPanel.add(subtitle);
        outer.add(topPanel, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(16);
        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(16);
        gbc.gridx = 1;
        form.add(passwordField, gbc);

        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        form.add(loginBtn, gbc);

        outer.add(form, BorderLayout.CENTER);

        // Demo accounts hint
        JLabel hint = new JLabel(
                "<html><center><font color='gray' size='2'>"
                + "Demo accounts — TA: ta1/ta123 | MO: mo1/mo123 | Admin: admin/admin123"
                + "</font></center></html>", SwingConstants.CENTER);
        outer.add(hint, BorderLayout.SOUTH);

        add(outer);

        loginBtn.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());

        setVisible(true);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<User> users = FileStorage.loadUsers();
        for (User u : users) {
            if (u.username.equals(username) && u.password.equals(password)) {
                dispose();
                openDashboard(u);
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Invalid username or password.",
                "Login Failed", JOptionPane.ERROR_MESSAGE);
        passwordField.setText("");
    }

    private void openDashboard(User user) {
        switch (user.role) {
            case "TA":
                new TADashboard(user);
                break;
            case "MO":
                new MODashboard(user);
                break;
            case "ADMIN":
                new AdminDashboard(user);
                break;
            default:
                JOptionPane.showMessageDialog(null, "Unknown role: " + user.role);
                new LoginFrame();
        }
    }
}
