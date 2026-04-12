package com.bupt.ta.recruitment;

import com.bupt.ta.recruitment.ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application entry point that launches the Swing login window.
 * It ensures UI startup happens on the Event Dispatch Thread.
 */
public class Main {
    public static void main(String[] args) {
        // 将界面启动逻辑提交到 Swing 事件派发线程，保证线程安全。
        SwingUtilities.invokeLater(() -> {
            try {
                // 优先使用当前操作系统的原生外观，让界面风格更贴近系统体验。
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // 如果设置系统外观失败，则保留 Swing 默认外观继续运行。
            }
            // 创建并显示登录窗口，作为整个系统的起点。
            new LoginFrame().setVisible(true);
        });
    }
}
