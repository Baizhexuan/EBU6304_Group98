import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
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
        setTitle(DemoMetadata.APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 620));
        setSize(1040, 640);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(243, 239, 230));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel shell = new JPanel(new GridLayout(1, 2, 18, 18));
        shell.setOpaque(false);

        HeroPanel heroPanel = new HeroPanel();
        heroPanel.setBorder(BorderFactory.createEmptyBorder(34, 34, 34, 34));
        heroPanel.setLayout(new BorderLayout(16, 16));

        JLabel eyebrow = new JLabel("BUPT International School");
        eyebrow.setForeground(new Color(217, 232, 239));
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 15));

        JLabel title = new JLabel("Teaching Assistant Recruitment");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 34));

        JLabel subtitle = new JLabel(
                "<html><div style='width:360px;'>A simple recruitment workspace for applicants, module organisers, and administrators.</div></html>");
        subtitle.setForeground(new Color(236, 244, 247));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JPanel headline = new JPanel();
        headline.setOpaque(false);
        headline.setLayout(new BoxLayout(headline, BoxLayout.Y_AXIS));
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        headline.add(eyebrow);
        headline.add(Box.createVerticalStrut(10));
        headline.add(title);
        headline.add(Box.createVerticalStrut(12));
        headline.add(subtitle);
        heroPanel.add(headline, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        JLabel flowTitle = new JLabel("Core workflow");
        flowTitle.setForeground(Color.WHITE);
        flowTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        flowTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(flowTitle);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(buildFeatureLine("1. TA creates a profile and applies for open jobs"));
        centerPanel.add(Box.createVerticalStrut(8));
        centerPanel.add(buildFeatureLine("2. MO reviews applicants and updates outcomes"));
        centerPanel.add(Box.createVerticalStrut(8));
        centerPanel.add(buildFeatureLine("3. Admin monitors workload and recommendations"));
        heroPanel.add(centerPanel, BorderLayout.CENTER);

        JLabel accounts = new JLabel(
                "<html><div style='width:360px;'>Demo accounts: admin/admin123, ta1/ta123, ta2/ta456, mo1/mo123, mo2/mo456</div></html>");
        accounts.setForeground(new Color(223, 237, 242));
        accounts.setFont(new Font("SansSerif", Font.PLAIN, 13));
        heroPanel.add(accounts, BorderLayout.SOUTH);

        JPanel card = new JPanel(new BorderLayout(16, 16));
        card.setBackground(new Color(255, 252, 247));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 228)),
                BorderFactory.createEmptyBorder(34, 34, 34, 34)));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel signIn = new JLabel("Sign In", SwingConstants.LEFT);
        signIn.setFont(new Font("SansSerif", Font.BOLD, 28));
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Access your TA recruitment workspace");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sub.setForeground(new Color(88, 96, 102));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(signIn);
        header.add(Box.createVerticalStrut(8));
        header.add(sub);
        card.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Username"), gbc);
        usernameField = new JTextField(22);
        usernameField.setPreferredSize(new Dimension(260, 38));
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(new JLabel("Password"), gbc);
        passwordField = new JPasswordField(22);
        passwordField.setPreferredSize(new Dimension(260, 38));
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(passwordField, gbc);

        JPanel buttonRow = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonRow.setOpaque(false);
        JButton loginButton = new JButton("Log in");
        JButton registerButton = new JButton("Register");
        JButton aboutButton = new JButton("About");
        styleButton(loginButton, new Color(33, 76, 95), Color.WHITE);
        styleButton(registerButton, new Color(255, 252, 247), new Color(33, 76, 95));
        styleButton(aboutButton, new Color(255, 252, 247), new Color(70, 56, 32));
        buttonRow.add(loginButton);
        buttonRow.add(registerButton);
        buttonRow.add(aboutButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        form.add(buttonRow, gbc);
        card.add(form, BorderLayout.CENTER);

        JLabel hint = new JLabel("Use a demo account above or create a TA/MO account to explore the flow.",
                SwingConstants.LEFT);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 13));
        hint.setForeground(new Color(88, 96, 102));
        card.add(hint, BorderLayout.SOUTH);

        shell.add(heroPanel);
        shell.add(card);
        root.add(shell, BorderLayout.CENTER);
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

    private JLabel buildFeatureLine(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(241, 247, 249));
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setOpaque(true);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(foreground.equals(Color.WHITE) ? new Color(23, 55, 69) : new Color(154, 170, 178)),
                BorderFactory.createEmptyBorder(11, 14, 11, 14)));
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
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
                    new Color(89, 126, 138));
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 28));
            g2.fillOval(getWidth() - 220, 36, 170, 170);
            g2.fillOval(40, getHeight() - 170, 210, 210);
            g2.fillOval(getWidth() / 2 - 60, getHeight() / 2 - 120, 120, 120);
            g2.dispose();
        }
    }
}
