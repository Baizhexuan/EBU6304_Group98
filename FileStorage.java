import java.io.*;
import java.util.*;

/**
 * FileStorage: Handles all CSV read/write operations.
 * All data is stored in the 'data/' directory relative to the working directory.
 * Fields use comma as delimiter; commas within field values are replaced by semicolons.
 *
 * <p>Version2 update — L1 password hashing:
 * <ul>
 *   <li>users.csv schema changed: id,username,passwordHash,salt,role</li>
 *   <li>Seed data now stores SHA-256 hashed passwords with random salts</li>
 *   <li>Plaintext passwords never appear in CSV storage</li>
 * </ul>
 *
 * @version 2.0
 * @since 2026-04-08
 */
public class FileStorage {

    private static final String DATA_DIR = "data" + File.separator;

    // ─────────────────────────────────────────────────────────
    // Initialisation: called once at startup to create data files
    // ─────────────────────────────────────────────────────────
    public static void initDataFiles() {
        new File(DATA_DIR).mkdirs();

        // --- Version2: users.csv now stores hashed passwords (SHA-256 + salt) ---
        File usersFile = new File(DATA_DIR + "users.csv");
        if (!usersFile.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(usersFile))) {
                pw.println("id,username,passwordHash,salt,role");
                // Seed demo accounts — passwords are hashed via PasswordUtil
                String[][] seeds = {
                    {"1", "admin",  "admin123", "ADMIN"},
                    {"2", "ta1",    "ta123",    "TA"},
                    {"3", "ta2",    "ta456",    "TA"},
                    {"4", "mo1",    "mo123",    "MO"},
                    {"5", "mo2",    "mo456",    "MO"}
                };
                for (String[] s : seeds) {
                    String salt = PasswordUtil.generateSalt();
                    String hash = PasswordUtil.hashPassword(s[2], salt);
                    pw.println(s[0] + "," + s[1] + "," + hash + "," + salt + "," + s[3]);
                }
            } catch (IOException e) {
                System.err.println("Could not create users.csv: " + e.getMessage());
            }
        }

        File profilesFile = new File(DATA_DIR + "profiles.csv");
        if (!profilesFile.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(profilesFile))) {
                pw.println("id,userId,fullName,email,studentId,skills,gpa,cvPath");
            } catch (IOException e) {
                System.err.println("Could not create profiles.csv: " + e.getMessage());
            }
        }

        File jobsFile = new File(DATA_DIR + "jobs.csv");
        if (!jobsFile.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(jobsFile))) {
                pw.println("id,moId,title,module,description,requiredSkills,maxHours,status");
                pw.println("1,4,Java Lab Assistant,EBU6304,Help students in Java lab sessions,Java;OOP,10,OPEN");
                pw.println("2,4,Exam Invigilator,General,Assist with exam invigilation,Communication;Reliability,5,OPEN");
                pw.println("3,5,Python Tutor,EBU5476,Tutor students in Python programming,Python;Data Structures,8,OPEN");
            } catch (IOException e) {
                System.err.println("Could not create jobs.csv: " + e.getMessage());
            }
        }

        File appsFile = new File(DATA_DIR + "applications.csv");
        if (!appsFile.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(appsFile))) {
                pw.println("id,taId,jobId,status,appliedAt");
            } catch (IOException e) {
                System.err.println("Could not create applications.csv: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // User operations  (Version2: 5-field CSV — id,username,passwordHash,salt,role)
    // ─────────────────────────────────────────────────────────
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_DIR + "users.csv"))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",", 5);
                if (p.length >= 5) {
                    // Version2 format: id,username,passwordHash,salt,role
                    try {
                        users.add(new User(
                            Integer.parseInt(p[0].trim()),
                            p[1].trim(),
                            p[2].trim(),   // passwordHash
                            p[3].trim(),   // salt
                            p[4].trim()    // role
                        ));
                    } catch (NumberFormatException ignored) {}
                } else if (p.length == 4) {
                    // Version1 legacy format: id,username,password,role — migrate on next save
                    try {
                        User u = new User(Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim(), p[3].trim());
                        users.add(u);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    public static void saveUsers(List<User> users) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_DIR + "users.csv"))) {
            pw.println("id,username,passwordHash,salt,role");
            for (User u : users) {
                pw.println(u.id + "," + sanitise(u.username) + ","
                        + sanitise(u.passwordHash) + "," + sanitise(u.salt) + ","
                        + sanitise(u.role));
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // TAProfile operations
    // ─────────────────────────────────────────────────────────
    public static List<TAProfile> loadProfiles() {
        List<TAProfile> profiles = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_DIR + "profiles.csv"))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // 8 fields: id,userId,fullName,email,studentId,skills,gpa,cvPath
                String[] p = line.split(",", 8);
                if (p.length >= 8) {
                    try {
                        TAProfile profile = new TAProfile();
                        profile.id = Integer.parseInt(p[0].trim());
                        profile.userId = Integer.parseInt(p[1].trim());
                        profile.fullName = p[2].trim();
                        profile.email = p[3].trim();
                        profile.studentId = p[4].trim();
                        profile.skills = p[5].trim();
                        try { profile.gpa = Double.parseDouble(p[6].trim()); } catch (NumberFormatException ignored) {}
                        profile.cvPath = p[7].trim();
                        profiles.add(profile);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading profiles: " + e.getMessage());
        }
        return profiles;
    }

    public static void saveProfiles(List<TAProfile> profiles) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_DIR + "profiles.csv"))) {
            pw.println("id,userId,fullName,email,studentId,skills,gpa,cvPath");
            for (TAProfile p : profiles) {
                pw.println(p.id + "," + p.userId + ","
                        + sanitise(p.fullName) + "," + sanitise(p.email) + ","
                        + sanitise(p.studentId) + "," + sanitise(p.skills) + ","
                        + p.gpa + "," + sanitise(p.cvPath));
            }
        } catch (IOException e) {
            System.err.println("Error saving profiles: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // Job operations
    // ─────────────────────────────────────────────────────────
    public static List<Job> loadJobs() {
        List<Job> jobs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_DIR + "jobs.csv"))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // 8 fields: id,moId,title,module,description,requiredSkills,maxHours,status
                String[] p = line.split(",", 8);
                if (p.length >= 8) {
                    try {
                        Job job = new Job();
                        job.id = Integer.parseInt(p[0].trim());
                        job.moId = Integer.parseInt(p[1].trim());
                        job.title = p[2].trim();
                        job.module = p[3].trim();
                        job.description = p[4].trim();
                        job.requiredSkills = p[5].trim();
                        try { job.maxHours = Integer.parseInt(p[6].trim()); } catch (NumberFormatException ignored) {}
                        job.status = p[7].trim();
                        jobs.add(job);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading jobs: " + e.getMessage());
        }
        return jobs;
    }

    public static void saveJobs(List<Job> jobs) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_DIR + "jobs.csv"))) {
            pw.println("id,moId,title,module,description,requiredSkills,maxHours,status");
            for (Job j : jobs) {
                pw.println(j.id + "," + j.moId + ","
                        + sanitise(j.title) + "," + sanitise(j.module) + ","
                        + sanitise(j.description) + "," + sanitise(j.requiredSkills) + ","
                        + j.maxHours + "," + sanitise(j.status));
            }
        } catch (IOException e) {
            System.err.println("Error saving jobs: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // Application operations
    // ─────────────────────────────────────────────────────────
    public static List<Application> loadApplications() {
        List<Application> apps = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_DIR + "applications.csv"))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // 5 fields: id,taId,jobId,status,appliedAt
                String[] p = line.split(",", 5);
                if (p.length >= 5) {
                    try {
                        Application a = new Application();
                        a.id = Integer.parseInt(p[0].trim());
                        a.taId = Integer.parseInt(p[1].trim());
                        a.jobId = Integer.parseInt(p[2].trim());
                        a.status = p[3].trim();
                        a.appliedAt = p[4].trim();
                        apps.add(a);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading applications: " + e.getMessage());
        }
        return apps;
    }

    public static void saveApplications(List<Application> apps) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_DIR + "applications.csv"))) {
            pw.println("id,taId,jobId,status,appliedAt");
            for (Application a : apps) {
                pw.println(a.id + "," + a.taId + "," + a.jobId + "," + sanitise(a.status) + "," + sanitise(a.appliedAt));
            }
        } catch (IOException e) {
            System.err.println("Error saving applications: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // Helper: replace commas inside field values to avoid CSV corruption
    // ─────────────────────────────────────────────────────────
    private static String sanitise(String value) {
        if (value == null) return "";
        return value.replace(",", ";");
    }
}
