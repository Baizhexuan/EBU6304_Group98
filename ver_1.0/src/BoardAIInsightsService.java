import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Builds board-specific AI insight summaries without changing the core workflow rules.
 *
 * <p>The service prepares explainable ranking text and compact context blocks for TA, MO, and
 * Admin boards so each dashboard can expose visible AI-assisted support plus a richer Ask AI
 * conversation entry point.</p>
 */
public final class BoardAIInsightsService {
    private BoardAIInsightsService() {
    }

    /**
     * Builds an explainable ranking of open jobs for a TA.
     *
     * @param taUser TA user whose profile should be matched
     * @param limit  maximum number of ranked jobs to include
     * @return plain-text ranking summary for the TA dashboard
     */
    public static String buildTaMatchRanking(User taUser, int limit) {
        if (taUser == null) {
            return "No TA context is available.";
        }
        TAProfile profile = FileStorage.findProfileByUserId(taUser.id);
        if (profile == null || !profile.isComplete()) {
            return "Complete the TA profile first to unlock AI match ranking and MO/job recommendations.";
        }

        List<RankedJob> rankedJobs = new ArrayList<RankedJob>();
        for (Job job : FileStorage.loadJobs()) {
            if (!job.isOpen()) {
                continue;
            }
            MatchResult result = ScoringService.evaluate(profile, job);
            User mo = FileStorage.findUserById(job.moId);
            rankedJobs.add(new RankedJob(job, mo, result));
        }
        Collections.sort(rankedJobs, new Comparator<RankedJob>() {
            @Override
            public int compare(RankedJob left, RankedJob right) {
                return right.result.score - left.result.score;
            }
        });

        if (rankedJobs.isEmpty()) {
            return "No open jobs are available for AI ranking right now.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("TA AI Match Ranking").append('\n');
        builder.append("Profile focus: ").append(briefProfileTraits(profile)).append("\n\n");
        int count = Math.min(limit, rankedJobs.size());
        for (int i = 0; i < count; i++) {
            RankedJob ranked = rankedJobs.get(i);
            builder.append(i + 1).append(". ")
                    .append(ranked.job.title).append(" / ").append(ranked.job.module)
                    .append(" | MO: ").append(ranked.mo == null ? "Unknown" : ranked.mo.getSafeDisplayName())
                    .append(" | Match: ").append(ranked.result.score).append("%").append('\n');
            builder.append("   Required skills: ").append(safe(ranked.job.requiredSkills)).append('\n');
            builder.append("   MO/job characteristics: ").append(buildJobCharacteristics(ranked.job)).append('\n');
            builder.append("   AI summary: ").append(ranked.result.summary).append("\n\n");
        }
        return builder.toString().trim();
    }

    /**
     * Builds the context block used when a TA asks for AI assistance.
     *
     * @param taUser TA user currently viewing the dashboard
     * @return compact TA profile and matching context
     */
    public static String buildTaAiContext(User taUser) {
        TAProfile profile = taUser == null ? null : FileStorage.findProfileByUserId(taUser.id);
        StringBuilder builder = new StringBuilder();
        builder.append("TA board context\n");
        builder.append("TA: ").append(taUser == null ? "Unknown" : taUser.getSafeDisplayName()).append('\n');
        builder.append("Profile status: ")
                .append(profile == null ? "missing" : (profile.isComplete() ? "complete" : "incomplete")).append('\n');
        if (profile != null) {
            builder.append("Skills: ").append(safe(profile.skills)).append('\n');
            builder.append("Availability: ").append(safe(profile.availability)).append('\n');
            builder.append("Statement: ").append(safe(profile.statement)).append("\n\n");
        }
        builder.append(buildTaMatchRanking(taUser, 5));
        return builder.toString();
    }

    /**
     * Builds an explainable applicant ranking for one MO-owned job.
     *
     * @param moUser MO user requesting the ranking
     * @param jobId  selected job identifier
     * @param limit  maximum number of applicants to include
     * @return plain-text applicant ranking summary
     */
    public static String buildMoApplicantRanking(User moUser, int jobId, int limit) {
        if (moUser == null) {
            return "No MO context is available.";
        }
        Job selectedJob = FileStorage.findJobById(jobId);
        if (selectedJob == null) {
            return "Select a job post to view AI-ranked applicants and TA characteristics.";
        }

        List<RankedApplicant> rankedApplicants = new ArrayList<RankedApplicant>();
        for (Application application : FileStorage.loadApplications()) {
            if (application.jobId != jobId) {
                continue;
            }
            User ta = FileStorage.findUserById(application.taId);
            TAProfile profile = FileStorage.findProfileByUserId(application.taId);
            if (ta == null || profile == null) {
                continue;
            }
            MatchResult result = ScoringService.evaluate(profile, selectedJob);
            rankedApplicants.add(new RankedApplicant(application, ta, profile, result));
        }
        Collections.sort(rankedApplicants, new Comparator<RankedApplicant>() {
            @Override
            public int compare(RankedApplicant left, RankedApplicant right) {
                return right.result.score - left.result.score;
            }
        });

        StringBuilder builder = new StringBuilder();
        builder.append("MO AI Applicant Ranking").append('\n');
        builder.append("Selected job: ").append(selectedJob.title).append(" / ").append(selectedJob.module).append('\n');
        builder.append("Job characteristics: ").append(buildJobCharacteristics(selectedJob)).append("\n\n");

        if (rankedApplicants.isEmpty()) {
            builder.append("No applicants are currently available for this job.");
            return builder.toString();
        }

        int count = Math.min(limit, rankedApplicants.size());
        for (int i = 0; i < count; i++) {
            RankedApplicant ranked = rankedApplicants.get(i);
            builder.append(i + 1).append(". ")
                    .append(ranked.ta.getSafeDisplayName())
                    .append(" | Match: ").append(ranked.result.score).append("%")
                    .append(" | Status: ").append(safe(ranked.application.status)).append('\n');
            builder.append("   TA characteristics: ").append(briefProfileTraits(ranked.profile)).append('\n');
            builder.append("   Current workload: ").append(calculateCurrentHours(ranked.ta.id)).append("h").append('\n');
            builder.append("   AI summary: ").append(ranked.result.summary).append("\n\n");
        }
        return builder.toString().trim();
    }

    /**
     * Builds the context block used when an MO asks for AI assistance.
     *
     * @param moUser MO user currently viewing the dashboard
     * @param jobId  selected job identifier
     * @return compact MO job and applicant context
     */
    public static String buildMoAiContext(User moUser, int jobId) {
        StringBuilder builder = new StringBuilder();
        builder.append("MO board context\n");
        builder.append("MO: ").append(moUser == null ? "Unknown" : moUser.getSafeDisplayName()).append('\n');
        builder.append("Visible job posts: ").append(countJobsForMo(moUser == null ? -1 : moUser.id)).append('\n');
        builder.append("Open job posts: ").append(countOpenJobsForMo(moUser == null ? -1 : moUser.id)).append("\n\n");
        builder.append(buildMoApplicantRanking(moUser, jobId, 5));
        return builder.toString();
    }

    /**
     * Builds an admin-wide system overview for AI explanation panels.
     *
     * @return plain-text summary of users, jobs, applications, and workload risk
     */
    public static String buildAdminSystemOverview() {
        int taCount = 0;
        int moCount = 0;
        int adminCount = 0;
        for (User user : FileStorage.loadUsers()) {
            if ("TA".equalsIgnoreCase(user.role)) {
                taCount++;
            } else if ("MO".equalsIgnoreCase(user.role)) {
                moCount++;
            } else if ("ADMIN".equalsIgnoreCase(user.role)) {
                adminCount++;
            }
        }

        int openJobs = 0;
        int closedJobs = 0;
        for (Job job : FileStorage.loadJobs()) {
            if (job.isOpen()) {
                openJobs++;
            } else {
                closedJobs++;
            }
        }

        int pending = 0;
        int selected = 0;
        int rejected = 0;
        int withdrawn = 0;
        for (Application application : FileStorage.loadApplications()) {
            if ("PENDING".equalsIgnoreCase(application.status)) {
                pending++;
            } else if ("SELECTED".equalsIgnoreCase(application.status)) {
                selected++;
            } else if ("REJECTED".equalsIgnoreCase(application.status)) {
                rejected++;
            } else if ("WITHDRAWN".equalsIgnoreCase(application.status)) {
                withdrawn++;
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Admin AI System Overview").append('\n');
        builder.append("Users -> Admin: ").append(adminCount)
                .append(", MO: ").append(moCount)
                .append(", TA: ").append(taCount).append('\n');
        builder.append("Jobs -> Open: ").append(openJobs)
                .append(", Closed: ").append(closedJobs).append('\n');
        builder.append("Applications -> Pending: ").append(pending)
                .append(", Selected: ").append(selected)
                .append(", Rejected: ").append(rejected)
                .append(", Withdrawn: ").append(withdrawn).append("\n\n");
        builder.append("Top workload ranking").append('\n');
        for (String line : buildTopWorkloadLines(5)) {
            builder.append(line).append('\n');
        }
        builder.append("\nSystem recommendation focus: use workload risk, visible counts, and match explanations together before changing allocations.");
        return builder.toString().trim();
    }

    /**
     * Builds the combined admin context for the Admin AI Assistant.
     *
     * @return system overview plus recommendation-service guidance
     */
    public static String buildAdminAiContext() {
        return buildAdminSystemOverview() + "\n\n" + AdminRecommendationService.buildGlobalAlertSummary();
    }

    private static List<String> buildTopWorkloadLines(int limit) {
        List<UserLoad> loads = new ArrayList<UserLoad>();
        for (User user : FileStorage.loadUsers()) {
            if (!"TA".equalsIgnoreCase(user.role)) {
                continue;
            }
            loads.add(new UserLoad(user, calculateCurrentHours(user.id)));
        }
        Collections.sort(loads, new Comparator<UserLoad>() {
            @Override
            public int compare(UserLoad left, UserLoad right) {
                return right.hours - left.hours;
            }
        });

        List<String> lines = new ArrayList<String>();
        int count = Math.min(limit, loads.size());
        for (int i = 0; i < count; i++) {
            UserLoad load = loads.get(i);
            lines.add((i + 1) + ". " + load.user.getSafeDisplayName() + " | " + load.hours + "h | "
                    + buildLoadStatus(load.hours));
        }
        if (lines.isEmpty()) {
            lines.add("No TA workload data is available.");
        }
        return lines;
    }

    private static int countJobsForMo(int moId) {
        int count = 0;
        for (Job job : FileStorage.loadJobs()) {
            if (job.moId == moId) {
                count++;
            }
        }
        return count;
    }

    private static int countOpenJobsForMo(int moId) {
        int count = 0;
        for (Job job : FileStorage.loadJobs()) {
            if (job.moId == moId && job.isOpen()) {
                count++;
            }
        }
        return count;
    }

    private static int calculateCurrentHours(int taId) {
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
            return "OVERLOAD";
        }
        if (hours >= FileStorage.getOverloadLimit() - 2) {
            return "NEAR LIMIT";
        }
        return "OK";
    }

    private static String briefProfileTraits(TAProfile profile) {
        if (profile == null) {
            return "No profile summary available.";
        }
        return "Skills: " + safe(profile.skills)
                + " | GPA: " + profile.gpa
                + " | Availability: " + safe(profile.availability)
                + " | Statement: " + limit(safe(profile.statement), 120);
    }

    private static String buildJobCharacteristics(Job job) {
        if (job == null) {
            return "No job characteristics available.";
        }
        return "Skills: " + safe(job.requiredSkills)
                + " | Hours: " + job.maxHours + "h"
                + " | Location: " + safe(job.location)
                + " | Description: " + limit(safe(job.description), 120);
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value.trim();
    }

    private static String limit(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max) + "...";
    }

    private static final class RankedJob {
        private final Job job;
        private final User mo;
        private final MatchResult result;

        private RankedJob(Job job, User mo, MatchResult result) {
            this.job = job;
            this.mo = mo;
            this.result = result;
        }
    }

    private static final class RankedApplicant {
        private final Application application;
        private final User ta;
        private final TAProfile profile;
        private final MatchResult result;

        private RankedApplicant(Application application, User ta, TAProfile profile, MatchResult result) {
            this.application = application;
            this.ta = ta;
            this.profile = profile;
            this.result = result;
        }
    }

    private static final class UserLoad {
        private final User user;
        private final int hours;

        private UserLoad(User user, int hours) {
            this.user = user;
            this.hours = hours;
        }
    }
}
