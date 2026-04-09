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
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class MODashboard extends BaseDashboard {
    private final CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
    private final CsvStorage<Application> applicationStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);
    private final CsvStorage<User> userStorage = new CsvStorage<>("data/users.csv", User::fromCsvRow);
    private final CsvStorage<TAProfile> profileStorage = new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);

    public MODashboard(User user) {
        super(user, "Module Organiser Dashboard");
        add(buildTabs(), BorderLayout.CENTER);
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Post Job", createPostJobPanel());
        tabs.addTab("My Posts", createMyPostsPanel());
        tabs.addTab("Applicants", createApplicantsPanel());
        return tabs;
    }

    private JPanel createPostJobPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(panel, gbc, 0, "Job Title:");
        addField(panel, gbc, 1, "Module Code:");
        addField(panel, gbc, 2, "Required Skills:");
        addField(panel, gbc, 3, "Max Hours per Week:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        JTextArea descriptionArea = new JTextArea(5, 24);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(descriptionArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(new JLabel("L2 Pair C scope: posting form layout is ready for later validation and save actions."), gbc);
        return panel;
    }

    private JPanel createMyPostsPanel() {
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
            if (currentUser.getId().equals(job.getMoId())) {
                model.addRow(new Object[] {job.getId(), job.getTitle(), job.getModule(), job.getRequiredSkills(), job.getMaxHours(), job.getStatus()});
            }
        }
        UIHelper.installSorter(table, 1);
        panel.add(new JLabel("Job posts created by the current MO are listed here."), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createApplicantsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        DefaultTableModel model = new DefaultTableModel(new String[] {"Application ID", "TA Username", "Email", "Skills", "Job", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);

        Map<String, User> userMap = new HashMap<>();
        for (User user : userStorage.loadAll()) {
            userMap.put(user.getId(), user);
        }
        Map<String, TAProfile> profileMap = new HashMap<>();
        for (TAProfile profile : profileStorage.loadAll()) {
            profileMap.put(profile.getUserId(), profile);
        }

        for (Application app : applicationStorage.loadAll()) {
            Job job = jobStorage.findById(app.getJobId(), Job::getId);
            if (job == null || !currentUser.getId().equals(job.getMoId())) {
                continue;
            }
            User taUser = userMap.get(app.getTaId());
            TAProfile profile = profileMap.get(app.getTaId());
            model.addRow(new Object[] {
                    app.getId(),
                    taUser == null ? "Unknown" : taUser.getUsername(),
                    profile == null ? "Not set" : profile.getEmail(),
                    profile == null ? "Not set" : profile.getSkills(),
                    job.getTitle(),
                    app.getStatus()
            });
        }
        UIHelper.installSorter(table, 4);
        panel.add(new JLabel("Applicant overview is ready for later review actions."), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        JTextField field = new JTextField(24);
        panel.add(field, gbc);
    }
}
