package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.Application;
import com.bupt.ta.recruitment.model.Job;
import com.bupt.ta.recruitment.model.TAProfile;
import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class TADashboard extends BaseDashboard {
    private final CsvStorage<TAProfile> profileStorage = new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);
    private final CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
    private final CsvStorage<Application> applicationStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);

    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField studentIdField;
    private JTextField skillsField;
    private JTextField gpaField;
    private JTextField cvPathField;

    private DefaultTableModel jobsModel;
    private TableRowSorter<TableModel> jobsSorter;
    private JTable jobsTable;
    private JTextField moduleFilterField;
    private JTextField skillFilterField;

    private DefaultTableModel applicationsModel;
    private JTable applicationsTable;

    public TADashboard(User user) {
        super(user, "TA Dashboard");
        add(buildTabs(), BorderLayout.CENTER);
        loadProfileIntoForm();
        reloadJobsTable();
        reloadApplicationsTable();
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Profile", createProfilePanel());
        tabs.addTab("Browse Jobs", createJobsPanel());
        tabs.addTab("My Applications", createApplicationsPanel());
        return tabs;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        fullNameField = new JTextField(24);
        emailField = new JTextField(24);
        studentIdField = new JTextField(24);
        skillsField = new JTextField(24);
        gpaField = new JTextField(24);
        cvPathField = new JTextField(18);
        cvPathField.setEditable(false);

        addField(panel, gbc, 0, "Full Name:", fullNameField);
        addField(panel, gbc, 1, "Email:", emailField);
        addField(panel, gbc, 2, "Student ID:", studentIdField);
        addField(panel, gbc, 3, "Skills (use ';' to separate):", skillsField);
        addField(panel, gbc, 4, "GPA:", gpaField);
        addCvField(panel, gbc, 5);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Profile");
        JButton reloadButton = new JButton("Reload");
        actions.add(reloadButton);
        actions.add(saveButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(actions, gbc);

        gbc.gridy = 7;
        JLabel note = new JLabel("L3 Pair C: TA profile editing supports validation, GPA checks, and CV file browsing.", SwingConstants.LEFT);
        note.setForeground(new Color(80, 80, 80));
        panel.add(note, gbc);

        saveButton.addActionListener(e -> saveProfile());
        reloadButton.addActionListener(e -> loadProfileIntoForm());
        return panel;
    }

    private JPanel createJobsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel filterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        moduleFilterField = new JTextField(16);
        skillFilterField = new JTextField(16);

        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(new JLabel("Module Filter:"), gbc);
        gbc.gridx = 1;
        filterPanel.add(moduleFilterField, gbc);
        gbc.gridx = 2;
        filterPanel.add(new JLabel("Skill Filter:"), gbc);
        gbc.gridx = 3;
        filterPanel.add(skillFilterField, gbc);

        jobsModel = new DefaultTableModel(new String[] {"Job ID", "Title", "Module", "Required Skills", "Hours", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jobsTable = new JTable(jobsModel);
        jobsSorter = UIHelper.installSorter(jobsTable, 1);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearFiltersButton = new JButton("Clear Filters");
        JButton applyButton = new JButton("Apply for Selected Job");
        actions.add(clearFiltersButton);
        actions.add(applyButton);

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(jobsTable), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);

        SimpleDocumentListener filterListener = new SimpleDocumentListener(this::applyJobFilters);
        moduleFilterField.getDocument().addDocumentListener(filterListener);
        skillFilterField.getDocument().addDocumentListener(filterListener);
        clearFiltersButton.addActionListener(e -> {
            moduleFilterField.setText("");
            skillFilterField.setText("");
            applyJobFilters();
        });
        applyButton.addActionListener(e -> applyForSelectedJob());
        return panel;
    }

    private JPanel createApplicationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        applicationsModel = new DefaultTableModel(new String[] {"Application ID", "Job Title", "Module", "Status", "Applied At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        applicationsTable = new JTable(applicationsModel);
        applicationsTable.setDefaultRenderer(Object.class, new StatusCellRenderer(3));
        UIHelper.installSorter(applicationsTable, 4);

        panel.add(new JLabel("Application records are shown here for TA tracking."), BorderLayout.NORTH);
        panel.add(new JScrollPane(applicationsTable), BorderLayout.CENTER);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void addCvField(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel("CV Path:"), gbc);

        JPanel cvPanel = new JPanel(new BorderLayout(6, 6));
        cvPanel.add(cvPathField, BorderLayout.CENTER);
        JButton browseButton = new JButton("Browse CV");
        browseButton.addActionListener(e -> chooseCvFile());
        cvPanel.add(browseButton, BorderLayout.EAST);

        gbc.gridx = 1;
        panel.add(cvPanel, gbc);
    }

    private void chooseCvFile() {
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            cvPathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void loadProfileIntoForm() {
        TAProfile profile = findProfile(currentUser.getId());
        fullNameField.setText(profile == null ? "" : profile.getFullName());
        emailField.setText(profile == null ? "" : profile.getEmail());
        studentIdField.setText(profile == null ? "" : profile.getStudentId());
        skillsField.setText(profile == null ? "" : profile.getSkills());
        gpaField.setText(profile == null ? "" : String.valueOf(profile.getGpa()));
        cvPathField.setText(profile == null ? "" : profile.getCvPath());
    }

    private void saveProfile() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String skills = skillsField.getText().trim();
        String gpaText = gpaField.getText().trim();
        String cvPath = cvPathField.getText().trim();

        if (fullName.isEmpty() || email.isEmpty() || studentId.isEmpty() || skills.isEmpty() || gpaText.isEmpty() || cvPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All profile fields are required before saving.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!UIHelper.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!UIHelper.isValidGpa(gpaText)) {
            JOptionPane.showMessageDialog(this, "GPA must be between 0.0 and 4.0.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double gpa = Double.parseDouble(gpaText);
        List<TAProfile> profiles = profileStorage.loadAll();
        TAProfile target = null;
        for (TAProfile profile : profiles) {
            if (currentUser.getId().equals(profile.getUserId())) {
                target = profile;
                break;
            }
        }
        if (target == null) {
            target = new TAProfile();
            target.setId(java.util.UUID.randomUUID().toString());
            target.setUserId(currentUser.getId());
            profiles.add(target);
        }

        target.setFullName(fullName);
        target.setEmail(email);
        target.setStudentId(studentId);
        target.setSkills(skills);
        target.setGpa(gpa);
        target.setCvPath(cvPath);
        profileStorage.saveAll(profiles);

        JOptionPane.showMessageDialog(this, "Profile saved successfully.", "Profile Updated", JOptionPane.INFORMATION_MESSAGE);
        reloadJobsTable();
    }

    private void reloadJobsTable() {
        jobsModel.setRowCount(0);
        for (Job job : jobStorage.loadAll()) {
            if (job.getStatus() == Job.JobStatus.OPEN) {
                jobsModel.addRow(new Object[] {
                        job.getId(),
                        job.getTitle(),
                        job.getModule(),
                        job.getRequiredSkills(),
                        job.getMaxHours(),
                        job.getStatus()
                });
            }
        }
        applyJobFilters();
    }

    private void applyJobFilters() {
        if (jobsSorter == null) {
            return;
        }
        String moduleKeyword = moduleFilterField == null ? "" : moduleFilterField.getText().trim().toLowerCase();
        String skillKeyword = skillFilterField == null ? "" : skillFilterField.getText().trim().toLowerCase();

        if (moduleKeyword.isEmpty() && skillKeyword.isEmpty()) {
            jobsSorter.setRowFilter(null);
            return;
        }

        jobsSorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                String module = String.valueOf(entry.getValue(2)).toLowerCase();
                String skills = String.valueOf(entry.getValue(3)).toLowerCase();
                boolean moduleMatched = moduleKeyword.isEmpty() || module.contains(moduleKeyword);
                boolean skillMatched = skillKeyword.isEmpty() || skills.contains(skillKeyword);
                return moduleMatched && skillMatched;
            }
        });
    }

    private void applyForSelectedJob() {
        int selectedViewRow = jobsTable.getSelectedRow();
        if (selectedViewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an OPEN job before applying.", "No Job Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TAProfile profile = findProfile(currentUser.getId());
        if (profile == null) {
            JOptionPane.showMessageDialog(this, "Please complete your TA profile before applying for a job.", "Profile Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedModelRow = jobsTable.convertRowIndexToModel(selectedViewRow);
        String jobId = String.valueOf(jobsModel.getValueAt(selectedModelRow, 0));

        for (Application application : applicationStorage.loadAll()) {
            if (currentUser.getId().equals(application.getTaId()) && jobId.equals(application.getJobId())) {
                JOptionPane.showMessageDialog(this, "You have already applied for this job.", "Duplicate Application", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        List<Application> applications = applicationStorage.loadAll();
        applications.add(new Application(currentUser.getId(), jobId, Application.AppStatus.PENDING));
        applicationStorage.saveAll(applications);

        JOptionPane.showMessageDialog(this, "Application submitted successfully.", "Application Created", JOptionPane.INFORMATION_MESSAGE);
        reloadApplicationsTable();
    }

    private void reloadApplicationsTable() {
        applicationsModel.setRowCount(0);
        for (Application application : applicationStorage.loadAll()) {
            if (currentUser.getId().equals(application.getTaId())) {
                Job job = jobStorage.findById(application.getJobId(), Job::getId);
                applicationsModel.addRow(new Object[] {
                        application.getId(),
                        job == null ? "Unknown" : job.getTitle(),
                        job == null ? "Unknown" : job.getModule(),
                        application.getStatus(),
                        formatTimestamp(application.getAppliedAt())
                });
            }
        }
    }

    private TAProfile findProfile(String userId) {
        List<TAProfile> profiles = profileStorage.loadAll();
        for (TAProfile profile : profiles) {
            if (userId.equals(profile.getUserId())) {
                return profile;
            }
        }
        return null;
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(timestamp));
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        private final int statusColumn;

        private StatusCellRenderer(int statusColumn) {
            this.statusColumn = statusColumn;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                Object rawStatus = table.getValueAt(row, statusColumn);
                component.setBackground(resolveStatusColor(String.valueOf(rawStatus)));
            }
            return component;
        }

        private Color resolveStatusColor(String status) {
            if ("SELECTED".equalsIgnoreCase(status)) {
                return UIHelper.STATUS_SELECTED;
            }
            if ("REJECTED".equalsIgnoreCase(status)) {
                return UIHelper.STATUS_REJECTED;
            }
            return UIHelper.STATUS_PENDING;
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
