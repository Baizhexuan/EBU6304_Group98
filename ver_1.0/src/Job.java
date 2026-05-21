/**
 * Represents a Teaching Assistant job posting created by a Module Organiser.
 *
 * <p>A job transitions from {@code OPEN} (accepting applications) to
 * {@code CLOSED} once the MO has filled the position or no longer needs a TA.
 * The {@code requiredSkills} field is a comma-separated list used by the
 * skill-matching engine to score TA applicants.</p>
 */
public class Job {
    /** Unique job identifier. */
    public int id;
    /** Identifier of the Module Organiser who owns the post. */
    public int moId;
    /** Human-readable job title. */
    public String title;
    /** Module code or module name associated with the job. */
    public String module;
    /** Free-text description of the TA work. */
    public String description;
    /** Semicolon-separated or comma-separated skills required for the job. */
    public String requiredSkills;
    /** Maximum weekly hours expected for the assignment. */
    public int maxHours;
    /** Current job status such as {@code OPEN} or {@code CLOSED}. */
    public String status;
    /** Teaching location or delivery mode for the assignment. */
    public String location;

    /**
     * Creates an empty job record for CSV population or form binding.
     */
    public Job() {
    }

    /**
     * Returns {@code true} when this job is currently accepting applications.
     *
     * @return {@code true} if status equals {@code OPEN} (case-insensitive)
     */
    public boolean isOpen() {
        return "OPEN".equalsIgnoreCase(status);
    }
}
