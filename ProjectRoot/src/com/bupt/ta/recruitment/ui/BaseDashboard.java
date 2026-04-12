package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.User;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Abstract base window for all dashboard screens.
 * It provides shared frame setup, welcome text, and logout behavior for
 * different user roles.
 */
public abstract class BaseDashboard extends JFrame {
    // 保存当前已登录用户，供子类页面读取身份信息。
    protected final User currentUser;

    protected BaseDashboard(User user, String title) {
        // 记录当前用户，后续各标签页都依赖这个上下文。
        this.currentUser = user;
        // 用角色标题和用户名组合窗口标题，方便用户辨识当前页面。
        setTitle(title + " - " + user.getUsername());
        // 统一设置仪表盘窗口尺寸。
        setSize(920, 620);
        // 关闭主窗口时直接结束程序。
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 将窗口居中显示到屏幕中央。
        setLocationRelativeTo(null);
        // 整个仪表盘采用边界布局，顶部公共区、中央业务区的分工更清晰。
        setLayout(new BorderLayout());

        // 构建顶部公共面板，用于放置欢迎信息与登出按钮。
        JPanel topPanel = new JPanel(new BorderLayout());
        // 给顶部公共区加内边距，让界面更易读。
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // 拼接欢迎语，直接展示用户名和角色。
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        // 加粗欢迎语，提高页面识别度。
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        // 创建公共登出按钮，供所有角色页面复用。
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            // 登出前先二次确认，防止误操作。
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                // 确认登出后返回登录页。
                new LoginFrame().setVisible(true);
                // 关闭当前仪表盘窗口，释放资源。
                dispose();
            }
        });

        // 将欢迎语放在顶部左侧。
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        // 将登出按钮放在顶部右侧。
        topPanel.add(logoutButton, BorderLayout.EAST);
        // 将公共顶部面板安装到窗口北侧。
        add(topPanel, BorderLayout.NORTH);
    }
}
