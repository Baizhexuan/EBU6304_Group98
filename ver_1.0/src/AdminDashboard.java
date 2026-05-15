            previousStatuses.put(job.id, job.status);
        }
        int closedNotifications = 0;
        for (int row = 0; row < jobsModel.getRowCount(); row++) {
            int jobId = ValidationUtils.parseInt(String.valueOf(jobsModel.getValueAt(row, 0)), 0);
            Job match = findJobById(jobs, jobId);
            if (match == null) {
                continue;
            }
            String moDisplayName = String.valueOf(jobsModel.getValueAt(row, 1)).trim();
            User mo = FileStorage.findUserByDisplayName(moDisplayName);
            if (mo == null || !"MO".equalsIgnoreCase(mo.role)) {
                JOptionPane.showMessageDialog(this,
                        "MO name '" + moDisplayName + "' is not recognised. Please use an existing MO display name.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int hours = ValidationUtils.parseInt(String.valueOf(jobsModel.getValueAt(row, 5)), -1);
            if (hours <= 0) {
                JOptionPane.showMessageDialog(this, "Hours must be a positive integer.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            match.moId = mo.id;
            match.title = String.valueOf(jobsModel.getValueAt(row, 2)).trim();
            match.module = String.valueOf(jobsModel.getValueAt(row, 3)).trim();
            match.requiredSkills = String.valueOf(jobsModel.getValueAt(row, 4)).trim();
            match.maxHours = hours;
            match.location = String.valueOf(jobsModel.getValueAt(row, 6)).trim();
            match.status = String.valueOf(jobsModel.getValueAt(row, 7)).trim().toUpperCase();
            String previousStatus = previousStatuses.get(match.id);
            if ("OPEN".equalsIgnoreCase(previousStatus) && "CLOSED".equalsIgnoreCase(match.status)) {
                closedNotifications += NotificationService.notifyJobClosed(match, currentUser);
            }
        }
        FileStorage.saveJobs(jobs);
        jobSnapshot = copyJobs(jobs);
        jobsDirty = false;
        String savedMessage = "Job updates saved.";
        if (closedNotifications > 0) {
            savedMessage += " Closure notifications sent: " + closedNotifications + ".";
        }
        JOptionPane.showMessageDialog(this, savedMessage, "Saved", JOptionPane.INFORMATION_MESSAGE);
        refreshWorkload();
        refreshJobs();
    }

    private void undoJobChanges() {
        FileStorage.saveJobs(copyJobs(jobSnapshot));
        refreshJobs();
    }

    private void exportWorkloadReport() {
        refreshWorkload();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String path = "data/admin_workload_report_" + timestamp + ".csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            writer.println("exportedAt," + timestamp);
            writer.println("provider," + ScoringService.getActiveProvider().getProviderName());
            writer.println("providerReady," + ScoringService.getActiveProvider().isReady());
            writer.println("taUsername,fullName,email,selectedJobs,currentHours,status");
            for (int row = 0; row < workloadModel.getRowCount(); row++) {
                writer.println(workloadModel.getValueAt(row, 0) + "," + workloadModel.getValueAt(row, 1) + ","
                        + workloadModel.getValueAt(row, 2) + "," + workloadModel.getValueAt(row, 3) + ","
                        + workloadModel.getValueAt(row, 4) + "," + workloadModel.getValueAt(row, 5));
            }
            JOptionPane.showMessageDialog(this, "Report exported to " + path, "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to export report: " + e.getMessage(), "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean confirmDiscardIfNeeded(String actionLabel) {
        if (!applicationsDirty && !jobsDirty) {
            return true;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "There are unsaved admin changes. Do you want to discard them and " + actionLabel + "?",
                "Unsaved Changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    private boolean matchesStatus(String actual, String filter) {
        if (filter == null || "ALL".equalsIgnoreCase(filter)) {
            return true;
        }
        if ("OK".equalsIgnoreCase(filter)) {
            return "OK".equalsIgnoreCase(actual);
        }
        return actual != null && actual.toUpperCase().startsWith(filter.toUpperCase());
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

    private String buildWorkloadStatus(int hours) {
        if (hours > FileStorage.getOverloadLimit()) {
            return "OVERLOAD - review allocation immediately";
        }
        if (hours >= FileStorage.getOverloadLimit() - 2) {
            return "NEAR LIMIT - monitor closely";
        }
        return "OK";
    }

    private Application findApplicationById(List<Application> applications, int appId) {
        for (Application app : applications) {
            if (app.id == appId) {
                return app;
            }
        }
        return null;
    }

    private Job findJobById(List<Job> jobs, int jobId) {
        for (Job job : jobs) {
            if (job.id == jobId) {
                return job;
            }
        }
        return null;
    }

    private List<Application> copyApplications(List<Application> source) {
        List<Application> copies = new ArrayList<Application>();
        for (Application app : source) {
            Application copy = new Application();
            copy.id = app.id;
            copy.taId = app.taId;
            copy.jobId = app.jobId;
            copy.status = app.status;
            copy.appliedAt = app.appliedAt;
            copy.matchScore = app.matchScore;
            copy.matchSummary = app.matchSummary;
            copy.reviewerNote = app.reviewerNote;
            copies.add(copy);
        }
        return copies;
    }

    private List<Job> copyJobs(List<Job> source) {
        List<Job> copies = new ArrayList<Job>();
        for (Job job : source) {
            Job copy = new Job();
            copy.id = job.id;
            copy.moId = job.moId;
            copy.title = job.title;
            copy.module = job.module;
            copy.description = job.description;
            copy.requiredSkills = job.requiredSkills;
            copy.maxHours = job.maxHours;
            copy.status = job.status;
            copy.location = job.location;
            copies.add(copy);
        }
        return copies;
    }

    private static class WorkloadRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String status = String.valueOf(table.getValueAt(row, 5));
                if (status.startsWith("OVERLOAD")) {
                    component.setBackground(new Color(250, 220, 220));
                } else if (status.startsWith("NEAR LIMIT")) {
                    component.setBackground(new Color(255, 239, 214));
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
