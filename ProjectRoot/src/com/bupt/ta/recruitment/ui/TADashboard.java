package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.User;

import javax.swing.*;
import java.awt.*;

public class TADashboard extends BaseDashboard {
    public TADashboard(User user) {
        super(user, "TA Dashboard");

        // Central panel for TA-specific content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        mainPanel.add(new JLabel("TA-specific features will be here.", SwingConstants.CENTER), BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }
}
