package models;

public class TA {
    public String id;
    public String name;
    public String skills; // e.g., "Java,Python,SQL"
    public int totalHours;
    public boolean hasCV;

    public TA(String id, String name, String skills, int totalHours, boolean hasCV) {
        this.id = id;
        this.name = name;
        this.skills = skills;
        this.totalHours = totalHours;
        this.hasCV = hasCV;
    }
    
    public String toCSV() { return id + "," + name + "," + skills + "," + totalHours + "," + hasCV; }
}
