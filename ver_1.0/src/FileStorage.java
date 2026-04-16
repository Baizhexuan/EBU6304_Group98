import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FileStorage {
    private static final String DATA_DIR = "data" + File.separator;
    private static final int OVERLOAD_LIMIT = 20;

    public static void initialise() {
        new File(DATA_DIR).mkdirs();
        ensureUsers();
        ensureProfiles();
        ensureJobs();
        ensureApplications();
    }

    public static int getOverloadLimit() {
        return OVERLOAD_LIMIT;
    }

    private static void ensureUsers() {
        File file = new File(DATA_DIR + "users.csv");
        if (file.exists()) {
            return;
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("id,username,password,role,displayName");
            writer.println("1,admin,admin123,ADMIN,System Admin");
            writer.println("2,ta1,ta123,TA,Li Ming");
            writer.println("3,ta2,ta456,TA,Wang Yue");
            writer.println("4,mo1,mo123,MO,Dr Chen");
            writer.println("5,mo2,mo456,MO,Prof Zhao");
        } catch (IOException e) {
            System.err.println("Unable to create users.csv: " + e.getMessage());
        }
    }

    private static void ensureProfiles() {
        File file = new File(DATA_DIR + "profiles.csv");
        if (file.exists()) {
            return;
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("id,userId,fullName,email,studentId,skills,gpa,cvPath,availability,statement");
            writer.println("1,2,Li Ming,li.ming@bupt.edu.cn,2023211001,Java;OOP;Git;Communication,3.7,/demo/cv/li-ming.pdf,Mon PM;Wed PM,Interested in software labs and mentoring first-year students.");
            writer.println("2,3,Wang Yue,wang.yue@bupt.edu.cn,2023211002,Python;Data Structures;SQL;Teamwork,3.8,/demo/cv/wang-yue.pdf,Tue PM;Thu PM,Enjoys lab assistance and data-focused teaching support.");
        } catch (IOException e) {
            System.err.println("Unable to create profiles.csv: " + e.getMessage());
        }
    }

    private static void ensureJobs() {
        File file = new File(DATA_DIR + "jobs.csv");
        if (file.exists()) {
            return;
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("id,moId,title,module,description,requiredSkills,maxHours,status,location");
            writer.println("1,4,Java Lab Assistant,EBU6304,Support Java lab sessions and help with debugging,Java;OOP;Communication,8,OPEN,Teaching Building 3");
            writer.println("2,4,Assessment Support TA,EBU6201,Assist with coursework briefing and marking preparation,Organisation;Communication;Excel,6,OPEN,Online and office hours");
            writer.println("3,5,Python Tutor,EBU5402,Run tutorial support for programming exercises,Python;Data Structures;Teamwork,10,OPEN,Computer Lab A");
            writer.println("4,5,Database Helper,EBU5207,Support SQL lab troubleshooting and sample walkthroughs,SQL;Problem Solving;Patience,7,OPEN,Computer Lab B");
        } catch (IOException e) {
            System.err.println("Unable to create jobs.csv: " + e.getMessage());
        }
    }

    private static void ensureApplications() {
        File file = new File(DATA_DIR + "applications.csv");
        if (file.exists()) {
            return;
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("id,taId,jobId,status,appliedAt,matchScore,matchSummary,reviewerNote");
            writer.println("1,2,1,SELECTED,2026-04-05 19:20,100,Matched: java; oop; communication,Strong fit for labs.");
            writer.println("2,3,3,PENDING,2026-04-06 11:00,67,Matched: python; data structures | Missing: teamwork,Awaiting MO review.");
        } catch (IOException e) {
            System.err.println("Unable to create applications.csv: " + e.getMessage());
        }
    }

    public static List<User> loadUsers() {
        List<User> users = new ArrayList<User>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_DIR + "users.csv"))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 5);
                if (parts.length < 5) {
                    continue;
                }
                User user = new User();
                user.id = ValidationUtils.parseInt(parts[0], 0);
                user.username = parts[1].trim();
                user.password = parts[2].trim();
                user.role = parts[3].trim();
                user.displayName = parts[4].trim();
                users.add(user);
            }
        } catch (IOException e) {
            System.err.println("Unable to load users: " + e.getMessage());
        }
        return users;
    }

    public static void saveUsers(List<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "users.csv"))) {
            writer.println("id,username,password,role,displayName");
            for (User user : users) {
                writer.println(user.id + "," + safe(user.username) + "," + safe(user.password) + "," + safe(user.role) + "," + safe(user.displayName));
            }
        } catch (IOException e) {
            System.err.println("Unable to save users: " + e.getMessage());
        }
    }

    public static List<TAProfile> loadProfiles() {
        List<TAProfile> profiles = new ArrayList<TAProfile>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_DIR + "profiles.csv"))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 10);
                if (parts.length < 10) {
                    continue;
                }
                TAProfile profile = new TAProfile();
                profile.id = ValidationUtils.parseInt(parts[0], 0);
                profile.userId = ValidationUtils.parseInt(parts[1], 0);
                profile.fullName = parts[2].trim();
                profile.email = parts[3].trim();
                profile.studentId = parts[4].trim();
                profile.skills = parts[5].trim();
                profile.gpa = ValidationUtils.parseDouble(parts[6], 0.0);
                profile.cvPath = parts[7].trim();
                profile.availability = parts[8].trim();
                profile.statement = parts[9].trim();
                profiles.add(profile);
            }
        } catch (IOException e) {
            System.err.println("Unable to load profiles: " + e.getMessage());
        }
        return profiles;
    }

    public static void saveProfiles(List<TAProfile> profiles) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "profiles.csv"))) {
            writer.println("id,userId,fullName,email,studentId,skills,gpa,cvPath,availability,statement");
            for (TAProfile profile : profiles) {
                writer.println(profile.id + "," + profile.userId + "," + safe(profile.fullName) + "," + safe(profile.email)
                        + "," + safe(profile.studentId) + "," + safe(profile.skills) + "," + profile.gpa + "," + safe(profile.cvPath)
                        + "," + safe(profile.availability) + "," + safe(profile.statement));
            }
        } catch (IOException e) {
            System.err.println("Unable to save profiles: " + e.getMessage());
        }
    }

    public static List<Job> loadJobs() {
        List<Job> jobs = new ArrayList<Job>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_DIR + "jobs.csv"))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 9);
                if (parts.length < 9) {
                    continue;
                }
                Job job = new Job();
                job.id = ValidationUtils.parseInt(parts[0], 0);
                job.moId = ValidationUtils.parseInt(parts[1], 0);
                job.title = parts[2].trim();
                job.module = parts[3].trim();
                job.description = parts[4].trim();
                job.requiredSkills = parts[5].trim();
                job.maxHours = ValidationUtils.parseInt(parts[6], 0);
                job.status = parts[7].trim();
                job.location = parts[8].trim();
                jobs.add(job);
            }
        } catch (IOException e) {
            System.err.println("Unable to load jobs: " + e.getMessage());
        }
        return jobs;
    }

    public static void saveJobs(List<Job> jobs) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "jobs.csv"))) {
            writer.println("id,moId,title,module,description,requiredSkills,maxHours,status,location");
            for (Job job : jobs) {
                writer.println(job.id + "," + job.moId + "," + safe(job.title) + "," + safe(job.module) + "," + safe(job.description)
                        + "," + safe(job.requiredSkills) + "," + job.maxHours + "," + safe(job.status) + "," + safe(job.location));
            }
        } catch (IOException e) {
            System.err.println("Unable to save jobs: " + e.getMessage());
        }
    }

    public static List<Application> loadApplications() {
        List<Application> applications = new ArrayList<Application>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_DIR + "applications.csv"))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 8);
                if (parts.length < 8) {
                    continue;
                }
                Application app = new Application();
                app.id = ValidationUtils.parseInt(parts[0], 0);
                app.taId = ValidationUtils.parseInt(parts[1], 0);
                app.jobId = ValidationUtils.parseInt(parts[2], 0);
                app.status = parts[3].trim();
                app.appliedAt = parts[4].trim();
                app.matchScore = ValidationUtils.parseInt(parts[5], 0);
                app.matchSummary = parts[6].trim();
                app.reviewerNote = parts[7].trim();
                applications.add(app);
            }
        } catch (IOException e) {
            System.err.println("Unable to load applications: " + e.getMessage());
        }
        return applications;
    }

    public static void saveApplications(List<Application> applications) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "applications.csv"))) {
            writer.println("id,taId,jobId,status,appliedAt,matchScore,matchSummary,reviewerNote");
            for (Application app : applications) {
                writer.println(app.id + "," + app.taId + "," + app.jobId + "," + safe(app.status) + "," + safe(app.appliedAt)
                        + "," + app.matchScore + "," + safe(app.matchSummary) + "," + safe(app.reviewerNote));
            }
        } catch (IOException e) {
            System.err.println("Unable to save applications: " + e.getMessage());
        }
    }

    public static User findUserById(int id) {
        for (User user : loadUsers()) {
            if (user.id == id) {
                return user;
            }
        }
        return null;
    }

    public static User findUserByUsername(String username) {
        for (User user : loadUsers()) {
            if (user.username.equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public static User findUserByDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }
        for (User user : loadUsers()) {
            if (user.getSafeDisplayName().equalsIgnoreCase(displayName.trim())) {
                return user;
            }
        }
        return null;
    }

    public static TAProfile findProfileByUserId(int userId) {
        for (TAProfile profile : loadProfiles()) {
            if (profile.userId == userId) {
                return profile;
            }
        }
        return null;
    }

    public static Job findJobById(int jobId) {
        for (Job job : loadJobs()) {
            if (job.id == jobId) {
                return job;
            }
        }
        return null;
    }

    public static int nextUserId() {
        int max = 0;
        for (User user : loadUsers()) {
            max = Math.max(max, user.id);
        }
        return max + 1;
    }

    public static int nextProfileId() {
        int max = 0;
        for (TAProfile profile : loadProfiles()) {
            max = Math.max(max, profile.id);
        }
        return max + 1;
    }

    public static int nextJobId() {
        int max = 0;
        for (Job job : loadJobs()) {
            max = Math.max(max, job.id);
        }
        return max + 1;
    }

    public static int nextApplicationId() {
        int max = 0;
        for (Application app : loadApplications()) {
            max = Math.max(max, app.id);
        }
        return max + 1;
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(',', ';').replace('\n', ' ').replace('\r', ' ');
    }
}
