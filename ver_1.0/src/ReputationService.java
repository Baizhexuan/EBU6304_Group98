import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Applies post-work feedback to TA reputation and future match scoring.
 */
public final class ReputationService {
    private static final int DEFAULT_SCORE = 100;
    private static final int MIN_SCORE = 40;
    private static final int HIGH_MATCH_THRESHOLD = 80;
    private static final int LOW_RATING_THRESHOLD = 2;
    private static final int HIGH_MATCH_LOW_RATING_PENALTY = 15;
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
        int adjustedScore = clamp((int) Math.round(baseResult.score * (reputation / 100.0)));
        String summary = baseResult.summary + " | Reputation penalty: " + reputation + "/100 adjusted score from "
                + baseResult.score + "% to " + adjustedScore + "%";
        return new MatchResult(adjustedScore, summary);
    }

    public static int getScoreForTa(int taId) {
        TAReputation reputation = findReputation(taId);
        return reputation == null ? DEFAULT_SCORE : reputation.score;
    }

    public static String describeReputation(int taId) {
        TAReputation reputation = findReputation(taId);
        if (reputation == null) {
            return "100/100 - no post-work penalty recorded";
        }
        return reputation.score + "/100 - penalties: " + reputation.penaltyCount + " - " + safe(reputation.note);
    }

    public static boolean hasEvaluationForApplication(int applicationId) {
        for (WorkEvaluation evaluation : FileStorage.loadWorkEvaluations()) {
            if (evaluation.applicationId == applicationId) {
                return true;
            }
        }
        return false;
    }

    public static boolean applyCompletedWorkEvaluation(WorkEvaluation evaluation, Application application) {
        if (evaluation == null || application == null) {
            return false;
        }
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
            reputation = new TAReputation();
            reputation.taId = evaluation.taId;
            reputation.score = DEFAULT_SCORE;
            reputation.penaltyCount = 0;
            reputations.add(reputation);
        }

        reputation.score = Math.max(MIN_SCORE, reputation.score - HIGH_MATCH_LOW_RATING_PENALTY);
        reputation.penaltyCount++;
        reputation.lastUpdated = LocalDateTime.now().format(FORMATTER);
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
