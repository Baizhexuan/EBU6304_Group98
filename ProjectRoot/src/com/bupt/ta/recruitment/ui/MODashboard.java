package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.Application;
import com.bupt.ta.recruitment.model.Job;
import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class MODashboard extends BaseDashboard {
    private final CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
    private final CsvStorage<Application> applicationStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);

    public MODashboard(User currentUser) {
        super(currentUser, "MO Dashboard");
        addTab("Post Job", createPostJobTab());
        addTab("My Posts", createMyPostsTab());
        addTab("Applicants", createApplicantsTab());
        setVisible(true);
    }

    private JPanel createPostJobTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(panel, gbc, 0, "Job Title:");
        addField(panel, gbc, 1, "Module Code:");
        addField(panel, gbc, 2, "Required Skills:");
        addField(panel, gbc, 3, "Max Hours/Week:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        JTextArea area = new JTextArea(5, 24);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        panel.add(new JScrollPane(area), gbc);
        return panel;
    }

    private JPanel createMyPostsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        DefaultTableModel model = new DefaultTableModel(new String[] {"Job ID", "Title", "Module", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        for (Job job : jobStorage.loadAll()) {
            if (currentUser.getId().equals(job.getMoId())) {
                model.addRow(new Object[] {job.getId(), job.getTitle(), job.getModule(), job.getStatus()});
            }
        }
        UIHelper.installSorter(table, 1);
        panel.add(new JLabel("L2 skeleton: MO published posts are listed here."), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createApplicantsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        DefaultTableModel model = new DefaultTableModel(new String[] {"Application ID", "TA ID", "Job ID", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        for (Application app : applicationStorage.loadAll()) {
            Job job = jobStorage.findById(app.getJobId(), Job::getId);
            if (job != null && currentUser.getId().equals(job.getMoId())) {
                model.addRow(new Object[] {app.getId(), app.getTaId(), app.getJobId(), app.getStatus()});
            }
        }
        panel.add(new JLabel("L2 skeleton: applicant review workflow is prepared for L3 implementation."), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(new JTextField(22), gbc);
    }
}
