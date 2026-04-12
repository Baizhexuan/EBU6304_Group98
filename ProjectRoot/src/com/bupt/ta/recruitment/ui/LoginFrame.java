package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.service.AuthService;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.DataSeeder;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Login window for authenticating users and routing them to the correct dashboard.
 * It coordinates startup data seeding, credential submission, and role-based
 * navigation after successful sign-in.
 */
public class LoginFrame extends JFrame {
    // 认证服务负责处理登录查询与用户名检查。
    private final AuthService authService;
    // 用户名输入框供用户填写登录名。
    private final JTextField usernameField = new JTextField(20);
    // 密码输入框使用掩码方式展示输入内容。
    private final JPasswordField passwordField = new JPasswordField(20);

    public LoginFrame() {
        // 程序第一次启动时先补齐基础测试数据。
        seedDataIfNeeded();
        // 使用用户 CSV 存储创建认证服务实例。
        this.authService = new AuthService(new CsvStorage<>("data/users.csv", User::fromCsvRow));

        // 设置窗口标题，说明当前页是登录入口。
        setTitle("TA Recruitment System - Login");
        // 设置登录窗口的基础尺寸。
        setSize(420, 250);
        // 关闭登录窗口时直接结束程序。
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 将登录窗口显示在屏幕中央。
        setLocationRelativeTo(null);

        // 使用 GridBagLayout 组织表单元素，便于双列表单对齐。
        JPanel panel = new JPanel(new GridBagLayout());
        // 统一定义表单控件的布局约束。
        GridBagConstraints gbc = new GridBagConstraints();
        // 给每个控件加外边距，避免界面过于拥挤。
        gbc.insets = new Insets(10, 10, 10, 10);
        // 让输入框在水平方向可以拉伸填充。
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 将“用户名”标签放在第一行左侧。
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        // 将用户名输入框放在第一行右侧。
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        // 将“密码”标签放在第二行左侧。
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        // 将密码输入框放在第二行右侧。
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        // 创建登录按钮，触发认证流程。
        JButton loginButton = new JButton("Login");
        // 创建注册按钮，跳转到注册界面。
        JButton registerButton = new JButton("Register");
        // 使用单独的按钮面板承载两个操作按钮。
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        // 将按钮面板放到第三行并横跨两列。
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        // 把组装完成的表单面板加入窗口。
        add(panel);

        // 点击登录按钮后执行登录校验。
        loginButton.addActionListener(e -> handleLogin());
        // 点击注册按钮后打开注册页，并关闭当前登录页。
        registerButton.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
    }

    private void seedDataIfNeeded() {
        // 临时读取用户数据，判断系统是否需要初始化示例账户。
        CsvStorage<User> storage = new CsvStorage<>("data/users.csv", User::fromCsvRow);
        // 当用户表为空时，执行数据种子程序生成演示数据。
        if (storage.loadAll().isEmpty()) {
            DataSeeder.main(new String[0]);
        }
    }

    private void handleLogin() {
        // 读取并去除用户名首尾空格。
        String username = usernameField.getText().trim();
        // 读取密码数组并转成字符串，再去除首尾空格。
        String password = new String(passwordField.getPassword()).trim();

        // 用户名和密码都为空时给出统一提示。
        if (username.isEmpty() && password.isEmpty()) {
            showError("Username and password cannot be empty.");
            return;
        }
        // 单独校验用户名为空的情况。
        if (username.isEmpty()) {
            showError("Username cannot be empty.");
            return;
        }
        // 单独校验密码为空的情况。
        if (password.isEmpty()) {
            showError("Password cannot be empty.");
            return;
        }

        // 调用服务层执行登录认证。
        Optional<User> userOpt = authService.login(username, password);
        if (userOpt.isPresent()) {
            // 登录成功后根据角色打开对应仪表盘。
            openDashboard(userOpt.get());
            // 关闭当前登录窗口，避免重复保留。
            dispose();
            return;
        }

        // 如果用户名存在，则说明失败原因更可能是密码错误。
        if (authService.findUserByUsername(username).isPresent()) {
            showError("Invalid password.");
        } else {
            // 用户名不存在时提示找不到用户。
            showError("User not found.");
        }
    }

    private void openDashboard(User user) {
        // 按当前登录用户的角色分发到对应首页。
        switch (user.getRole()) {
            case ADMIN:
                new AdminDashboard(user).setVisible(true);
                break;
            case MO:
                new MODashboard(user).setVisible(true);
                break;
            case TA:
                new TADashboard(user).setVisible(true);
                break;
            default:
                // 如果角色超出预期，直接报错而不是静默失败。
                showError("Unknown role. Cannot open dashboard.");
        }
    }

    private void showError(String message) {
        // 统一弹出登录错误消息框，减少重复 UI 代码。
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }
}
