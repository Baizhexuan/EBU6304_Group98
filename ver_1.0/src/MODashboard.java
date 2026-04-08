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
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class MODashboard extends JFrame {
    private final User currentUser;
    private JTextField titleField;
    private JTextField moduleField;
    private JTextField skillsField;
    private JTextField hoursField;
    private JTextField locationField;
    private JTextArea descriptionArea;
    private JTable myJobsTable;
    private DefaultTableModel myJobsModel;
    private JTextField myJobsSearchField;
    private JComboBox<String> jobSelector;
    private List<Integer> selectorJobIds = new ArrayList<Integer>();
    private JTable applicantsTable;
    private DefaultTableModel applicantsModel;
    private JTextField applicantSearchField;

    public MODashboard(User currentUser) {
        this.currentUser = currentUser;
        setTitle("MO Dashboard - " + currentUser.getSafeDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 700);
        setLocationRelativeTo(null);

        JMenuBar bar = new JMenuBar();
        JMenu accountMenu = new JMenu("Account");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        accountMenu.add(logoutItem);
        bar.add(accountMenu);
        setJMenuBar(bar);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Post Job", createPostJobPanel());
        tabs.addTab("My Job Posts", createMyJobsPanel());
        tabs.addTab("Applicants", createApplicantsPanel());
        tabs.addChangeListener(e -> {
            refreshMyJobs();
            refreshJobSelector();
            refreshApplicants();
        });

        add(tabs);
        refreshMyJobs();
        refreshJobSelector();
        refreshApplicants();
        setVisible(true);
    }

    private JPanel createPostJobPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
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

        addRow(panel, gbc, 0, "Job Title:", titleField);
        addRow(panel, gbc, 1, "Module Code:", moduleField);
        addRow(panel, gbc, 2, "Required Skills:", skillsField);
        addRow(panel, gbc, 3, "Max Hours/Week:", hoursField);
        addRow(panel, gbc, 4, "Location:", locationField);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(descriptionArea), gbc);

        JButton postButton = new JButton("Publish Job");
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(postButton, gbc);
        postButton.addActionListener(e -> publishJob());
        return panel;
    }

    private JPanel createMyJobsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        myJobsModel = new DefaultTableModel(new String[] {"Job ID", "Title", "Module", "Skills", "Hours", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        myJobsTable = new JTable(myJobsModel);
        myJobsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel topBar = new JPanel(new BorderLayout(6, 6));
        topBar.add(new JLabel("Search My Jobs:"), BorderLayout.WEST);
        myJobsSearchField = new JTextField();
        topBar.add(myJobsSearchField, BorderLayout.CENTER);
        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(myJobsTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        JButton toggleButton = new JButton("Open / Close Selected Job");
        actions.add(refreshButton);
        actions.add(toggleButton);
        panel.add(actions, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshMyJobs());
        toggleButton.addActionListener(e -> toggleSelectedJob());
        myJobsSearchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::refreshMyJobs));
        return panel;
    }

    private JPanel createApplicantsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Job Post:"));
        jobSelector = new JComboBox<String>();
        jobSelector.addActionListener(e -> refreshApplicants());
        top.add(jobSelector);
        top.add(new JLabel("Filter Applicant:"));
        applicantSearchField = new JTextField(18);
        top.add(applicantSearchField);
        panel.add(top, BorderLayout.NORTH);

        applicantsModel = new DefaultTableModel(
                new String[] {"App ID", "TA", "Email", "Skills", "Match", "Summary", "Status", "Current Hours"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        applicantsTable = new JTable(applicantsModel);
        applicantsTable.setDefaultRenderer(Object.class, new MatchRenderer());
        applicantsTable.setRowHeight(24);
        panel.add(new JScrollPane(applicantsTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton acceptButton = new JButton("Select Applicant");
        JButton rejectButton = new JButton("Reject Applicant");
        actions.add(acceptButton);
        actions.add(rejectButton);
        panel.add(actions, BorderLayout.SOUTH);

        acceptButton.addActionListener(e -> reviewSelectedApplicant("SELECTED"));
        rejectButton.addActionListener(e -> reviewSelectedApplicant("REJECTED"));
        applicantSearchField.getDocument().addDocumentListener(new SimpleDocumentListener(this::refreshApplicants));
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

        refreshMyJobs();
        refreshJobSelector();
        JOptionPane.showMessageDialog(this, "Job published successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshMyJobs() {
        myJobsModel.setRowCount(0);
        String keyword = myJobsSearchField == null ? "" : myJobsSearchField.getText().trim().toLowerCase();
        for (Job job : FileStorage.loadJobs()) {
            if (job.moId != currentUser.id) {
                continue;
            }
            if (!matchesJobKeyword(job, keyword)) {
                continue;
            }
            myJobsModel.addRow(new Object[] {job.id, job.title, job.module, job.requiredSkills, job.maxHours, job.status});
        }
    }

    private void toggleSelectedJob() {
        int row = myJobsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a job first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int jobId = Integer.parseInt(String.valueOf(myJobsModel.getValueAt(row, 0)));
        List<Job> jobs = FileStorage.loadJobs();
        for (Job job : jobs) {
            if (job.id == jobId) {
                job.status = job.isOpen() ? "CLOSED" : "OPEN";
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
            return;
        }
        String keyword = applicantSearchField == null ? "" : applicantSearchField.getText().trim().toLowerCase();

        Map<Integer, TAProfile> profiles = new HashMap<Integer, TAProfile>();
        for (TAProfile profile : FileStorage.loadProfiles()) {
            profiles.put(profile.userId, profile);
        }

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
        }
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

        int appId = Integer.parseInt(String.valueOf(applicantsModel.getValueAt(row, 0)));
        int currentHours = Integer.parseInt(String.valueOf(applicantsModel.getValueAt(row, 7)));
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
                app.reviewerNote = "Reviewed by " + currentUser.getSafeDisplayName();
                break;
            }
        }
        FileStorage.saveApplications(applications);
        refreshApplicants();
    }

    private void logout() {
        dispose();
        new LoginFrame();
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
