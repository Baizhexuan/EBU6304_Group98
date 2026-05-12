package models;

public class Job {
    public String jobId;
    public String moduleName;
    public String requiredSkills;
    public int hours;

    public Job(String jobId, String moduleName, String requiredSkills, int hours) {
        this.jobId = jobId;
        this.moduleName = moduleName;
        this.requiredSkills = requiredSkills;
        this.hours = hours;
    }
    
    public String toCSV() { return jobId + "," + moduleName + "," + requiredSkills + "," + hours; }
}