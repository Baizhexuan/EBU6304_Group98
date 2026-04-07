import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * AdminDashboard: Dashboard for Admin users.
 *
 * Tab 1 - TA Workload Monitor : Shows each TA's application count,
 *         number of accepted jobs, and total committed hours.
 *         Highlights TAs over the informal 20-hour threshold.
 * Tab 2 - All Applications    : Full application list across all TAs/jobs.
 * Tab 3 - All Jobs            : Full job listing across all MOs.
 */
public class AdminDashboard extends JFrame {

    private final User currentUser;

    private DefaultTableModel workloadModel;
    private DefaultTableModel allAppsModel;
    private DefaultTableModel allJobsModel;

    // Version1: Keep stable row-to-entity mappings so inline edits can be synced back to CSV.
    private final List<Integer> workloadUserIds = new ArrayList<>();
    private boolean isRefreshingWorkload;
    private boolean isRefreshingAllApps;
    private boolean isRefreshingAllJobs;
    private boolean workloadDirty;
    private boolean allAppsDirty;
    private boolean allJobsDirty;

    public AdminDashboard(User user) {
        this.currentUser = user;
        setTitle("Admin Dashboard  –  " + user.username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 620);
        setLocationRelativeTo(null);

        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("Account");
        JMenuItem logout = new JMenuItem("Logout");
        // Version1: Confirm logout and return to the login screen for account switching.
        logout.addActionListener(e -> logoutToLogin());
        menu.add(logout);
        mb.add(menu);
        setJMenuBar(mb);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("TA Workload Monitor", createWorkloadPanel());
        tabs.addTab("All Applications",   createAllAppsPanel());
        tabs.addTab("All Jobs",           createAllJobsPanel());

        add(tabs);
        refreshWorkload();
        refreshAllApps();
        refreshAllJobs();
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

    // ─── Workload Panel ───────────────────────────────────────
    private JPanel createWorkloadPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel note = new JLabel(
            "  Version1: Edit the row, then click Save Changes. Undo Changes reloads CSV; workload totals remain auto-calculated.",
                SwingConstants.LEFT);
        note.setForeground(Color.DARK_GRAY);
        panel.add(note, BorderLayout.NORTH);

        String[] cols = {"TA Username", "Full Name", "Email", "Total Apps", "Selected Jobs", "Total Hours"};
        workloadModel = new DefaultTableModel(cols, 0) {
            // Version1: Identity columns are editable; workload metrics stay derived from applications/jobs.
            public boolean isCellEditable(int r, int c) { return c <= 2; }
        };
        workloadModel.addTableModelListener(this::trackWorkloadDirty);
        JTable table = new JTable(workloadModel);
        table.setDefaultRenderer(Object.class, new WorkloadRenderer());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        JButton saveBtn = new JButton("Save Changes");
        JButton undoBtn = new JButton("Undo Changes");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refreshBtn);
        south.add(saveBtn);
        south.add(undoBtn);
        panel.add(south, BorderLayout.SOUTH);
        refreshBtn.addActionListener(e -> reloadWorkloadWithPrompt());
        saveBtn.addActionListener(e -> saveWorkloadChanges());
        undoBtn.addActionListener(e -> undoWorkloadChanges());
        return panel;
    }

    private void refreshWorkload() {
        isRefreshingWorkload = true;
        workloadModel.setRowCount(0);
        workloadUserIds.clear();
        List<User>        users    = FileStorage.loadUsers();
        List<Application> apps     = FileStorage.loadApplications();
        List<Job>         jobs     = FileStorage.loadJobs();
        List<TAProfile>   profiles = FileStorage.loadProfiles();

        Map<Integer, String>      fullNames = new HashMap<>();
        Map<Integer, String>      emails    = new HashMap<>();
        Map<Integer, Job>         jobMap    = new HashMap<>();

        for (TAProfile p : profiles) { fullNames.put(p.userId, p.fullName); emails.put(p.userId, p.email); }
        for (Job j : jobs)            jobMap.put(j.id, j);

        for (User u : users) {
            if (!"TA".equals(u.role)) continue;
            int totalApps = 0, selected = 0, totalHours = 0;
            for (Application a : apps) {
                if (a.taId == u.id) {
                    totalApps++;
                    if ("SELECTED".equals(a.status)) {
                        selected++;
                        Job j = jobMap.get(a.jobId);
                        if (j != null) totalHours += j.maxHours;
                    }
                }
            }
            workloadModel.addRow(new Object[]{
                    u.username,
                    fullNames.getOrDefault(u.id, "N/A"),
                    emails.getOrDefault(u.id,    "N/A"),
                    totalApps, selected, totalHours
            });
            workloadUserIds.add(u.id);
        }
        workloadDirty = false;
        isRefreshingWorkload = false;
    }

    // ─── All Applications Panel ───────────────────────────────
    private JPanel createAllAppsPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel note = new JLabel(
            "  Version1: Edit the table, then click Save Changes. Undo Changes restores the last CSV version.",
            SwingConstants.LEFT);
        note.setForeground(Color.DARK_GRAY);
        panel.add(note, BorderLayout.NORTH);

        String[] cols = {"App ID", "TA Username", "Job Title", "Module", "MO", "Status", "Applied At"};
        allAppsModel = new DefaultTableModel(cols, 0) {
            // Version1: Admin can edit application-facing fields directly from the overview table.
            public boolean isCellEditable(int r, int c) { return c > 0; }
        };
        allAppsModel.addTableModelListener(this::trackAllAppsDirty);
        JTable table = new JTable(allAppsModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        JButton saveBtn = new JButton("Save Changes");
        JButton undoBtn = new JButton("Undo Changes");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refreshBtn);
        south.add(saveBtn);
        south.add(undoBtn);
        panel.add(south, BorderLayout.SOUTH);
        refreshBtn.addActionListener(e -> reloadAllAppsWithPrompt());
        saveBtn.addActionListener(e -> saveAllApplicationsChanges());
        undoBtn.addActionListener(e -> undoAllApplicationsChanges());
        return panel;
    }

    private void refreshAllApps() {
        isRefreshingAllApps = true;
        allAppsModel.setRowCount(0);
        List<User>  users = FileStorage.loadUsers();
        List<Job>   jobs  = FileStorage.loadJobs();

        Map<Integer, String> usernames = new HashMap<>();
        Map<Integer, Job>    jobMap    = new HashMap<>();
        for (User u : users) usernames.put(u.id, u.username);
        for (Job j : jobs)   jobMap.put(j.id, j);

        for (Application a : FileStorage.loadApplications()) {
            Job j = jobMap.get(a.jobId);
            allAppsModel.addRow(new Object[]{
                    a.id,
                    usernames.getOrDefault(a.taId, "?"),
                    j != null ? j.title  : "?",
                    j != null ? j.module : "?",
                    j != null ? usernames.getOrDefault(j.moId, "?") : "?",
                    a.status,
                    a.appliedAt
            });
        }
        allAppsDirty = false;
        isRefreshingAllApps = false;
    }

    // ─── All Jobs Panel ───────────────────────────────────────
    private JPanel createAllJobsPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel note = new JLabel(
            "  Version1: Edit the table, then click Save Changes. Undo Changes restores the last CSV version.",
            SwingConstants.LEFT);
        note.setForeground(Color.DARK_GRAY);
        panel.add(note, BorderLayout.NORTH);

        String[] cols = {"Job ID", "Title", "Module", "Posted By (MO)", "Required Skills", "Max Hours", "Status"};
        allJobsModel = new DefaultTableModel(cols, 0) {
            // Version1: Admin can edit persisted job fields directly from the overview table.
            public boolean isCellEditable(int r, int c) { return c > 0; }
        };
        allJobsModel.addTableModelListener(this::trackAllJobsDirty);
        JTable table = new JTable(allJobsModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        JButton saveBtn = new JButton("Save Changes");
        JButton undoBtn = new JButton("Undo Changes");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refreshBtn);
        south.add(saveBtn);
        south.add(undoBtn);
        panel.add(south, BorderLayout.SOUTH);
        refreshBtn.addActionListener(e -> reloadAllJobsWithPrompt());
        saveBtn.addActionListener(e -> saveAllJobsChanges());
        undoBtn.addActionListener(e -> undoAllJobsChanges());
        return panel;
    }

    private void refreshAllJobs() {
        isRefreshingAllJobs = true;
        allJobsModel.setRowCount(0);
        Map<Integer, String> usernames = new HashMap<>();
        for (User u : FileStorage.loadUsers()) usernames.put(u.id, u.username);
        for (Job j : FileStorage.loadJobs()) {
            allJobsModel.addRow(new Object[]{
                    j.id, j.title, j.module,
                    usernames.getOrDefault(j.moId, "?"),
                    j.requiredSkills, j.maxHours, j.status
            });
        }
        allJobsDirty = false;
        isRefreshingAllJobs = false;
    }

    private void trackWorkloadDirty(TableModelEvent event) {
        if (!isRefreshingWorkload && event.getType() == TableModelEvent.UPDATE) {
            workloadDirty = true;
        }
    }

    private void trackAllAppsDirty(TableModelEvent event) {
        if (!isRefreshingAllApps && event.getType() == TableModelEvent.UPDATE) {
            allAppsDirty = true;
        }
    }

    private void trackAllJobsDirty(TableModelEvent event) {
        if (!isRefreshingAllJobs && event.getType() == TableModelEvent.UPDATE) {
            allJobsDirty = true;
        }
    }

    // Version1: Save staged workload identity edits to users.csv / profiles.csv only when explicitly requested.
    private void saveWorkloadChanges() {
        List<User> users = FileStorage.loadUsers();
        List<TAProfile> profiles = FileStorage.loadProfiles();

        Set<String> seenUsernames = new HashSet<>();
        for (int row = 0; row < workloadModel.getRowCount(); row++) {
            if (row >= workloadUserIds.size()) continue;
            int userId = workloadUserIds.get(row);
            User user = findUserById(users, userId);
            if (user == null) {
                showRefreshWarning("A TA record no longer exists. The table will be refreshed.");
                refreshWorkload();
                return;
            }

            String username = stringValue(workloadModel.getValueAt(row, 0));
            String fullName = stringValue(workloadModel.getValueAt(row, 1));
            String email = stringValue(workloadModel.getValueAt(row, 2));
            if (username.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
                showRefreshWarning("TA Username, Full Name, and Email cannot be empty.");
                return;
            }
            if (!seenUsernames.add(username)) {
                showRefreshWarning("Duplicate username found in the workload table: " + username);
                return;
            }

            User duplicate = findUserByUsername(users, username);
            if (duplicate != null && duplicate.id != user.id) {
                showRefreshWarning("Username already exists: " + username);
                return;
            }

            user.username = username;
            TAProfile profile = findOrCreateProfile(profiles, user.id);
            profile.fullName = fullName;
            profile.email = email;
        }

        FileStorage.saveUsers(users);
        FileStorage.saveProfiles(profiles);
        refreshWorkload();
        refreshAllApps();
        refreshAllJobs();
        JOptionPane.showMessageDialog(this, "Version1: TA workload edits saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    // Version1: Save staged application overview edits only when explicitly requested.
    private void saveAllApplicationsChanges() {
        List<Application> apps = FileStorage.loadApplications();
        List<Job> jobs = FileStorage.loadJobs();
        List<User> users = FileStorage.loadUsers();

        for (int row = 0; row < allAppsModel.getRowCount(); row++) {
            int appId = intValue(allAppsModel.getValueAt(row, 0), -1);
            if (appId < 0) {
                showRefreshWarning("Invalid application row. Please refresh the table.");
                return;
            }

            Application app = findApplicationById(apps, appId);
            if (app == null) {
                showRefreshWarning("An application no longer exists. Please refresh the table.");
                return;
            }

            Job job = findJobById(jobs, app.jobId);
            if (job == null) {
                showRefreshWarning("A related job could not be found. Please refresh the table.");
                return;
            }

            String taUsername = stringValue(allAppsModel.getValueAt(row, 1));
            String jobTitle = stringValue(allAppsModel.getValueAt(row, 2));
            String module = stringValue(allAppsModel.getValueAt(row, 3));
            String moUsername = stringValue(allAppsModel.getValueAt(row, 4));
            String status = stringValue(allAppsModel.getValueAt(row, 5));
            String appliedAt = stringValue(allAppsModel.getValueAt(row, 6));

            if (taUsername.isEmpty() || jobTitle.isEmpty() || module.isEmpty() || moUsername.isEmpty() || status.isEmpty() || appliedAt.isEmpty()) {
                showRefreshWarning("All editable application fields must be filled before saving.");
                return;
            }

            User taUser = findUserByUsername(users, taUsername);
            if (taUser == null || !"TA".equals(taUser.role)) {
                showRefreshWarning("TA Username must be an existing TA account.");
                return;
            }

            User moUser = findUserByUsername(users, moUsername);
            if (moUser == null || !"MO".equals(moUser.role)) {
                showRefreshWarning("MO must be an existing MO account username.");
                return;
            }

            app.taId = taUser.id;
            app.status = status.toUpperCase();
            app.appliedAt = appliedAt;
            job.title = jobTitle;
            job.module = module;
            job.moId = moUser.id;
        }

        FileStorage.saveApplications(apps);
        FileStorage.saveJobs(jobs);
        refreshAllApps();
        refreshAllJobs();
        refreshWorkload();
        JOptionPane.showMessageDialog(this, "Version1: All Applications edits saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    // Version1: Save staged job overview edits only when explicitly requested.
    private void saveAllJobsChanges() {
        List<Job> jobs = FileStorage.loadJobs();
        List<User> users = FileStorage.loadUsers();
        for (int row = 0; row < allJobsModel.getRowCount(); row++) {
            int jobId = intValue(allJobsModel.getValueAt(row, 0), -1);
            if (jobId < 0) {
                showRefreshWarning("Invalid job row. Please refresh the table.");
                return;
            }

            Job job = findJobById(jobs, jobId);
            if (job == null) {
                showRefreshWarning("A job no longer exists. Please refresh the table.");
                return;
            }

            String title = stringValue(allJobsModel.getValueAt(row, 1));
            String module = stringValue(allJobsModel.getValueAt(row, 2));
            String moUsername = stringValue(allJobsModel.getValueAt(row, 3));
            String requiredSkills = stringValue(allJobsModel.getValueAt(row, 4));
            String maxHoursValue = stringValue(allJobsModel.getValueAt(row, 5));
            String status = stringValue(allJobsModel.getValueAt(row, 6));

            if (title.isEmpty() || module.isEmpty() || moUsername.isEmpty() || status.isEmpty()) {
                showRefreshWarning("Job title, module, MO, and status cannot be empty.");
                return;
            }

            User moUser = findUserByUsername(users, moUsername);
            if (moUser == null || !"MO".equals(moUser.role)) {
                showRefreshWarning("Posted By (MO) must be an existing MO username.");
                return;
            }

            int maxHours;
            try {
                maxHours = Integer.parseInt(maxHoursValue);
            } catch (NumberFormatException ex) {
                showRefreshWarning("Max Hours must be a whole number.");
                return;
            }

            job.title = title;
            job.module = module;
            job.moId = moUser.id;
            job.requiredSkills = requiredSkills;
            job.maxHours = maxHours;
            job.status = status.toUpperCase();
        }

        FileStorage.saveJobs(jobs);
        refreshAllJobs();
        refreshAllApps();
        refreshWorkload();
        JOptionPane.showMessageDialog(this, "Version1: All Jobs edits saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void reloadWorkloadWithPrompt() {
        if (workloadDirty && !confirmDiscardChanges()) return;
        refreshWorkload();
    }

    private void reloadAllAppsWithPrompt() {
        if (allAppsDirty && !confirmDiscardChanges()) return;
        refreshAllApps();
    }

    private void reloadAllJobsWithPrompt() {
        if (allJobsDirty && !confirmDiscardChanges()) return;
        refreshAllJobs();
    }

    private void undoWorkloadChanges() {
        if (!workloadDirty || confirmDiscardChanges()) {
            refreshWorkload();
        }
    }

    private void undoAllApplicationsChanges() {
        if (!allAppsDirty || confirmDiscardChanges()) {
            refreshAllApps();
        }
    }

    private void undoAllJobsChanges() {
        if (!allJobsDirty || confirmDiscardChanges()) {
            refreshAllJobs();
        }
    }

    private boolean confirmDiscardChanges() {
        return JOptionPane.showConfirmDialog(this,
                "Discard unsaved changes and reload data from CSV?",
                "Unsaved Changes", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void showRefreshWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Edit Not Saved", JOptionPane.WARNING_MESSAGE);
    }

    private User findUserById(List<User> users, int userId) {
        for (User user : users) {
            if (user.id == userId) return user;
        }
        return null;
    }

    private User findUserByUsername(List<User> users, String username) {
        for (User user : users) {
            if (user.username.equals(username)) return user;
        }
        return null;
    }

    private Job findJobById(List<Job> jobs, int jobId) {
        for (Job job : jobs) {
            if (job.id == jobId) return job;
        }
        return null;
    }

    private Application findApplicationById(List<Application> apps, int appId) {
        for (Application app : apps) {
            if (app.id == appId) return app;
        }
        return null;
    }

    private TAProfile findOrCreateProfile(List<TAProfile> profiles, int userId) {
        for (TAProfile profile : profiles) {
            if (profile.userId == userId) return profile;
        }

        TAProfile profile = new TAProfile();
        profile.id = nextProfileId(profiles);
        profile.userId = userId;
        profile.fullName = "";
        profile.email = "";
        profile.studentId = "";
        profile.skills = "";
        profile.gpa = 0.0;
        profile.cvPath = "";
        profiles.add(profile);
        return profile;
    }

    private int nextProfileId(List<TAProfile> profiles) {
        int maxId = 0;
        for (TAProfile profile : profiles) {
            if (profile.id > maxId) maxId = profile.id;
        }
        return maxId + 1;
    }

    private int intValue(Object value, int fallback) {
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    // ─── Custom renderer: red row when hours > 20 ─────────────
    static class WorkloadRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            if (!sel) {
                Object hrs = t.getModel().getValueAt(row, 5); // "Total Hours" column
                int hours = hrs instanceof Integer ? (Integer) hrs : 0;
                setBackground(hours > 20 ? new Color(255, 200, 200) : Color.WHITE);
                setForeground(Color.BLACK);
            }
            return this;
        }
    }
}
