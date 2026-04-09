package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.User;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends BaseDashboard {
    public AdminDashboard(User user) {
        super(user, "Administrator Dashboard");

        // Central panel for Admin-specific content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        mainPanel.add(new JLabel("Administrator features will be here.", SwingConstants.CENTER), BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }
}
