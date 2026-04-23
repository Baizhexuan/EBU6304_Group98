import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
        setSize(760, 430);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(new Color(243, 239, 230));

        HeroPanel heroPanel = new HeroPanel();
        heroPanel.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));
        heroPanel.setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("BUPT International School");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 28));
        JLabel subtitle = new JLabel("TA Recruitment Demo Workspace");
        subtitle.setForeground(new Color(230, 241, 246));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        JLabel version = new JLabel(DemoMetadata.VERSION_LABEL + "  |  " + DemoMetadata.APP_SUBTITLE);
        version.setForeground(new Color(210, 230, 236));
        version.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel headline = new JPanel(new GridLayout(3, 1, 0, 8));
        headline.setOpaque(false);
        headline.add(title);
        headline.add(subtitle);
        headline.add(version);
        heroPanel.add(headline, BorderLayout.NORTH);

        JLabel note = new JLabel(
                "<html><div style='width:280px;'>Admin can now monitor workload risk, review replacement recommendations, and switch to a live AI placeholder provider when API credentials are available.</div></html>");
        note.setForeground(new Color(245, 247, 248));
        note.setFont(new Font("SansSerif", Font.PLAIN, 14));
        heroPanel.add(note, BorderLayout.CENTER);

        String readinessText = "<html><div style='width:300px;'>" + AIIntegrationPlan.buildReadinessSummary() + "</div></html>";
        JLabel readiness = new JLabel(readinessText);
        readiness.setForeground(new Color(222, 238, 242));
        readiness.setFont(new Font("SansSerif", Font.PLAIN, 12));
        heroPanel.add(readiness, BorderLayout.SOUTH);

        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(new Color(255, 252, 247));
        card.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));

        JLabel signIn = new JLabel("Sign In", SwingConstants.LEFT);
        signIn.setFont(new Font("SansSerif", Font.BOLD, 24));
        card.add(signIn, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
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
        styleButton(loginButton, new Color(33, 76, 95), Color.WHITE);
        styleButton(registerButton, new Color(225, 234, 238), new Color(33, 76, 95));
        styleButton(aboutButton, new Color(240, 229, 206), new Color(70, 56, 32));

        JPanel buttonRow = new JPanel();
        buttonRow.setOpaque(false);
        buttonRow.add(loginButton);
        buttonRow.add(registerButton);
        buttonRow.add(aboutButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        form.add(buttonRow, gbc);
        card.add(form, BorderLayout.CENTER);

        JLabel hint = new JLabel(
                "<html><center>Demo accounts: admin/admin123 | ta1/ta123 | ta2/ta456 | mo1/mo123 | mo2/mo456</center></html>",
                SwingConstants.CENTER);
        hint.setPreferredSize(new Dimension(300, 45));
        card.add(hint, BorderLayout.SOUTH);

        root.add(heroPanel);
        root.add(card);
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

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    public void prefillCredentials(String username) {
        usernameField.setText(username);
        passwordField.setText("");
        usernameField.requestFocusInWindow();
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (ValidationUtils.isBlank(username) && ValidationUtils.isBlank(password)) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Missing Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ValidationUtils.isBlank(username)) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Missing Username",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ValidationUtils.isBlank(password)) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Missing Password",
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

    private static class HeroPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            GradientPaint gradient = new GradientPaint(0, 0, new Color(25, 74, 92), getWidth(), getHeight(),
                    new Color(84, 122, 136));
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 35));
            g2.fillOval(getWidth() - 180, 30, 140, 140);
            g2.fillOval(30, getHeight() - 140, 180, 180);
            g2.dispose();
        }
    }
}
