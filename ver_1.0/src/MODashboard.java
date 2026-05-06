import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

public class MODashboard extends BaseDashboard {
    private JTextField titleField;
    private JTextField moduleField;
    private JTextField skillsField;
    private JTextField hoursField;
    private JTextField locationField;
    private JTextArea descriptionArea;
    private JLabel postStatusLabel;
    private JTable myJobsTable;
    private DefaultTableModel myJobsModel;
    private JTextField myJobsSearchField;
    private JLabel myJobsSummaryLabel;
    private JComboBox<String> jobSelector;
    private List<Integer> selectorJobIds = new ArrayList<Integer>();
    private JTable applicantsTable;
    private DefaultTableModel applicantsModel;
    private JTextField applicantSearchField;
    private JLabel applicantSummaryLabel;

    public MODashboard(User currentUser) {
        super(currentUser, "MO Dashboard", 1080, 720);
        addTab("Post Job", createPostJobPanel());
        addTab("My Job Posts", createMyJobsPanel());
        addTab("Applicants", createApplicantsPanel());
        installRefreshOnTabSwitch(() -> {
            refreshMyJobs();
            refreshJobSelector();
            refreshApplicants();
        });
        refreshMyJobs();
        refreshJobSelector();
        refreshApplicants();
        setVisible(true);
    }

    private JPanel createPostJobPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(APP_BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        content.add(buildSectionIntro(
                "Publish and Maintain Job Posts",
                "Use this screen to release a new TA opportunity with clear skills, workload, and location information. Stronger descriptions help later AI-assisted screening and admin-side reallocation decisions."));
        content.add(Box.createVerticalStrut(12));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(SURFACE_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 224, 228)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        titleField = new JTextField(24);
        moduleField = new JTextField(24);
        skillsField = new JTextField(24);
        hoursField = new JTextField(24);
        locationField = new JTextField(24);
        descriptionArea = new JTextArea(6, 24);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        addRow(formPanel, gbc, 0, "Job Title:", titleField);
        addRow(formPanel, gbc, 1, "Module Code:", moduleField);
        addRow(formPanel, gbc, 2, "Required Skills:", skillsField);
        addRow(formPanel, gbc, 3, "Max Hours/Week:", hoursField);
        addRow(formPanel, gbc, 4, "Location:", locationField);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(descriptionArea), gbc);

        JPanel bottomRow = new JPanel(new BorderLayout(10, 10));
        bottomRow.setOpaque(false);
        postStatusLabel = new JLabel("Posting status: ready to create a new job.");
        JButton postButton = new JButton("Publish Job");
        styleActionButton(postButton, ACCENT_COLOR, Color.WHITE);
        bottomRow.add(postStatusLabel, BorderLayout.CENTER);
        bottomRow.add(postButton, BorderLayout.EAST);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        formPanel.add(bottomRow, gbc);
        postButton.addActionListener(e -> publishJob());

        content.add(formPanel);
        content.add(Box.createVerticalGlue());
        return wrapContent(content);
    }

    private JPanel createMyJobsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(APP_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panel.add(buildSectionIntro(
                "My Job Posts",
                "Review your open and closed jobs, filter quickly during demos, and toggle availability when a role has already been filled or paused."),
                BorderLayout.NORTH);

        myJobsModel = new DefaultTableModel(new String[] {"Job ID", "Title", "Module", "Skills", "Hours", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        myJobsTable = new JTable(myJobsModel);
        myJobsTable.setAutoCreateRowSorter(true);
        myJobsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel topBar = new JPanel(new BorderLayout(8, 8));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        topBar.add(new JLabel("Search My Jobs:"), BorderLayout.WEST);
        myJobsSearchField = new JTextField();
        topBar.add(myJobsSearchField, BorderLayout.CENTER);
        myJobsSummaryLabel = new JLabel("Job summary will appear here after refresh.");
        topBar.add(myJobsSummaryLabel, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(topBar, BorderLayout.NORTH);
        center.add(new JScrollPane(myJobsTable), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton refreshButton = new JButton("Refresh");
        JButton toggleButton = new JButton("Open / Close Selected Job");
        styleActionButton(refreshButton, new Color(225, 234, 238), ACCENT_COLOR);
        styleActionButton(toggleButton, new Color(240, 229, 206), new Color(70, 56, 32));
        actions.add(refreshButton);
        actions.add(toggleButton);
        panel.add(actions, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshMyJobs());
        toggleButton.addActionListener(e -> toggleSelectedJob());
        myJobsSearchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::refreshMyJobs));
        return panel;
    }

    private JPanel createApplicantsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(APP_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panel.add(buildSectionIntro(
                "Applicant Review",
                "Inspect match scores, skills, and current workload before selecting or rejecting a TA. The list is sortable so stronger candidates can be surfaced quickly during live review."),
                BorderLayout.NORTH);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        jobSelector = new JComboBox<String>();
        jobSelector.addActionListener(e -> refreshApplicants());
        applicantSearchField = new JTextField(18);
        top.add(new JLabel("Job Post:"));
        top.add(jobSelector);
        top.add(new JLabel("Filter Applicant:"));
        top.add(applicantSearchField);

        applicantsModel = new DefaultTableModel(
                new String[] {"App ID", "TA", "Email", "Skills", "Match", "Summary", "Status", "Current Hours"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        applicantsTable = new JTable(applicantsModel);
        applicantsTable.setAutoCreateRowSorter(true);
        applicantsTable.setDefaultRenderer(Object.class, new MatchRenderer());
        applicantsTable.setRowHeight(24);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(top, BorderLayout.NORTH);
        center.add(new JScrollPane(applicantsTable), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout(8, 8));
        actions.setOpaque(false);
        applicantSummaryLabel = new JLabel("Applicant summary will appear here after refresh.");
        actions.add(applicantSummaryLabel, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonRow.setOpaque(false);
        JButton acceptButton = new JButton("Select Applicant");
        JButton rejectButton = new JButton("Reject Applicant");
        styleActionButton(acceptButton, ACCENT_COLOR, Color.WHITE);
        styleActionButton(rejectButton, new Color(240, 229, 206), new Color(70, 56, 32));
        buttonRow.add(acceptButton);
        buttonRow.add(rejectButton);
        actions.add(buttonRow, BorderLayout.EAST);
        panel.add(actions, BorderLayout.SOUTH);

        acceptButton.addActionListener(e -> reviewSelectedApplicant("SELECTED"));
        rejectButton.addActionListener(e -> reviewSelectedApplicant("REJECTED"));
        applicantSearchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::refreshApplicants));
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

    private void publishJob() {
        if (ValidationUtils.isBlank(titleField.getText()) || ValidationUtils.isBlank(moduleField.getText())
                || ValidationUtils.isBlank(skillsField.getText())) {
            JOptionPane.showMessageDialog(this, "Title, module and required skills are mandatory.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int hours = ValidationUtils.parseInt(hoursField.getText(), -1);
        if (hours <= 0) {
            JOptionPane.showMessageDialog(this, "Please enter a positive integer for weekly hours.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Job> jobs = FileStorage.loadJobs();
        Job job = new Job();
        job.id = FileStorage.nextJobId();
        job.moId = currentUser.id;
        job.title = titleField.getText().trim();
        job.module = moduleField.getText().trim();
        job.requiredSkills = skillsField.getText().trim();
        job.maxHours = hours;
        job.location = locationField.getText().trim();
        job.description = descriptionArea.getText().trim();
        job.status = "OPEN";
        jobs.add(job);
        FileStorage.saveJobs(jobs);

        titleField.setText("");
        moduleField.setText("");
        skillsField.setText("");
        hoursField.setText("");
        locationField.setText("");
        descriptionArea.setText("");
        postStatusLabel.setText("Posting status: new job published and ready for TA applications.");

        refreshMyJobs();
        refreshJobSelector();
        JOptionPane.showMessageDialog(this, "Job published successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshMyJobs() {
        myJobsModel.setRowCount(0);
        String keyword = myJobsSearchField == null ? "" : myJobsSearchField.getText().trim().toLowerCase();
        int visibleJobs = 0;
        int openJobs = 0;
        for (Job job : FileStorage.loadJobs()) {
            if (job.moId != currentUser.id) {
                continue;
            }
            if (!matchesJobKeyword(job, keyword)) {
                continue;
            }
            myJobsModel.addRow(new Object[] {job.id, job.title, job.module, job.requiredSkills, job.maxHours, job.status});
            visibleJobs++;
            if (job.isOpen()) {
                openJobs++;
            }
        }
        myJobsSummaryLabel.setText("Visible jobs: " + visibleJobs + " | Open jobs: " + openJobs + " | Closed jobs: "
                + Math.max(visibleJobs - openJobs, 0));
    }

    private void toggleSelectedJob() {
        int row = myJobsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a job first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = myJobsTable.convertRowIndexToModel(row);
        int jobId = Integer.parseInt(String.valueOf(myJobsModel.getValueAt(modelRow, 0)));
        List<Job> jobs = FileStorage.loadJobs();
        for (Job job : jobs) {
            if (job.id == jobId) {
                job.status = job.isOpen() ? "CLOSED" : "OPEN";
                postStatusLabel.setText("Posting status: job '" + job.title + "' is now " + job.status + ".");
                break;
            }
        }
        FileStorage.saveJobs(jobs);
        refreshMyJobs();
        refreshJobSelector();
        refreshApplicants();
    }

    private void refreshJobSelector() {
        selectorJobIds.clear();
        jobSelector.removeAllItems();
        for (Job job : FileStorage.loadJobs()) {
            if (job.moId != currentUser.id) {
                continue;
            }
            selectorJobIds.add(job.id);
            jobSelector.addItem(job.title + " - " + job.module + " (" + job.status + ")");
        }
    }

    private void refreshApplicants() {
        applicantsModel.setRowCount(0);
        int selectedJobId = getSelectedJobId();
        if (selectedJobId < 0) {
            applicantSummaryLabel.setText("No job selected yet. Publish or choose a job to inspect applicants.");
            return;
        }
        String keyword = applicantSearchField == null ? "" : applicantSearchField.getText().trim().toLowerCase();

        Map<Integer, TAProfile> profiles = new HashMap<Integer, TAProfile>();
        for (TAProfile profile : FileStorage.loadProfiles()) {
            profiles.put(profile.userId, profile);
        }

        int pending = 0;
        int selected = 0;
        int rejected = 0;
        int strongestScore = -1;
        String strongestName = "";
        for (Application app : FileStorage.loadApplications()) {
            if (app.jobId != selectedJobId) {
                continue;
            }
            User taUser = FileStorage.findUserById(app.taId);
            TAProfile profile = profiles.get(app.taId);
            if (!matchesApplicantKeyword(taUser, profile, app, keyword)) {
                continue;
            }
            applicantsModel.addRow(new Object[] {
                    app.id,
                    taUser == null ? "Unknown" : taUser.getSafeDisplayName(),
                    profile == null ? "N/A" : profile.email,
                    profile == null ? "N/A" : profile.skills,
                    app.matchScore + "%",
                    app.matchSummary,
                    app.status,
                    calculateCurrentHours(app.taId)
            });
            if ("PENDING".equalsIgnoreCase(app.status)) {
                pending++;
            } else if ("SELECTED".equalsIgnoreCase(app.status)) {
                selected++;
            } else if ("REJECTED".equalsIgnoreCase(app.status)) {
                rejected++;
            }
            if (app.matchScore > strongestScore) {
                strongestScore = app.matchScore;
                strongestName = taUser == null ? "Unknown" : taUser.getSafeDisplayName();
            }
        }

        if (applicantsModel.getRowCount() == 0) {
            applicantSummaryLabel.setText("No applicants match the current selection or filter.");
            return;
        }
        applicantSummaryLabel.setText("Pending: " + pending + " | Selected: " + selected + " | Rejected: " + rejected
                + " | Strongest visible fit: " + strongestName + " at " + Math.max(strongestScore, 0) + "%");
    }

    private int getSelectedJobId() {
        int index = jobSelector.getSelectedIndex();
        if (index < 0 || index >= selectorJobIds.size()) {
            return -1;
        }
        return selectorJobIds.get(index);
    }

    private int calculateCurrentHours(int taId) {
        int hours = 0;
        for (Application app : FileStorage.loadApplications()) {
            if (app.taId == taId && "SELECTED".equalsIgnoreCase(app.status)) {
                Job job = FileStorage.findJobById(app.jobId);
                if (job != null) {
                    hours += job.maxHours;
                }
            }
        }
        return hours;
    }

    private boolean matchesJobKeyword(Job job, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        return contains(job.title, keyword)
                || contains(job.module, keyword)
                || contains(job.requiredSkills, keyword)
                || contains(job.status, keyword)
                || contains(job.location, keyword);
    }

    private boolean matchesApplicantKeyword(User taUser, TAProfile profile, Application app, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        return contains(taUser == null ? null : taUser.getSafeDisplayName(), keyword)
                || contains(profile == null ? null : profile.email, keyword)
                || contains(profile == null ? null : profile.skills, keyword)
                || contains(app.status, keyword)
                || contains(app.matchSummary, keyword);
    }

    private boolean contains(String text, String keyword) {
        return text != null && text.toLowerCase().contains(keyword);
    }

    private void reviewSelectedApplicant(String decision) {
        int row = applicantsTable.getSelectedRow();
        int selectedJobId = getSelectedJobId();
        if (row < 0 || selectedJobId < 0) {
            JOptionPane.showMessageDialog(this, "Please select an applicant row.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = applicantsTable.convertRowIndexToModel(row);
        int appId = Integer.parseInt(String.valueOf(applicantsModel.getValueAt(modelRow, 0)));
        int currentHours = Integer.parseInt(String.valueOf(applicantsModel.getValueAt(modelRow, 7)));
        Job job = FileStorage.findJobById(selectedJobId);

        if ("SELECTED".equals(decision) && job != null && currentHours + job.maxHours > FileStorage.getOverloadLimit()) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "This selection would push the TA above the workload threshold of " + FileStorage.getOverloadLimit()
                            + " hours. Continue anyway?",
                    "Workload Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        List<Application> applications = FileStorage.loadApplications();
        for (Application app : applications) {
            if (app.id == appId) {
                app.status = decision;
                app.reviewerNote = "Reviewed by " + currentUser.getSafeDisplayName()
                        + " using " + ScoringService.getActiveProvider().getProviderName();
                break;
            }
        }
        FileStorage.saveApplications(applications);
        refreshApplicants();
    }

    private static class MatchRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            java.awt.Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            if (!isSelected) {
                String status = String.valueOf(table.getValueAt(row, 6));
                int hours = Integer.parseInt(String.valueOf(table.getValueAt(row, 7)));
                if ("SELECTED".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(214, 245, 214));
                } else if ("REJECTED".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(250, 220, 220));
                } else if (hours >= FileStorage.getOverloadLimit()) {
                    component.setBackground(new Color(255, 232, 204));
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
