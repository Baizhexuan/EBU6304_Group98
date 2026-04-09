package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.Application;
import com.bupt.ta.recruitment.model.Job;
import com.bupt.ta.recruitment.model.TAProfile;
import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.UIHelper;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends BaseDashboard {
    private final CsvStorage<User> userStorage = new CsvStorage<>("data/users.csv", User::fromCsvRow);
    private final CsvStorage<TAProfile> profileStorage = new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);
    private final CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
    private final CsvStorage<Application> applicationStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);

    public AdminDashboard(User user) {
        super(user, "Administrator Dashboard");
        add(buildTabs(), BorderLayout.CENTER);
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Workload", createWorkloadPanel());
        tabs.addTab("All Apps", createApplicationsPanel());
        tabs.addTab("All Jobs", createJobsPanel());
        return tabs;
    }

    private JPanel createWorkloadPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        DefaultTableModel model = new DefaultTableModel(new String[] {"TA Username", "Full Name", "Email", "Selected Jobs", "Current Hours"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);

        Map<String, TAProfile> profileMap = new HashMap<>();
        for (TAProfile profile : profileStorage.loadAll()) {
            profileMap.put(profile.getUserId(), profile);
        }

        for (User user : userStorage.loadAll()) {
            if (user.getRole() != User.UserRole.TA) {
                continue;
            }
            int selectedJobs = 0;
            int currentHours = 0;
            for (Application app : applicationStorage.loadAll()) {
                if (user.getId().equals(app.getTaId()) && app.getStatus() == Application.AppStatus.SELECTED) {
                    selectedJobs++;
                    Job job = jobStorage.findById(app.getJobId(), Job::getId);
                    if (job != null) {
                        currentHours += job.getMaxHours();
                    }
                }
            }
            TAProfile profile = profileMap.get(user.getId());
            model.addRow(new Object[] {
                    user.getUsername(),
                    profile == null ? "Not set" : profile.getFullName(),
                    profile == null ? "Not set" : profile.getEmail(),
                    selectedJobs,
                    currentHours
            });
        }
        UIHelper.installSorter(table, 4);
        panel.add(new JLabel("Admin workload monitor for all TA users."), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createApplicationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        DefaultTableModel model = new DefaultTableModel(new String[] {"Application ID", "TA ID", "Job ID", "Status", "Applied At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        for (Application app : applicationStorage.loadAll()) {
            model.addRow(new Object[] {app.getId(), app.getTaId(), app.getJobId(), app.getStatus(), app.getAppliedAt()});
        }
        UIHelper.installSorter(table, 3);
        panel.add(new JLabel("All application records are visible to the administrator."), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createJobsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        DefaultTableModel model = new DefaultTableModel(new String[] {"Job ID", "MO ID", "Title", "Module", "Hours", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        for (Job job : jobStorage.loadAll()) {
            model.addRow(new Object[] {job.getId(), job.getMoId(), job.getTitle(), job.getModule(), job.getMaxHours(), job.getStatus()});
        }
        UIHelper.installSorter(table, 2);
        panel.add(new JLabel("All jobs in the system are listed here for admin overview."), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
}
