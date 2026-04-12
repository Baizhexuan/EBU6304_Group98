package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.service.AuthService;
import com.bupt.ta.recruitment.util.CsvStorage;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Registration window for creating new TA and MO accounts.
 * It validates user input before delegating persistence to the authentication
 * service.
 */
public class RegisterFrame extends JFrame {
    // 注册页同样复用认证服务来完成用户持久化。
    private final AuthService authService;
    // 用户名输入框。
    private final JTextField usernameField = new JTextField(20);
    // 密码输入框。
    private final JPasswordField passwordField = new JPasswordField(20);
    // 二次确认密码输入框。
    private final JPasswordField confirmPasswordField = new JPasswordField(20);
    // 角色下拉框只允许注册 TA 或 MO 账号。
    private final JComboBox<User.UserRole> roleComboBox = new JComboBox<>(new User.UserRole[] {User.UserRole.TA, User.UserRole.MO});

    public RegisterFrame() {
        // 创建连接用户 CSV 的认证服务实例。
        this.authService = new AuthService(new CsvStorage<>("data/users.csv", User::fromCsvRow));

        // 设置注册窗口标题。
        setTitle("TA Recruitment System - Register");
        // 设置窗口尺寸，略大于登录页以容纳更多字段。
        setSize(450, 300);
        // 关闭注册页时仅销毁当前窗口，不直接结束程序。
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // 将窗口放到屏幕中央。
        setLocationRelativeTo(null);

        // 创建表单主面板。
        JPanel panel = new JPanel(new GridBagLayout());
        // 创建统一布局约束对象。
        GridBagConstraints gbc = new GridBagConstraints();
        // 统一设置表单控件外边距。
        gbc.insets = new Insets(10, 10, 10, 10);
        // 输入控件在水平方向拉伸。
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 第一行放用户名标签和输入框。
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        // 第二行放密码标签和输入框。
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        // 第三行放确认密码标签和输入框。
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        panel.add(confirmPasswordField, gbc);

        // 第四行放角色选择标签和下拉框。
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        panel.add(roleComboBox, gbc);

        // 创建注册按钮，提交注册流程。
        JButton registerButton = new JButton("Register");
        // 创建返回登录页按钮。
        JButton backButton = new JButton("Back to Login");
        // 用单独按钮面板承载底部操作按钮。
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.add(registerButton);
        buttons.add(backButton);

        // 将按钮面板放到最后一行并横跨两列。
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(buttons, gbc);
        // 把表单加入窗口。
        add(panel);

        // 绑定注册按钮事件。
        registerButton.addActionListener(e -> handleRegister());
        // 绑定返回按钮事件，回到登录页并关闭当前页。
        backButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private void handleRegister() {
        // 读取并清理用户名输入。
        String username = usernameField.getText().trim();
        // 读取并清理密码输入。
        String password = new String(passwordField.getPassword()).trim();
        // 读取并清理确认密码输入。
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
        // 获取当前选择的目标角色。
        User.UserRole role = (User.UserRole) roleComboBox.getSelectedItem();

        // 所有字段都必须填写，否则不允许注册。
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required.");
            return;
        }
        // 两次密码输入不一致时立即阻止提交。
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }
        // 服务层返回 false 时表示用户名已存在。
        if (!authService.register(username, password, role)) {
            showError("Username already exists. Please choose another one.");
            return;
        }

        // 注册成功后给出成功提示。
        JOptionPane.showMessageDialog(this, "Registration successful. Please log in.", "Success",
                JOptionPane.INFORMATION_MESSAGE);
        // 成功后返回登录页。
        new LoginFrame().setVisible(true);
        // 关闭当前注册窗口。
        dispose();
    }

    private void showError(String message) {
        // 统一显示注册错误提示框。
        JOptionPane.showMessageDialog(this, message, "Registration Error", JOptionPane.ERROR_MESSAGE);
    }
}
