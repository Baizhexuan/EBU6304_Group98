package models;

public class Application {
    public String appId;
    public String taId;
    public String jobId;
    public String status; // PENDING, ACCEPTED, REJECTED
    public double aiMatchScore;

    public Application(String appId, String taId, String jobId, String status, double aiMatchScore) {
        this.appId = appId;
        this.taId = taId;
        this.jobId = jobId;
        this.status = status;
        this.aiMatchScore = aiMatchScore;
    }
    
    public String toCSV() { return appId + "," + taId + "," + jobId + "," + status + "," + aiMatchScore; }
}