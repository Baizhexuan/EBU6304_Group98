/**
 * Builds user-facing summaries of the fixed workflow rules used by the demo.
 *
 * <p>Admin uses this service to inspect the same thresholds that drive workload monitoring,
 * Bell Centre consent, and reputation penalties.</p>
 */
public final class SystemRulesService {
    private SystemRulesService() {
    }

    public static String buildCompactSummary() {
        return "Workload limit: " + FileStorage.getOverloadLimit() + "h"
                + " | Near-limit warning: " + FileStorage.getNearLimitThreshold() + "h"
                + " | Pre-approval message limit: " + MessageService.getMaxMessagesWithoutConsent()
                + " | Reputation penalty: -" + ReputationService.getPenaltyPoints();
    }

    public static String buildDetailedRules() {
        StringBuilder builder = new StringBuilder();
        builder.append("Current System Rules\n");
        builder.append("- Workload limit: ").append(FileStorage.getOverloadLimit())
                .append("h. Above this value, Admin sees OVERLOAD.\n");
        builder.append("- Near-limit warning: ").append(FileStorage.getNearLimitThreshold())
                .append("h. At or above this value, Admin sees NEAR LIMIT.\n");
        builder.append("- TA-MO pre-approval message limit: ")
                .append(MessageService.getMaxMessagesWithoutConsent())
                .append(" messages per sender before MO approval.\n");
        builder.append("- Reputation default/floor: ").append(ReputationService.getDefaultScore())
                .append("/").append(ReputationService.getMinimumScore()).append(".\n");
        builder.append("- Reputation penalty trigger: original match >= ")
                .append(ReputationService.getHighMatchThreshold()).append("% and MO final rating <= ")
                .append(ReputationService.getLowRatingThreshold()).append("/5.\n");
        builder.append("- Reputation penalty amount: -").append(ReputationService.getPenaltyPoints())
                .append(" points. Future match score = base match * reputation / 100.\n");
        return builder.toString();
    }
}
