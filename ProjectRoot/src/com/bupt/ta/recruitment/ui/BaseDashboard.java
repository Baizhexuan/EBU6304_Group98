package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.User;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

public abstract class BaseDashboard extends JFrame {
    protected final User currentUser;
    protected final JTabbedPane tabbedPane;

    protected BaseDashboard(User currentUser, String title) {
        this.currentUser = currentUser;
        setTitle(title + " - " + currentUser.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu accountMenu = new JMenu("Account");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
        accountMenu.add(logoutItem);
        menuBar.add(accountMenu);
        setJMenuBar(menuBar);

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }

    protected void addTab(String title, java.awt.Component component) {
        tabbedPane.addTab(title, component);
    }
}
