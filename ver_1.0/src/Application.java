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
 * The {@code matchScore} and {@code matchSummary} are populated by the
 * scoring engine when the application is created or refreshed.
 */
public class Application {
    /** Unique application identifier. */
    public int id;
    /** Identifier of the TA user who submitted the application. */
    public int taId;
    /** Identifier of the job being applied for. */
    public int jobId;
    /** Current workflow status such as {@code PENDING}, {@code SELECTED}, or {@code REJECTED}. */
    public String status;
    /** Human-readable timestamp showing when the application was submitted. */
    public String appliedAt;
    /** Cached match score assigned by the active scoring provider. */
    public int matchScore;
    /** Explainable summary of matched and missing skills. */
    public String matchSummary;
    /** Optional note recorded by the reviewing Module Organiser. */
    public String reviewerNote;

    /**
     * Creates an empty application record for CSV population or form binding.
     */
    public Application() {
    }
}
