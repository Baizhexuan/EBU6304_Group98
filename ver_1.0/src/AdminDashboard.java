import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends JFrame {
    private final User currentUser;
    private JTable workloadTable;
    private DefaultTableModel workloadModel;
    private JTable applicationsTable;
    private DefaultTableModel applicationsModel;
    private JTable jobsTable;
    private DefaultTableModel jobsModel;

    public AdminDashboard(User currentUser) {
        this.currentUser = currentUser;
        setTitle("Admin Dashboard - " + currentUser.getSafeDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 700);
        setLocationRelativeTo(null);

        JMenuBar bar = new JMenuBar();
        JMenu accountMenu = new JMenu("Account");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        accountMenu.add(logoutItem);
        bar.add(accountMenu);
        setJMenuBar(bar);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Workload Monitor", createWorkloadPanel());
        tabs.addTab("Applications Overview", createApplicationsPanel());
        tabs.addTab("Jobs Overview", createJobsPanel());
        tabs.addChangeListener(e -> refreshAll());

        add(tabs);
        refreshAll();
        setVisible(true);
    }

    private JPanel createWorkloadPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        workloadModel = new DefaultTableModel(
                new String[] {"TA Username", "Full Name", "Email", "Selected Jobs", "Current Hours", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        workloadTable = new JTable(workloadModel);
        workloadTable.setDefaultRenderer(Object.class, new WorkloadRenderer());
        panel.add(new JScrollPane(workloadTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        JButton exportButton = new JButton("Export CSV Report");
        actions.add(refreshButton);
        actions.add(exportButton);
        panel.add(actions, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshWorkload());
        exportButton.addActionListener(e -> exportWorkloadReport());
        return panel;
    }

    private JPanel createApplicationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        applicationsModel = new DefaultTableModel(
                new String[] {"App ID", "TA", "Job", "Module", "Status", "Applied At", "Match", "Note"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        applicationsTable = new JTable(applicationsModel);
        panel.add(new JScrollPane(applicationsTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        actions.add(refreshButton);
        panel.add(actions, BorderLayout.SOUTH);
        refreshButton.addActionListener(e -> refreshApplications());
        return panel;
    }

    private JPanel createJobsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        jobsModel = new DefaultTableModel(
                new String[] {"Job ID", "MO", "Title", "Module", "Skills", "Hours", "Location", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jobsTable = new JTable(jobsModel);
        panel.add(new JScrollPane(jobsTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        actions.add(refreshButton);
        panel.add(actions, BorderLayout.SOUTH);
        refreshButton.addActionListener(e -> refreshJobs());
        return panel;
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

        for (User user : FileStorage.loadUsers()) {
            if (!"TA".equalsIgnoreCase(user.role)) {
                continue;
            }
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
            TAProfile profile = profiles.get(user.id);
            String status = currentHours > FileStorage.getOverloadLimit() ? "OVERLOAD" : "OK";
            workloadModel.addRow(new Object[] {
                    user.username,
                    profile == null ? user.getSafeDisplayName() : profile.fullName,
                    profile == null ? "N/A" : profile.email,
                    selectedJobs,
                    currentHours,
                    status
            });
        }
    }

    private void refreshApplications() {
        applicationsModel.setRowCount(0);
        for (Application app : FileStorage.loadApplications()) {
            User ta = FileStorage.findUserById(app.taId);
            Job job = FileStorage.findJobById(app.jobId);
            applicationsModel.addRow(new Object[] {
                    app.id,
                    ta == null ? "Unknown" : ta.getSafeDisplayName(),
                    job == null ? "Unknown" : job.title,
                    job == null ? "Unknown" : job.module,
                    app.status,
                    app.appliedAt,
                    app.matchScore + "%",
                    app.reviewerNote
            });
        }
    }

    private void refreshJobs() {
        jobsModel.setRowCount(0);
        for (Job job : FileStorage.loadJobs()) {
            User mo = FileStorage.findUserById(job.moId);
            jobsModel.addRow(new Object[] {
                    job.id,
                    mo == null ? "Unknown" : mo.getSafeDisplayName(),
                    job.title,
                    job.module,
                    job.requiredSkills,
                    job.maxHours,
                    job.location,
                    job.status
            });
        }
    }

    private void exportWorkloadReport() {
        refreshWorkload();
        String path = "data/admin_workload_report.csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
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

    private void logout() {
        dispose();
        new LoginFrame();
    }

    private static class WorkloadRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            java.awt.Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            if (!isSelected) {
                String status = String.valueOf(table.getValueAt(row, 5));
                if ("OVERLOAD".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(255, 221, 221));
                } else {
                    component.setBackground(Color.WHITE);
                }
            }
            return component;
        }
    }
}
