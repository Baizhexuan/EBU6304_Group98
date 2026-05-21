/**
 * Stores the academic and skills profile of a Teaching Assistant applicant.
 *
 * <p>A profile must be completed before a TA can apply for any job. Completion
 * requires at least {@code fullName}, {@code email}, {@code studentId}, and
 * {@code skills} to be non-blank.</p>
 */
public class TAProfile {
    /** Unique profile identifier. */
    public int id;
    /** Identifier of the linked TA user account. */
    public int userId;
    /** Applicant's full name. */
    public String fullName;
    /** Applicant's contact e-mail address. */
    public String email;
    /** Student identifier used by the school. */
    public String studentId;
    /** Skills listed by the applicant for matching. */
    public String skills;
    /** Applicant GPA used as supporting evidence. */
    public double gpa;
    /** Local or submitted CV path. */
    public String cvPath;
    /** Applicant availability notes. */
    public String availability;
    /** Applicant personal statement. */
    public String statement;

    /**
     * Creates an empty TA profile for CSV population or form binding.
     */
    public TAProfile() {
    }

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
