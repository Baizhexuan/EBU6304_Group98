/**
 * Stores an MO's post-work rating for a selected TA application.
 *
 * <p>The rating closes the feedback loop after recruitment. A very low completion
 * rating after a very high match score can trigger a reputation penalty, but it is
 * treated as a review signal rather than proof of misconduct.</p>
 */
public class WorkEvaluation {
    public int id;
    public int applicationId;
    public int taId;
    public int moId;
    public int jobId;
    public int rating;
    public String comment;
    public String evaluatedAt;
    public boolean penaltyApplied;
}
