import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Applies the post-work reputation feedback loop.
 *
 * <p>Viva explanation: the system should not trust self-reported skills forever. A TA can obtain
 * a high original match score by listing strong skills, but if the MO later gives a very low
 * completion rating, the system treats that as a reliability warning and lowers the TA's future
 * matching score.</p>
 */
public final class ReputationService {
    /** Every TA starts with full reputation unless a penalty row exists in ta_reputations.csv. */
    private static final int DEFAULT_SCORE = 100;
    /** Reputation can now fall to zero, so repeated high-match/low-rating cases have real cost. */
    private static final int MIN_SCORE = 0;
    /** Original application match must be at least 80% before a low rating counts as suspicious. */
    private static final int HIGH_MATCH_THRESHOLD = 80;
    /** MO ratings 1 or 2 are treated as poor delivery quality. */
    private static final int LOW_RATING_THRESHOLD = 2;
    /** One confirmed high-match/low-rating case subtracts 30 reputation points. */
    private static final int HIGH_MATCH_LOW_RATING_PENALTY = 30;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ReputationService() {
    }

    public static MatchResult applyReputationPenalty(TAProfile profile, MatchResult baseResult) {
        if (profile == null || baseResult == null) {
            return baseResult;
        }
        int reputation = getScoreForTa(profile.userId);
        if (reputation >= DEFAULT_SCORE) {
            return baseResult;
        }
        // Future matching formula:
        // adjusted score = current skill-match score * reputation / 100.
        // Example for viva: base 100% with reputation 70 becomes 70%.
        int adjustedScore = clamp((int) Math.round(baseResult.score * (reputation / 100.0)));
        String summary = baseResult.summary + " | Reputation penalty: " + reputation + "/100 adjusted score from "
                + baseResult.score + "% to " + adjustedScore + "%";
        return new MatchResult(adjustedScore, summary);
    }

    /**
     * Returns the current reputation value for a TA. Missing rows mean the TA has never been
     * penalised, so the system treats them as 100/100.
     */
    public static int getScoreForTa(int taId) {
        TAReputation reputation = findReputation(taId);
        return reputation == null ? DEFAULT_SCORE : reputation.score;
    }

    /**
     * Builds the compact reputation label shown in MO/Admin tables.
     */
    public static String describeReputation(int taId) {
        TAReputation reputation = findReputation(taId);
        if (reputation == null) {
            return "100/100 - no post-work penalty recorded";
        }
        return reputation.score + "/100 - penalties: " + reputation.penaltyCount + " - " + safe(reputation.note);
    }

    /**
     * Prevents one completed job from being rated repeatedly. Without this guard, the same MO
     * action could unfairly apply multiple penalties to one piece of completed work.
     */
    public static boolean hasEvaluationForApplication(int applicationId) {
        for (WorkEvaluation evaluation : FileStorage.loadWorkEvaluations()) {
            if (evaluation.applicationId == applicationId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Main penalty decision after an MO submits a final work rating.
     *
     * <p>Business rule: only a high original match combined with a low completion rating is
     * penalised. A low match with low rating is not treated as skill dishonesty because the system
     * already knew the fit was weak.</p>
     */
    public static boolean applyCompletedWorkEvaluation(WorkEvaluation evaluation, Application application) {
        if (evaluation == null || application == null) {
            return false;
        }
        // This uses application.matchScore, the historical score captured when the TA applied.
        // That is intentional: the question is whether the TA looked strong at application time.
        boolean shouldPenalise = application.matchScore >= HIGH_MATCH_THRESHOLD
                && evaluation.rating <= LOW_RATING_THRESHOLD;
        evaluation.penaltyApplied = shouldPenalise;
        if (!shouldPenalise) {
            return false;
        }

        List<TAReputation> reputations = FileStorage.loadTAReputations();
        TAReputation reputation = null;
        for (TAReputation item : reputations) {
            if (item.taId == evaluation.taId) {
                reputation = item;
                break;
            }
        }
        if (reputation == null) {
            // First penalty for this TA: create the reputation row lazily.
            reputation = new TAReputation();
            reputation.taId = evaluation.taId;
            reputation.score = DEFAULT_SCORE;
            reputation.penaltyCount = 0;
            reputations.add(reputation);
        }

        // Clamp at MIN_SCORE so repeated penalties can reach zero but never become negative.
        reputation.score = Math.max(MIN_SCORE, reputation.score - HIGH_MATCH_LOW_RATING_PENALTY);
        reputation.penaltyCount++;
        reputation.lastUpdated = LocalDateTime.now().format(FORMATTER);
        // The note is deliberately human-readable so the MO/Admin can explain why the penalty
        // appeared in the table or exported report.
        reputation.note = "High match score " + application.matchScore + "% but low MO completion rating "
                + evaluation.rating + "/5; treat as skill reliability review signal.";
        FileStorage.saveTAReputations(reputations);
        return true;
    }

    private static TAReputation findReputation(int taId) {
        for (TAReputation reputation : FileStorage.loadTAReputations()) {
            if (reputation.taId == taId) {
                return reputation;
            }
        }
        return null;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
