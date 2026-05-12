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
        builder.append("Action memo: ").append(buildActionMemoForTa(taId)).append("\n\n");

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
                builder.append("- No safe replacement candidate is currently available.\n\n");
                continue;
            }
            for (CandidateRecommendation candidate : candidates) {
                builder.append("- ").append(candidate.name)
                        .append(" | predicted fit ").append(candidate.matchScore).append("%")
                        .append(" | current load ").append(candidate.currentHours).append("h")
                        .append(" | projected load ").append(candidate.projectedHours).append("h")
                        .append(" | ").append(candidate.reason)
                        .append("\n");
            }
            builder.append('\n');
        }
        return builder.toString().trim();
    }

    public static String buildOperationalChecklist() {
        return "1. Review overload rows first.\n"
                + "2. Confirm whether the current match score is still acceptable.\n"
                + "3. Prefer candidates who stay under the " + FileStorage.getOverloadLimit() + "h safe limit.\n"
                + "4. Record a reviewer note after reallocation or rejection.";
    }

    public static String buildActionMemoForTa(int taId) {
        int hours = getSelectedHours(taId);
        if (hours > FileStorage.getOverloadLimit()) {
            return "Immediate redistribution recommended before further selections are approved.";
        }
        if (hours >= FileStorage.getOverloadLimit() - 2) {
            return "Monitor closely and consider backup candidates for the heaviest assigned job.";
        }
        return "Current allocation is stable. Reallocation is optional rather than urgent.";
    }

    private static List<CandidateRecommendation> findTopCandidates(int overloadedTaId, Job job, int limit) {
        List<CandidateRecommendation> candidates = new ArrayList<CandidateRecommendation>();
        for (User user : FileStorage.loadUsers()) {
            if (!"TA".equalsIgnoreCase(user.role) || user.id == overloadedTaId) {
                continue;
            }
            TAProfile profile = FileStorage.findProfileByUserId(user.id);
            if (profile == null || !profile.isComplete()) {
                continue;
            }
            if (hasActiveApplication(user.id, job.id)) {
                continue;
            }
            int currentHours = getSelectedHours(user.id);
            MatchResult match = ScoringService.evaluate(profile, job);
            int projectedHours = currentHours + job.maxHours;
            String reason = projectedHours > FileStorage.getOverloadLimit()
                    ? "strong fit but would still exceed safe hours after reassignment"
                    : "keeps projected load at " + projectedHours + "h and aligns with job skills";
            candidates.add(new CandidateRecommendation(user.getSafeDisplayName(), match.score, currentHours,
                    projectedHours, reason));
        }

        Collections.sort(candidates, new Comparator<CandidateRecommendation>() {
            @Override
            public int compare(CandidateRecommendation left, CandidateRecommendation right) {
                int leftPenalty = left.projectedHours > FileStorage.getOverloadLimit() ? 1 : 0;
                int rightPenalty = right.projectedHours > FileStorage.getOverloadLimit() ? 1 : 0;
                if (leftPenalty != rightPenalty) {
                    return leftPenalty - rightPenalty;
                }
                if (left.matchScore != right.matchScore) {
                    return right.matchScore - left.matchScore;
                }
                return left.currentHours - right.currentHours;
            }
        });

        if (candidates.size() > limit) {
            return new ArrayList<CandidateRecommendation>(candidates.subList(0, limit));
        }
        return candidates;
    }

    private static boolean hasActiveApplication(int taId, int jobId) {
        for (Application application : FileStorage.loadApplications()) {
            if (application.taId == taId && application.jobId == jobId
                    && !"WITHDRAWN".equalsIgnoreCase(application.status)
                    && !"REJECTED".equalsIgnoreCase(application.status)) {
                return true;
            }
        }
        return false;
    }

    private static List<Application> getSelectedApplications(int taId) {
        List<Application> selected = new ArrayList<Application>();
        for (Application application : FileStorage.loadApplications()) {
            if (application.taId == taId && "SELECTED".equalsIgnoreCase(application.status)) {
                selected.add(application);
            }
        }
        return selected;
    }

    private static int getSelectedHours(int taId) {
        int hours = 0;
        for (Application application : FileStorage.loadApplications()) {
            if (application.taId == taId && "SELECTED".equalsIgnoreCase(application.status)) {
                Job job = FileStorage.findJobById(application.jobId);
                if (job != null) {
                    hours += job.maxHours;
                }
            }
        }
        return hours;
    }

    private static String buildLoadStatus(int hours) {
        if (hours > FileStorage.getOverloadLimit()) {
            return "OVERLOAD - action recommended";
        }
        if (hours >= FileStorage.getOverloadLimit() - 2) {
            return "NEAR LIMIT - monitor closely";
        }
        return "OK";
    }

    private static final class CandidateRecommendation {
        private final String name;
        private final int matchScore;
        private final int currentHours;
        private final int projectedHours;
        private final String reason;

        private CandidateRecommendation(String name, int matchScore, int currentHours, int projectedHours,
                String reason) {
            this.name = name;
            this.matchScore = matchScore;
            this.currentHours = currentHours;
            this.projectedHours = projectedHours;
            this.reason = reason;
        }
    }
}
