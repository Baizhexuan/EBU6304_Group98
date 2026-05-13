        List<Job> jobs = FileStorage.loadJobs();
        for (Job job : jobs) {
            if (job.id == jobId) {
                boolean closingJob = job.isOpen();
                job.status = closingJob ? "CLOSED" : "OPEN";
                if (closingJob) {
                    int notified = NotificationService.notifyJobClosed(job, currentUser);
                    postStatusLabel.setText("Posting status: job '" + job.title + "' is now CLOSED. Notifications sent: " + notified + ".");
                } else {
                    postStatusLabel.setText("Posting status: job '" + job.title + "' is now OPEN.");
                }
                break;
            }
        }
        FileStorage.saveJobs(jobs);
        refreshMyJobs();
        refreshJobSelector();
        refreshApplicants();
    }

    private void refreshJobSelector() {
        selectorJobIds.clear();
        jobSelector.removeAllItems();
        for (Job job : FileStorage.loadJobs()) {
            if (job.moId != currentUser.id) {
                continue;
            }
            selectorJobIds.add(job.id);
            jobSelector.addItem(job.title + " - " + job.module + " (" + job.status + ")");
        }
    }

    private void refreshApplicants() {
        applicantsModel.setRowCount(0);
        int selectedJobId = getSelectedJobId();
        if (selectedJobId < 0) {
            applicantSummaryLabel.setText("No job selected yet. Publish or choose a job to inspect applicants.");
            return;
        }
        String nameFilter = getLower(applicantNameFilterField);
        String emailFilter = getLower(applicantEmailFilterField);
        String skillsFilter = getLower(applicantSkillsFilterField);
        String statusFilter = getLower(applicantStatusFilterField);

        Map<Integer, TAProfile> profiles = new HashMap<Integer, TAProfile>();
        for (TAProfile profile : FileStorage.loadProfiles()) {
            profiles.put(profile.userId, profile);
        }

        int pending = 0;
        int selected = 0;
        int rejected = 0;
        int strongestScore = -1;
        String strongestName = "";
        for (Application app : FileStorage.loadApplications()) {
            if (app.jobId != selectedJobId) {
                continue;
            }
            User taUser = FileStorage.findUserById(app.taId);
            TAProfile profile = profiles.get(app.taId);
            String displayName = taUser == null ? "Unknown" : taUser.getSafeDisplayName();
            String email = profile == null ? "N/A" : profile.email;
            String skills = profile == null ? "N/A" : profile.skills;
            if (!contains(displayName, nameFilter) || !contains(email, emailFilter)
                    || !contains(skills, skillsFilter) || !contains(app.status, statusFilter)) {
                continue;
            }
            applicantsModel.addRow(new Object[] {
                    app.id,
                    displayName,
                    email,
                    skills,
                    app.matchScore + "%",
                    extractMissingSkills(app.matchSummary),
                    app.matchSummary,
                    app.status,
                    calculateCurrentHours(app.taId)
            });
            if ("PENDING".equalsIgnoreCase(app.status)) {
                pending++;
            } else if ("SELECTED".equalsIgnoreCase(app.status)) {
                selected++;
            } else if ("REJECTED".equalsIgnoreCase(app.status)) {
                rejected++;
            }
            if (app.matchScore > strongestScore) {
                strongestScore = app.matchScore;
                strongestName = displayName;
            }
        }

        if (applicantsModel.getRowCount() == 0) {
            applicantSummaryLabel.setText("No applicants match the current selection or filters.");
            return;
        }
        applicantSummaryLabel.setText("Pending: " + pending + " | Selected: " + selected + " | Rejected: " + rejected
                + " | Strongest visible fit: " + strongestName + " at " + Math.max(strongestScore, 0) + "%");
    }

    private int getSelectedJobId() {
        int index = jobSelector.getSelectedIndex();
        if (index < 0 || index >= selectorJobIds.size()) {
            return -1;
        }
        return selectorJobIds.get(index);
    }

    private int calculateCurrentHours(int taId) {
        int hours = 0;
        for (Application app : FileStorage.loadApplications()) {
            if (app.taId == taId && "SELECTED".equalsIgnoreCase(app.status)) {
                Job job = FileStorage.findJobById(app.jobId);
                if (job != null) {
                    hours += job.maxHours;
                }
            }
        }
        return hours;
    }

    private void reviewSelectedApplicant(String decision) {
        int row = applicantsTable.getSelectedRow();
        int selectedJobId = getSelectedJobId();
        if (row < 0 || selectedJobId < 0) {
            JOptionPane.showMessageDialog(this, "Please select an applicant row.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = applicantsTable.convertRowIndexToModel(row);
        int appId = Integer.parseInt(String.valueOf(applicantsModel.getValueAt(modelRow, 0)));
        int currentHours = Integer.parseInt(String.valueOf(applicantsModel.getValueAt(modelRow, 8)));
        Job job = FileStorage.findJobById(selectedJobId);

        if ("SELECTED".equals(decision) && job != null && currentHours + job.maxHours > FileStorage.getOverloadLimit()) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "This selection would push the TA above the workload threshold of " + FileStorage.getOverloadLimit()
                            + " hours. Continue anyway?",
                    "Workload Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        List<Application> applications = FileStorage.loadApplications();
        for (Application app : applications) {
            if (app.id == appId) {
                app.status = decision;
                app.reviewerNote = "Reviewed by " + currentUser.getSafeDisplayName()
                        + " using " + ScoringService.getActiveProvider().getProviderName();
                NotificationService.notifyApplicationDecision(app, currentUser, job, decision);
                break;
            }
        }
        FileStorage.saveApplications(applications);
        refreshApplicants();
        postStatusLabel.setText("Posting status: applicant review updated and notification sent.");
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

    private static class MatchRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            java.awt.Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            if (!isSelected) {
                String status = String.valueOf(table.getValueAt(row, 7));
                int hours = Integer.parseInt(String.valueOf(table.getValueAt(row, 8)));
                if ("SELECTED".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(214, 245, 214));
                } else if ("REJECTED".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(250, 220, 220));
                } else if (hours >= FileStorage.getOverloadLimit()) {
                    component.setBackground(new Color(255, 232, 204));
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
