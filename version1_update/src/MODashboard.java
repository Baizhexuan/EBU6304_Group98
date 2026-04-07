import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * MODashboard: Dashboard for Module Organiser users.
 *
 * Tab 1 - Post a Job   : Fill a form and publish a new job posting.
 * Tab 2 - My Job Posts : View and manage jobs posted by this MO.
 * Tab 3 - View Applicants: Select a job, see applicants, accept or reject them.
 */
public class MODashboard extends JFrame {

    private final User currentUser;
    private JTabbedPane tabs;

    // Post-Job tab
    private JTextField titleField, moduleField, skillsField, hoursField;
    private JTextArea  descArea;

    // My-Jobs tab
    private JTable           jobTable;
    private DefaultTableModel jobModel;

    // Applicants tab
    private JComboBox<String> jobSelector;
    private JTable            appTable;
    private DefaultTableModel appModel;
    // maps combobox index → job id
    private final List<Integer> selectorJobIds = new ArrayList<>();

    public MODashboard(User user) {
        this.currentUser = user;
        setTitle("MO Dashboard  –  " + user.username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);

        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("Account");
        JMenuItem logout = new JMenuItem("Logout");
        // Version1: Confirm logout and return to the login screen for account switching.
        logout.addActionListener(e -> logoutToLogin());
        menu.add(logout);
        mb.add(menu);
        setJMenuBar(mb);

        tabs = new JTabbedPane();
        tabs.addTab("Post a Job",       createPostJobPanel());
        tabs.addTab("My Job Posts",     createMyJobsPanel());
        tabs.addTab("View Applicants",  createApplicantsPanel());

        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 1) refreshMyJobs();
            if (idx == 2) refreshJobSelector();
        });

        add(tabs);
        refreshMyJobs();
        setVisible(true);
    }

    private void logoutToLogin() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Log out and return to the login screen?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame();
        }
    }

    // ─── Post Job Panel ───────────────────────────────────────
    private JPanel createPostJobPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        JLabel header = new JLabel("Publish a New TA Job");
        header.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2;
        panel.add(header, gbc);
        gbc.gridwidth = 1;

        titleField  = addRow(panel, gbc, "Job Title:",                  y++);
        moduleField = addRow(panel, gbc, "Module Code:",                y++);
        skillsField = addRow(panel, gbc, "Required Skills (semicolon):", y++);
        hoursField  = addRow(panel, gbc, "Max Hours/Week:",             y++);

        gbc.gridx = 0; gbc.gridy = y;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Job Description:"), gbc);
        descArea = new JTextArea(5, 22);
        descArea.setLineWrap(true);
        gbc.gridx = 1; gbc.gridy = y++; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JScrollPane(descArea), gbc);

        JButton postBtn = new JButton("Post Job");
        postBtn.setPreferredSize(new Dimension(120, 30));
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(postBtn, gbc);
        postBtn.addActionListener(e -> postJob());

        return panel;
    }

    private void postJob() {
        String title  = titleField.getText().trim();
        String module = moduleField.getText().trim();
        String skills = skillsField.getText().trim();
        String desc   = descArea.getText().trim();
        if (title.isEmpty() || module.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Job Title and Module Code are required.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int hours = 0;
        try { hours = Integer.parseInt(hoursField.getText().trim()); } catch (NumberFormatException ignored) {}

        List<Job> jobs = FileStorage.loadJobs();
        int maxId = jobs.stream().mapToInt(j -> j.id).max().orElse(0);
        Job job = new Job();
        job.id            = maxId + 1;
        job.moId          = currentUser.id;
        job.title         = title;
        job.module        = module;
        job.description   = desc;
        job.requiredSkills = skills;
        job.maxHours      = hours;
        job.status        = "OPEN";
        jobs.add(job);
        FileStorage.saveJobs(jobs);

        // Clear form
        titleField.setText(""); moduleField.setText(""); skillsField.setText("");
        hoursField.setText(""); descArea.setText("");

        JOptionPane.showMessageDialog(this, "Job \"" + title + "\" posted successfully!");
        refreshMyJobs();
    }

    // ─── My Jobs Panel ────────────────────────────────────────
    private JPanel createMyJobsPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] cols = {"ID", "Title", "Module", "Required Skills", "Max Hours", "Status"};
        jobModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        jobTable = new JTable(jobModel);
        jobTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(jobTable), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        JButton closeBtn   = new JButton("Close Selected Job");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refreshBtn); south.add(closeBtn);
        panel.add(south, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> refreshMyJobs());
        closeBtn.addActionListener(e -> closeSelectedJob());
        return panel;
    }

    private void refreshMyJobs() {
        jobModel.setRowCount(0);
        for (Job j : FileStorage.loadJobs()) {
            if (j.moId == currentUser.id) {
                jobModel.addRow(new Object[]{j.id, j.title, j.module, j.requiredSkills, j.maxHours, j.status});
            }
        }
    }

    private void closeSelectedJob() {
        int row = jobTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a job."); return; }
        int jobId = (int) jobModel.getValueAt(row, 0);

        List<Job> jobs = FileStorage.loadJobs();
        for (Job j : jobs) {
            if (j.id == jobId) { j.status = "CLOSED"; break; }
        }
        FileStorage.saveJobs(jobs);
        refreshMyJobs();
        JOptionPane.showMessageDialog(this, "Job closed. New applications will no longer be accepted.");
    }

    // ─── Applicants Panel ────────────────────────────────────
    private JPanel createApplicantsPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Top: job selector
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.add(new JLabel("Select Job:"));
        jobSelector = new JComboBox<>();
        topBar.add(jobSelector);
        JButton loadBtn = new JButton("Load Applicants");
        topBar.add(loadBtn);
        panel.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"App ID", "TA Username", "Full Name", "Skills", "GPA", "CV Path", "Status"};
        appModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        appTable = new JTable(appModel);
        appTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(appTable), BorderLayout.CENTER);

        // Bottom: action buttons
        JButton selectBtn = new JButton("✔  Select Applicant");
        JButton rejectBtn = new JButton("✘  Reject Applicant");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(selectBtn); south.add(rejectBtn);
        panel.add(south, BorderLayout.SOUTH);

        loadBtn.addActionListener(e -> loadApplicants());
        selectBtn.addActionListener(e -> updateStatus("SELECTED"));
        rejectBtn.addActionListener(e -> updateStatus("REJECTED"));
        return panel;
    }

    private void refreshJobSelector() {
        jobSelector.removeAllItems();
        selectorJobIds.clear();
        for (Job j : FileStorage.loadJobs()) {
            if (j.moId == currentUser.id) {
                jobSelector.addItem(j.id + "  –  " + j.title + "  [" + j.status + "]");
                selectorJobIds.add(j.id);
            }
        }
    }

    private void loadApplicants() {
        appModel.setRowCount(0);
        int idx = jobSelector.getSelectedIndex();
        if (idx < 0 || idx >= selectorJobIds.size()) return;
        int jobId = selectorJobIds.get(idx);

        List<User>      users    = FileStorage.loadUsers();
        List<TAProfile> profiles = FileStorage.loadProfiles();

        Map<Integer, User>      userMap    = new HashMap<>();
        Map<Integer, TAProfile> profileMap = new HashMap<>();
        for (User u : users)        userMap.put(u.id, u);
        for (TAProfile p : profiles) profileMap.put(p.userId, p);

        for (Application a : FileStorage.loadApplications()) {
            if (a.jobId == jobId) {
                User u      = userMap.get(a.taId);
                TAProfile p = profileMap.get(a.taId);
                appModel.addRow(new Object[]{
                        a.id,
                        u != null ? u.username : "?",
                        p != null ? p.fullName  : "N/A",
                        p != null ? p.skills    : "N/A",
                        p != null ? p.gpa       : 0.0,
                        p != null ? p.cvPath    : "N/A",
                        a.status
                });
            }
        }
    }

    private void updateStatus(String newStatus) {
        int row = appTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select an applicant."); return; }
        int appId = (int) appModel.getValueAt(row, 0);

        List<Application> apps = FileStorage.loadApplications();
        for (Application a : apps) {
            if (a.id == appId) { a.status = newStatus; break; }
        }
        FileStorage.saveApplications(apps);
        loadApplicants();
        JOptionPane.showMessageDialog(this, "Application status updated to: " + newStatus);
    }

    // ─── Helper ──────────────────────────────────────────────
    private JTextField addRow(JPanel panel, GridBagConstraints gbc, String label, int y) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), gbc);
        JTextField f = new JTextField(22);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(f, gbc);
        return f;
    }
}
