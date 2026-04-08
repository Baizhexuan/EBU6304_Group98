# BUPT TA Recruitment System - Iteration 1 & 2 Prototype

本代码库包含了 EBU6304 软件工程项目组（Group 098）在 Iteration 1 和 Iteration 2 中实现的核心 Java GUI 原型。

**技术栈与合规说明：**
* **UI 框架**: Java Swing (纯原生，无外部依赖)
* **数据存储**: 本地文本文件 I/O (严格遵守项目手册中“禁止使用数据库”的约束)

---

### 1. 核心数据存储模块 (`DataStorage.java`)

**功能描述：** 该类负责处理系统所有的持久化数据操作。通过 Java 原生的 `FileWriter` 和 `PrintWriter`，将用户的交互记录和状态追加保存到本地的 `.txt` 文件中，以此替代传统数据库。

```java
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 数据存储类 (无数据库实现方案)
 * 用于满足项目需求中 "禁止使用 SQL/NoSQL 数据库" 的硬性规定。
 */
public class DataStorage {
    // 定义本地存储文件路径 (自动生成在项目根目录)
    private static final String FILE_PATH = "system_data.txt";

    /**
     * 将操作记录保存到本地文本文件
     * @param role 触发该操作的角色 (如: TA, MO, Admin)
     * @param action 执行的具体动作 (如: Upload CV)
     * @param details 详细信息或附带的数据
     */
    public static void saveData(String role, String action, String details) {
        // 使用 try-with-resources 确保文件流自动关闭
        try (FileWriter fw = new FileWriter(FILE_PATH, true);
             PrintWriter pw = new PrintWriter(fw)) {
             
            // 格式化输出: [角色] - 动作: 详情
            String record = String.format("[%s] - %s: %s", role, action, details);
            pw.println(record);
            System.out.println("✅ 成功写入本地文件: " + record);
            
        } catch (IOException e) {
            System.err.println("❌ 保存失败！请检查文件读写权限！");
            e.printStackTrace();
        }
    }
}


```
### 2. 系统主界面与交互逻辑 (`SystemUI.java`)

**功能描述：** 系统的图形用户界面（GUI），基于 Java Swing 开发。它提供了一个中央仪表盘，通过三个核心按钮将用户导航至不同角色的工作流（TA 门户、MO 面板、管理员监控）。点击按钮不仅会弹出交互反馈，还会触发底层的 `DataStorage` 进行数据记录。

```java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 核心系统界面类
 * 提供 TA (学生), MO (教师), Admin (管理员) 三大角色的入口。
 */
public class SystemUI extends JFrame {

    public SystemUI() {
        // 1. 设置窗口基础属性
        setTitle("BUPT TA Recruitment System - Group 098");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 窗口居中显示

        // 2. 初始化主面板与布局 (网格布局)
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // 添加欢迎标题
        JLabel titleLabel = new JLabel("Welcome! Please select your role:", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(titleLabel);

        // 3. 实例化三大核心角色按钮
        JButton btnTA = new JButton("👨‍🎓 I am a TA (Student Portal)");
        JButton btnMO = new JButton("👨‍🏫 I am an MO (Teacher Dashboard)");
        JButton btnAdmin = new JButton("⚙️ I am an Admin (Workload Monitor)");

        // 4. 绑定按钮点击事件 (模拟 User Stories 的验收标准)
        
        // TA 逻辑 (对应 US01, US02)
        btnTA.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "TA Portal Opened!\nFeature: CV Upload & Job Application");
                // 模拟上传简历并调用存储接口
                DataStorage.saveData("TA", "Upload CV", "baizhexuan_resume.pdf uploaded successfully.");
            }
        });

        // MO 逻辑 (对应 US05, US06)
        btnMO.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "MO Dashboard Opened!\nFeature: Post Jobs & Review TAs");
                DataStorage.saveData("MO", "Post Job", "Created new TA position for module CS101.");
            }
        });

        // Admin 逻辑 (对应 US03, US07)
        btnAdmin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Admin Monitor Opened!\nFeature: TA Workload Checking");
                DataStorage.saveData("Admin", "Check Workload", "Viewed system-wide TA working hours.");
            }
        });

        // 5. 将按钮加入面板
        panel.add(btnTA);
        panel.add(btnMO);
        panel.add(btnAdmin);

        add(panel);
    }
}
