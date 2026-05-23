/**
 * Holds the output of a single TA-to-Job skill matching evaluation.
 *
 * <p>The {@code score} is an integer in the range [0, 100] representing
 * the percentage of required skills that the TA's profile satisfies.
 * The {@code summary} is a human-readable explanation listing matched and
 * missing skills, produced by the active {@link SkillScoringProvider}.</p>
 */
public class MatchResult {
    public int score;
    public String summary;

    /**
     * Constructs a {@code MatchResult} with the given score and explanation.
     *
     * @param score   match percentage in the range [0, 100]
     * @param summary human-readable explanation of matched and missing skills
     */
    public MatchResult(int score, String summary) {
        this.score = score;
        this.summary = summary;
    }
}
