import javax.swing.*;
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

    public AdminDashboard(User user) {
        this.currentUser = user;
        setTitle("Admin Dashboard  –  " + user.username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 620);
        setLocationRelativeTo(null);

        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("Account");
        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(e -> { dispose(); new LoginFrame(); });
        menu.add(logout);
        mb.add(menu);
        setJMenuBar(mb);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("TA Workload Monitor", createWorkloadPanel());
        tabs.addTab("All Applications",   createAllAppsPanel());
        tabs.addTab("All Jobs",           createAllJobsPanel());

        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 0) refreshWorkload();
            if (idx == 1) refreshAllApps();
            if (idx == 2) refreshAllJobs();
        });

        add(tabs);
        refreshWorkload();
        setVisible(true);
    }

    // ─── Workload Panel ───────────────────────────────────────
    private JPanel createWorkloadPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel note = new JLabel(
                "  Highlight: TAs with total accepted hours > 20 are shown in red.",
                SwingConstants.LEFT);
        note.setForeground(Color.DARK_GRAY);
        panel.add(note, BorderLayout.NORTH);

        String[] cols = {"TA Username", "Full Name", "Email", "Total Apps", "Selected Jobs", "Total Hours"};
        workloadModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(workloadModel);
        table.setDefaultRenderer(Object.class, new WorkloadRenderer());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refreshBtn);
        panel.add(south, BorderLayout.SOUTH);
        refreshBtn.addActionListener(e -> refreshWorkload());
        return panel;
    }

    private void refreshWorkload() {
        workloadModel.setRowCount(0);
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
        }
    }

    // ─── All Applications Panel ───────────────────────────────
    private JPanel createAllAppsPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] cols = {"App ID", "TA Username", "Job Title", "Module", "MO", "Status", "Applied At"};
        allAppsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(allAppsModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refreshBtn);
        panel.add(south, BorderLayout.SOUTH);
        refreshBtn.addActionListener(e -> refreshAllApps());
        return panel;
    }

    private void refreshAllApps() {
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
    }

    // ─── All Jobs Panel ───────────────────────────────────────
    private JPanel createAllJobsPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] cols = {"Job ID", "Title", "Module", "Posted By (MO)", "Required Skills", "Max Hours", "Status"};
        allJobsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(allJobsModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refreshBtn);
        panel.add(south, BorderLayout.SOUTH);
        refreshBtn.addActionListener(e -> refreshAllJobs());
        return panel;
    }

    private void refreshAllJobs() {
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
