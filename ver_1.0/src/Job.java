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

    public boolean isOpen() {
        return "OPEN".equalsIgnoreCase(status);
    }
}
