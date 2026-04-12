package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.Application;
import com.bupt.ta.recruitment.model.Job;
import com.bupt.ta.recruitment.model.TAProfile;
import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.UIHelper;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Dashboard for module organisers to review their posts and applicant decisions.
 * It presents posting-related UI, the current MO's jobs, and status updates for
 * submitted applications.
 */
public class MODashboard extends BaseDashboard {
    // 存储当前系统中的岗位数据。
    private final CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
    // 存储岗位申请数据。
    private final CsvStorage<Application> applicationStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);
    // 存储用户数据，用于把申请中的 taId 映射为用户名。
    private final CsvStorage<User> userStorage = new CsvStorage<>("data/users.csv", User::fromCsvRow);
    // 存储 TA 档案数据，用于显示邮箱和技能。
    private final CsvStorage<TAProfile> profileStorage = new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);

    public MODashboard(User user) {
        // 完成父类公共布局初始化。
        super(user, "Module Organiser Dashboard");
        // 把 MO 页面专属标签页放到窗口中心。
        add(buildTabs(), BorderLayout.CENTER);
    }

    private JTabbedPane buildTabs() {
        // 创建 MO 工作区标签页容器。
        JTabbedPane tabs = new JTabbedPane();
        // 岗位发布表单页。
        tabs.addTab("Post Job", createPostJobPanel());
        // 当前 MO 已发布岗位列表页。
        tabs.addTab("My Posts", createMyPostsPanel());
        // 岗位申请者审核页。
        tabs.addTab("Applicants", createApplicantsPanel());
        return tabs;
    }

    private JPanel createPostJobPanel() {
        // 使用网格包布局创建岗位发布表单。
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        // 统一定义表单布局约束。
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 逐行添加岗位标题输入位。
        addField(panel, gbc, 0, "Job Title:");
        // 逐行添加课程代码输入位。
        addField(panel, gbc, 1, "Module Code:");
        // 逐行添加技能要求输入位。
        addField(panel, gbc, 2, "Required Skills:");
        // 逐行添加最大工时输入位。
        addField(panel, gbc, 3, "Max Hours per Week:");
        // 第五行左侧放描述标签。
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Description:"), gbc);
        // 第五行右侧放多行文本框用于填写描述。
        gbc.gridx = 1;
        JTextArea descriptionArea = new JTextArea(5, 24);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(descriptionArea), gbc);

        // 底部展示当前表单仍主要是界面骨架的说明。
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(new JLabel("L2 Pair C scope: posting form layout is ready for later validation and save actions."), gbc);
        return panel;
    }

    private JPanel createMyPostsPanel() {
        // 创建展示当前 MO 岗位列表的面板。
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // 定义只读岗位表格模型。
        DefaultTableModel model = new DefaultTableModel(new String[] {"Job ID", "Title", "Module", "Required Skills", "Hours", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // 创建岗位表格。
        JTable table = new JTable(model);
        // 只加载当前已登录 MO 发布的岗位。
        for (Job job : jobStorage.loadAll()) {
            if (currentUser.getId().equals(job.getMoId())) {
                model.addRow(new Object[] {job.getId(), job.getTitle(), job.getModule(), job.getRequiredSkills(), job.getMaxHours(), job.getStatus()});
            }
        }
        // 为标题列安装排序器。
        UIHelper.installSorter(table, 1);
        // 顶部添加提示文字。
        panel.add(new JLabel("Job posts created by the current MO are listed here."), BorderLayout.NORTH);
        // 中部放置表格滚动视图。
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createApplicantsPanel() {
        // 创建申请者审核面板。
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // 定义申请者表格模型，展示申请人与目标岗位的摘要信息。
        DefaultTableModel model = new DefaultTableModel(new String[] {"Application ID", "TA Username", "Email", "Skills", "Job", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // 创建表格并限制一次只能选中一行。
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 初始加载当前 MO 能看到的所有申请记录。
        reloadApplicantTable(model);

        // 创建录用按钮，并绑定状态更新逻辑。
        JButton approveButton = new JButton("Approve Selected");
        approveButton.addActionListener(e -> updateApplicationStatus(table, model, Application.AppStatus.SELECTED));

        // 创建拒绝按钮，并绑定状态更新逻辑。
        JButton rejectButton = new JButton("Reject Selected");
        rejectButton.addActionListener(e -> updateApplicationStatus(table, model, Application.AppStatus.REJECTED));

        // 创建刷新按钮，重新读取 CSV 数据。
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> reloadApplicantTable(model));

        // 额外提供一个页面内登出按钮，便于从底部操作区直接退出。
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        // 为岗位列安装排序器。
        UIHelper.installSorter(table, 4);
        // 底部操作区承载审核相关按钮。
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.add(approveButton);
        actionPanel.add(rejectButton);
        actionPanel.add(refreshButton);
        actionPanel.add(logoutButton);

        // 顶部说明审核操作会即时保存。
        panel.add(new JLabel("Select an applicant, then approve or reject. Changes are saved immediately."), BorderLayout.NORTH);
        // 中部展示可滚动的申请列表。
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        // 底部放置审核按钮区。
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void reloadApplicantTable(DefaultTableModel model) {
        // 每次刷新前先清空旧数据。
        model.setRowCount(0);

        // 建立用户映射，便于通过 taId 查用户名。
        Map<String, User> userMap = new HashMap<>();
        for (User user : userStorage.loadAll()) {
            userMap.put(user.getId(), user);
        }

        // 建立档案映射，便于通过 userId 查邮箱和技能。
        Map<String, TAProfile> profileMap = new HashMap<>();
        for (TAProfile profile : profileStorage.loadAll()) {
            profileMap.put(profile.getUserId(), profile);
        }

        // 只遍历当前 MO 自己岗位上的申请。
        for (Application app : loadMoApplications()) {
            // 读取申请对应的岗位信息。
            Job job = jobStorage.findById(app.getJobId(), Job::getId);
            // 读取申请对应的 TA 账号信息。
            User taUser = userMap.get(app.getTaId());
            // 读取申请对应的 TA 档案信息。
            TAProfile profile = profileMap.get(app.getTaId());
            // 将一条申请的展示数据追加到表格中。
            model.addRow(new Object[] {
                    app.getId(),
                    taUser == null ? "Unknown" : taUser.getUsername(),
                    profile == null ? "Not set" : profile.getEmail(),
                    profile == null ? "Not set" : profile.getSkills(),
                    job == null ? "Unknown" : job.getTitle(),
                    app.getStatus()
            });
        }
    }

    private List<Application> loadMoApplications() {
        // 收集当前 MO 可审核的申请列表。
        List<Application> applications = new ArrayList<>();
        for (Application app : applicationStorage.loadAll()) {
            // 根据申请里的 jobId 找到岗位实体。
            Job job = jobStorage.findById(app.getJobId(), Job::getId);
            // 只保留岗位存在且该岗位属于当前 MO 的申请。
            if (job != null && currentUser.getId().equals(job.getMoId())) {
                applications.add(app);
            }
        }
        return applications;
    }

    private void updateApplicationStatus(JTable table, DefaultTableModel model, Application.AppStatus newStatus) {
        // 读取表格当前选中的视图行。
        int viewRow = table.getSelectedRow();
        // 没有选择记录时不允许执行审核动作。
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an application first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 将视图行号转换为模型行号，避免排序后索引错位。
        int modelRow = table.convertRowIndexToModel(viewRow);
        // 从模型第一列读取申请 ID。
        String applicationId = String.valueOf(model.getValueAt(modelRow, 0));
        // 根据申请 ID 重新从存储层查询最新实体。
        Application application = applicationStorage.findById(applicationId, Application::getId);
        if (application == null) {
            // 如果数据已被删除，则提示并刷新表格。
            JOptionPane.showMessageDialog(this, "The selected application no longer exists.", "Application Missing", JOptionPane.ERROR_MESSAGE);
            reloadApplicantTable(model);
            return;
        }

        // 如果目标状态与当前状态相同，则无需重复更新。
        if (application.getStatus() == newStatus) {
            JOptionPane.showMessageDialog(this, "This application is already marked as " + newStatus + ".", "No Change", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 在真正修改前要求用户确认。
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Confirm marking this application as " + newStatus + "?",
                "Confirm Decision",
                JOptionPane.YES_NO_OPTION);
        // 用户取消时直接终止流程。
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        // 先更新内存中的申请状态。
        application.setStatus(newStatus);
        // 再通过存储层按 ID 回写到 CSV。
        applicationStorage.update(application, Application::getId);
        // 同步刷新表格中当前行的状态列，避免整表重载。
        model.setValueAt(newStatus, modelRow, 5);

        // 向用户提示审核结果已经保存。
        JOptionPane.showMessageDialog(
                this,
                "Application status updated to " + newStatus + ".",
                "Decision Saved",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label) {
        // 在左列放字段标签。
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        // 在右列放一个新的单行输入框。
        gbc.gridx = 1;
        JTextField field = new JTextField(24);
        panel.add(field, gbc);
    }
}
