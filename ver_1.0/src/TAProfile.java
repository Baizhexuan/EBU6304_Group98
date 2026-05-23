/**
 * Stores the academic and skills profile of a Teaching Assistant applicant.
 *
 * <p>A profile must be completed before a TA can apply for any job. Completion
 * requires at least {@code fullName}, {@code email}, {@code studentId}, and
 * {@code skills} to be non-blank.</p>
 */
public class TAProfile {
    public int id;
    public int userId;
    public String fullName;
    public String email;
    public String studentId;
    public String skills;
    public double gpa;
    public String cvPath;
    public String availability;
    public String statement;

    /**
     * Returns {@code true} when the profile contains the minimum required fields.
     *
     * <p>A complete profile has non-blank values for fullName, email, studentId,
     * and skills. GPA and cvPath are optional for completion purposes.</p>
     *
     * @return {@code true} if the profile passes the completeness check
     */
    public boolean isComplete() {
        return notBlank(fullName) && notBlank(email) && notBlank(studentId) && notBlank(skills);
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
