import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TADashboard extends BaseDashboard {
    private JTextField nameField;
    private JTextField emailField;
    private JTextField studentIdField;
    private JTextField skillsField;
    private JTextField gpaField;
    private JTextField cvPathField;
    private JTextField availabilityField;
    private JTextArea statementArea;
    private JLabel profileStatusLabel;

    private JTable jobsTable;
    private DefaultTableModel jobsModel;
    private JTextField jobTitleFilterField;
    private JTextField jobModuleFilterField;
    private JTextField jobSkillsFilterField;
    private JTextField jobLocationFilterField;
    private JLabel jobInsightLabel;

    private JTable applicationsTable;
    private DefaultTableModel applicationsModel;
    private JTextField applicationJobFilterField;
    private JTextField applicationModuleFilterField;
    private JTextField applicationStatusFilterField;
    private JLabel applicationInsightLabel;

    private JTable notificationsTable;
    private DefaultTableModel notificationsModel;
    private JLabel notificationSummaryLabel;

    public TADashboard(User currentUser) {
        super(currentUser, "TA Dashboard", 1100, 760);
        addTab("My Profile", createProfilePanel());
        addTab("Browse Jobs", createBrowseJobsPanel());
        addTab("My Applications", createApplicationsPanel());
        addTab("Notifications", createNotificationsPanel());
        installRefreshOnTabSwitch(() -> {
            refreshJobs();
            refreshApplications();
            refreshNotifications();
        });
        loadProfile();
        refreshJobs();
        refreshApplications();
        refreshNotifications();
        setVisible(true);
    }

    private JPanel createProfilePanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(APP_BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        content.add(buildSectionIntro(
                "Profile Completion",
                "Keep your TA profile complete before applying. This demo checks email format, GPA range, CV path, availability, and a short statement so the next AI matching stage has richer context to work with."));
        content.add(Box.createVerticalStrut(12));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(SURFACE_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 224, 228)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

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

        addRow(formPanel, gbc, 0, "Full Name:", nameField);
        addRow(formPanel, gbc, 1, "Email:", emailField);
        addRow(formPanel, gbc, 2, "Student ID:", studentIdField);
        addRow(formPanel, gbc, 3, "Skills (semicolon separated):", skillsField);
        addRow(formPanel, gbc, 4, "GPA:", gpaField);
        addCvRow(formPanel, gbc, 5);
        addRow(formPanel, gbc, 6, "Availability:", availabilityField);

        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(new JLabel("Personal Statement:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(statementArea), gbc);

        JPanel bottomRow = new JPanel(new BorderLayout(10, 10));
        bottomRow.setOpaque(false);
        profileStatusLabel = new JLabel("Profile status: not loaded yet.");
        JButton saveButton = new JButton("Save Profile");
        styleActionButton(saveButton, ACCENT_COLOR, Color.WHITE);
        bottomRow.add(profileStatusLabel, BorderLayout.CENTER);
        bottomRow.add(saveButton, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        formPanel.add(bottomRow, gbc);
        saveButton.addActionListener(e -> saveProfile());

        content.add(formPanel);
        content.add(Box.createVerticalGlue());
        return wrapContent(content);
    }

    private JPanel createBrowseJobsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(APP_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panel.add(buildSectionIntro(
                "Open Jobs and Match Guidance",
                "Browse currently open jobs, filter each major attribute separately, and compare the AI-ready match explanation before applying. Missing skills are surfaced explicitly for decision making."),
                BorderLayout.NORTH);

        jobsModel = new DefaultTableModel(
                new String[] {"Job ID", "Title", "Module", "Skills", "Hours", "Location", "AI Match", "Missing Skills", "Summary"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jobsTable = new JTable(jobsModel);
        jobsTable.setAutoCreateRowSorter(true);
        jobsTable.setRowHeight(24);
        jobsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel filterPanel = new JPanel(new GridLayout(2, 9, 6, 6));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("Job ID"));
        filterPanel.add(new JLabel("Title"));
        filterPanel.add(new JLabel("Module"));
        filterPanel.add(new JLabel("Skills"));
        filterPanel.add(new JLabel("Hours"));
        filterPanel.add(new JLabel("Location"));
        filterPanel.add(new JLabel("AI Match"));
        filterPanel.add(new JLabel("Missing Skills"));
        filterPanel.add(new JLabel("Summary"));
        filterPanel.add(new JLabel(""));
        jobTitleFilterField = new JTextField();
        filterPanel.add(jobTitleFilterField);
        jobModuleFilterField = new JTextField();
        filterPanel.add(jobModuleFilterField);
        jobSkillsFilterField = new JTextField();
        filterPanel.add(jobSkillsFilterField);
        filterPanel.add(new JLabel(""));
        jobLocationFilterField = new JTextField();
        filterPanel.add(jobLocationFilterField);
        filterPanel.add(new JLabel(""));
        filterPanel.add(new JLabel(""));
        filterPanel.add(new JLabel(""));

        FilterToolbar compactFilter = new FilterToolbar("Search open jobs", this::refreshJobs);
        compactFilter.addField("Title", jobTitleFilterField);
        compactFilter.addField("Module", jobModuleFilterField);
        compactFilter.addField("Skills", jobSkillsFilterField);
        compactFilter.addField("Location", jobLocationFilterField);
        JPanel filters = new JPanel(new BorderLayout(6, 6));
        filters.setOpaque(false);
        filters.add(compactFilter, BorderLayout.NORTH);
        filters.add(filterPanel, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(filters, BorderLayout.NORTH);
        center.add(new JScrollPane(jobsTable), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);
        jobInsightLabel = new JLabel("Match insight will appear here after refresh.");
        bottom.add(jobInsightLabel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton refreshButton = new JButton("Refresh");
        JButton applyButton = new JButton("Apply for Selected Job");
        styleActionButton(refreshButton, new Color(225, 234, 238), ACCENT_COLOR);
        styleActionButton(applyButton, ACCENT_COLOR, Color.WHITE);
        buttons.add(refreshButton);
        buttons.add(applyButton);
        bottom.add(buttons, BorderLayout.EAST);
        panel.add(bottom, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshJobs());
        applyButton.addActionListener(e -> applyForSelectedJob());
        installFieldListener(jobTitleFilterField, this::refreshJobs);
        installFieldListener(jobModuleFilterField, this::refreshJobs);
        installFieldListener(jobSkillsFilterField, this::refreshJobs);
        installFieldListener(jobLocationFilterField, this::refreshJobs);
        return panel;
    }

    private JPanel createApplicationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(APP_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panel.add(buildSectionIntro(
                "Application Tracking",
                "Monitor application outcomes with colour cues, compare earlier AI match details, and withdraw only pending applications. Status updates from MO actions now also generate in-app notifications."),
                BorderLayout.NORTH);

        applicationsModel = new DefaultTableModel(
                new String[] {"App ID", "Job", "Module", "Status", "Applied At", "Match", "MO Note"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        applicationsTable = new JTable(applicationsModel);
        applicationsTable.setAutoCreateRowSorter(true);
        applicationsTable.setDefaultRenderer(Object.class, new StatusRenderer());
        applicationsTable.setRowHeight(24);

        JPanel filterPanel = new JPanel(new GridLayout(2, 7, 6, 6));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("App ID"));
        filterPanel.add(new JLabel("Job"));
        filterPanel.add(new JLabel("Module"));
        filterPanel.add(new JLabel("Status"));
        filterPanel.add(new JLabel("Applied At"));
        filterPanel.add(new JLabel("Match"));
        filterPanel.add(new JLabel("MO Note"));
        filterPanel.add(new JLabel(""));
        applicationJobFilterField = new JTextField();
        filterPanel.add(applicationJobFilterField);
        applicationModuleFilterField = new JTextField();
        filterPanel.add(applicationModuleFilterField);
        applicationStatusFilterField = new JTextField();
        filterPanel.add(applicationStatusFilterField);
        filterPanel.add(new JLabel(""));
        filterPanel.add(new JLabel(""));
        filterPanel.add(new JLabel(""));

        FilterToolbar compactFilter = new FilterToolbar("Search applications", this::refreshApplications);
        compactFilter.addField("Job", applicationJobFilterField);
        compactFilter.addField("Module", applicationModuleFilterField);
        compactFilter.addField("Status", applicationStatusFilterField);
        JPanel filters = new JPanel(new BorderLayout(6, 6));
        filters.setOpaque(false);
        filters.add(compactFilter, BorderLayout.NORTH);
        filters.add(filterPanel, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(filters, BorderLayout.NORTH);
        center.add(new JScrollPane(applicationsTable), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout(8, 8));
        actions.setOpaque(false);
        applicationInsightLabel = new JLabel("Application summary will appear here after refresh.");
        actions.add(applicationInsightLabel, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonRow.setOpaque(false);
        JButton refreshButton = new JButton("Refresh");
        JButton withdrawButton = new JButton("Withdraw Selected Pending Application");
        styleActionButton(refreshButton, new Color(225, 234, 238), ACCENT_COLOR);
        styleActionButton(withdrawButton, new Color(240, 229, 206), new Color(70, 56, 32));
        buttonRow.add(refreshButton);
        buttonRow.add(withdrawButton);
        actions.add(buttonRow, BorderLayout.EAST);
        panel.add(actions, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshApplications());
        withdrawButton.addActionListener(e -> withdrawSelectedApplication());
        installFieldListener(applicationJobFilterField, this::refreshApplications);
        installFieldListener(applicationModuleFilterField, this::refreshApplications);
        installFieldListener(applicationStatusFilterField, this::refreshApplications);
        return panel;
    }

    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(APP_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panel.add(buildSectionIntro(
                "In-App Notifications",
                "This tab implements US-8 style in-app notifications. MO decisions generate updates automatically so TAs can review unread status changes without leaving the system."),
                BorderLayout.NORTH);

        notificationsModel = new DefaultTableModel(
                new String[] {"ID", "Title", "Message", "Status", "Created At", "Action Hint"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        notificationsTable = new JTable(notificationsModel);
        notificationsTable.setAutoCreateRowSorter(true);
        notificationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notificationsTable.setRowHeight(24);
        notificationsTable.setDefaultRenderer(Object.class, new NotificationRenderer());
        panel.add(new JScrollPane(notificationsTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);
        notificationSummaryLabel = new JLabel("Notification summary will appear here after refresh.");
        bottom.add(notificationSummaryLabel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton refreshButton = new JButton("Refresh Notifications");
        JButton markReadButton = new JButton("Mark Selected as Read");
        JButton markAllButton = new JButton("Mark All as Read");
        styleActionButton(refreshButton, new Color(225, 234, 238), ACCENT_COLOR);
        styleActionButton(markReadButton, ACCENT_COLOR, Color.WHITE);
        styleActionButton(markAllButton, new Color(240, 229, 206), new Color(70, 56, 32));
        actions.add(refreshButton);
        actions.add(markReadButton);
        actions.add(markAllButton);
        bottom.add(actions, BorderLayout.EAST);
        panel.add(bottom, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshNotifications());
        markReadButton.addActionListener(e -> markSelectedNotificationAsRead());
        markAllButton.addActionListener(e -> {
            NotificationService.markAllAsRead(currentUser.id);
            refreshNotifications();
        });
        return panel;
    }

    private JPanel wrapContent(JPanel content) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(APP_BACKGROUND);
        root.add(wrapScrollable(content), BorderLayout.CENTER);
        return root;
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
        rowPanel.setOpaque(false);
        rowPanel.add(cvPathField, BorderLayout.CENTER);
        JButton browseButton = new JButton("Browse");
        styleActionButton(browseButton, new Color(225, 234, 238), ACCENT_COLOR);
        browseButton.addActionListener(e -> chooseCv());
        rowPanel.add(browseButton, BorderLayout.EAST);

        gbc.gridx = 1;
        panel.add(rowPanel, gbc);
    }

    private void installFieldListener(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new SimpleDocumentListener(action));
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
            profileStatusLabel.setText("Profile status: complete your information before applying.");
            NotificationService.notifyProfileRequired(currentUser);
            refreshNotifications();
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
        profileStatusLabel.setText(profile.isComplete()
                ? "Profile status: ready for applications and AI matching."
                : "Profile status: partially complete. Fill all required fields before applying.");
        if (!profile.isComplete()) {
            NotificationService.notifyProfileRequired(currentUser);
        }
        refreshNotifications();
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

        double gpa = ValidationUtils.parseDouble(gpaField.getText(), -1.0);
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
        profileStatusLabel.setText("Profile status: saved and ready for AI-assisted job matching.");
        NotificationService.markProfileReminderResolved(currentUser);

        JOptionPane.showMessageDialog(this, "Profile saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        refreshJobs();
        refreshNotifications();
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
        String titleFilter = getLower(jobTitleFilterField);
        String moduleFilter = getLower(jobModuleFilterField);
        String skillsFilter = getLower(jobSkillsFilterField);
        String locationFilter = getLower(jobLocationFilterField);
        int visibleJobs = 0;
        int bestScore = -1;
        String bestJob = "";
        for (Job job : FileStorage.loadJobs()) {
            if (!job.isOpen()) {
                continue;
            }
            if (!matchesJobFilters(job, titleFilter, moduleFilter, skillsFilter, locationFilter)) {
                continue;
            }
            MatchResult match = ScoringService.evaluate(profile, job);
            jobsModel.addRow(new Object[] {
                    job.id,
                    job.title,
                    job.module,
                    job.requiredSkills,
                    job.maxHours,
                    job.location,
                    match.score + "%",
                    extractMissingSkills(match.summary),
                    match.summary
            });
            visibleJobs++;
            if (match.score > bestScore) {
                bestScore = match.score;
                bestJob = job.title + " (" + job.module + ")";
            }
        }
        if (visibleJobs == 0) {
            jobInsightLabel.setText("No open jobs match the current filters.");
        } else {
            jobInsightLabel.setText("Visible jobs: " + visibleJobs + " | Best current match: " + bestJob + " at "
                    + Math.max(bestScore, 0) + "% via " + ScoringService.getActiveProvider().getProviderName());
        }
    }

    private boolean matchesJobFilters(Job job, String titleFilter, String moduleFilter, String skillsFilter,
            String locationFilter) {
        return contains(job.title, titleFilter)
                && contains(job.module, moduleFilter)
                && contains(job.requiredSkills, skillsFilter)
                && contains(job.location, locationFilter);
    }

    private void applyForSelectedJob() {
        int row = jobsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a job first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        TAProfile profile = FileStorage.findProfileByUserId(currentUser.id);
        if (profile == null || !profile.isComplete()) {
            NotificationService.notifyProfileRequired(currentUser);
            refreshNotifications();
            JOptionPane.showMessageDialog(this,
                    "Please complete your profile before applying. A reminder has also been added to Notifications.",
                    "Profile Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = jobsTable.convertRowIndexToModel(row);
        int jobId = Integer.parseInt(String.valueOf(jobsModel.getValueAt(modelRow, 0)));
        List<Application> applications = FileStorage.loadApplications();
        for (Application app : applications) {
            if (app.taId == currentUser.id && app.jobId == jobId && !"WITHDRAWN".equalsIgnoreCase(app.status)) {
                JOptionPane.showMessageDialog(this, "You have already applied for this job.", "Duplicate Application",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Job job = FileStorage.findJobById(jobId);
        MatchResult match = ScoringService.evaluate(profile, job);

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
                "Application submitted with AI match score: " + match.score + "% and missing-skills summary: "
                        + extractMissingSkills(match.summary) + ".",
                "Application Submitted", JOptionPane.INFORMATION_MESSAGE);
        refreshApplications();
    }

    private void refreshApplications() {
        applicationsModel.setRowCount(0);
        String jobFilter = getLower(applicationJobFilterField);
        String moduleFilter = getLower(applicationModuleFilterField);
        String statusFilter = getLower(applicationStatusFilterField);
        int pending = 0;
        int selected = 0;
        int rejected = 0;
        int withdrawn = 0;
        for (Application app : FileStorage.loadApplications()) {
            if (app.taId != currentUser.id) {
                continue;
            }
            Job job = FileStorage.findJobById(app.jobId);
            String jobTitle = job == null ? "Unknown" : job.title;
            String module = job == null ? "Unknown" : job.module;
            if (!contains(jobTitle, jobFilter) || !contains(module, moduleFilter) || !contains(app.status, statusFilter)) {
                continue;
            }
            applicationsModel.addRow(new Object[] {
                    app.id,
                    jobTitle,
                    module,
                    app.status,
                    app.appliedAt,
                    app.matchScore + "%",
                    app.reviewerNote
            });
            if ("PENDING".equalsIgnoreCase(app.status)) {
                pending++;
            } else if ("SELECTED".equalsIgnoreCase(app.status)) {
                selected++;
            } else if ("REJECTED".equalsIgnoreCase(app.status)) {
                rejected++;
            } else if ("WITHDRAWN".equalsIgnoreCase(app.status)) {
                withdrawn++;
            }
        }
        applicationInsightLabel.setText("Pending: " + pending + " | Selected: " + selected + " | Rejected: "
                + rejected + " | Withdrawn: " + withdrawn + " | Unread notifications: "
                + NotificationService.countUnreadForUser(currentUser.id));
    }

    private void refreshNotifications() {
        notificationsModel.setRowCount(0);
        List<Notification> notifications = NotificationService.getNotificationsForUser(currentUser.id);
        int unread = 0;
        for (Notification notification : notifications) {
            notificationsModel.addRow(new Object[] {
                    notification.id,
                    notification.title,
                    notification.message,
                    notification.status,
                    notification.createdAt,
                    notification.actionHint
            });
            if (notification.isUnread()) {
                unread++;
            }
        }
        notificationSummaryLabel.setText("Total notifications: " + notifications.size() + " | Unread: " + unread);
        tabs.setTitleAt(3, unread > 0 ? "Notifications (" + unread + ")" : "Notifications");
    }

    private void markSelectedNotificationAsRead() {
        int row = notificationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a notification first.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = notificationsTable.convertRowIndexToModel(row);
        int notificationId = ValidationUtils.parseInt(String.valueOf(notificationsModel.getValueAt(modelRow, 0)), 0);
        NotificationService.markAsRead(notificationId);
        refreshNotifications();
    }

    private void withdrawSelectedApplication() {
        int row = applicationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an application first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = applicationsTable.convertRowIndexToModel(row);
        int appId = Integer.parseInt(String.valueOf(applicationsModel.getValueAt(modelRow, 0)));
        String status = String.valueOf(applicationsModel.getValueAt(modelRow, 3));
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

    private String getLower(JTextField field) {
        return field == null ? "" : field.getText().trim().toLowerCase();
    }

    private boolean contains(String text, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return true;
        }
        return text != null && text.toLowerCase().contains(keyword);
    }

    private String extractMissingSkills(String summary) {
        if (ValidationUtils.isBlank(summary)) {
            return "None";
        }
        String[] pieces = summary.split("\\|");
        for (String piece : pieces) {
            String trimmed = piece.trim();
            if (trimmed.toLowerCase().startsWith("missing:")) {
                return trimmed.substring("Missing:".length()).trim();
            }
        }
        return "None";
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

    private static class NotificationRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            java.awt.Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            if (!isSelected) {
                String status = String.valueOf(table.getValueAt(row, 3));
                if ("UNREAD".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(232, 244, 250));
                } else {
                    component.setBackground(Color.WHITE);
                }
            }
            return component;
        }
    }

    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable action;

        private SimpleDocumentListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            action.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            action.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            action.run();
        }
    }
}
