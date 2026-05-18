import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Central CSV persistence helper for the stand-alone demo.
 *
 * <p>The coursework requires simple text-file storage rather than a database, so every major
 * entity is loaded from and saved to CSV files under {@code data/}. The class also seeds a small
 * deterministic dataset when those files do not yet exist.</p>
 *
 * <p>The implementation intentionally stays lightweight and dependency-free. It now includes a
 * compact CSV parser/writer that preserves commas, quotes, and empty values while remaining
 * compatible with the earlier plain rows already stored in the repository.</p>
 */
public class FileStorage {
    private static final String DATA_DIR = "data" + File.separator;
    private static final int OVERLOAD_LIMIT = 20;

    /**
     * Ensures the CSV storage directory and seed files exist before the UI or tests access them.
     */
    public static void initialise() {
        new File(DATA_DIR).mkdirs();
        ensureUsers();
        ensureProfiles();
        ensureJobs();
        ensureApplications();
        ensureNotifications();
    }

    /**
     * Returns the shared workload threshold used by admin monitoring and recommendation features.
     */
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

    private static void ensureNotifications() {
        File file = new File(DATA_DIR + "notifications.csv");
        if (file.exists()) {
            return;
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("id,userId,title,message,status,createdAt,actionHint");
            writer.println("1,2,Welcome to the TA system,Complete your profile and check open jobs to get started.,READ,2026-04-05 10:00,Open My Profile to complete your details.");
        } catch (IOException e) {
            System.err.println("Unable to create notifications.csv: " + e.getMessage());
        }
    }

    /**
     * Loads all users from {@code data/users.csv}.
     */
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<User>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_DIR + "users.csv"))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> parts = parseCsvRow(line, 5);
                if (parts.size() < 5) {
                    continue;
                }
                User user = new User();
                user.id = ValidationUtils.parseInt(parts.get(0), 0);
                user.username = parts.get(1).trim();
                user.password = parts.get(2).trim();
                user.role = parts.get(3).trim();
                user.displayName = parts.get(4).trim();
                users.add(user);
            }
        } catch (IOException e) {
            System.err.println("Unable to load users: " + e.getMessage());
        }
        return users;
    }

    /**
     * Persists the full user list back to {@code data/users.csv}.
     */
    public static void saveUsers(List<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "users.csv"))) {
            writer.println("id,username,password,role,displayName");
            for (User user : users) {
                writer.println(csvLine(String.valueOf(user.id), user.username, user.password, user.role, user.displayName));
            }
        } catch (IOException e) {
            System.err.println("Unable to save users: " + e.getMessage());
        }
    }

    /**
     * Loads TA profiles used by the TA dashboard and matching logic.
     */
    public static List<TAProfile> loadProfiles() {
        List<TAProfile> profiles = new ArrayList<TAProfile>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_DIR + "profiles.csv"))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> parts = parseCsvRow(line, 10);
                if (parts.size() < 10) {
                    continue;
                }
                TAProfile profile = new TAProfile();
                profile.id = ValidationUtils.parseInt(parts.get(0), 0);
                profile.userId = ValidationUtils.parseInt(parts.get(1), 0);
                profile.fullName = parts.get(2).trim();
                profile.email = parts.get(3).trim();
                profile.studentId = parts.get(4).trim();
                profile.skills = parts.get(5).trim();
                profile.gpa = ValidationUtils.parseDouble(parts.get(6), 0.0);
                profile.cvPath = parts.get(7).trim();
                profile.availability = parts.get(8).trim();
                profile.statement = parts.get(9).trim();
                profiles.add(profile);
            }
        } catch (IOException e) {
            System.err.println("Unable to load profiles: " + e.getMessage());
        }
        return profiles;
    }

    /**
     * Saves all TA profiles while preserving commas and empty fields in text columns.
     */
    public static void saveProfiles(List<TAProfile> profiles) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "profiles.csv"))) {
            writer.println("id,userId,fullName,email,studentId,skills,gpa,cvPath,availability,statement");
            for (TAProfile profile : profiles) {
                writer.println(csvLine(
                        String.valueOf(profile.id),
                        String.valueOf(profile.userId),
                        profile.fullName,
                        profile.email,
                        profile.studentId,
                        profile.skills,
                        String.valueOf(profile.gpa),
                        profile.cvPath,
                        profile.availability,
                        profile.statement));
            }
        } catch (IOException e) {
            System.err.println("Unable to save profiles: " + e.getMessage());
        }
    }

    /**
     * Loads all jobs visible to MO, TA, and Admin workflows.
     */
    public static List<Job> loadJobs() {
        List<Job> jobs = new ArrayList<Job>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_DIR + "jobs.csv"))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> parts = parseCsvRow(line, 9);
                if (parts.size() < 9) {
                    continue;
                }
                Job job = new Job();
                job.id = ValidationUtils.parseInt(parts.get(0), 0);
                job.moId = ValidationUtils.parseInt(parts.get(1), 0);
                job.title = parts.get(2).trim();
                job.module = parts.get(3).trim();
                job.description = parts.get(4).trim();
                job.requiredSkills = parts.get(5).trim();
                job.maxHours = ValidationUtils.parseInt(parts.get(6), 0);
                job.status = parts.get(7).trim();
                job.location = parts.get(8).trim();
                jobs.add(job);
            }
        } catch (IOException e) {
            System.err.println("Unable to load jobs: " + e.getMessage());
        }
        return jobs;
    }

    /**
     * Saves jobs to CSV for use across MO posting, TA browsing, and admin review screens.
     */
    public static void saveJobs(List<Job> jobs) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "jobs.csv"))) {
            writer.println("id,moId,title,module,description,requiredSkills,maxHours,status,location");
            for (Job job : jobs) {
                writer.println(csvLine(
                        String.valueOf(job.id),
                        String.valueOf(job.moId),
                        job.title,
                        job.module,
                        job.description,
                        job.requiredSkills,
                        String.valueOf(job.maxHours),
                        job.status,
                        job.location));
            }
        } catch (IOException e) {
            System.err.println("Unable to save jobs: " + e.getMessage());
        }
    }

    /**
     * Loads job applications, including AI match summaries and reviewer notes.
     */
    public static List<Application> loadApplications() {
        List<Application> applications = new ArrayList<Application>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_DIR + "applications.csv"))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> parts = parseCsvRow(line, 8);
                if (parts.size() < 8) {
                    continue;
                }
                Application app = new Application();
                app.id = ValidationUtils.parseInt(parts.get(0), 0);
                app.taId = ValidationUtils.parseInt(parts.get(1), 0);
                app.jobId = ValidationUtils.parseInt(parts.get(2), 0);
                app.status = parts.get(3).trim();
                app.appliedAt = parts.get(4).trim();
                app.matchScore = ValidationUtils.parseInt(parts.get(5), 0);
                app.matchSummary = parts.get(6).trim();
                app.reviewerNote = parts.get(7).trim();
                applications.add(app);
            }
        } catch (IOException e) {
            System.err.println("Unable to load applications: " + e.getMessage());
        }
        return applications;
    }

    /**
     * Saves applications while preserving reviewer notes and explainable AI summaries.
     */
    public static void saveApplications(List<Application> applications) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "applications.csv"))) {
            writer.println("id,taId,jobId,status,appliedAt,matchScore,matchSummary,reviewerNote");
            for (Application app : applications) {
                writer.println(csvLine(
                        String.valueOf(app.id),
                        String.valueOf(app.taId),
                        String.valueOf(app.jobId),
                        app.status,
                        app.appliedAt,
                        String.valueOf(app.matchScore),
                        app.matchSummary,
                        app.reviewerNote));
            }
        } catch (IOException e) {
            System.err.println("Unable to save applications: " + e.getMessage());
        }
    }

    /**
     * Loads in-app notifications stored in {@code data/notifications.csv}.
     */
    public static List<Notification> loadNotifications() {
        List<Notification> notifications = new ArrayList<Notification>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_DIR + "notifications.csv"))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> parts = parseCsvRow(line, 7);
                if (parts.size() < 7) {
                    continue;
                }
                Notification notification = new Notification();
                notification.id = ValidationUtils.parseInt(parts.get(0), 0);
                notification.userId = ValidationUtils.parseInt(parts.get(1), 0);
                notification.title = parts.get(2).trim();
                notification.message = parts.get(3).trim();
                notification.status = parts.get(4).trim();
                notification.createdAt = parts.get(5).trim();
                notification.actionHint = parts.get(6).trim();
                notifications.add(notification);
            }
        } catch (IOException e) {
            System.err.println("Unable to load notifications: " + e.getMessage());
        }
        return notifications;
    }

    /**
     * Persists notifications generated by MO decisions, profile reminders, and job closures.
     */
    public static void saveNotifications(List<Notification> notifications) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "notifications.csv"))) {
            writer.println("id,userId,title,message,status,createdAt,actionHint");
            for (Notification notification : notifications) {
                writer.println(csvLine(
                        String.valueOf(notification.id),
                        String.valueOf(notification.userId),
                        notification.title,
                        notification.message,
                        notification.status,
                        notification.createdAt,
                        notification.actionHint));
            }
        } catch (IOException e) {
            System.err.println("Unable to save notifications: " + e.getMessage());
        }
    }

    /**
     * Finds a user by numeric identifier in the current CSV snapshot.
     */
    public static User findUserById(int id) {
        for (User user : loadUsers()) {
            if (user.id == id) {
                return user;
            }
        }
        return null;
    }

    /**
     * Finds a user by username using a case-insensitive comparison to match login behaviour.
     */
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

    public static int nextNotificationId() {
        int max = 0;
        for (Notification notification : loadNotifications()) {
            max = Math.max(max, notification.id);
        }
        return max + 1;
    }

    private static List<String> parseCsvRow(String line, int expectedColumns) {
        List<String> values = new ArrayList<String>();
        if (line == null) {
            return values;
        }

        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (quoted) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cell.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    cell.append(ch);
                }
            } else if (ch == '"') {
                quoted = true;
            } else if (ch == ',') {
                values.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(ch);
            }
        }
        values.add(cell.toString());
        while (values.size() < expectedColumns) {
            values.add("");
        }
        return values;
    }

    private static String csvLine(String... values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(csvCell(values[i]));
        }
        return builder.toString();
    }

    private static String csvCell(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value.replace('\n', ' ').replace('\r', ' ');
        boolean shouldQuote = cleaned.indexOf(',') >= 0 || cleaned.indexOf('"') >= 0;
        if (!shouldQuote) {
            return cleaned;
        }
        return "\"" + cleaned.replace("\"", "\"\"") + "\"";
    }
}
