//TA申请人子类

public class TA extends User {
    private String skills;
    private String currentStatus; // 如: "Pending", "Accepted"

    public TA(String id, String name, String email, String skills) {
        super(id, name, email);
        this.skills = skills;
        this.currentStatus = "Available";
    }

    @Override
    public void displayMenu() {
        System.out.println("TA Menu: 1. Create Profile 2. Apply for Job 3. Check Status");
    }

    //CSV格式存储

    public String toCSV() {
        return id + "," + name + "," + email + "," + skills + "," + currentStatus;
    }
}