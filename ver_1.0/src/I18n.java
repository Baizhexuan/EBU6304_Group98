import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;

/**
 * Lightweight UI translation helper for the BUPT/QMUL demo.
 *
 * <p>The application stores workflow data in English, so this helper only
 * translates visible interface text. It deliberately avoids editable fields so
 * user-entered names, skills, CV paths, notes, and messages are not modified.</p>
 */
public final class I18n {
    public enum Language {
        ENGLISH,
        CHINESE
    }

    private static Language currentLanguage = Language.ENGLISH;
    private static final Map<String, String> EN_TO_ZH = new LinkedHashMap<String, String>();

    static {
        addCommon();
        addLoginAndRegistration();
        addDashboards();
        addNotificationsAndMessages();
        addValidationAndStatus();
    }

    private I18n() {
    }

    public static Language getLanguage() {
        return currentLanguage;
    }

    public static boolean isChinese() {
        return currentLanguage == Language.CHINESE;
    }

    public static void setLanguage(Language language) {
        if (language != null) {
            currentLanguage = language;
        }
    }

    public static String t(String text) {
        return translate(text);
    }

    public static JPanel createLanguageSwitcher(Runnable afterChange) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.putClientProperty("i18n.skip", Boolean.TRUE);

        javax.swing.JButton englishButton = new javax.swing.JButton("English");
        javax.swing.JButton chineseButton = new javax.swing.JButton("中文");
        englishButton.putClientProperty("i18n.skip", Boolean.TRUE);
        chineseButton.putClientProperty("i18n.skip", Boolean.TRUE);

        Runnable refreshStyle = () -> {
            boolean english = currentLanguage == Language.ENGLISH;
            styleLanguageButton(englishButton, english);
            styleLanguageButton(chineseButton, !english);
        };
        englishButton.addActionListener(e -> {
            currentLanguage = Language.ENGLISH;
            refreshStyle.run();
            if (afterChange != null) {
                afterChange.run();
            }
        });
        chineseButton.addActionListener(e -> {
            currentLanguage = Language.CHINESE;
            refreshStyle.run();
            if (afterChange != null) {
                afterChange.run();
            }
        });
        refreshStyle.run();
        panel.add(englishButton);
        panel.add(chineseButton);
        return panel;
    }

    public static void applyTo(Window window) {
        if (window == null) {
            return;
        }
        if (window instanceof javax.swing.JFrame) {
            JMenuBar menuBar = ((javax.swing.JFrame) window).getJMenuBar();
            if (menuBar != null) {
                applyToComponent(menuBar);
            }
        }
        if (window instanceof javax.swing.JDialog) {
            JMenuBar menuBar = ((javax.swing.JDialog) window).getJMenuBar();
            if (menuBar != null) {
                applyToComponent(menuBar);
            }
        }
        applyToComponent(window);
        SwingUtilities.invokeLater(() -> {
            window.invalidate();
            window.validate();
            window.repaint();
        });
    }

    public static String translate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        return currentLanguage == Language.CHINESE ? translateEnglishToChinese(value) : translateChineseToEnglish(value);
    }

    private static void applyToComponent(Component component) {
        if (component == null) {
            return;
        }
        if (component instanceof JComponent
                && Boolean.TRUE.equals(((JComponent) component).getClientProperty("i18n.skip"))) {
            return;
        }

        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            label.setText(translate(label.getText()));
            label.setToolTipText(translate(label.getToolTipText()));
        } else if (component instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) component;
            button.setText(translate(button.getText()));
            button.setToolTipText(translate(button.getToolTipText()));
            BaseDashboard.resizeButtonToFitText(button);
        } else if (component instanceof JTextArea) {
            JTextArea area = (JTextArea) component;
            if (!area.isEditable()) {
                area.setText(translate(area.getText()));
            }
        } else if (component instanceof JTabbedPane) {
            translateTabs((JTabbedPane) component);
        } else if (component instanceof JTable) {
            translateTableHeaders((JTable) component);
        } else if (component instanceof JComboBox) {
            installComboRenderer((JComboBox<?>) component);
        }

        if (component instanceof JComponent) {
            translateBorder((JComponent) component);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyToComponent(child);
            }
        }
    }

    private static void translateTabs(JTabbedPane pane) {
        for (int i = 0; i < pane.getTabCount(); i++) {
            pane.setTitleAt(i, translate(pane.getTitleAt(i)));
            pane.setToolTipTextAt(i, translate(pane.getToolTipTextAt(i)));
        }
    }

    private static void translateTableHeaders(JTable table) {
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            Object header = column.getHeaderValue();
            if (header instanceof String) {
                column.setHeaderValue(translate((String) header));
            }
        }
        if (table.getTableHeader() != null) {
            table.getTableHeader().repaint();
        }
    }

    private static void translateBorder(JComponent component) {
        Border border = component.getBorder();
        if (border instanceof TitledBorder) {
            TitledBorder titled = (TitledBorder) border;
            titled.setTitle(translate(titled.getTitle()));
        }
    }

    private static void installComboRenderer(JComboBox<?> comboBox) {
        if (Boolean.TRUE.equals(comboBox.getClientProperty("i18n.renderer.installed"))) {
            comboBox.repaint();
            return;
        }
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (component instanceof JLabel && value instanceof String) {
                    ((JLabel) component).setText(I18n.t((String) value));
                }
                return component;
            }
        });
        comboBox.putClientProperty("i18n.renderer.installed", Boolean.TRUE);
    }

    private static String translateEnglishToChinese(String value) {
        String translated = value;
        for (String key : sortedEnglishKeys()) {
            translated = translated.replace(key, EN_TO_ZH.get(key));
        }
        return translated;
    }

    private static String translateChineseToEnglish(String value) {
        String translated = value;
        for (Map.Entry<String, String> entry : sortedChineseEntries()) {
            translated = translated.replace(entry.getValue(), entry.getKey());
        }
        return translated;
    }

    private static List<String> sortedEnglishKeys() {
        List<String> keys = new ArrayList<String>(EN_TO_ZH.keySet());
        keys.sort(Comparator.comparingInt(String::length).reversed());
        return keys;
    }

    private static List<Map.Entry<String, String>> sortedChineseEntries() {
        List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>(EN_TO_ZH.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue().length(), a.getValue().length()));
        return entries;
    }

    private static void styleLanguageButton(javax.swing.JButton button, boolean selected) {
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBackground(selected ? BaseDashboard.ACCENT_COLOR : BaseDashboard.SURFACE_COLOR);
        button.setForeground(selected ? java.awt.Color.WHITE : BaseDashboard.ACCENT_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(selected ? BaseDashboard.ACCENT_COLOR : BaseDashboard.BORDER_COLOR),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        button.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 11));
        BaseDashboard.resizeButtonToFitText(button);
    }

    private static void put(String english, String chinese) {
        EN_TO_ZH.put(english, chinese);
    }

    private static void addCommon() {
        put("BUPT International School", "北京邮电大学国际学院");
        put("BUPT TA Recruitment System", "BUPT TA 招聘系统");
        put("TA Recruitment System", "TA 招聘系统");
        put("Account", "账户");
        put("Logout", "退出登录");
        put("Help", "帮助");
        put("About This Build", "关于此版本");
        put("About This Demo", "关于此演示");
        put("About", "关于");
        put("Close", "关闭");
        put("Cancel", "取消");
        put("Save", "保存");
        put("Saved", "已保存");
        put("Refresh", "刷新");
        put("Search", "搜索");
        put("Clear", "清空");
        put("Info", "提示");
        put("Error", "错误");
        put("Validation", "验证提示");
        put("Status", "状态");
        put("Summary", "摘要");
        put("Title", "标题");
        put("Module", "模块");
        put("Skills", "技能");
        put("Hours", "小时");
        put("Location", "地点");
        put("Email", "邮箱");
        put("Username", "用户名");
        put("Password", "密码");
        put("Role", "角色");
        put("Message", "消息");
        put("Messages", "消息");
        put("Notifications", "通知");
        put("Open notifications and messages", "打开通知和消息");
        put("Created At", "创建时间");
        put("Action Hint", "操作提示");
        put("Suggested action", "建议操作");
        put("No action required.", "无需操作。");
        put("No suggested action.", "暂无建议操作。");
        put("Refresh Advice", "刷新建议");
        put("Ask AI Assistant", "询问 AI 助手");
        put("Ask AI", "询问 AI");
        put("Copy Response", "复制回复");
        put("AI Recruitment Assistant", "AI 招聘助手");
        put("Ask AI about matching, workload, or applicant risk", "询问 AI 关于匹配、工作量或申请者风险的问题");
        put("Which TA should be considered as a safer replacement, and why?", "哪位 TA 更适合作为更稳妥的替补人选？为什么？");
        put("Ask a question to generate model-backed recruitment guidance. If OPENAI_API_KEY is not set, the dialog will use a local explainable fallback.",
                "输入问题后生成招聘建议。如果未设置 OPENAI_API_KEY，系统会使用本地可解释的备用逻辑。");
    }

    private static void addLoginAndRegistration() {
        put("TA Portal", "TA 门户");
        put("MO Dashboard", "MO 工作台");
        put("Admin Workload", "管理员工作负载");
        put("Account Registration", "账户注册");
        put("Sign In", "登录");
        put("Access the applicant, module organiser, or administrator workspace.", "进入申请者、Module Organiser 或管理员工作区。");
        put("Log In", "登录");
        put("Register", "注册");
        put("Use a demo account or register a TA/MO account.", "使用演示账户，或注册 TA/MO 账户。");
        put("Demo Access", "演示账户");
        put("Choose a role and continue the recruitment flow from the matching dashboard.", "选择一个角色，并从匹配工作台继续招聘流程。");
        put("TA applies &rarr; MO reviews &rarr; Admin checks workload", "TA 申请 &rarr; MO 审核 &rarr; Admin 检查工作量");
        put("Create Demo Account", "创建演示账户");
        put("Create Account", "创建账户");
        put("Register a TA or MO account for the recruitment portal.", "为招聘系统注册 TA 或 MO 账户。");
        put("Confirm Password", "确认密码");
        put("Display Name", "显示名称");
        put("After registration, sign in and continue from the role dashboard.", "注册后请登录，并从对应角色工作台继续。");
        put("Registration Complete", "注册完成");
        put("Login Failed", "登录失败");
        put("Missing Input", "缺少输入");
        put("Missing Username", "缺少用户名");
        put("Missing Password", "缺少密码");
    }

    private static void addDashboards() {
        put("TA Dashboard", "TA 工作台");
        put("Admin Dashboard", "管理员工作台");
        put("My Profile", "我的资料");
        put("Browse Jobs", "浏览岗位");
        put("My Applications", "我的申请");
        put("Profile Completion", "资料完整度");
        put("Keep your TA profile complete before applying. This demo checks email format, GPA range, CV path, availability, and a short statement so the next AI matching stage has richer context to work with.",
                "申请前请完善 TA 资料。本演示会检查邮箱格式、GPA 范围、CV 路径、可工作时间和个人陈述，为后续 AI 匹配提供更完整的上下文。");
        put("Full Name:", "姓名:");
        put("Email:", "邮箱:");
        put("Student ID:", "学号:");
        put("Skills:", "技能:");
        put("GPA:", "GPA:");
        put("CV Path:", "CV 路径:");
        put("Availability:", "可工作时间:");
        put("Personal Statement:", "个人陈述:");
        put("Save Profile", "保存资料");
        put("Browse", "浏览");
        put("AI Match Ranking", "AI 匹配排名");
        put("Open Jobs and Match Guidance", "开放岗位与匹配建议");
        put("Browse currently open jobs, filter each major attribute separately, and compare the AI-ready match explanation before applying. Missing skills are surfaced explicitly for decision making.",
                "浏览当前开放岗位，按主要属性分别筛选，并在申请前对比 AI 匹配解释。缺失技能会直接展示，方便做决定。");
        put("TA AI Match Ranking", "TA AI 匹配排名");
        put("Profile focus:", "资料重点:");
        put("Required skills:", "所需技能:");
        put("MO/job characteristics:", "MO/岗位特征:");
        put("AI summary:", "AI 摘要:");
        put("Availability:", "可工作时间:");
        put("Statement:", "个人陈述:");
        put("Hours:", "小时:");
        put("Description:", "描述:");
        put("N/A", "无");
        put("Match insight will appear here after refresh.", "刷新后会显示匹配洞察。");
        put("Apply for Selected Job", "申请选中岗位");
        put("Ask AI About Matches", "询问 AI 匹配建议");
        put("Search open jobs", "搜索开放岗位");
        put("Search applications", "搜索申请");
        put("Application summary will appear here after refresh.", "刷新后会显示申请摘要。");
        put("Withdraw Selected Pending Application", "撤回选中的待处理申请");
        put("Notification summary will appear here after refresh.", "刷新后会显示通知摘要。");
        put("Refresh Notifications", "刷新通知");
        put("Mark Selected as Read", "标记选中为已读");
        put("Mark All as Read", "全部标记为已读");
        put("Job ID", "岗位 ID");
        put("App ID", "申请 ID");
        put("AI Match", "AI 匹配");
        put("Missing Skills", "缺失技能");
        put("MO Note", "MO 备注");
        put("Applied At", "申请时间");
        put("Match", "匹配度");

        put("Post Job", "发布岗位");
        put("My Job Posts", "我的岗位");
        put("Applicants", "申请者");
        put("Publish and Maintain Job Posts", "发布与维护岗位");
        put("Use this screen to release a new TA opportunity with clear skills, workload, and location information. Stronger descriptions help later AI-assisted screening and admin-side reallocation decisions.",
                "在此发布新的 TA 机会，并填写清晰的技能、工作量和地点信息。更完整的描述有助于后续 AI 辅助筛选和管理员调配。");
        put("Job Title:", "岗位标题:");
        put("Module Code:", "模块代码:");
        put("Required Skills:", "所需技能:");
        put("Max Weekly Hours:", "每周最大小时:");
        put("Location:", "地点:");
        put("Description:", "描述:");
        put("Post Job Opening", "发布岗位");
        put("Clear Form", "清空表单");
        put("Search job posts", "搜索岗位发布");
        put("Job summary will appear here after refresh.", "刷新后会显示岗位摘要。");
        put("Search applicants", "搜索申请者");
        put("Applicant summary will appear here after refresh.", "刷新后会显示申请者摘要。");
        put("Select Application", "录用申请");
        put("Reject Application", "拒绝申请");
        put("Rate Completed Work", "评价完成工作");
        put("Reputation", "信誉分");
        put("Current Hours", "当前小时");
        put("Current Workload", "当前工作量");

        put("Workload Monitor", "工作量监控");
        put("Applications Overview", "申请总览");
        put("Jobs Overview", "岗位总览");
        put("TA Username", "TA 用户名");
        put("Full Name", "姓名");
        put("Selected Jobs", "已选岗位");
        put("AI System Insight and Reallocation Advice", "AI 系统洞察与调配建议");
        put("Export CSV Report", "导出 CSV 报告");
        put("Save Changes", "保存修改");
        put("Undo Unsaved Changes", "撤销未保存修改");
        put("Search workload", "搜索工作量");
        put("Search jobs", "搜索岗位");
        put("Recommendation focus", "建议关注点");
        put("Global risk overview", "全局风险概览");
        put("MO", "MO");
        put("TA", "TA");
        put("Note", "备注");
        put("OPEN", "开放");
        put("CLOSED", "关闭");
        put("PENDING", "待处理");
        put("SELECTED", "已录用");
        put("REJECTED", "已拒绝");
        put("WITHDRAWN", "已撤回");
        put("NEAR LIMIT", "接近上限");
        put("OVERLOAD", "超负荷");
        put("OK", "正常");
        put("ALL", "全部");
        put("UNREAD", "未读");
        put("READ", "已读");
        put("None", "无");
        put("Matched:", "已匹配:");
        put("Missing:", "缺失:");
        put("Source:", "来源:");
        put("local rule-based scorer", "本地规则评分器");
        put("Reputation penalty", "信誉分惩罚");
        put("Base match", "基础匹配");
        put("adjusted", "已调整");
    }

    private static void addNotificationsAndMessages() {
        put("Bell Centre", "提醒中心");
        put("Notifications and TA-MO conversations", "通知与 TA-MO 对话");
        put("Select a notification", "选择一条通知");
        put("No notifications yet", "暂无通知");
        put("Workflow updates will appear here.", "流程更新会显示在这里。");
        put("You are all caught up.", "当前没有待处理事项。");
        put("Mark Read", "标记已读");
        put("Mark All Read", "全部已读");
        put("All notifications marked as read.", "所有通知已标记为已读。");
        put("Select a conversation", "选择一个对话");
        put("No conversation selected", "未选择对话");
        put("TA-MO conversations become available after an application connects both sides.", "申请把 TA 和 MO 关联后，对话才会出现在这里。");
        put("Approve", "同意对话");
        put("Read Incoming", "读取新消息");
        put("Send", "发送");
        put("Incoming messages marked as read.", "收到的消息已标记为已读。");
        put("No messages yet.", "暂无消息。");
        put("No messages yet", "暂无消息");
        put("Send a first message to start the conversation.", "发送第一条消息开始对话。");
        put("You", "我");
        put("Them", "对方");
        put("NEW", "新");
        put("Notification marked as read.", "通知已标记为已读。");
        put("No TA-MO contact is available for this user.", "此用户暂无 TA-MO 联系人。");
        put("No Contact", "暂无联系人");
        put("Conversation approved. The three-message limit is now lifted for this contact.", "对话已同意，此联系人不再受三条消息限制。");
        put("Remaining pre-approval messages", "同意前剩余可发送消息数");
        put("Conversation approved", "对话已同意");
        put("No notifications are available. Open Messages to view TA-MO conversations.",
                "当前没有通知。请打开 Messages / 消息 查看 TA-MO 对话。");
        put("No notifications are available, so Messages is shown first.",
                "当前没有通知，因此已优先显示 Messages / 消息 页面。");
        put("No TA-MO conversations are available for this account yet.",
                "当前账号暂时没有可用的 TA-MO 对话。");
        put("No application-linked conversations were found, so open MO job contacts are shown for demo use.",
                "没有找到已关联申请的对话，因此系统显示开放岗位的 MO 联系人用于演示。");
        put("No applicant-linked conversations were found, so TA contacts are shown for demo use.",
                "没有找到已关联申请的对话，因此系统显示 TA 联系人用于演示。");
        put("Pre-approval messages left: ", "同意前剩余可发送消息数: ");
        put("TA is waiting for MO approval. Pre-approval messages left: ", "TA 正在等待 MO 同意对话。同意前剩余可发送消息数: ");
        put("Waiting for MO approval. Pre-approval messages left: ", "等待 MO 同意对话。同意前剩余可发送消息数: ");
        put("Only the MO for this job can approve the conversation.", "只有该岗位的 MO 可以同意对话。");
        put("MO can approve this conversation.", "MO 可以同意开启此对话。");
        put("Send a concise question or approve the conversation when the other side needs a longer discussion.",
                "可以先发送简短问题；如果需要继续深入沟通，则由 MO 同意开启完整对话。");
        put("Message text is required.", "消息内容不能为空。");
        put("Recipient not found.", "未找到接收方。");
        put("Conversation consent required. You cannot send more than 3 messages before the other side approves.",
                "需要对方同意对话。在对方同意前，你不能发送超过 3 条消息。");
        put("Message sent.", "消息已发送。");
        put("Message sent. Conversation already approved, so the three-message limit is lifted.",
                "消息已发送。该对话已经同意，因此三条消息限制已解除。");
        put("Message sent. Pre-approval messages left: ", "消息已发送。同意前剩余可发送消息数: ");

        put("Application update for ", "申请状态更新: ");
        put("Open My Applications to review the latest status and note.", "打开“我的申请”查看最新状态和备注。");
        put("You have been selected for ", "你已被录用到 ");
        put("Your application for ", "你对 ");
        put(" was not selected by ", " 的申请未被录用，审核人: ");
        put(" was updated to ", " 的申请状态已更新为 ");
        put(" by ", "，审核人: ");
        put("Profile completion required", "需要完善资料");
        put("Please complete your TA profile before applying for jobs. The system needs your skills, GPA, availability and CV path for fair screening.",
                "申请岗位前请完善 TA 资料。系统需要你的技能、GPA、可工作时间和 CV 路径，以便公平筛选。");
        put("Open My Profile and complete all required fields before submitting applications.", "打开“我的资料”，在提交申请前补全所有必填项。");
        put("Job closed: ", "岗位已关闭: ");
        put(" has been closed by ", " 已由 ");
        put(". Check My Applications for the current status before planning further applications.",
                " 关闭。请在计划其他申请前查看“我的申请”的当前状态。");
        put("Open My Applications and review other open jobs if needed.", "打开“我的申请”，必要时查看其他开放岗位。");
        put("New message from ", "新消息来自 ");
        put(" sent you a message about ", " 发来一条关于 ");
        put("general recruitment conversation", "一般招聘沟通");
        put("Open the bell centre to read the message and reply.", "打开提醒中心阅读消息并回复。");
        put("Work evaluation for ", "工作评价: ");
        put(" rated your completed work for ", " 对你在 ");
        put(" as ", " 的完成工作评分为 ");
        put(" Because the original match score was high but the completion rating was low, your reputation score is now ",
                " 因为原始匹配分较高但完成评分较低，你当前的信誉分为 ");
        put(". This is a review signal for future matching, not an automatic misconduct decision.",
                "。这是后续匹配的审核信号，不是自动判定违规。");

        put("Could we discuss the Java lab role?", "我们可以讨论一下 Java lab 岗位吗？");
        put("I have a question about timing.", "我想问一下时间安排。");
        put("Please confirm the expected workload.", "请确认预期工作量。");
        put("Fourth message before approval.", "同意前的第四条消息。");
        put("Thanks for approving the chat.", "感谢你同意开启对话。");
        put("Could we discuss", "我们可以讨论");
        put("I have a question", "我有一个问题");
        put("Please confirm", "请确认");
        put("expected workload", "预期工作量");
        put("before approval", "同意前");
        put("approving the chat", "同意开启对话");
    }

    private static void addValidationAndStatus() {
        put("Please enter both username and password.", "请输入用户名和密码。");
        put("Username cannot be empty.", "用户名不能为空。");
        put("Password cannot be empty.", "密码不能为空。");
        put("Username not found.", "未找到该用户名。");
        put("Password is incorrect.", "密码不正确。");
        put("Unknown role:", "未知角色:");
        put("All fields are required.", "所有字段均为必填。");
        put("Password confirmation does not match.", "两次输入的密码不一致。");
        put("Username already exists.", "用户名已存在。");
        put("Account created. Please sign in and complete the remaining workflow in the dashboard.", "账户已创建。请登录并在工作台中完成后续流程。");
        put("Profile status: not loaded yet.", "资料状态: 尚未加载。");
        put("Profile status: complete your information before applying.", "资料状态: 申请前请先完善信息。");
        put("Profile status: saved and ready for AI-assisted job matching. Reputation:", "资料状态: 已保存，可用于 AI 辅助岗位匹配。信誉分:");
        put("Name, email, student ID and skills are required.", "姓名、邮箱、学号和技能为必填。");
        put("Please enter a valid email address.", "请输入有效邮箱地址。");
        put("GPA should be between 0.0 and 4.0.", "GPA 应在 0.0 到 4.0 之间。");
        put("Profile saved successfully.", "资料保存成功。");
        put("No open jobs match the current filters.", "当前筛选条件下没有开放岗位。");
        put("Visible jobs:", "可见岗位:");
        put("Best current match:", "当前最佳匹配:");
        put("Please select a job first.", "请先选择一个岗位。");
        put("CV uploaded to local demo storage.", "CV 已上传到本地演示存储。");
        put("CV Uploaded", "CV 上传完成");
        put("Unable to upload CV:", "无法上传 CV:");
        put("Upload Error", "上传错误");
        put("Profile Required", "需要完善资料");
        put("You have already applied for this job.", "你已经申请过该岗位。");
        put("Duplicate Application", "重复申请");
        put("Application Submitted", "申请已提交");
        put("Please select a notification first.", "请先选择一条通知。");
        put("Please select an application first.", "请先选择一个申请。");
        put("Only pending applications can be withdrawn.", "只有待处理申请可以撤回。");
        put("Pending:", "待处理:");
        put("Selected:", "已录用:");
        put("Rejected:", "已拒绝:");
        put("Total notifications:", "通知总数:");
        put("Unread:", "未读:");
        put("Refreshing...", "正在刷新...");
        put("Please wait a moment.", "请稍候。");
        put("Refreshing open jobs and match ranking...", "正在刷新开放岗位和匹配排名...");
        put("Refreshing your applications...", "正在刷新你的申请...");
        put("Refreshing notifications...", "正在刷新通知...");
        put("Refreshing your job posts...", "正在刷新你的岗位发布...");
        put("Refreshing applicants and match ranking...", "正在刷新申请者和匹配排名...");
        put("Refreshing workload overview...", "正在刷新工作量总览...");
        put("Application updates saved.", "申请更新已保存。");
        put("Hours must be a positive integer.", "小时数必须为正整数。");
        put("Report exported to", "报告已导出至");
        put("Export Complete", "导出完成");
        put("Failed to export report:", "导出报告失败:");
        put("Export Error", "导出错误");
        put("Unsaved Changes", "未保存修改");
        put("Response copied to clipboard.", "回复已复制到剪贴板。");
        put("Requesting model response...", "正在请求模型回复...");
        put("Thinking with the current recruitment context...", "正在结合当前招聘上下文思考...");
        put("AI request failed:", "AI 请求失败:");
        put("Teaching Assistant Recruitment Demo", "助教招聘演示系统");
        put("Current focus: faster page refresh, cleaner search interactions, board-level AI matching support, and smoother final-demo responsiveness.",
                "当前重点: 更快的页面刷新、更清晰的搜索交互、管理层 AI 匹配支持，以及更流畅的最终演示响应。");
        put("Planned next steps: package the final report, capture polished screenshots, and submit the final software bundle with documentation and tests.",
                "后续计划: 打包最终报告、截取精修截图，并提交包含文档和测试的最终软件包。");
        put("Scoring provider:", "评分提供方:");
        put("Mode:", "模式:");
        put("Provider ready:", "提供方可用:");
        put("Yes", "是");
        put("No", "否");
        put("RuleBasedSkillScoringProvider", "规则型技能评分器");
        put("RULE", "规则模式");
        put("Local rule-based scoring is active. No network access or API key is required.",
                "当前启用本地规则评分。不需要网络访问或 API key。");
    }
}
