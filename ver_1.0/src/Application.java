/**
 * Represents a TA's application for a specific job posting.
 *
 * <p>The lifecycle of an application is:
 * <ol>
 *   <li>{@code PENDING} — submitted, awaiting MO review</li>
 *   <li>{@code SELECTED} — MO accepted this applicant</li>
 *   <li>{@code REJECTED} — MO rejected this applicant</li>
 *   <li>{@code WITHDRAWN} — TA cancelled the application</li>
 * </ol>
 * <p>The {@code matchScore} and {@code matchSummary} are populated by the
 * scoring engine when the application is created or refreshed.</p>
 */
public class Application {
    public int id;
    public int taId;
    public int jobId;
    public String status;
    public String appliedAt;
    public int matchScore;
    public String matchSummary;
    public String reviewerNote;
}
