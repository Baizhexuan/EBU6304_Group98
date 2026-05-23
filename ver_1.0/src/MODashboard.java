import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Module Organiser dashboard for posting jobs, reviewing applicants, and updating decisions.
 *
 * <p>The MO workflow stays directly connected to CSV persistence and the notification service so
 * recruitment decisions remain visible and easy to explain in the final demo.</p>
 */
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
    private JTextField myJobsTitleFilterField;
    private JTextField myJobsModuleFilterField;
    private JTextField myJobsStatusFilterField;
    private JLabel myJobsSummaryLabel;

    private JComboBox<String> jobSelector;
    private List<Integer> selectorJobIds = new ArrayList<Integer>();
    private JTable applicantsTable;
    private DefaultTableModel applicantsModel;
    private JTextField applicantNameFilterField;
    private JTextField applicantEmailFilterField;
    private JTextField applicantSkillsFilterField;
    private JTextField applicantStatusFilterField;
    private JLabel applicantSummaryLabel;
    private JTextArea applicantAiRankingArea;

    /**
     * Constructs and displays the MO dashboard for the given user.
     *
     * <p>Tabs are created for job posting, managing existing posts, and
     * reviewing applicants. A tab-switch listener triggers data refreshes
     * automatically to reflect the latest CSV state.</p>
     *
     * @param currentUser the authenticated Module Organiser user
     */
    public MODashboard(User currentUser) {
        super(currentUser, "MO Dashboard", 1160, 760);
        addTab("Post Job", createPostJobPanel());
        addTab("My Job Posts", createMyJobsPanel());
        addTab("Applicants", createApplicantsPanel());
        installRefreshOnTabSwitch(this::refreshVisibleTab);
        applyCurrentLanguage();
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
                BorderFactory.createLineBorder(BORDER_COLOR),
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
                "Review your open and closed jobs, filter each visible attribute separately, and toggle availability when a role has already been filled or paused."),
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
        styleTable(myJobsTable);

        JPanel filterPanel = new JPanel(new GridLayout(2, 6, 6, 6));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("Job ID"));
        filterPanel.add(new JLabel("Title"));
        filterPanel.add(new JLabel("Module"));
        filterPanel.add(new JLabel("Skills"));
        filterPanel.add(new JLabel("Hours"));
        filterPanel.add(new JLabel("Status"));
        filterPanel.add(new JLabel(""));
        myJobsTitleFilterField = new JTextField();
        filterPanel.add(myJobsTitleFilterField);
        myJobsModuleFilterField = new JTextField();
        filterPanel.add(myJobsModuleFilterField);
        filterPanel.add(new JLabel(""));
        filterPanel.add(new JLabel(""));
        myJobsStatusFilterField = new JTextField();
        filterPanel.add(myJobsStatusFilterField);

        FilterToolbar compactFilter = new FilterToolbar("Search job posts", this::refreshMyJobs);
        compactFilter.addField("Title", myJobsTitleFilterField);
        compactFilter.addField("Module", myJobsModuleFilterField);
        compactFilter.addField("Status", myJobsStatusFilterField);
        JPanel filters = new JPanel(new BorderLayout(6, 6));
        filters.setOpaque(false);
        filters.add(compactFilter, BorderLayout.NORTH);
        filters.add(filterPanel, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(filters, BorderLayout.NORTH);
        center.add(new JScrollPane(myJobsTable), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);
        myJobsSummaryLabel = new JLabel("Job summary will appear here after refresh.");
        bottom.add(myJobsSummaryLabel, BorderLayout.CENTER);

        JPanel actions = buildActionRow();
        JButton refreshButton = new JButton("Refresh");
        JButton toggleButton = new JButton("Open / Close Selected Job");
        styleActionButton(refreshButton, SECONDARY_SURFACE, ACCENT_COLOR);
        styleActionButton(toggleButton, WARNING_SURFACE, new Color(101, 73, 30));
        actions.add(refreshButton);
        actions.add(toggleButton);
        bottom.add(actions, BorderLayout.EAST);
        panel.add(bottom, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshMyJobs());
        toggleButton.addActionListener(e -> toggleSelectedJob());
        installFieldListener(myJobsTitleFilterField, this::refreshMyJobs);
        installFieldListener(myJobsModuleFilterField, this::refreshMyJobs);
        installFieldListener(myJobsStatusFilterField, this::refreshMyJobs);
        return panel;
    }

    private JPanel createApplicantsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(APP_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panel.add(buildSectionIntro(
                "Applicant Review",
                "Inspect match scores, missing skills, and current workload before selecting or rejecting a TA. MO decisions now generate in-app notifications for the applicant automatically."),
                BorderLayout.NORTH);

        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPanel.setOpaque(false);
        jobSelector = new JComboBox<String>();
        jobSelector.addActionListener(e -> refreshApplicants());
        selectorPanel.add(new JLabel("Job Post:"));
        selectorPanel.add(jobSelector);

        applicantsModel = new DefaultTableModel(
                new String[] {"App ID", "TA", "Email", "Skills", "Match", "Missing Skills", "Summary", "Status", "Reputation", "Current Hours"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        applicantsTable = new JTable(applicantsModel);
        applicantsTable.setAutoCreateRowSorter(true);
        applicantsTable.setDefaultRenderer(Object.class, new MatchRenderer());
        styleTable(applicantsTable);

        JPanel filterPanel = new JPanel(new GridLayout(2, 10, 6, 6));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("App ID"));
        filterPanel.add(new JLabel("TA"));
        filterPanel.add(new JLabel("Email"));
        filterPanel.add(new JLabel("Skills"));
        filterPanel.add(new JLabel("Match"));
        filterPanel.add(new JLabel("Missing Skills"));
        filterPanel.add(new JLabel("Summary"));
        filterPanel.add(new JLabel("Status"));
        filterPanel.add(new JLabel("Reputation"));
        filterPanel.add(new JLabel("Current Hours"));
        filterPanel.add(new JLabel(""));
        applicantNameFilterField = new JTextField();
        filterPanel.add(applicantNameFilterField);
        applicantEmailFilterField = new JTextField();
        filterPanel.add(applicantEmailFilterField);
        applicantSkillsFilterField = new JTextField();
        filterPanel.add(applicantSkillsFilterField);
        filterPanel.add(new JLabel(""));
        filterPanel.add(new JLabel(""));
        filterPanel.add(new JLabel(""));
        applicantStatusFilterField = new JTextField();
        filterPanel.add(applicantStatusFilterField);
        filterPanel.add(new JLabel(""));
        filterPanel.add(new JLabel(""));

        FilterToolbar compactFilter = new FilterToolbar("Search applicants", this::refreshApplicants);
        compactFilter.addField("TA", applicantNameFilterField);
        compactFilter.addField("Email", applicantEmailFilterField);
        compactFilter.addField("Skills", applicantSkillsFilterField);
        compactFilter.addField("Status", applicantStatusFilterField);
        JPanel filters = new JPanel(new BorderLayout(6, 6));
        filters.setOpaque(false);
        filters.add(selectorPanel, BorderLayout.NORTH);
        filters.add(compactFilter, BorderLayout.CENTER);
        filters.add(filterPanel, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(filters, BorderLayout.NORTH);

        applicantAiRankingArea = new JTextArea();
        applicantAiRankingArea.setEditable(false);
        applicantAiRankingArea.setLineWrap(true);
        applicantAiRankingArea.setWrapStyleWord(true);
        applicantAiRankingArea.setBackground(SURFACE_COLOR);
        applicantAiRankingArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel aiPanel = new JPanel(new BorderLayout(6, 6));
        aiPanel.setBackground(SURFACE_COLOR);
        aiPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        aiPanel.add(new JLabel("AI Applicant Ranking"), BorderLayout.NORTH);
        aiPanel.add(new JScrollPane(applicantAiRankingArea), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(applicantsTable), aiPanel);
        splitPane.setResizeWeight(0.7);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        center.add(splitPane, BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout(8, 8));
        actions.setOpaque(false);
        applicantSummaryLabel = new JLabel("Applicant summary will appear here after refresh.");
        actions.add(applicantSummaryLabel, BorderLayout.CENTER);

        JPanel buttonRow = buildActionRow();
        JButton acceptButton = new JButton("Select Applicant");
        JButton rejectButton = new JButton("Reject Applicant");
        JButton rateButton = new JButton("Rate Completed Work");
        JButton aiButton = new JButton("Ask AI About Applicants");
        styleActionButton(acceptButton, ACCENT_COLOR, Color.WHITE);
        styleActionButton(rejectButton, WARNING_SURFACE, new Color(101, 73, 30));
        styleActionButton(rateButton, SUCCESS_SURFACE, new Color(35, 82, 55));
        styleActionButton(aiButton, SUCCESS_SURFACE, new Color(35, 82, 55));
        buttonRow.add(acceptButton);
        buttonRow.add(rejectButton);
        buttonRow.add(rateButton);
        buttonRow.add(aiButton);
        actions.add(buttonRow, BorderLayout.EAST);
        panel.add(actions, BorderLayout.SOUTH);

        acceptButton.addActionListener(e -> reviewSelectedApplicant("SELECTED"));
        rejectButton.addActionListener(e -> reviewSelectedApplicant("REJECTED"));
        rateButton.addActionListener(e -> rateSelectedCompletedWork());
        aiButton.addActionListener(e -> openMoAiAssistantDialog());
        installFieldListener(applicantNameFilterField, this::refreshApplicants);
        installFieldListener(applicantEmailFilterField, this::refreshApplicants);
        installFieldListener(applicantSkillsFilterField, this::refreshApplicants);
        installFieldListener(applicantStatusFilterField, this::refreshApplicants);
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

    private void installFieldListener(JTextField field, Runnable action) {
        field.addActionListener(e -> action.run());
    }

    private void refreshVisibleTab() {
        int index = tabs.getSelectedIndex();
        if (index == 1) {
            refreshMyJobs();
        } else if (index == 2) {
            refreshJobSelector();
            refreshApplicants();
        }
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
        if (hours > FileStorage.getOverloadLimit()) {
            JOptionPane.showMessageDialog(this,
                    "Weekly hours must not exceed " + FileStorage.getOverloadLimit()
                            + ". Please split very large workloads into smaller posts.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
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
        beginRefreshFeedback(myJobsSummaryLabel, myJobsModel, null, "Refreshing your job posts...");
        try {
            String titleFilter = getLower(myJobsTitleFilterField);
            String moduleFilter = getLower(myJobsModuleFilterField);
            String statusFilter = getLower(myJobsStatusFilterField);
            int visibleJobs = 0;
            int openJobs = 0;
            for (Job job : FileStorage.loadJobs()) {
                if (job.moId != currentUser.id) {
                    continue;
                }
                if (!contains(job.title, titleFilter) || !contains(job.module, moduleFilter)
                        || !contains(job.status, statusFilter)) {
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
        } finally {
            endRefreshFeedback();
        }
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
                boolean closingJob = job.isOpen();
                job.status = closingJob ? "CLOSED" : "OPEN";
                if (closingJob) {
                    int notified = NotificationService.notifyJobClosed(job, currentUser);
                    postStatusLabel.setText("Posting status: job '" + job.title + "' is now CLOSED. Notifications sent: " + notified + ".");
                } else {
                    int updated = NotificationService.markJobClosureNotificationsRead(job);
                    postStatusLabel.setText("Posting status: job '" + job.title
                            + "' is now OPEN. Old closure notifications marked read: " + updated + ".");
                }
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
        beginRefreshFeedback(applicantSummaryLabel, applicantsModel, applicantAiRankingArea, "Refreshing applicants and match ranking...");
        try {
            int selectedJobId = getSelectedJobId();
            if (selectedJobId < 0) {
                applicantSummaryLabel.setText("No job selected yet. Publish or choose a job to inspect applicants.");
                return;
            }
            String nameFilter = getLower(applicantNameFilterField);
            String emailFilter = getLower(applicantEmailFilterField);
            String skillsFilter = getLower(applicantSkillsFilterField);
            String statusFilter = getLower(applicantStatusFilterField);

            Map<Integer, TAProfile> profiles = new HashMap<Integer, TAProfile>();
            for (TAProfile profile : FileStorage.loadProfiles()) {
                profiles.put(profile.userId, profile);
            }
            Map<Integer, User> usersById = new HashMap<Integer, User>();
            for (User user : FileStorage.loadUsers()) {
                usersById.put(user.id, user);
            }
            Map<Integer, Job> jobsById = new HashMap<Integer, Job>();
            for (Job job : FileStorage.loadJobs()) {
                jobsById.put(job.id, job);
            }
            Map<Integer, Integer> selectedHoursByTa = new HashMap<Integer, Integer>();
            for (Application app : FileStorage.loadApplications()) {
                if ("SELECTED".equalsIgnoreCase(app.status)) {
                    Job job = jobsById.get(app.jobId);
                    if (job != null) {
                        Integer hours = selectedHoursByTa.get(app.taId);
                        selectedHoursByTa.put(app.taId, (hours == null ? 0 : hours) + job.maxHours);
                    }
                }
            }

            int pending = 0;
            int selected = 0;
            int rejected = 0;
            int strongestScore = -1;
            String strongestName = "";
            Job selectedJob = jobsById.get(selectedJobId);
            for (Application app : FileStorage.loadApplications()) {
                if (app.jobId != selectedJobId) {
                    continue;
                }
                User taUser = usersById.get(app.taId);
                TAProfile profile = profiles.get(app.taId);
                MatchResult currentMatch = evaluateCurrentMatch(profile, selectedJob, app);
                String displayName = taUser == null ? "Unknown" : taUser.getSafeDisplayName();
                String email = profile == null ? "N/A" : profile.email;
                String skills = profile == null ? "N/A" : profile.skills;
                if (!contains(displayName, nameFilter) || !contains(email, emailFilter)
                        || !contains(skills, skillsFilter) || !contains(app.status, statusFilter)) {
                    continue;
                }
                applicantsModel.addRow(new Object[] {
                        app.id,
                        displayName,
                        email,
                        skills,
                        currentMatch.score + "%",
                        extractMissingSkills(currentMatch.summary),
                        currentMatch.summary,
                        app.status,
                        ReputationService.describeReputation(app.taId),
                        selectedHoursByTa.containsKey(app.taId) ? selectedHoursByTa.get(app.taId) : 0
                });
                if ("PENDING".equalsIgnoreCase(app.status)) {
                    pending++;
                } else if ("SELECTED".equalsIgnoreCase(app.status)) {
                    selected++;
                } else if ("REJECTED".equalsIgnoreCase(app.status)) {
                    rejected++;
                }
                if (currentMatch.score > strongestScore) {
                    strongestScore = currentMatch.score;
                    strongestName = displayName;
                }
            }

            if (applicantAiRankingArea != null) {
                applicantAiRankingArea.setText(BoardAIInsightsService.buildMoApplicantRanking(currentUser, selectedJobId, 5));
                applicantAiRankingArea.setCaretPosition(0);
            }
            if (applicantsModel.getRowCount() == 0) {
                applicantSummaryLabel.setText("No applicants match the current selection or filters.");
                return;
            }
            applicantSummaryLabel.setText("Pending: " + pending + " | Selected: " + selected + " | Rejected: " + rejected
                    + " | Strongest visible fit: " + strongestName + " at " + Math.max(strongestScore, 0) + "%");
        } finally {
            endRefreshFeedback();
        }
    }

    private MatchResult evaluateCurrentMatch(TAProfile profile, Job job, Application application) {
        if (profile != null && job != null) {
            return ScoringService.evaluate(profile, job);
        }
        return new MatchResult(application.matchScore, application.matchSummary);
    }

    private void openMoAiAssistantDialog() {
        AIConversationDialog dialog = new AIConversationDialog(
                this,
                "MO AI Matching Assistant",
                "Ask AI about the best TA candidates for this job",
                "Which applicant is the strongest fit for the selected job, and what risks should I review before deciding?",
                BoardAIInsightsService.buildMoAiContext(currentUser, getSelectedJobId()));
        dialog.setVisible(true);
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

    private void reviewSelectedApplicant(String decision) {
        int row = applicantsTable.getSelectedRow();
        int selectedJobId = getSelectedJobId();
        if (row < 0 || selectedJobId < 0) {
            JOptionPane.showMessageDialog(this, "Please select an applicant row.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = applicantsTable.convertRowIndexToModel(row);
        int appId = Integer.parseInt(String.valueOf(applicantsModel.getValueAt(modelRow, 0)));
        int currentHours = Integer.parseInt(String.valueOf(applicantsModel.getValueAt(modelRow, 9)));
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
                NotificationService.notifyApplicationDecision(app, currentUser, job, decision);
                break;
            }
        }
        FileStorage.saveApplications(applications);
        refreshApplicants();
        postStatusLabel.setText("Posting status: applicant review updated and notification sent.");
        refreshBellBadge();
    }

    private void rateSelectedCompletedWork() {
        // Entry point for the post-work feedback loop. The MO must first select one row in the
        // applicants table, and only SELECTED applications can receive a completion rating.
        int row = applicantsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a selected applicant first.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = applicantsTable.convertRowIndexToModel(row);
        int appId = ValidationUtils.parseInt(String.valueOf(applicantsModel.getValueAt(modelRow, 0)), 0);
        String status = String.valueOf(applicantsModel.getValueAt(modelRow, 7));
        if (!"SELECTED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Only selected applications can be rated after completed work.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // One completed work record should be rated once. This prevents repeated penalties from
        // the same job/application and makes the reputation history fairer.
        if (ReputationService.hasEvaluationForApplication(appId)) {
            JOptionPane.showMessageDialog(this, "This completed work has already been rated.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object value = JOptionPane.showInputDialog(this,
                "Completion rating for this TA's finished work (1 = very poor, 5 = excellent):",
                "Rate Completed Work",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[] {"5", "4", "3", "2", "1"},
                "5");
        if (value == null) {
            return;
        }
        // The rating is intentionally constrained to 1-5 so the penalty rule can be explained:
        // ratings 1-2 are low quality; ratings 3-5 do not trigger reputation punishment.
        int rating = ValidationUtils.parseInt(String.valueOf(value), 0);
        if (rating < 1 || rating > 5) {
            JOptionPane.showMessageDialog(this, "Rating must be between 1 and 5.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String comment = JOptionPane.showInputDialog(this,
                "Optional MO comment for this evaluation:",
                "Completed Work Comment",
                JOptionPane.QUESTION_MESSAGE);
        if (comment == null) {
            comment = "";
        }

        Application application = findApplicationById(FileStorage.loadApplications(), appId);
        Job job = application == null ? null : FileStorage.findJobById(application.jobId);
        if (application == null || job == null) {
            JOptionPane.showMessageDialog(this, "Application or job data is missing.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Build a persistent evaluation record before applying any reputation change.
        WorkEvaluation evaluation = new WorkEvaluation();
        evaluation.id = FileStorage.nextWorkEvaluationId();
        evaluation.applicationId = application.id;
        evaluation.taId = application.taId;
        evaluation.moId = currentUser.id;
        evaluation.jobId = application.jobId;
        evaluation.rating = rating;
        evaluation.comment = comment.trim();
        evaluation.evaluatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // ReputationService decides whether the rating is serious enough to penalise future
        // matching. MODashboard only collects the rating and persists the evaluation.
        boolean penaltyApplied = ReputationService.applyCompletedWorkEvaluation(evaluation, application);
        List<WorkEvaluation> evaluations = FileStorage.loadWorkEvaluations();
        evaluations.add(evaluation);
        FileStorage.saveWorkEvaluations(evaluations);

        // Notify the TA whether a normal rating or a penalty-related rating was recorded.
        NotificationService.notifyWorkEvaluation(application, currentUser, job, rating, penaltyApplied,
                ReputationService.getScoreForTa(application.taId));

        String message = "Completed work rating saved.";
        if (penaltyApplied) {
            message += " Reputation penalty applied because the original match score was high but the MO rating was low.";
        }
        JOptionPane.showMessageDialog(this, message, "Evaluation Saved", JOptionPane.INFORMATION_MESSAGE);
        refreshApplicants();
        refreshBellBadge();
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

    private Application findApplicationById(List<Application> applications, int appId) {
        for (Application application : applications) {
            if (application.id == appId) {
                return application;
            }
        }
        return null;
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

    private static class MatchRenderer extends BaseDashboard.WrappingTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            java.awt.Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            if (!isSelected) {
                String status = String.valueOf(table.getValueAt(row, 7));
                int hours = Integer.parseInt(String.valueOf(table.getValueAt(row, 9)));
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
