package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.Application;
import com.bupt.ta.recruitment.model.Job;
import com.bupt.ta.recruitment.model.TAProfile;
import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TADashboard extends BaseDashboard {
    private final CsvStorage<TAProfile> profileStorage = new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);
    private final CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
    private final CsvStorage<Application> applicationStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);

    public TADashboard(User user) {
        super(user, "TA Dashboard");
        add(buildTabs(), BorderLayout.CENTER);
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Profile", createProfilePanel());
        tabs.addTab("Browse Jobs", createJobsPanel());
        tabs.addTab("My Applications", createApplicationsPanel());
        return tabs;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
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

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        JLabel note = new JLabel("L2 Pair C scope: profile tab structure is ready for later editing features.", SwingConstants.LEFT);
        note.setForeground(new Color(80, 80, 80));
        panel.add(note, gbc);
        return panel;
    }

    private JPanel createJobsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        DefaultTableModel model = new DefaultTableModel(new String[] {"Job ID", "Title", "Module", "Required Skills", "Hours", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        for (Job job : jobStorage.loadAll()) {
            if (job.getStatus() == Job.JobStatus.OPEN) {
                model.addRow(new Object[] {job.getId(), job.getTitle(), job.getModule(), job.getRequiredSkills(), job.getMaxHours(), job.getStatus()});
            }
        }
        UIHelper.installSorter(table, 1);
        panel.add(new JLabel("Open jobs available for TA browsing."), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createApplicationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        DefaultTableModel model = new DefaultTableModel(new String[] {"Application ID", "Job Title", "Module", "Status", "Applied At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setDefaultRenderer(Object.class, new StatusCellRenderer(3));
        for (Application application : applicationStorage.loadAll()) {
            if (currentUser.getId().equals(application.getTaId())) {
                Job job = jobStorage.findById(application.getJobId(), Job::getId);
                model.addRow(new Object[] {
                        application.getId(),
                        job == null ? "Unknown" : job.getTitle(),
                        job == null ? "Unknown" : job.getModule(),
                        application.getStatus(),
                        formatTimestamp(application.getAppliedAt())
                });
            }
        }
        UIHelper.installSorter(table, 4);
        panel.add(new JLabel("Application records are shown here for TA tracking."), BorderLayout.NORTH);
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
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        JTextField field = new JTextField(value, 24);
        field.setEditable(false);
        panel.add(field, gbc);
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(timestamp));
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        private final int statusColumn;

        private StatusCellRenderer(int statusColumn) {
            this.statusColumn = statusColumn;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                Object rawStatus = table.getValueAt(row, statusColumn);
                component.setBackground(resolveStatusColor(String.valueOf(rawStatus)));
            }
            return component;
        }

        private Color resolveStatusColor(String status) {
            if ("SELECTED".equalsIgnoreCase(status)) {
                return UIHelper.STATUS_SELECTED;
            }
            if ("REJECTED".equalsIgnoreCase(status)) {
                return UIHelper.STATUS_REJECTED;
            }
            return UIHelper.STATUS_PENDING;
        }
    }
}
