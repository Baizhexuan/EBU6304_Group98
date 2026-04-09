package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.Application;
import com.bupt.ta.recruitment.model.Job;
import com.bupt.ta.recruitment.model.TAProfile;
import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.service.MOService;
import com.bupt.ta.recruitment.util.UIHelper;

import java.awt.*;
import java.util.List;
import java.util.UUID;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Dashboard for Module Organisers.
 * Implements Tasks 5 (Post Job), 6 (Manage Posts), and 7 (Review Applicants).
 */
public class MODashboard extends BaseDashboard {

    private final MOService moService = new MOService();

    private DefaultTableModel postsModel;
    private DefaultTableModel applicantsModel;
    private JComboBox<JobItem> jobComboBox;

    public MODashboard(User user) {
        super(user, "Module Organiser Dashboard");
        setLayout(new BorderLayout());
        add(buildTabs(), BorderLayout.CENTER);
        refreshData();
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
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(25);
        JTextField moduleField = new JTextField(25);
        JTextField skillsField = new JTextField(25);
        JTextField hoursField = new JTextField(25);
        JTextArea descArea = new JTextArea(5, 25);
        descArea.setLineWrap(true);

        addField(panel, gbc, 0, "Job Title:", titleField);
        addField(panel, gbc, 1, "Module Code:", moduleField);
        addField(panel, gbc, 2, "Required Skills:", skillsField);
        addField(panel, gbc, 3, "Max Hours/Week:", hoursField);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(descArea), gbc);

        gbc.gridx = 1; gbc.gridy = 5;
        JButton postBtn = new JButton("Post Vacancy");
        panel.add(postBtn, gbc);

        postBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String module = moduleField.getText().trim();
            String hoursStr = hoursField.getText().trim();

            if (title.isEmpty() || module.isEmpty() || hoursStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int hours = Integer.parseInt(hoursStr);
                if (hours <= 0) throw new NumberFormatException();

                // 修正：使用 Job.JobStatus.OPEN 枚举
                Job job = new Job(UUID.randomUUID().toString(), currentUser.getId(), title, module,
                        descArea.getText().trim(), skillsField.getText().trim(), hours, Job.JobStatus.OPEN);

                moService.postJob(job);
                JOptionPane.showMessageDialog(this, "Job posted successfully!");
                titleField.setText(""); moduleField.setText(""); skillsField.setText(""); hoursField.setText(""); descArea.setText("");
                refreshData();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Max Hours must be a positive integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "System Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    private JPanel createMyPostsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        postsModel = new DefaultTableModel(new String[]{"ID", "Title", "Module", "Skills", "Hours", "Status"}, 0);
        JTable table = new JTable(postsModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close Selected Job");
        closeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            String id = (String) postsModel.getValueAt(row, 0);
            try {
                moService.closeJob(id);
                refreshData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        panel.add(closeBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createApplicantsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        jobComboBox = new JComboBox<>();
        jobComboBox.addActionListener(e -> refreshApplicantsTable());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Select your Job post:"));
        top.add(jobComboBox);
        panel.add(top, BorderLayout.NORTH);

        applicantsModel = new DefaultTableModel(new String[]{"App ID", "Applicant", "Email", "Skills", "Status"}, 0);
        JTable table = new JTable(applicantsModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bot = new JPanel();
        JButton selBtn = new JButton("Select for Interview");
        JButton rejBtn = new JButton("Reject");
        bot.add(selBtn); bot.add(rejBtn);
        panel.add(bot, BorderLayout.SOUTH);

        selBtn.addActionListener(e -> updateStatus(table, "SELECTED"));
        rejBtn.addActionListener(e -> updateStatus(table, "REJECTED"));

        return panel;
    }

    private void updateStatus(JTable table, String status) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String appId = (String) applicantsModel.getValueAt(row, 0);
        try {
            moService.updateApplicationStatus(appId, status);
            refreshApplicantsTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed.");
        }
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(comp, gbc);
    }

    private void refreshData() {
        postsModel.setRowCount(0);
        List<Job> jobs = moService.getJobsByMo(currentUser.getId());
        jobComboBox.removeAllItems();
        for (Job j : jobs) {
            postsModel.addRow(new Object[]{j.getId(), j.getTitle(), j.getModule(), j.getRequiredSkills(), j.getMaxHours(), j.getStatus()});
            jobComboBox.addItem(new JobItem(j.getId(), j.getTitle()));
        }
    }

    private void refreshApplicantsTable() {
        applicantsModel.setRowCount(0);
        JobItem item = (JobItem) jobComboBox.getSelectedItem();
        if (item == null) return;

        List<Application> apps = moService.getApplicationsByMoJob(item.getId());
        for (Application a : apps) {
            User u = moService.getUserById(a.getTaId());
            TAProfile p = moService.getProfileByUserId(a.getTaId());
            applicantsModel.addRow(new Object[]{
                    a.getId(),
                    u == null ? "N/A" : u.getUsername(),
                    p == null ? "N/A" : p.getEmail(),
                    p == null ? "N/A" : p.getSkills(),
                    a.getStatus()
            });
        }
    }

    private static class JobItem {
        String id, title;
        JobItem(String i, String t) { id = i; title = t; }
        String getId() { return id; }
        @Override public String toString() { return title; }
    }
}