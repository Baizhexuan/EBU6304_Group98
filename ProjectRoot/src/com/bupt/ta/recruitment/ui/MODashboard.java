package com.bupt.ta.recruitment.ui;

import com.bupt.ta.recruitment.model.User;

import javax.swing.*;
import java.awt.*;

public class MODashboard extends BaseDashboard {
    public MODashboard(User user) {
        super(user, "Module Organiser Dashboard");

        // Central panel for MO-specific content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        mainPanel.add(new JLabel("Module Organiser features will be here.", SwingConstants.CENTER), BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }
}
