    }

    private void markSelectedNotificationAsRead() {
        int row = notificationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a notification first.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = notificationsTable.convertRowIndexToModel(row);
        int notificationId = ValidationUtils.parseInt(String.valueOf(notificationsModel.getValueAt(modelRow, 0)), 0);
        NotificationService.markAsRead(notificationId);
        refreshNotifications();
    }

    private void withdrawSelectedApplication() {
        int row = applicationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an application first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = applicationsTable.convertRowIndexToModel(row);
        int appId = Integer.parseInt(String.valueOf(applicationsModel.getValueAt(modelRow, 0)));
        String status = String.valueOf(applicationsModel.getValueAt(modelRow, 3));
        if (!"PENDING".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Only pending applications can be withdrawn.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Application> applications = FileStorage.loadApplications();
        for (Application app : applications) {
            if (app.id == appId) {
                app.status = "WITHDRAWN";
                app.reviewerNote = "Withdrawn by TA.";
                break;
            }
        }
        FileStorage.saveApplications(applications);
        refreshApplications();
    }

    private String getLower(JTextField field) {
        return field == null ? "" : field.getText().trim().toLowerCase();
    }

    private boolean contains(String text, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return true;
        }
        return text != null && text.toLowerCase().contains(keyword);
    }

    private String extractMissingSkills(String summary) {
        if (ValidationUtils.isBlank(summary)) {
            return "None";
        }
        String[] pieces = summary.split("\\|");
        for (String piece : pieces) {
            String trimmed = piece.trim();
            if (trimmed.toLowerCase().startsWith("missing:")) {
                return trimmed.substring("Missing:".length()).trim();
            }
        }
        return "None";
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            java.awt.Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            if (!isSelected) {
                String status = String.valueOf(table.getValueAt(row, 3));
                if ("SELECTED".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(214, 245, 214));
                } else if ("REJECTED".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(250, 220, 220));
                } else if ("WITHDRAWN".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(232, 232, 232));
                } else {
                    component.setBackground(new Color(255, 249, 214));
                }
            }
            return component;
        }
    }

    private static class NotificationRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            java.awt.Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            if (!isSelected) {
                String status = String.valueOf(table.getValueAt(row, 3));
                if ("UNREAD".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(232, 244, 250));
                } else {
                    component.setBackground(Color.WHITE);
                }
            }
            return component;
        }
    }

    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable action;

        private SimpleDocumentListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            action.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            action.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            action.run();
        }
    }
}
