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

/**
 * Administrator dashboard for monitoring users, applications, and jobs.
 * It gives the admin read-only oversight across the whole recruitment system.
 */
public class AdminDashboard extends BaseDashboard {
    // 管理员页面需要访问所有用户数据。
    private final CsvStorage<User> userStorage = new CsvStorage<>("data/users.csv", User::fromCsvRow);
    // 管理员页面需要访问所有 TA 档案。
    private final CsvStorage<TAProfile> profileStorage = new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);
    // 管理员页面需要访问所有岗位数据。
    private final CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
    // 管理员页面需要访问全部申请记录。
    private final CsvStorage<Application> applicationStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);

    public AdminDashboard(User user) {
        // 调用父类完成公共窗口初始化。
        super(user, "Administrator Dashboard");
        // 将管理员专属的标签页区域挂到窗口中心。
        add(buildTabs(), BorderLayout.CENTER);
    }

    private JTabbedPane buildTabs() {
        // 使用标签页承载不同管理视图。
        JTabbedPane tabs = new JTabbedPane();
        // 工作量总览标签页。
        tabs.addTab("Workload", createWorkloadPanel());
        // 全部申请记录标签页。
        tabs.addTab("All Apps", createApplicationsPanel());
        // 全部岗位记录标签页。
        tabs.addTab("All Jobs", createJobsPanel());
        return tabs;
    }

    private JPanel createWorkloadPanel() {
        // 创建带边界布局的工作量面板。
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        // 设置内边距提升可读性。
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // 定义只读表格模型，展示每个 TA 的工作量信息。
        DefaultTableModel model = new DefaultTableModel(new String[] {"TA Username", "Full Name", "Email", "Selected Jobs", "Current Hours"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // 使用 JTable 承载上面的表格模型。
        JTable table = new JTable(model);

        // 先建立 userId 到档案的映射，减少重复查找。
        Map<String, TAProfile> profileMap = new HashMap<>();
        for (TAProfile profile : profileStorage.loadAll()) {
            profileMap.put(profile.getUserId(), profile);
        }

        // 遍历所有用户，只统计 TA 角色的工作量。
        for (User user : userStorage.loadAll()) {
            if (user.getRole() != User.UserRole.TA) {
                continue;
            }
            // 初始化当前 TA 的已录用岗位数。
            int selectedJobs = 0;
            // 初始化当前 TA 的总工时。
            int currentHours = 0;
            // 遍历全部申请，筛选当前 TA 且状态为已录用的记录。
            for (Application app : applicationStorage.loadAll()) {
                if (user.getId().equals(app.getTaId()) && app.getStatus() == Application.AppStatus.SELECTED) {
                    selectedJobs++;
                    // 找到对应岗位后累计其最大工时。
                    Job job = jobStorage.findById(app.getJobId(), Job::getId);
                    if (job != null) {
                        currentHours += job.getMaxHours();
                    }
                }
            }
            // 读取当前 TA 的档案信息，可能为空。
            TAProfile profile = profileMap.get(user.getId());
            // 将统计结果加入表格。
            model.addRow(new Object[] {
                    user.getUsername(),
                    profile == null ? "Not set" : profile.getFullName(),
                    profile == null ? "Not set" : profile.getEmail(),
                    selectedJobs,
                    currentHours
            });
        }
        // 为工时列安装排序器，便于管理员按工作量查看。
        UIHelper.installSorter(table, 4);
        // 在顶部添加功能说明。
        panel.add(new JLabel("Admin workload monitor for all TA users."), BorderLayout.NORTH);
        // 将表格放入滚动面板后加入中心区域。
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createApplicationsPanel() {
        // 创建展示全量申请的面板。
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // 定义只读申请表格模型。
        DefaultTableModel model = new DefaultTableModel(new String[] {"Application ID", "TA ID", "Job ID", "Status", "Applied At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // 创建申请表格组件。
        JTable table = new JTable(model);
        // 将全部申请逐条写入表格。
        for (Application app : applicationStorage.loadAll()) {
            model.addRow(new Object[] {app.getId(), app.getTaId(), app.getJobId(), app.getStatus(), app.getAppliedAt()});
        }
        // 为状态列附近安装排序功能。
        UIHelper.installSorter(table, 3);
        // 在顶部添加管理员视角说明。
        panel.add(new JLabel("All application records are visible to the administrator."), BorderLayout.NORTH);
        // 中部展示滚动表格。
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createJobsPanel() {
        // 创建展示全部岗位的面板。
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // 定义只读岗位表格模型。
        DefaultTableModel model = new DefaultTableModel(new String[] {"Job ID", "MO ID", "Title", "Module", "Hours", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // 创建岗位表格组件。
        JTable table = new JTable(model);
        // 将全部岗位数据加载到表格中。
        for (Job job : jobStorage.loadAll()) {
            model.addRow(new Object[] {job.getId(), job.getMoId(), job.getTitle(), job.getModule(), job.getMaxHours(), job.getStatus()});
        }
        // 允许按标题列排序，便于检索岗位。
        UIHelper.installSorter(table, 2);
        // 顶部添加功能说明。
        panel.add(new JLabel("All jobs in the system are listed here for admin overview."), BorderLayout.NORTH);
        // 中部加入滚动表格。
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
}
