package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.Application;
import com.bupt.ta.recruitment.model.Job;
import com.bupt.ta.recruitment.model.TAProfile;
import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class TADashboard extends BaseDashboard {
    private final CsvStorage<TAProfile> profileStorage = new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);
    private final CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
    private final CsvStorage<Application> applicationStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);

    public TADashboard(User currentUser) {
        super(currentUser, "TA Dashboard");
        addTab("Profile", createProfileTab());
        addTab("Browse Jobs", createJobsTab());
        addTab("My Applications", createApplicationsTab());
        setVisible(true);
    }

    private JPanel createProfileTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        TAProfile profile = findProfile(currentUser.getId());
        addField(panel, gbc, 0, "Full Name:", profile == null ? "Not set" : profile.getFullName());
        addField(panel, gbc, 1, "Email:", profile == null ? "Not set" : profile.getEmail());
        addField(panel, gbc, 2, "Student ID:", profile == null ? "Not set" : profile.getStudentId());
        addField(panel, gbc, 3, "Skills:", profile == null ? "Not set" : profile.getSkills());
        addField(panel, gbc, 4, "GPA:", profile == null ? "Not set" : String.valueOf(profile.getGpa()));
        addField(panel, gbc, 5, "CV Path:", profile == null ? "Not set" : profile.getCvPath());
        return panel;
    }

    private JPanel createJobsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        DefaultTableModel model = new DefaultTableModel(new String[] {"Job ID", "Title", "Module", "Required Skills", "Hours", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        for (Job job : jobStorage.loadAll()) {
            model.addRow(new Object[] {job.getId(), job.getTitle(), job.getModule(), job.getRequiredSkills(), job.getMaxHours(), job.getStatus()});
        }
        UIHelper.installSorter(table, 1);
        panel.add(new JLabel("L2 skeleton: TA can browse job posts here. Filtering and applications will be completed in L3."), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createApplicationsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        DefaultTableModel model = new DefaultTableModel(new String[] {"Application ID", "Job ID", "Status", "Applied At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        for (Application app : applicationStorage.loadAll()) {
            if (currentUser.getId().equals(app.getTaId())) {
                model.addRow(new Object[] {app.getId(), app.getJobId(), app.getStatus(), app.getAppliedAt()});
            }
        }
        panel.add(new JLabel("L2 skeleton: TA application tracking tab ready for later business logic integration."), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private TAProfile findProfile(String userId) {
        List<TAProfile> profiles = profileStorage.loadAll();
        for (TAProfile profile : profiles) {
            if (userId.equals(profile.getUserId())) {
                return profile;
            }
        }
        return null;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        JTextField field = new JTextField(value, 22);
        field.setEditable(false);
        panel.add(field, gbc);
    }
}
