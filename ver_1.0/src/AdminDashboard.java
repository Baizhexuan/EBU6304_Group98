import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends BaseDashboard {
    private static final String[] APPLICATION_STATUSES = {"PENDING", "SELECTED", "REJECTED", "WITHDRAWN"};
    private static final String[] JOB_STATUSES = {"OPEN", "CLOSED"};

    private JTable workloadTable;
    private DefaultTableModel workloadModel;
    private JTextField workloadSearchField;
    private JComboBox<String> workloadStatusFilter;
    private JLabel adminSummaryLabel;
    private JLabel aiReadinessLabel;
    private JLabel recommendationTitleLabel;
    private JTextArea recommendationArea;

    private JTable applicationsTable;
    private DefaultTableModel applicationsModel;
    private JTextField applicationSearchField;
    private boolean applicationsDirty;
    private List<Application> applicationSnapshot = new ArrayList<Application>();

    private JTable jobsTable;
    private DefaultTableModel jobsModel;
    private JTextField jobSearchField;
    private boolean jobsDirty;
    private List<Job> jobSnapshot = new ArrayList<Job>();

    public AdminDashboard(User currentUser) {
        super(currentUser, "Admin Dashboard", 1240, 780);
        addTab("Workload Monitor", createWorkloadPanel());
        addTab("Applications Overview", createApplicationsPanel());
        addTab("Jobs Overview", createJobsPanel());
        installRefreshOnTabSwitch(this::refreshVisibleTab);
        installCloseGuard();
        refreshAll();
        setVisible(true);
    }

    @Override
    protected void logout() {
        if (!confirmDiscardIfNeeded("logout")) {
            return;
        }
        super.logout();
    }

    private void installCloseGuard() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmDiscardIfNeeded("close the dashboard")) {
                    dispose();
                    new LoginFrame();
                }
            }
        });
    }

    private JPanel createWorkloadPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(APP_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        summaryPanel.setOpaque(false);
        adminSummaryLabel = buildCardLabel("Refreshing admin summary...");
        aiReadinessLabel = buildCardLabel(AIIntegrationPlan.buildReadinessSummary());
        recommendationTitleLabel = buildCardLabel("Recommendation focus: Global risk overview");
        summaryPanel.add(buildCard("Allocation Overview", adminSummaryLabel, new Color(231, 240, 228)));
        summaryPanel.add(buildCard("AI Scoring Status", aiReadinessLabel, new Color(225, 236, 241)));
        summaryPanel.add(buildCard("Recommendation Focus", recommendationTitleLabel, new Color(243, 234, 221)));
        panel.add(summaryPanel, BorderLayout.NORTH);

        workloadModel = new DefaultTableModel(
                new String[] {"TA Username", "Full Name", "Email", "Selected Jobs", "Current Hours", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        workloadTable = new JTable(workloadModel);
        workloadTable.setDefaultRenderer(Object.class, new WorkloadRenderer());
        workloadTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        workloadTable.setRowHeight(26);
        workloadTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshRecommendationPanel();
            }
        });

        recommendationArea = new JTextArea();
        recommendationArea.setEditable(false);
        recommendationArea.setLineWrap(true);
        recommendationArea.setWrapStyleWord(true);
        recommendationArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        recommendationArea.setBackground(SURFACE_COLOR);
        recommendationArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel recommendationPanel = new JPanel(new BorderLayout(8, 8));
        recommendationPanel.setBackground(SURFACE_COLOR);
        recommendationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 220, 224)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JLabel recommendationHeader = new JLabel("Reallocation Advice");
        recommendationHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        recommendationPanel.add(recommendationHeader, BorderLayout.NORTH);
        recommendationPanel.add(new JScrollPane(recommendationArea), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(workloadTable), recommendationPanel);
        splitPane.setResizeWeight(0.66);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(splitPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.setOpaque(false);
        filters.add(new JLabel("Search:"));
        workloadSearchField = new JTextField(18);
        filters.add(workloadSearchField);
        filters.add(new JLabel("Status:"));
        workloadStatusFilter = new JComboBox<String>(new String[] {"ALL", "OK", "NEAR LIMIT", "OVERLOAD"});
        filters.add(workloadStatusFilter);
        bottom.add(filters, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton refreshButton = new JButton("Refresh");
        JButton recommendationButton = new JButton("Refresh Advice");
        JButton exportButton = new JButton("Export CSV Report");
        styleButton(refreshButton, new Color(225, 234, 238), new Color(33, 76, 95));
        styleButton(recommendationButton, new Color(240, 229, 206), new Color(70, 56, 32));
        styleButton(exportButton, new Color(33, 76, 95), Color.WHITE);
        actions.add(refreshButton);
        actions.add(recommendationButton);
        actions.add(exportButton);
        bottom.add(actions, BorderLayout.EAST);
        panel.add(bottom, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshWorkload());
        recommendationButton.addActionListener(e -> refreshRecommendationPanel());
        exportButton.addActionListener(e -> exportWorkloadReport());
        workloadSearchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::refreshWorkload));
        workloadStatusFilter.addActionListener(e -> refreshWorkload());
        return panel;
    }

    private JPanel createApplicationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(APP_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setOpaque(false);
        topBar.add(new JLabel("Search applications:"));
        applicationSearchField = new JTextField(28);
        topBar.add(applicationSearchField);
        panel.add(topBar, BorderLayout.NORTH);

        applicationsModel = new DefaultTableModel(
                new String[] {"App ID", "TA", "Job", "Module", "Status", "Applied At", "Match", "Summary", "Note"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 8;
            }
        };
        applicationsTable = new JTable(applicationsModel);
        applicationsTable.setRowHeight(24);
        applicationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        applicationsTable.getColumnModel().getColumn(4)
                .setCellEditor(new DefaultCellEditor(new JComboBox<String>(APPLICATION_STATUSES)));
        applicationsTable.getModel().addTableModelListener(e -> {
            if (e.getFirstRow() >= 0) {
                applicationsDirty = true;
            }
        });
        panel.add(new JScrollPane(applicationsTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton refreshButton = new JButton("Refresh");
        JButton saveButton = new JButton("Save Changes");
        JButton undoButton = new JButton("Undo Unsaved Changes");
        styleButton(refreshButton, new Color(225, 234, 238), new Color(33, 76, 95));
        styleButton(saveButton, new Color(33, 76, 95), Color.WHITE);
        styleButton(undoButton, new Color(240, 229, 206), new Color(70, 56, 32));
        actions.add(refreshButton);
        actions.add(saveButton);
        actions.add(undoButton);
        panel.add(actions, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshApplications());
        saveButton.addActionListener(e -> saveApplicationChanges());
        undoButton.addActionListener(e -> undoApplicationChanges());
        applicationSearchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::refreshApplications));
        return panel;
    }

    private JPanel createJobsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(APP_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setOpaque(false);
        topBar.add(new JLabel("Search jobs:"));
        jobSearchField = new JTextField(28);
        topBar.add(jobSearchField);
        panel.add(topBar, BorderLayout.NORTH);

        jobsModel = new DefaultTableModel(
                new String[] {"Job ID", "MO", "Title", "Module", "Skills", "Hours", "Location", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 1;
            }
        };
        jobsTable = new JTable(jobsModel);
        jobsTable.setRowHeight(24);
        jobsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jobsTable.getColumnModel().getColumn(7)
                .setCellEditor(new DefaultCellEditor(new JComboBox<String>(JOB_STATUSES)));
        jobsTable.getModel().addTableModelListener(e -> {
            if (e.getFirstRow() >= 0) {
                jobsDirty = true;
            }
        });
        panel.add(new JScrollPane(jobsTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton refreshButton = new JButton("Refresh");
        JButton saveButton = new JButton("Save Changes");
        JButton undoButton = new JButton("Undo Unsaved Changes");
        styleButton(refreshButton, new Color(225, 234, 238), new Color(33, 76, 95));
        styleButton(saveButton, new Color(33, 76, 95), Color.WHITE);
        styleButton(undoButton, new Color(240, 229, 206), new Color(70, 56, 32));
        actions.add(refreshButton);
        actions.add(saveButton);
        actions.add(undoButton);
        panel.add(actions, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshJobs());
        saveButton.addActionListener(e -> saveJobChanges());
        undoButton.addActionListener(e -> undoJobChanges());
        jobSearchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::refreshJobs));
        return panel;
    }

    private JLabel buildCardLabel(String text) {
        JLabel label = new JLabel("<html><div style='width:300px;'>" + text + "</div></html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return label;
    }

    private JPanel buildCard(String title, JLabel content, Color background) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(background);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 220, 224)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 14));
        card.add(heading, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
    }

    private void refreshVisibleTab() {
        int index = tabs.getSelectedIndex();
        if (index == 0) {
            refreshWorkload();
        } else if (index == 1) {
            refreshApplications();
        } else if (index == 2) {
            refreshJobs();
        }
    }

    private void refreshAll() {
        refreshWorkload();
        refreshApplications();
        refreshJobs();
    }

    private void refreshWorkload() {
        workloadModel.setRowCount(0);
        Map<Integer, TAProfile> profiles = new HashMap<Integer, TAProfile>();
        for (TAProfile profile : FileStorage.loadProfiles()) {
            profiles.put(profile.userId, profile);
        }

        String keyword = workloadSearchField == null ? "" : workloadSearchField.getText().trim().toLowerCase();
        String selectedStatus = workloadStatusFilter == null ? "ALL" : String.valueOf(workloadStatusFilter.getSelectedItem());
        int taCount = 0;
        int overloadCount = 0;
        int totalHours = 0;

        for (User user : FileStorage.loadUsers()) {
            if (!"TA".equalsIgnoreCase(user.role)) {
                continue;
            }
            TAProfile profile = profiles.get(user.id);
            int selectedJobs = 0;
            int currentHours = 0;
            for (Application app : FileStorage.loadApplications()) {
                if (app.taId == user.id && "SELECTED".equalsIgnoreCase(app.status)) {
                    selectedJobs++;
                    Job job = FileStorage.findJobById(app.jobId);
                    if (job != null) {
                        currentHours += job.maxHours;
                    }
                }
            }

            String fullName = profile == null ? user.getSafeDisplayName() : profile.fullName;
            String email = profile == null ? "N/A" : profile.email;
            String status = buildWorkloadStatus(currentHours);
            if (!matchesWorkloadFilters(user, fullName, email, status, keyword, selectedStatus)) {
                continue;
            }

            taCount++;
            totalHours += currentHours;
            if (status.startsWith("OVERLOAD")) {
                overloadCount++;
            }
            workloadModel.addRow(new Object[] {user.username, fullName, email, selectedJobs, currentHours, status});
        }

        adminSummaryLabel.setText("<html><div style='width:300px;'>Visible TAs: " + taCount
                + " | Total allocated hours: " + totalHours
                + " | Overload cases: " + overloadCount
                + " | Overload limit: " + FileStorage.getOverloadLimit() + "h</div></html>");
        aiReadinessLabel.setText("<html><div style='width:300px;'>" + AIIntegrationPlan.buildReadinessSummary() + "</div></html>");
        if (workloadModel.getRowCount() > 0 && workloadTable.getSelectedRow() < 0) {
            workloadTable.setRowSelectionInterval(0, 0);
        }
        refreshRecommendationPanel();
    }

    private void refreshRecommendationPanel() {
        if (recommendationArea == null) {
            return;
        }
        int selectedRow = workloadTable == null ? -1 : workloadTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= workloadModel.getRowCount()) {
            recommendationTitleLabel.setText("<html><div style='width:300px;'>Recommendation focus: Global risk overview</div></html>");
            recommendationArea.setText(AdminRecommendationService.buildGlobalAlertSummary());
            recommendationArea.setCaretPosition(0);
            return;
        }
        String username = String.valueOf(workloadModel.getValueAt(selectedRow, 0));
        User user = FileStorage.findUserByUsername(username);
        if (user == null) {
            recommendationArea.setText(AdminRecommendationService.buildGlobalAlertSummary());
            recommendationArea.setCaretPosition(0);
            return;
        }
        recommendationTitleLabel.setText("<html><div style='width:300px;'>Recommendation focus: " + user.getSafeDisplayName() + "</div></html>");
        recommendationArea.setText(AdminRecommendationService.buildRecommendationReportForTa(user.id));
        recommendationArea.setCaretPosition(0);
    }

    private boolean matchesWorkloadFilters(User user, String fullName, String email, String status, String keyword,
            String selectedStatus) {
        boolean keywordMatch = keyword.isEmpty()
                || contains(user.username, keyword)
                || contains(fullName, keyword)
                || contains(email, keyword)
                || contains(status, keyword);
        if (!keywordMatch) {
            return false;
        }
        if ("ALL".equalsIgnoreCase(selectedStatus)) {
            return true;
        }
        if ("OK".equalsIgnoreCase(selectedStatus)) {
            return "OK".equalsIgnoreCase(status);
        }
        return status.toUpperCase().startsWith(selectedStatus.toUpperCase());
    }

    private void refreshApplications() {
        applicationsModel.setRowCount(0);
        String keyword = applicationSearchField == null ? "" : applicationSearchField.getText().trim().toLowerCase();
        List<Application> applications = FileStorage.loadApplications();
        applicationSnapshot = copyApplications(applications);
        for (Application app : applications) {
            User ta = FileStorage.findUserById(app.taId);
            Job job = FileStorage.findJobById(app.jobId);
            String taName = ta == null ? "Unknown" : ta.getSafeDisplayName();
            String jobTitle = job == null ? "Unknown" : job.title;
            String module = job == null ? "Unknown" : job.module;
            if (!keyword.isEmpty()
                    && !contains(taName, keyword)
                    && !contains(jobTitle, keyword)
                    && !contains(module, keyword)
                    && !contains(app.status, keyword)
                    && !contains(app.matchSummary, keyword)
                    && !contains(app.reviewerNote, keyword)) {
                continue;
            }
            applicationsModel.addRow(new Object[] {app.id, taName, jobTitle, module, app.status, app.appliedAt,
                    app.matchScore + "%", app.matchSummary, app.reviewerNote});
        }
        applicationsDirty = false;
    }

    private void saveApplicationChanges() {
        List<Application> applications = FileStorage.loadApplications();
        for (int row = 0; row < applicationsModel.getRowCount(); row++) {
            int appId = ValidationUtils.parseInt(String.valueOf(applicationsModel.getValueAt(row, 0)), 0);
            Application match = findApplicationById(applications, appId);
            if (match == null) {
                continue;
            }
            match.status = String.valueOf(applicationsModel.getValueAt(row, 4)).trim().toUpperCase();
            match.reviewerNote = String.valueOf(applicationsModel.getValueAt(row, 8)).trim();
        }
        FileStorage.saveApplications(applications);
        applicationSnapshot = copyApplications(applications);
        applicationsDirty = false;
        JOptionPane.showMessageDialog(this, "Application updates saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        refreshWorkload();
        refreshApplications();
    }

    private void undoApplicationChanges() {
        FileStorage.saveApplications(copyApplications(applicationSnapshot));
        refreshApplications();
    }

    private void refreshJobs() {
        jobsModel.setRowCount(0);
        String keyword = jobSearchField == null ? "" : jobSearchField.getText().trim().toLowerCase();
        List<Job> jobs = FileStorage.loadJobs();
        jobSnapshot = copyJobs(jobs);
        for (Job job : jobs) {
            User mo = FileStorage.findUserById(job.moId);
            String moName = mo == null ? "Unknown" : mo.getSafeDisplayName();
            if (!keyword.isEmpty()
                    && !contains(moName, keyword)
                    && !contains(job.title, keyword)
                    && !contains(job.module, keyword)
                    && !contains(job.requiredSkills, keyword)
                    && !contains(job.location, keyword)
                    && !contains(job.status, keyword)) {
                continue;
            }
            jobsModel.addRow(new Object[] {job.id, moName, job.title, job.module, job.requiredSkills, job.maxHours,
                    job.location, job.status});
        }
        jobsDirty = false;
    }

    private void saveJobChanges() {
        List<Job> jobs = FileStorage.loadJobs();
        for (int row = 0; row < jobsModel.getRowCount(); row++) {
            int jobId = ValidationUtils.parseInt(String.valueOf(jobsModel.getValueAt(row, 0)), 0);
            Job match = findJobById(jobs, jobId);
            if (match == null) {
                continue;
            }
            String moDisplayName = String.valueOf(jobsModel.getValueAt(row, 1)).trim();
            User mo = FileStorage.findUserByDisplayName(moDisplayName);
            if (mo == null || !"MO".equalsIgnoreCase(mo.role)) {
                JOptionPane.showMessageDialog(this,
                        "MO name '" + moDisplayName + "' is not recognised. Please use an existing MO display name.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int hours = ValidationUtils.parseInt(String.valueOf(jobsModel.getValueAt(row, 5)), -1);
            if (hours <= 0) {
                JOptionPane.showMessageDialog(this, "Hours must be a positive integer.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            match.moId = mo.id;
            match.title = String.valueOf(jobsModel.getValueAt(row, 2)).trim();
            match.module = String.valueOf(jobsModel.getValueAt(row, 3)).trim();
            match.requiredSkills = String.valueOf(jobsModel.getValueAt(row, 4)).trim();
            match.maxHours = hours;
            match.location = String.valueOf(jobsModel.getValueAt(row, 6)).trim();
            match.status = String.valueOf(jobsModel.getValueAt(row, 7)).trim().toUpperCase();
        }
        FileStorage.saveJobs(jobs);
        jobSnapshot = copyJobs(jobs);
        jobsDirty = false;
        JOptionPane.showMessageDialog(this, "Job updates saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        refreshWorkload();
        refreshJobs();
    }

    private void undoJobChanges() {
        FileStorage.saveJobs(copyJobs(jobSnapshot));
        refreshJobs();
    }

    private void exportWorkloadReport() {
        refreshWorkload();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String path = "data/admin_workload_report_" + timestamp + ".csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            writer.println("exportedAt," + timestamp);
            writer.println("provider," + ScoringService.getActiveProvider().getProviderName());
            writer.println("providerReady," + ScoringService.getActiveProvider().isReady());
            writer.println("taUsername,fullName,email,selectedJobs,currentHours,status");
            for (int row = 0; row < workloadModel.getRowCount(); row++) {
                writer.println(workloadModel.getValueAt(row, 0) + "," + workloadModel.getValueAt(row, 1) + ","
                        + workloadModel.getValueAt(row, 2) + "," + workloadModel.getValueAt(row, 3) + ","
                        + workloadModel.getValueAt(row, 4) + "," + workloadModel.getValueAt(row, 5));
            }
            JOptionPane.showMessageDialog(this, "Report exported to " + path, "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to export report: " + e.getMessage(), "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean confirmDiscardIfNeeded(String actionLabel) {
        if (!applicationsDirty && !jobsDirty) {
            return true;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "There are unsaved admin changes. Do you want to discard them and " + actionLabel + "?",
                "Unsaved Changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    private Application findApplicationById(List<Application> applications, int appId) {
        for (Application app : applications) {
            if (app.id == appId) {
                return app;
            }
        }
        return null;
    }

    private Job findJobById(List<Job> jobs, int jobId) {
        for (Job job : jobs) {
            if (job.id == jobId) {
                return job;
            }
        }
        return null;
    }

    private List<Application> copyApplications(List<Application> source) {
        List<Application> copies = new ArrayList<Application>();
        for (Application app : source) {
            Application copy = new Application();
            copy.id = app.id;
            copy.taId = app.taId;
            copy.jobId = app.jobId;
            copy.status = app.status;
            copy.appliedAt = app.appliedAt;
            copy.matchScore = app.matchScore;
            copy.matchSummary = app.matchSummary;
            copy.reviewerNote = app.reviewerNote;
            copies.add(copy);
        }
        return copies;
    }

    private List<Job> copyJobs(List<Job> source) {
        List<Job> copies = new ArrayList<Job>();
        for (Job job : source) {
            Job copy = new Job();
            copy.id = job.id;
            copy.moId = job.moId;
            copy.title = job.title;
            copy.module = job.module;
            copy.description = job.description;
            copy.requiredSkills = job.requiredSkills;
            copy.maxHours = job.maxHours;
            copy.status = job.status;
            copy.location = job.location;
            copies.add(copy);
        }
        return copies;
    }

    private boolean contains(String text, String keyword) {
        return text != null && text.toLowerCase().contains(keyword);
    }

    private String buildWorkloadStatus(int currentHours) {
        if (currentHours > FileStorage.getOverloadLimit()) {
            return "OVERLOAD - review allocation immediately";
        }
        if (currentHours >= FileStorage.getOverloadLimit() - 2) {
            return "NEAR LIMIT - monitor closely";
        }
        return "OK";
    }

    private static class WorkloadRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String status = String.valueOf(table.getValueAt(row, 5));
                if (status.startsWith("OVERLOAD")) {
                    component.setBackground(new Color(255, 221, 221));
                } else if (status.startsWith("NEAR LIMIT")) {
                    component.setBackground(new Color(255, 242, 204));
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
