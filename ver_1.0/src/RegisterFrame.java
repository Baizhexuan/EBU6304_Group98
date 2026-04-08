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
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField displayNameField;
    private JComboBox<String> roleBox;

    public RegisterFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
        setTitle("Register Demo Account");
        setSize(420, 260);
        setLocationRelativeTo(loginFrame);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        usernameField = new JTextField(16);
        passwordField = new JPasswordField(16);
        displayNameField = new JTextField(16);
        roleBox = new JComboBox<String>(new String[] {"TA", "MO"});

        addRow(form, gbc, 0, "Username:", usernameField);
        addRow(form, gbc, 1, "Password:", passwordField);
        addRow(form, gbc, 2, "Display Name:", displayNameField);
        addRow(form, gbc, 3, "Role:", roleBox);

        JButton registerButton = new JButton("Create Account");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        form.add(registerButton, gbc);

        registerButton.addActionListener(e -> registerUser());

        root.add(form, BorderLayout.CENTER);
        add(root);
        setVisible(true);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        form.add(field, gbc);
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String displayName = displayNameField.getText().trim();
        String role = String.valueOf(roleBox.getSelectedItem());

        if (ValidationUtils.isBlank(username) || ValidationUtils.isBlank(password) || ValidationUtils.isBlank(displayName)) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
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
                "Account created. Please log in and complete the rest of the profile in the dashboard.",
                "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
        loginFrame.prefillCredentials(username);
        dispose();
    }
}
