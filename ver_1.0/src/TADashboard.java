import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TADashboard extends JFrame {
    private final User currentUser;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField studentIdField;
    private JTextField skillsField;
    private JTextField gpaField;
    private JTextField cvPathField;
    private JTextField availabilityField;
    private JTextArea statementArea;
    private JTable jobsTable;
    private DefaultTableModel jobsModel;
    private JTable applicationsTable;
    private DefaultTableModel applicationsModel;

    public TADashboard(User currentUser) {
        this.currentUser = currentUser;
        setTitle("TA Dashboard - " + currentUser.getSafeDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 680);
        setLocationRelativeTo(null);

        JMenuBar bar = new JMenuBar();
        JMenu accountMenu = new JMenu("Account");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        accountMenu.add(logoutItem);
        bar.add(accountMenu);
        setJMenuBar(bar);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Profile", createProfilePanel());
        tabs.addTab("Browse Jobs", createBrowseJobsPanel());
        tabs.addTab("My Applications", createApplicationsPanel());
        tabs.addChangeListener(e -> {
            refreshJobs();
            refreshApplications();
        });

        add(tabs);
        loadProfile();
        refreshJobs();
        refreshApplications();
        setVisible(true);
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(24);
        emailField = new JTextField(24);
        studentIdField = new JTextField(24);
        skillsField = new JTextField(24);
        gpaField = new JTextField(24);
        cvPathField = new JTextField(20);
        availabilityField = new JTextField(24);
        statementArea = new JTextArea(5, 24);
        statementArea.setLineWrap(true);
        statementArea.setWrapStyleWord(true);

        addRow(panel, gbc, 0, "Full Name:", nameField);
        addRow(panel, gbc, 1, "Email:", emailField);
        addRow(panel, gbc, 2, "Student ID:", studentIdField);
        addRow(panel, gbc, 3, "Skills (semicolon separated):", skillsField);
        addRow(panel, gbc, 4, "GPA:", gpaField);
        addCvRow(panel, gbc, 5);
        addRow(panel, gbc, 6, "Availability:", availabilityField);

        gbc.gridx = 0;
        gbc.gridy = 7;
        panel.add(new JLabel("Personal Statement:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(statementArea), gbc);

        JButton saveButton = new JButton("Save Profile");
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        panel.add(saveButton, gbc);

        saveButton.addActionListener(e -> saveProfile());
        return panel;
    }

    private JPanel createBrowseJobsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        jobsModel = new DefaultTableModel(
                new String[] {"Job ID", "Title", "Module", "Skills", "Hours", "Location", "AI Match", "Summary"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jobsTable = new JTable(jobsModel);
        jobsTable.setRowHeight(24);
        jobsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(jobsTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        JButton applyButton = new JButton("Apply for Selected Job");
        buttons.add(refreshButton);
        buttons.add(applyButton);
        panel.add(buttons, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshJobs());
        applyButton.addActionListener(e -> applyForSelectedJob());
        return panel;
    }

    private JPanel createApplicationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        applicationsModel = new DefaultTableModel(
                new String[] {"App ID", "Job", "Module", "Status", "Applied At", "Match", "MO Note"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        applicationsTable = new JTable(applicationsModel);
        applicationsTable.setDefaultRenderer(Object.class, new StatusRenderer());
        applicationsTable.setRowHeight(24);
        panel.add(new JScrollPane(applicationsTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        JButton withdrawButton = new JButton("Withdraw Selected Pending Application");
        actions.add(refreshButton);
        actions.add(withdrawButton);
        panel.add(actions, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshApplications());
        withdrawButton.addActionListener(e -> withdrawSelectedApplication());
        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void addCvRow(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("CV Path:"), gbc);

        JPanel rowPanel = new JPanel(new BorderLayout(6, 6));
        rowPanel.add(cvPathField, BorderLayout.CENTER);
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> chooseCv());
        rowPanel.add(browseButton, BorderLayout.EAST);

        gbc.gridx = 1;
        panel.add(rowPanel, gbc);
    }

    private void chooseCv() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            cvPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void loadProfile() {
        TAProfile profile = FileStorage.findProfileByUserId(currentUser.id);
        if (profile == null) {
            return;
        }
        nameField.setText(profile.fullName);
        emailField.setText(profile.email);
        studentIdField.setText(profile.studentId);
        skillsField.setText(profile.skills);
        gpaField.setText(String.valueOf(profile.gpa));
        cvPathField.setText(profile.cvPath);
        availabilityField.setText(profile.availability);
        statementArea.setText(profile.statement);
    }

    private void saveProfile() {
        if (ValidationUtils.isBlank(nameField.getText()) || ValidationUtils.isBlank(emailField.getText())
                || ValidationUtils.isBlank(studentIdField.getText()) || ValidationUtils.isBlank(skillsField.getText())) {
            JOptionPane.showMessageDialog(this, "Name, email, student ID and skills are required.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ValidationUtils.isEmail(emailField.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        double gpa = ValidationUtils.parseDouble(gpaField.getText(), 0.0);
        if (gpa < 0.0 || gpa > 4.0) {
            JOptionPane.showMessageDialog(this, "GPA should be between 0.0 and 4.0.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<TAProfile> profiles = FileStorage.loadProfiles();
        TAProfile profile = FileStorage.findProfileByUserId(currentUser.id);
        if (profile == null) {
            profile = new TAProfile();
            profile.id = FileStorage.nextProfileId();
            profile.userId = currentUser.id;
            profiles.add(profile);
        }

        profile.fullName = nameField.getText().trim();
        profile.email = emailField.getText().trim();
        profile.studentId = studentIdField.getText().trim();
        profile.skills = skillsField.getText().trim();
        profile.gpa = gpa;
        profile.cvPath = cvPathField.getText().trim();
        profile.availability = availabilityField.getText().trim();
        profile.statement = statementArea.getText().trim();
        FileStorage.saveProfiles(profiles);
        syncDisplayName(profile.fullName);

        JOptionPane.showMessageDialog(this, "Profile saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        refreshJobs();
    }

    private void syncDisplayName(String displayName) {
        List<User> users = FileStorage.loadUsers();
        for (User user : users) {
            if (user.id == currentUser.id) {
                user.displayName = displayName;
                currentUser.displayName = displayName;
                break;
            }
        }
        FileStorage.saveUsers(users);
    }

    private void refreshJobs() {
        jobsModel.setRowCount(0);
        TAProfile profile = FileStorage.findProfileByUserId(currentUser.id);
        for (Job job : FileStorage.loadJobs()) {
            if (!job.isOpen()) {
                continue;
            }
            MatchResult match = MatchingService.evaluate(profile, job);
            jobsModel.addRow(new Object[] {
                    job.id,
                    job.title,
                    job.module,
                    job.requiredSkills,
                    job.maxHours,
                    job.location,
                    match.score + "%",
                    match.summary
            });
        }
    }

    private void applyForSelectedJob() {
        int row = jobsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a job first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        TAProfile profile = FileStorage.findProfileByUserId(currentUser.id);
        if (profile == null || !profile.isComplete()) {
            JOptionPane.showMessageDialog(this,
                    "Please complete your profile before applying. This matches the project requirement for TA profile creation.",
                    "Profile Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int jobId = Integer.parseInt(String.valueOf(jobsModel.getValueAt(row, 0)));
        List<Application> applications = FileStorage.loadApplications();
        for (Application app : applications) {
            if (app.taId == currentUser.id && app.jobId == jobId && !"WITHDRAWN".equalsIgnoreCase(app.status)) {
                JOptionPane.showMessageDialog(this, "You have already applied for this job.", "Duplicate Application",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Job job = FileStorage.findJobById(jobId);
        MatchResult match = MatchingService.evaluate(profile, job);

        Application app = new Application();
        app.id = FileStorage.nextApplicationId();
        app.taId = currentUser.id;
        app.jobId = jobId;
        app.status = "PENDING";
        app.appliedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        app.matchScore = match.score;
        app.matchSummary = match.summary;
        app.reviewerNote = "Awaiting MO review.";
        applications.add(app);
        FileStorage.saveApplications(applications);

        JOptionPane.showMessageDialog(this,
                "Application submitted with AI match score: " + match.score + "%.",
                "Application Submitted", JOptionPane.INFORMATION_MESSAGE);
        refreshApplications();
    }

    private void refreshApplications() {
        applicationsModel.setRowCount(0);
        for (Application app : FileStorage.loadApplications()) {
            if (app.taId != currentUser.id) {
                continue;
            }
            Job job = FileStorage.findJobById(app.jobId);
            applicationsModel.addRow(new Object[] {
                    app.id,
                    job == null ? "Unknown" : job.title,
                    job == null ? "Unknown" : job.module,
                    app.status,
                    app.appliedAt,
                    app.matchScore + "%",
                    app.reviewerNote
            });
        }
    }

    private void withdrawSelectedApplication() {
        int row = applicationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an application first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int appId = Integer.parseInt(String.valueOf(applicationsModel.getValueAt(row, 0)));
        String status = String.valueOf(applicationsModel.getValueAt(row, 3));
        if (!"PENDING".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Only pending applications can be withdrawn.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Application> applications = FileStorage.loadApplications();
        for (Application app : applications) {
            if (app.id == appId) {
                app.status = "WITHDRAWN";
                app.reviewerNote = "Withdrawn by TA.";
                break;
            }
        }
        FileStorage.saveApplications(applications);
        refreshApplications();
    }

    private void logout() {
        dispose();
        new LoginFrame();
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            java.awt.Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            if (!isSelected) {
                String status = String.valueOf(table.getValueAt(row, 3));
                if ("SELECTED".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(214, 245, 214));
                } else if ("REJECTED".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(250, 220, 220));
                } else if ("WITHDRAWN".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(232, 232, 232));
                } else {
                    component.setBackground(new Color(255, 249, 214));
                }
            }
            return component;
        }
    }
}
