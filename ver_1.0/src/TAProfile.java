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

    public boolean isComplete() {
        return notBlank(fullName) && notBlank(email) && notBlank(studentId) && notBlank(skills);
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
