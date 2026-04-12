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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Dashboard for teaching assistant users to manage profiles, browse jobs,
 * and track application outcomes.
 * It contains the TA-side executable L3 workflows available in the project.
 */
public class TADashboard extends BaseDashboard {
    // 存储 TA 档案数据。
    private final CsvStorage<TAProfile> profileStorage = new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);
    // 存储岗位数据，用于浏览开放岗位。
    private final CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
    // 存储申请数据，用于创建和展示申请记录。
    private final CsvStorage<Application> applicationStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);

    // 个人资料表单中的姓名输入框。
    private JTextField fullNameField;
    // 个人资料表单中的邮箱输入框。
    private JTextField emailField;
    // 个人资料表单中的学号输入框。
    private JTextField studentIdField;
    // 个人资料表单中的技能输入框。
    private JTextField skillsField;
    // 个人资料表单中的 GPA 输入框。
    private JTextField gpaField;
    // 个人资料表单中的简历路径输入框。
    private JTextField cvPathField;

    // 岗位表格对应的数据模型。
    private DefaultTableModel jobsModel;
    // 岗位表格的排序与过滤器。
    private TableRowSorter<TableModel> jobsSorter;
    // 岗位浏览表格组件。
    private JTable jobsTable;
    // 课程关键字过滤输入框。
    private JTextField moduleFilterField;
    // 技能关键字过滤输入框。
    private JTextField skillFilterField;

    // 申请记录表格的数据模型。
    private DefaultTableModel applicationsModel;
    // 申请记录表格组件。
    private JTable applicationsTable;

    public TADashboard(User user) {
        // 初始化公共仪表盘部分。
        super(user, "TA Dashboard");
        // 挂载 TA 专属标签页。
        add(buildTabs(), BorderLayout.CENTER);
        // 页面加载后立即回填当前用户已有档案。
        loadProfileIntoForm();
        // 初始加载开放岗位数据。
        reloadJobsTable();
        // 初始加载当前用户的申请记录。
        reloadApplicationsTable();
    }

    private JTabbedPane buildTabs() {
        // 创建 TA 工作区标签容器。
        JTabbedPane tabs = new JTabbedPane();
        // 资料维护页。
        tabs.addTab("Profile", createProfilePanel());
        // 岗位浏览页。
        tabs.addTab("Browse Jobs", createJobsPanel());
        // 申请追踪页。
        tabs.addTab("My Applications", createApplicationsPanel());
        return tabs;
    }

    private JPanel createProfilePanel() {
        // 使用表单布局构建个人资料编辑区。
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        // 定义统一布局约束。
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 创建姓名输入框。
        fullNameField = new JTextField(24);
        // 创建邮箱输入框。
        emailField = new JTextField(24);
        // 创建学号输入框。
        studentIdField = new JTextField(24);
        // 创建技能输入框。
        skillsField = new JTextField(24);
        // 创建 GPA 输入框。
        gpaField = new JTextField(24);
        // 创建简历路径输入框。
        cvPathField = new JTextField(18);
        // 简历路径由文件选择器回填，用户不能手动编辑。
        cvPathField.setEditable(false);

        // 添加姓名表单行。
        addField(panel, gbc, 0, "Full Name:", fullNameField);
        // 添加邮箱表单行。
        addField(panel, gbc, 1, "Email:", emailField);
        // 添加学号表单行。
        addField(panel, gbc, 2, "Student ID:", studentIdField);
        // 添加技能表单行。
        addField(panel, gbc, 3, "Skills (use ';' to separate):", skillsField);
        // 添加 GPA 表单行。
        addField(panel, gbc, 4, "GPA:", gpaField);
        // 添加 CV 文件选择行。
        addCvField(panel, gbc, 5);

        // 底部操作区放置保存与重新加载按钮。
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Profile");
        JButton reloadButton = new JButton("Reload");
        actions.add(reloadButton);
        actions.add(saveButton);

        // 将操作区放到第七行。
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(actions, gbc);

        // 增加当前功能说明文本。
        gbc.gridy = 7;
        JLabel note = new JLabel("L3 Pair C: TA profile editing supports validation, GPA checks, and CV file browsing.", SwingConstants.LEFT);
        note.setForeground(new Color(80, 80, 80));
        panel.add(note, gbc);

        // 点击保存时执行资料校验与持久化。
        saveButton.addActionListener(e -> saveProfile());
        // 点击重新加载时从 CSV 重新读取当前资料。
        reloadButton.addActionListener(e -> loadProfileIntoForm());
        return panel;
    }

    private JPanel createJobsPanel() {
        // 创建岗位浏览主面板。
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // 顶部过滤器区域使用网格布局。
        JPanel filterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 创建课程过滤输入框。
        moduleFilterField = new JTextField(16);
        // 创建技能过滤输入框。
        skillFilterField = new JTextField(16);

        // 第一行左侧放模块过滤标签。
        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(new JLabel("Module Filter:"), gbc);
        // 第一行放模块过滤输入框。
        gbc.gridx = 1;
        filterPanel.add(moduleFilterField, gbc);
        // 第一行继续放技能过滤标签。
        gbc.gridx = 2;
        filterPanel.add(new JLabel("Skill Filter:"), gbc);
        // 第一行最后放技能过滤输入框。
        gbc.gridx = 3;
        filterPanel.add(skillFilterField, gbc);

        // 定义只读岗位表格模型。
        jobsModel = new DefaultTableModel(new String[] {"Job ID", "Title", "Module", "Required Skills", "Hours", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // 创建岗位表格组件。
        jobsTable = new JTable(jobsModel);
        // 安装排序器，默认支持标题列排序，同时后续也用于过滤。
        jobsSorter = UIHelper.installSorter(jobsTable, 1);

        // 创建底部操作按钮区。
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearFiltersButton = new JButton("Clear Filters");
        JButton applyButton = new JButton("Apply for Selected Job");
        actions.add(clearFiltersButton);
        actions.add(applyButton);

        // 顶部放过滤区。
        panel.add(filterPanel, BorderLayout.NORTH);
        // 中部放岗位表格。
        panel.add(new JScrollPane(jobsTable), BorderLayout.CENTER);
        // 底部放操作按钮。
        panel.add(actions, BorderLayout.SOUTH);

        // 文本变化时自动刷新过滤结果。
        SimpleDocumentListener filterListener = new SimpleDocumentListener(this::applyJobFilters);
        moduleFilterField.getDocument().addDocumentListener(filterListener);
        skillFilterField.getDocument().addDocumentListener(filterListener);
        // 点击清空后清除两个过滤条件并重新展示全部开放岗位。
        clearFiltersButton.addActionListener(e -> {
            moduleFilterField.setText("");
            skillFilterField.setText("");
            applyJobFilters();
        });
        // 点击申请按钮后对当前选中岗位发起申请。
        applyButton.addActionListener(e -> applyForSelectedJob());
        return panel;
    }

    private JPanel createApplicationsPanel() {
        // 创建申请记录面板。
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // 定义只读申请表格模型。
        applicationsModel = new DefaultTableModel(new String[] {"Application ID", "Job Title", "Module", "Status", "Applied At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // 创建申请表格组件。
        applicationsTable = new JTable(applicationsModel);
        // 对整行启用状态着色渲染器。
        applicationsTable.setDefaultRenderer(Object.class, new StatusCellRenderer(3));
        // 对申请时间列安装排序器。
        UIHelper.installSorter(applicationsTable, 4);

        // 顶部展示说明文字。
        panel.add(new JLabel("Application records are shown here for TA tracking."), BorderLayout.NORTH);
        // 中部展示可滚动表格。
        panel.add(new JScrollPane(applicationsTable), BorderLayout.CENTER);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        // 左列放字段标签。
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        // 右列放对应输入框。
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void addCvField(JPanel panel, GridBagConstraints gbc, int row) {
        // 左列放 CV 标签。
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel("CV Path:"), gbc);

        // 右侧组合面板承载路径框和浏览按钮。
        JPanel cvPanel = new JPanel(new BorderLayout(6, 6));
        cvPanel.add(cvPathField, BorderLayout.CENTER);
        // 创建文件浏览按钮。
        JButton browseButton = new JButton("Browse CV");
        browseButton.addActionListener(e -> chooseCvFile());
        cvPanel.add(browseButton, BorderLayout.EAST);

        // 将组合面板放到当前行右列。
        gbc.gridx = 1;
        panel.add(cvPanel, gbc);
    }

    private void chooseCvFile() {
        // 打开文件选择器让用户选择简历文件。
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showOpenDialog(this);
        // 只有用户确认选择文件时才回填路径。
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            cvPathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void loadProfileIntoForm() {
        // 根据当前用户 ID 查找已保存的档案。
        TAProfile profile = findProfile(currentUser.getId());
        // 如果没有档案则清空表单，否则回填已有值。
        fullNameField.setText(profile == null ? "" : profile.getFullName());
        emailField.setText(profile == null ? "" : profile.getEmail());
        studentIdField.setText(profile == null ? "" : profile.getStudentId());
        skillsField.setText(profile == null ? "" : profile.getSkills());
        gpaField.setText(profile == null ? "" : String.valueOf(profile.getGpa()));
        cvPathField.setText(profile == null ? "" : profile.getCvPath());
    }

    private void saveProfile() {
        // 读取并清理姓名输入。
        String fullName = fullNameField.getText().trim();
        // 读取并清理邮箱输入。
        String email = emailField.getText().trim();
        // 读取并清理学号输入。
        String studentId = studentIdField.getText().trim();
        // 读取并清理技能输入。
        String skills = skillsField.getText().trim();
        // 读取并清理 GPA 文本。
        String gpaText = gpaField.getText().trim();
        // 读取并清理 CV 路径。
        String cvPath = cvPathField.getText().trim();

        // 任一字段为空都不允许保存档案。
        if (fullName.isEmpty() || email.isEmpty() || studentId.isEmpty() || skills.isEmpty() || gpaText.isEmpty() || cvPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All profile fields are required before saving.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 使用公共工具校验邮箱格式。
        if (!UIHelper.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 使用公共工具校验 GPA 范围与格式。
        if (!UIHelper.isValidGpa(gpaText)) {
            JOptionPane.showMessageDialog(this, "GPA must be between 0.0 and 4.0.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 通过解析后的数值写入档案对象。
        double gpa = Double.parseDouble(gpaText);
        // 先读取全部档案，准备更新或新增当前用户记录。
        List<TAProfile> profiles = profileStorage.loadAll();
        // target 指向当前用户对应的档案对象。
        TAProfile target = null;
        for (TAProfile profile : profiles) {
            if (currentUser.getId().equals(profile.getUserId())) {
                target = profile;
                break;
            }
        }
        // 如果当前用户还没有档案，则创建一条新记录。
        if (target == null) {
            target = new TAProfile();
            target.setId(java.util.UUID.randomUUID().toString());
            target.setUserId(currentUser.getId());
            profiles.add(target);
        }

        // 将表单中的值逐项回写到档案对象。
        target.setFullName(fullName);
        target.setEmail(email);
        target.setStudentId(studentId);
        target.setSkills(skills);
        target.setGpa(gpa);
        target.setCvPath(cvPath);
        // 将更新后的档案列表整体保存。
        profileStorage.saveAll(profiles);

        // 告知用户保存成功。
        JOptionPane.showMessageDialog(this, "Profile saved successfully.", "Profile Updated", JOptionPane.INFORMATION_MESSAGE);
        // 资料更新后刷新岗位列表，以保持页面状态一致。
        reloadJobsTable();
    }

    private void reloadJobsTable() {
        // 每次重载前先清空现有表格数据。
        jobsModel.setRowCount(0);
        // 只加载当前处于开放状态的岗位。
        for (Job job : jobStorage.loadAll()) {
            if (job.getStatus() == Job.JobStatus.OPEN) {
                jobsModel.addRow(new Object[] {
                        job.getId(),
                        job.getTitle(),
                        job.getModule(),
                        job.getRequiredSkills(),
                        job.getMaxHours(),
                        job.getStatus()
                });
            }
        }
        // 重载完成后重新应用当前过滤条件。
        applyJobFilters();
    }

    private void applyJobFilters() {
        // 排序器尚未初始化时直接返回，避免空指针。
        if (jobsSorter == null) {
            return;
        }
        // 读取模块关键字并统一转小写。
        String moduleKeyword = moduleFilterField == null ? "" : moduleFilterField.getText().trim().toLowerCase();
        // 读取技能关键字并统一转小写。
        String skillKeyword = skillFilterField == null ? "" : skillFilterField.getText().trim().toLowerCase();

        // 两个过滤条件都为空时，清除所有过滤器。
        if (moduleKeyword.isEmpty() && skillKeyword.isEmpty()) {
            jobsSorter.setRowFilter(null);
            return;
        }

        // 根据模块和技能两个条件组合设置行过滤器。
        jobsSorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                // 读取当前行的课程列内容并统一转小写。
                String module = String.valueOf(entry.getValue(2)).toLowerCase();
                // 读取当前行的技能列内容并统一转小写。
                String skills = String.valueOf(entry.getValue(3)).toLowerCase();
                // 课程关键字为空时视为自动匹配，否则要求包含关键字。
                boolean moduleMatched = moduleKeyword.isEmpty() || module.contains(moduleKeyword);
                // 技能关键字为空时视为自动匹配，否则要求包含关键字。
                boolean skillMatched = skillKeyword.isEmpty() || skills.contains(skillKeyword);
                // 只有两个条件都通过时才显示该行。
                return moduleMatched && skillMatched;
            }
        });
    }

    private void applyForSelectedJob() {
        // 读取当前在岗位表格中选中的行。
        int selectedViewRow = jobsTable.getSelectedRow();
        // 没有选中任何岗位时不允许提交申请。
        if (selectedViewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an OPEN job before applying.", "No Job Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 申请岗位前必须先存在个人档案。
        TAProfile profile = findProfile(currentUser.getId());
        if (profile == null) {
            JOptionPane.showMessageDialog(this, "Please complete your TA profile before applying for a job.", "Profile Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 将视图行号转成模型行号，适配排序和过滤后的表格。
        int selectedModelRow = jobsTable.convertRowIndexToModel(selectedViewRow);
        // 读取当前选中岗位的主键 ID。
        String jobId = String.valueOf(jobsModel.getValueAt(selectedModelRow, 0));

        // 遍历申请记录，拦截同一 TA 对同一岗位的重复申请。
        for (Application application : applicationStorage.loadAll()) {
            if (currentUser.getId().equals(application.getTaId()) && jobId.equals(application.getJobId())) {
                JOptionPane.showMessageDialog(this, "You have already applied for this job.", "Duplicate Application", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // 加载现有申请列表并追加一条新的待处理记录。
        List<Application> applications = applicationStorage.loadAll();
        applications.add(new Application(currentUser.getId(), jobId, Application.AppStatus.PENDING));
        // 将新申请保存到 CSV。
        applicationStorage.saveAll(applications);

        // 通知用户申请已提交成功。
        JOptionPane.showMessageDialog(this, "Application submitted successfully.", "Application Created", JOptionPane.INFORMATION_MESSAGE);
        // 刷新申请记录表格以显示新数据。
        reloadApplicationsTable();
    }

    private void reloadApplicationsTable() {
        // 每次重载前先清空申请表格。
        applicationsModel.setRowCount(0);
        // 仅展示当前 TA 自己提交的申请记录。
        for (Application application : applicationStorage.loadAll()) {
            if (currentUser.getId().equals(application.getTaId())) {
                // 根据 jobId 找到岗位信息，用于显示标题和模块。
                Job job = jobStorage.findById(application.getJobId(), Job::getId);
                applicationsModel.addRow(new Object[] {
                        application.getId(),
                        job == null ? "Unknown" : job.getTitle(),
                        job == null ? "Unknown" : job.getModule(),
                        application.getStatus(),
                        formatTimestamp(application.getAppliedAt())
                });
            }
        }
    }

    private TAProfile findProfile(String userId) {
        // 读取全部档案并按 userId 查找当前用户记录。
        List<TAProfile> profiles = profileStorage.loadAll();
        for (TAProfile profile : profiles) {
            if (userId.equals(profile.getUserId())) {
                return profile;
            }
        }
        // 未找到档案时返回 null。
        return null;
    }

    private String formatTimestamp(long timestamp) {
        // 将毫秒级时间戳格式化为易读的日期时间字符串。
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(timestamp));
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        // 记录状态列索引，供渲染时定位状态值。
        private final int statusColumn;

        private StatusCellRenderer(int statusColumn) {
            // 保存状态列位置。
            this.statusColumn = statusColumn;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // 先沿用父类默认渲染逻辑创建基础组件。
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            // 只有未选中时才根据状态上色，避免覆盖选中高亮样式。
            if (!isSelected) {
                // 读取当前行的状态列原始值。
                Object rawStatus = table.getValueAt(row, statusColumn);
                // 根据状态文本设置背景色。
                component.setBackground(resolveStatusColor(String.valueOf(rawStatus)));
            }
            return component;
        }

        private Color resolveStatusColor(String status) {
            // 已录用状态使用绿色系提示。
            if ("SELECTED".equalsIgnoreCase(status)) {
                return UIHelper.STATUS_SELECTED;
            }
            // 已拒绝状态使用红色系提示。
            if ("REJECTED".equalsIgnoreCase(status)) {
                return UIHelper.STATUS_REJECTED;
            }
            // 其他情况默认视为待处理状态。
            return UIHelper.STATUS_PENDING;
        }
    }

    private static class SimpleDocumentListener implements DocumentListener {
        // 统一保存文本变化后要执行的动作。
        private final Runnable action;

        private SimpleDocumentListener(Runnable action) {
            // 注入监听器触发时要执行的回调。
            this.action = action;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            // 插入文本后执行回调。
            action.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            // 删除文本后执行回调。
            action.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            // 属性变化时同样执行回调。
            action.run();
        }
    }
}
