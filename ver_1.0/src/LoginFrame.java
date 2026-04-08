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
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle(DemoMetadata.APP_TITLE + " - " + DemoMetadata.VERSION_LABEL);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 320);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        JLabel title = new JLabel("BUPT International School", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        JLabel subtitle = new JLabel(DemoMetadata.APP_SUBTITLE + " (" + DemoMetadata.VERSION_LABEL + ")", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
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
        usernameField = new JTextField(18);
        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(18);
        gbc.gridx = 1;
        form.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register Demo Account");
        JButton aboutButton = new JButton("About");
        JPanel buttonRow = new JPanel();
        buttonRow.add(loginButton);
        buttonRow.add(registerButton);
        buttonRow.add(aboutButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        form.add(buttonRow, gbc);

        root.add(form, BorderLayout.CENTER);

        JLabel hint = new JLabel(
                "<html><center>Demo accounts: admin/admin123 | ta1/ta123 | ta2/ta456 | mo1/mo123 | mo2/mo456</center></html>",
                SwingConstants.CENTER);
        hint.setPreferredSize(new Dimension(420, 45));
        root.add(hint, BorderLayout.SOUTH);

        add(root);

        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> new RegisterFrame(this));
        aboutButton.addActionListener(e -> JOptionPane.showMessageDialog(this,
                DemoMetadata.buildAboutMessage(),
                "About This Demo",
                JOptionPane.INFORMATION_MESSAGE));
        passwordField.addActionListener(e -> attemptLogin());

        setVisible(true);
    }

    public void prefillCredentials(String username) {
        usernameField.setText(username);
        passwordField.setText("");
        usernameField.requestFocusInWindow();
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (ValidationUtils.isBlank(username) || ValidationUtils.isBlank(password)) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Missing Input",
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
            JOptionPane.showMessageDialog(this, "Username not found.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!matched.password.equals(password)) {
            JOptionPane.showMessageDialog(this, "Password is incorrect.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            return;
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
        JOptionPane.showMessageDialog(this, "Unknown role: " + user.role, "Error", JOptionPane.ERROR_MESSAGE);
        new LoginFrame();
    }
}
