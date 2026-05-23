/**
 * Represents a Teaching Assistant job posting created by a Module Organiser.
 *
 * <p>A job transitions from {@code OPEN} (accepting applications) to
 * {@code CLOSED} once the MO has filled the position or no longer needs a TA.
 * The {@code requiredSkills} field is a comma-separated list used by the
 * skill-matching engine to score TA applicants.</p>
 */
public class Job {
    public int id;
    public int moId;
    public String title;
    public String module;
    public String description;
    public String requiredSkills;
    public int maxHours;
    public String status;
    public String location;

    /**
     * Returns {@code true} when this job is currently accepting applications.
     *
     * @return {@code true} if status equals {@code OPEN} (case-insensitive)
     */
    public boolean isOpen() {
        return "OPEN".equalsIgnoreCase(status);
    }
}
