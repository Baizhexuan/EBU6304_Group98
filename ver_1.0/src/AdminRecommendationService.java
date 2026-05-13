import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class AdminRecommendationService {
    private AdminRecommendationService() {
    }

    public static String buildGlobalAlertSummary() {
        List<User> users = FileStorage.loadUsers();
        StringBuilder builder = new StringBuilder();
        int flagged = 0;
        int overload = 0;
        int nearLimit = 0;
        for (User user : users) {
            if (!"TA".equalsIgnoreCase(user.role)) {
                continue;
            }
            int hours = getSelectedHours(user.id);
            String status = buildLoadStatus(hours);
            if (hours >= FileStorage.getOverloadLimit() - 2) {
                flagged++;
                if (status.startsWith("OVERLOAD")) {
                    overload++;
                } else {
                    nearLimit++;
                }
                builder.append(user.getSafeDisplayName())
                        .append(" -> ")
                        .append(status)
                        .append(" (")
                        .append(hours)
                        .append("h)\n");
            }
        }
        if (flagged == 0) {
            builder.append("No high-load TA risk is detected right now. Current allocations are within safe limits.\n");
        } else {
            builder.insert(0, "High-risk overview: " + overload + " overload, " + nearLimit + " near-limit.\n\n");
        }
        builder.append("\nOperational checklist:\n")
                .append(buildOperationalChecklist())
                .append("\n\nRecommendation engine uses the currently active scoring provider to estimate replacements.");
        return builder.toString().trim();
    }

    public static String buildRecommendationReportForTa(int taId) {
        User ta = FileStorage.findUserById(taId);
        if (ta == null) {
            return "No TA selected.";
        }

        int hours = getSelectedHours(taId);
        List<Application> selectedApplications = getSelectedApplications(taId);
        StringBuilder builder = new StringBuilder();
        builder.append("Load status for ").append(ta.getSafeDisplayName()).append(": ")
                .append(buildLoadStatus(hours)).append(" (current selected hours: ").append(hours).append("h)\n\n");
        builder.append("Action memo: ").append(buildActionMemoForTa(taId)).append("\n");
        builder.append("AI decision guardrail: use the recommendation as evidence, then confirm availability and module-specific fit manually.\n\n");

        if (selectedApplications.isEmpty()) {
            builder.append("This TA has no selected jobs yet, so no reallocation is required.");
            return builder.toString();
        }

        if (hours < FileStorage.getOverloadLimit() - 2) {
            builder.append("This TA is not yet near the overload threshold. Suggestions below are proactive only.\n\n");
        } else {
            builder.append("This TA is close to or above the overload threshold. Consider rebalancing the following selected jobs.\n\n");
        }

        for (Application selectedApp : selectedApplications) {
            Job job = FileStorage.findJobById(selectedApp.jobId);
            if (job == null) {
                continue;
            }
            builder.append("Job: ").append(job.title).append(" / ").append(job.module)
                    .append(" (").append(job.maxHours).append("h)\n");
            List<CandidateRecommendation> candidates = findTopCandidates(taId, job, 3);
            if (candidates.isEmpty()) {
                builder.append("- No safe replacement candidate is currently available. Keep the original allocation under review or reopen recruitment.\n\n");
                continue;
            }
            for (CandidateRecommendation candidate : candidates) {
                builder.append("- ").append(candidate.name)
                        .append(" | predicted fit ").append(candidate.matchScore).append("%")
                        .append(" | current load ").append(candidate.currentHours).append("h")
                        .append(" | projected load ").append(candidate.projectedHours).append("h")
                        .append(" | risk ").append(candidate.riskLabel)
                        .append(" | ").append(candidate.reason)
                        .append(" | next step: ").append(candidate.nextStep)
                        .append("\n");
            }
            builder.append('\n');
        }
        return builder.toString().trim();
    }

    public static String buildOperationalChecklist() {
