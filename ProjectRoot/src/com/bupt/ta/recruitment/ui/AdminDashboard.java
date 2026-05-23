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
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends BaseDashboard {
    private final CsvStorage<User> userStorage = new CsvStorage<>("data/users.csv", User::fromCsvRow);
    private final CsvStorage<TAProfile> profileStorage = new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);
    private final CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
    private final CsvStorage<Application> applicationStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);

    public AdminDashboard(User currentUser) {
        super(currentUser, "Admin Dashboard");
        addTab("Workload", createWorkloadTab());
        addTab("All Apps", createApplicationsTab());
        addTab("All Jobs", createJobsTab());
        setVisible(true);
    }

    private JPanel createWorkloadTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        DefaultTableModel model = new DefaultTableModel(new String[] {"TA Username", "Full Name", "Selected Jobs", "Current Hours"}, 0) {
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
            int hours = 0;
            for (Application app : applicationStorage.loadAll()) {
                if (user.getId().equals(app.getTaId()) && app.getStatus() == Application.AppStatus.SELECTED) {
                    selectedJobs++;
                    Job job = jobStorage.findById(app.getJobId(), Job::getId);
                    if (job != null) {
                        hours += job.getMaxHours();
                    }
                }
            }
            TAProfile profile = profileMap.get(user.getId());
            model.addRow(new Object[] {user.getUsername(), profile == null ? "N/A" : profile.getFullName(), selectedJobs, hours});
        }
        UIHelper.installSorter(table, 3);
        panel.add(new JLabel("L2 skeleton: admin monitoring framework is ready and will be expanded in later layers."), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createApplicationsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        DefaultTableModel model = new DefaultTableModel(new String[] {"App ID", "TA ID", "Job ID", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        for (Application app : applicationStorage.loadAll()) {
            model.addRow(new Object[] {app.getId(), app.getTaId(), app.getJobId(), app.getStatus()});
        }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createJobsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        DefaultTableModel model = new DefaultTableModel(new String[] {"Job ID", "MO ID", "Title", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        for (Job job : jobStorage.loadAll()) {
            model.addRow(new Object[] {job.getId(), job.getMoId(), job.getTitle(), job.getStatus()});
        }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
}
