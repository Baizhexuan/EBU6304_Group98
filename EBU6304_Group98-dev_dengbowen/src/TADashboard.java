import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * TADashboard: Dashboard for Teaching Assistant users.
 *
 * Tab 1 - My Profile : Create / edit personal profile and record CV path.
 * Tab 2 - Browse Jobs: View all OPEN jobs and submit an application.
 * Tab 3 - My Applications: View submitted applications and their status.
 */
public class TADashboard extends JFrame {

    private final User currentUser;
    private JTabbedPane tabs;

    // Profile tab fields
    private JTextField nameField, emailField, studentIdField, skillsField, gpaField, cvPathField;

    // Browse-jobs tab
    private JTable jobTable;
    private DefaultTableModel jobModel;

    // My-applications tab
    private JTable appTable;
    private DefaultTableModel appModel;

    public TADashboard(User user) {
        this.currentUser = user;
        setTitle("TA Dashboard  –  " + user.username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 580);
        setLocationRelativeTo(null);

        // Logout menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Account");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> { dispose(); new LoginFrame(); });
        menu.add(logoutItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        tabs = new JTabbedPane();
        tabs.addTab("My Profile",       createProfilePanel());
        tabs.addTab("Browse Jobs",      createBrowsePanel());
        tabs.addTab("My Applications",  createApplicationsPanel());

        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 1) refreshJobTable();
            if (idx == 2) refreshAppTable();
        });

        add(tabs);
        loadProfileData();
        refreshJobTable();
        setVisible(true);
    }

    // ─── Profile Panel ────────────────────────────────────────
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        panel.add(headerLabel("Personal Profile"), labelGbc(gbc, 0, y++, 2));

        nameField      = addFormRow(panel, gbc, "Full Name:",             y++);
        emailField     = addFormRow(panel, gbc, "Email:",                 y++);
        studentIdField = addFormRow(panel, gbc, "Student ID:",            y++);
        skillsField    = addFormRow(panel, gbc, "Skills (semicolon-sep):", y++);
        gpaField       = addFormRow(panel, gbc, "GPA (0-4.0):",           y++);

        // CV row with Browse button
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel("CV File Path:"), gbc);
        JPanel cvRow = new JPanel(new BorderLayout(4, 0));
        cvPathField = new JTextField(18);
        JButton browseBtn = new JButton("Browse…");
        browseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                cvPathField.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
        cvRow.add(cvPathField, BorderLayout.CENTER);
        cvRow.add(browseBtn, BorderLayout.EAST);
        gbc.gridx = 1; gbc.gridy = y++;
        panel.add(cvRow, gbc);

        JButton saveBtn = new JButton("Save Profile");
        saveBtn.setPreferredSize(new Dimension(130, 30));
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill   = GridBagConstraints.NONE;
        panel.add(saveBtn, gbc);
        saveBtn.addActionListener(e -> saveProfile());

        return panel;
    }

    private void loadProfileData() {
        for (TAProfile p : FileStorage.loadProfiles()) {
            if (p.userId == currentUser.id) {
                nameField.setText(p.fullName);
                emailField.setText(p.email);
                studentIdField.setText(p.studentId);
                skillsField.setText(p.skills);
                gpaField.setText(String.valueOf(p.gpa));
                cvPathField.setText(p.cvPath);
                return;
            }
        }
    }

    private void saveProfile() {
        String name      = nameField.getText().trim();
        String email     = emailField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String skills    = skillsField.getText().trim();
        String cvPath    = cvPathField.getText().trim();
        double gpa       = 0.0;

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name and Email are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try { gpa = Double.parseDouble(gpaField.getText().trim()); } catch (NumberFormatException ignored) {}

        List<TAProfile> profiles = FileStorage.loadProfiles();
        TAProfile existing = null;
        int maxId = 0;
        for (TAProfile p : profiles) {
            if (p.id > maxId) maxId = p.id;
            if (p.userId == currentUser.id) existing = p;
        }

        if (existing != null) {
            existing.fullName  = name;
            existing.email     = email;
            existing.studentId = studentId;
            existing.skills    = skills;
            existing.gpa       = gpa;
            existing.cvPath    = cvPath;
        } else {
            TAProfile np = new TAProfile();
            np.id        = maxId + 1;
            np.userId    = currentUser.id;
            np.fullName  = name;
            np.email     = email;
            np.studentId = studentId;
            np.skills    = skills;
            np.gpa       = gpa;
            np.cvPath    = cvPath;
            profiles.add(np);
        }
        FileStorage.saveProfiles(profiles);
        JOptionPane.showMessageDialog(this, "Profile saved successfully!");
    }

    // ─── Browse Jobs Panel ────────────────────────────────────
    private JPanel createBrowsePanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] cols = {"ID", "Title", "Module", "Required Skills", "Max Hours/Week"};
        jobModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        jobTable = new JTable(jobModel);
        jobTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jobTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        panel.add(new JScrollPane(jobTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh");
        JButton applyBtn   = new JButton("Apply for Selected Job");
        south.add(refreshBtn);
        south.add(applyBtn);
        panel.add(south, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> refreshJobTable());
        applyBtn.addActionListener(e -> applyForJob());
        return panel;
    }

    private void refreshJobTable() {
        jobModel.setRowCount(0);
        for (Job j : FileStorage.loadJobs()) {
            if ("OPEN".equals(j.status)) {
                jobModel.addRow(new Object[]{j.id, j.title, j.module, j.requiredSkills, j.maxHours});
            }
        }
    }

    private void applyForJob() {
        int row = jobTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a job first.");
            return;
        }
        int jobId = (int) jobModel.getValueAt(row, 0);

        List<Application> apps = FileStorage.loadApplications();
        for (Application a : apps) {
            if (a.taId == currentUser.id && a.jobId == jobId) {
                JOptionPane.showMessageDialog(this, "You have already applied for this job.");
                return;
            }
        }

        int maxId = apps.stream().mapToInt(a -> a.id).max().orElse(0);
        Application na = new Application();
        na.id        = maxId + 1;
        na.taId      = currentUser.id;
        na.jobId     = jobId;
        na.status    = "PENDING";
        na.appliedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        apps.add(na);
        FileStorage.saveApplications(apps);

        JOptionPane.showMessageDialog(this, "Application submitted! Status: PENDING");
        refreshAppTable();
    }

    // ─── My Applications Panel ────────────────────────────────
    private JPanel createApplicationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] cols = {"App ID", "Job Title", "Module", "Status", "Applied At"};
        appModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        appTable = new JTable(appModel);
        appTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Colour-code the Status column
        appTable.setDefaultRenderer(Object.class, new StatusCellRenderer());
        panel.add(new JScrollPane(appTable), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refreshBtn);
        panel.add(south, BorderLayout.SOUTH);
        refreshBtn.addActionListener(e -> refreshAppTable());
        return panel;
    }

    private void refreshAppTable() {
        appModel.setRowCount(0);
        List<Application> apps = FileStorage.loadApplications();
        Map<Integer, Job> jobMap = new HashMap<>();
        for (Job j : FileStorage.loadJobs()) jobMap.put(j.id, j);

        for (Application a : apps) {
            if (a.taId == currentUser.id) {
                Job j = jobMap.get(a.jobId);
                appModel.addRow(new Object[]{
                        a.id,
                        j != null ? j.title  : "N/A",
                        j != null ? j.module : "N/A",
                        a.status,
                        a.appliedAt
                });
            }
        }
    }

    // ─── Helpers ─────────────────────────────────────────────
    private JTextField addFormRow(JPanel panel, GridBagConstraints gbc, String label, int y) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), gbc);
        JTextField field = new JTextField(20);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
        return field;
    }

    private JLabel headerLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        return lbl;
    }

    private GridBagConstraints labelGbc(GridBagConstraints gbc, int x, int y, int width) {
        GridBagConstraints g = (GridBagConstraints) gbc.clone();
        g.gridx = x; g.gridy = y; g.gridwidth = width;
        return g;
    }

    // Renderer to colour-code status values
    static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            if (!sel && col == 3) {
                String v = val != null ? val.toString() : "";
                switch (v) {
                    case "SELECTED": setForeground(new Color(0, 128, 0)); break;
                    case "REJECTED": setForeground(Color.RED);            break;
                    default:         setForeground(Color.ORANGE.darker()); break;
                }
            } else {
                setForeground(sel ? Color.WHITE : Color.BLACK);
            }
            return this;
        }
    }
}
