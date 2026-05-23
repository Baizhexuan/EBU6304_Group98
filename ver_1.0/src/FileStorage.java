import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private static final String ID_COUNTERS_FILE = "id_counters.csv";

    private static BufferedReader newUtf8Reader(String fileName) throws IOException {
        return newUtf8Reader(new File(DATA_DIR + fileName));
    }

    private static BufferedReader newUtf8Reader(File file) throws IOException {
        String stored = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        return new BufferedReader(new StringReader(DataCryptoService.decryptIfNeeded(stored)));
    }

    private static PrintWriter newUtf8Writer(File file) throws IOException {
        return new PrintWriter(new CsvFileWriter(file));
    }

    private static boolean shouldEncryptDataFile(File file) {
        if (file == null) {
            return false;
        }
        String name = file.getName().toLowerCase();
        return "users.csv".equals(name)
                || "profiles.csv".equals(name)
                || "jobs.csv".equals(name)
                || "applications.csv".equals(name)
                || "notifications.csv".equals(name)
                || "ta_reputations.csv".equals(name)
                || "work_evaluations.csv".equals(name)
                || "messages.csv".equals(name)
                || "message_consents.csv".equals(name)
                || ID_COUNTERS_FILE.equals(name);
    }

    private static class CsvFileWriter extends Writer {
        private final File file;
        private final StringBuilder buffer = new StringBuilder();
        private boolean closed;

        private CsvFileWriter(File file) {
            this.file = file;
        }

        @Override
        public void write(char[] cbuf, int off, int len) {
            buffer.append(cbuf, off, len);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            String text = buffer.toString();
            if (shouldEncryptDataFile(file)) {
                text = DataCryptoService.encrypt(text);
            }
            Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
        }
    }

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
        ensureTAReputations();
        ensureWorkEvaluations();
        ensureMessages();
        ensureMessageConsents();
        secureAllDataFiles();
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
        try (PrintWriter writer = newUtf8Writer(file)) {
            writer.println("id,username,password,role,displayName");
            writer.println(csvLine("1", "admin", PasswordService.hashPassword("admin123"), "ADMIN", "System Admin"));
            writer.println(csvLine("2", "ta1", PasswordService.hashPassword("ta123"), "TA", "Li Ming"));
            writer.println(csvLine("3", "ta2", PasswordService.hashPassword("ta456"), "TA", "Wang Yue"));
            writer.println(csvLine("4", "mo1", PasswordService.hashPassword("mo123"), "MO", "Dr Chen"));
            writer.println(csvLine("5", "mo2", PasswordService.hashPassword("mo456"), "MO", "Prof Zhao"));
        } catch (IOException e) {
            System.err.println("Unable to create users.csv: " + e.getMessage());
        }
    }

    private static void ensureProfiles() {
        File file = new File(DATA_DIR + "profiles.csv");
        if (file.exists()) {
            return;
        }
        try (PrintWriter writer = newUtf8Writer(file)) {
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
        try (PrintWriter writer = newUtf8Writer(file)) {
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
        try (PrintWriter writer = newUtf8Writer(file)) {
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
        try (PrintWriter writer = newUtf8Writer(file)) {
            writer.println("id,userId,title,message,status,createdAt,actionHint");
            writer.println("1,2,Welcome to the TA system,Complete your profile and check open jobs to get started.,READ,2026-04-05 10:00,Open My Profile to complete your details.");
        } catch (IOException e) {
            System.err.println("Unable to create notifications.csv: " + e.getMessage());
        }
    }

    private static void ensureTAReputations() {
        File file = new File(DATA_DIR + "ta_reputations.csv");
        if (file.exists()) {
            return;
        }
        try (PrintWriter writer = newUtf8Writer(file)) {
            writer.println("taId,score,penaltyCount,lastUpdated,note");
        } catch (IOException e) {
            System.err.println("Unable to create ta_reputations.csv: " + e.getMessage());
        }
    }

    private static void ensureWorkEvaluations() {
        File file = new File(DATA_DIR + "work_evaluations.csv");
        if (file.exists()) {
            return;
        }
        try (PrintWriter writer = newUtf8Writer(file)) {
            writer.println("id,applicationId,taId,moId,jobId,rating,comment,evaluatedAt,penaltyApplied");
        } catch (IOException e) {
            System.err.println("Unable to create work_evaluations.csv: " + e.getMessage());
        }
    }

    private static void ensureMessages() {
        File file = new File(DATA_DIR + "messages.csv");
        if (file.exists()) {
            return;
        }
        try (PrintWriter writer = newUtf8Writer(file)) {
            writer.println("id,fromUserId,toUserId,jobId,body,status,createdAt");
        } catch (IOException e) {
            System.err.println("Unable to create messages.csv: " + e.getMessage());
        }
    }

    private static void ensureMessageConsents() {
        File file = new File(DATA_DIR + "message_consents.csv");
        if (file.exists()) {
            return;
        }
        try (PrintWriter writer = newUtf8Writer(file)) {
            writer.println("id,userAId,userBId,jobId,approved,requestedBy,updatedAt");
        } catch (IOException e) {
            System.err.println("Unable to create message_consents.csv: " + e.getMessage());
        }
    }

    /**
     * Loads all users from {@code data/users.csv}.
     */
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<User>();
        try (BufferedReader reader = newUtf8Reader("users.csv")) {
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
    public static synchronized void saveUsers(List<User> users) {
        File file = new File(DATA_DIR + "users.csv");
        try (PrintWriter writer = newUtf8Writer(file)) {
            writer.println("id,username,password,role,displayName");
            for (User user : users) {
                writer.println(csvLine(String.valueOf(user.id), user.username, user.password, user.role, user.displayName));
            }
            secureDataFile(file);
        } catch (IOException e) {
            System.err.println("Unable to save users: " + e.getMessage());
        }
    }

    /**
     * Loads TA profiles used by the TA dashboard and matching logic.
     */
    public static List<TAProfile> loadProfiles() {
        List<TAProfile> profiles = new ArrayList<TAProfile>();
        try (BufferedReader reader = newUtf8Reader("profiles.csv")) {
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
    public static synchronized void saveProfiles(List<TAProfile> profiles) {
        File file = new File(DATA_DIR + "profiles.csv");
        try (PrintWriter writer = newUtf8Writer(file)) {
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
            secureDataFile(file);
        } catch (IOException e) {
            System.err.println("Unable to save profiles: " + e.getMessage());
        }
    }

    /**
     * Loads all jobs visible to MO, TA, and Admin workflows.
     */
    public static List<Job> loadJobs() {
        List<Job> jobs = new ArrayList<Job>();
        try (BufferedReader reader = newUtf8Reader("jobs.csv")) {
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
    public static synchronized void saveJobs(List<Job> jobs) {
        File file = new File(DATA_DIR + "jobs.csv");
        try (PrintWriter writer = newUtf8Writer(file)) {
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
            secureDataFile(file);
        } catch (IOException e) {
            System.err.println("Unable to save jobs: " + e.getMessage());
        }
    }

    /**
     * Loads job applications, including AI match summaries and reviewer notes.
     */
    public static List<Application> loadApplications() {
        List<Application> applications = new ArrayList<Application>();
        try (BufferedReader reader = newUtf8Reader("applications.csv")) {
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
    public static synchronized void saveApplications(List<Application> applications) {
        File file = new File(DATA_DIR + "applications.csv");
        try (PrintWriter writer = newUtf8Writer(file)) {
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
            secureDataFile(file);
        } catch (IOException e) {
            System.err.println("Unable to save applications: " + e.getMessage());
        }
    }

    /**
     * Loads in-app notifications stored in {@code data/notifications.csv}.
     */
    public static List<Notification> loadNotifications() {
        List<Notification> notifications = new ArrayList<Notification>();
        try (BufferedReader reader = newUtf8Reader("notifications.csv")) {
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
    public static synchronized void saveNotifications(List<Notification> notifications) {
        File file = new File(DATA_DIR + "notifications.csv");
        try (PrintWriter writer = newUtf8Writer(file)) {
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
            secureDataFile(file);
        } catch (IOException e) {
            System.err.println("Unable to save notifications: " + e.getMessage());
        }
    }

    public static List<TAReputation> loadTAReputations() {
        // Reads reputation state used by ReputationService.applyReputationPenalty().
        // Empty file means every TA still has the default 100/100 score.
        List<TAReputation> reputations = new ArrayList<TAReputation>();
        try (BufferedReader reader = newUtf8Reader("ta_reputations.csv")) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> parts = parseCsvRow(line, 5);
                if (parts.size() < 5) {
                    continue;
                }
                TAReputation reputation = new TAReputation();
                reputation.taId = ValidationUtils.parseInt(parts.get(0), 0);
                reputation.score = ValidationUtils.parseInt(parts.get(1), 100);
                reputation.penaltyCount = ValidationUtils.parseInt(parts.get(2), 0);
                reputation.lastUpdated = parts.get(3).trim();
                reputation.note = parts.get(4).trim();
                reputations.add(reputation);
            }
        } catch (IOException e) {
            System.err.println("Unable to load TA reputations: " + e.getMessage());
        }
        return reputations;
    }

    public static synchronized void saveTAReputations(List<TAReputation> reputations) {
        // Persist all reputation rows after an MO rating triggers a penalty.
        File file = new File(DATA_DIR + "ta_reputations.csv");
        try (PrintWriter writer = newUtf8Writer(file)) {
            writer.println("taId,score,penaltyCount,lastUpdated,note");
            for (TAReputation reputation : reputations) {
                writer.println(csvLine(
                        String.valueOf(reputation.taId),
                        String.valueOf(reputation.score),
                        String.valueOf(reputation.penaltyCount),
                        reputation.lastUpdated,
                        reputation.note));
            }
            secureDataFile(file);
        } catch (IOException e) {
            System.err.println("Unable to save TA reputations: " + e.getMessage());
        }
    }

    public static List<WorkEvaluation> loadWorkEvaluations() {
        // Work evaluations are the audit trail for MO completion ratings.
        // ReputationService uses this data to prevent duplicate ratings on one application.
        List<WorkEvaluation> evaluations = new ArrayList<WorkEvaluation>();
        try (BufferedReader reader = newUtf8Reader("work_evaluations.csv")) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> parts = parseCsvRow(line, 9);
                if (parts.size() < 9) {
                    continue;
                }
                WorkEvaluation evaluation = new WorkEvaluation();
                evaluation.id = ValidationUtils.parseInt(parts.get(0), 0);
                evaluation.applicationId = ValidationUtils.parseInt(parts.get(1), 0);
                evaluation.taId = ValidationUtils.parseInt(parts.get(2), 0);
                evaluation.moId = ValidationUtils.parseInt(parts.get(3), 0);
                evaluation.jobId = ValidationUtils.parseInt(parts.get(4), 0);
                evaluation.rating = ValidationUtils.parseInt(parts.get(5), 0);
                evaluation.comment = parts.get(6).trim();
                evaluation.evaluatedAt = parts.get(7).trim();
                evaluation.penaltyApplied = Boolean.parseBoolean(parts.get(8).trim());
                evaluations.add(evaluation);
            }
        } catch (IOException e) {
            System.err.println("Unable to load work evaluations: " + e.getMessage());
        }
        return evaluations;
    }

    public static synchronized void saveWorkEvaluations(List<WorkEvaluation> evaluations) {
        // Stores the MO's final work rating and whether that rating caused a reputation penalty.
        File file = new File(DATA_DIR + "work_evaluations.csv");
        try (PrintWriter writer = newUtf8Writer(file)) {
            writer.println("id,applicationId,taId,moId,jobId,rating,comment,evaluatedAt,penaltyApplied");
            for (WorkEvaluation evaluation : evaluations) {
                writer.println(csvLine(
                        String.valueOf(evaluation.id),
                        String.valueOf(evaluation.applicationId),
                        String.valueOf(evaluation.taId),
                        String.valueOf(evaluation.moId),
                        String.valueOf(evaluation.jobId),
                        String.valueOf(evaluation.rating),
                        evaluation.comment,
                        evaluation.evaluatedAt,
                        String.valueOf(evaluation.penaltyApplied)));
            }
            secureDataFile(file);
        } catch (IOException e) {
            System.err.println("Unable to save work evaluations: " + e.getMessage());
        }
    }

    public static List<MessageRecord> loadMessages() {
        // Chat history for the Bell Centre. UTF-8 is required so Chinese messages survive
        // Windows/macOS round trips without becoming garbled.
        List<MessageRecord> messages = new ArrayList<MessageRecord>();
        try (BufferedReader reader = newUtf8Reader("messages.csv")) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> parts = parseCsvRow(line, 7);
                if (parts.size() < 7) {
                    continue;
                }
                MessageRecord message = new MessageRecord();
                message.id = ValidationUtils.parseInt(parts.get(0), 0);
                message.fromUserId = ValidationUtils.parseInt(parts.get(1), 0);
                message.toUserId = ValidationUtils.parseInt(parts.get(2), 0);
                message.jobId = ValidationUtils.parseInt(parts.get(3), 0);
                message.body = parts.get(4).trim();
                message.status = parts.get(5).trim();
                message.createdAt = parts.get(6).trim();
                messages.add(message);
            }
        } catch (IOException e) {
            System.err.println("Unable to load messages: " + e.getMessage());
        }
        return messages;
    }

    public static synchronized void saveMessages(List<MessageRecord> messages) {
        // Writes all chat messages after MessageService appends a new outgoing message.
        File file = new File(DATA_DIR + "messages.csv");
        try (PrintWriter writer = newUtf8Writer(file)) {
            writer.println("id,fromUserId,toUserId,jobId,body,status,createdAt");
            for (MessageRecord message : messages) {
                writer.println(csvLine(
                        String.valueOf(message.id),
                        String.valueOf(message.fromUserId),
                        String.valueOf(message.toUserId),
                        String.valueOf(message.jobId),
                        message.body,
                        message.status,
                        message.createdAt));
            }
            secureDataFile(file);
        } catch (IOException e) {
            System.err.println("Unable to save messages: " + e.getMessage());
        }
    }

    public static List<MessageConsent> loadMessageConsents() {
        // Consent rows record whether the MO has approved a TA-MO-job conversation.
        List<MessageConsent> consents = new ArrayList<MessageConsent>();
        try (BufferedReader reader = newUtf8Reader("message_consents.csv")) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> parts = parseCsvRow(line, 7);
                if (parts.size() < 7) {
                    continue;
                }
                MessageConsent consent = new MessageConsent();
                consent.id = ValidationUtils.parseInt(parts.get(0), 0);
                consent.userAId = ValidationUtils.parseInt(parts.get(1), 0);
                consent.userBId = ValidationUtils.parseInt(parts.get(2), 0);
                consent.jobId = ValidationUtils.parseInt(parts.get(3), 0);
                consent.approved = Boolean.parseBoolean(parts.get(4).trim());
                consent.requestedBy = ValidationUtils.parseInt(parts.get(5), 0);
                consent.updatedAt = parts.get(6).trim();
                consents.add(consent);
            }
        } catch (IOException e) {
            System.err.println("Unable to load message consents: " + e.getMessage());
        }
        return consents;
    }

    public static synchronized void saveMessageConsents(List<MessageConsent> consents) {
        // Saves approval state. Once approved=true, MessageService stops enforcing the 3-message cap.
        File file = new File(DATA_DIR + "message_consents.csv");
        try (PrintWriter writer = newUtf8Writer(file)) {
            writer.println("id,userAId,userBId,jobId,approved,requestedBy,updatedAt");
            for (MessageConsent consent : consents) {
                writer.println(csvLine(
                        String.valueOf(consent.id),
                        String.valueOf(consent.userAId),
                        String.valueOf(consent.userBId),
                        String.valueOf(consent.jobId),
                        String.valueOf(consent.approved),
                        String.valueOf(consent.requestedBy),
                        consent.updatedAt));
            }
            secureDataFile(file);
        } catch (IOException e) {
            System.err.println("Unable to save message consents: " + e.getMessage());
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

    public static synchronized int nextUserId() {
        int max = 0;
        for (User user : loadUsers()) {
            max = Math.max(max, user.id);
        }
        return nextIdFromCounter("users", max);
    }

    public static synchronized int nextProfileId() {
        int max = 0;
        for (TAProfile profile : loadProfiles()) {
            max = Math.max(max, profile.id);
        }
        return nextIdFromCounter("profiles", max);
    }

    public static synchronized int nextJobId() {
        int max = 0;
        for (Job job : loadJobs()) {
            max = Math.max(max, job.id);
        }
        return nextIdFromCounter("jobs", max);
    }

    public static synchronized int nextApplicationId() {
        int max = 0;
        for (Application app : loadApplications()) {
            max = Math.max(max, app.id);
        }
        return nextIdFromCounter("applications", max);
    }

    public static synchronized int nextNotificationId() {
        int max = 0;
        for (Notification notification : loadNotifications()) {
            max = Math.max(max, notification.id);
        }
        return nextIdFromCounter("notifications", max);
    }

    public static synchronized int nextWorkEvaluationId() {
        int max = 0;
        for (WorkEvaluation evaluation : loadWorkEvaluations()) {
            max = Math.max(max, evaluation.id);
        }
        return nextIdFromCounter("work_evaluations", max);
    }

    public static synchronized int nextMessageId() {
        int max = 0;
        for (MessageRecord message : loadMessages()) {
            max = Math.max(max, message.id);
        }
        return nextIdFromCounter("messages", max);
    }

    public static synchronized int nextMessageConsentId() {
        int max = 0;
        for (MessageConsent consent : loadMessageConsents()) {
            max = Math.max(max, consent.id);
        }
        return nextIdFromCounter("message_consents", max);
    }

    private static int nextIdFromCounter(String entity, int currentMax) {
        Map<String, Integer> counters = loadIdCounters();
        int lastIssued = counters.containsKey(entity) ? counters.get(entity) : currentMax;
        int next = Math.max(lastIssued, currentMax) + 1;
        counters.put(entity, next);
        saveIdCounters(counters);
        return next;
    }

    private static Map<String, Integer> loadIdCounters() {
        Map<String, Integer> counters = new HashMap<String, Integer>();
        File file = new File(DATA_DIR + ID_COUNTERS_FILE);
        if (!file.exists()) {
            return counters;
        }
        try (BufferedReader reader = newUtf8Reader(file)) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> parts = parseCsvRow(line, 2);
                if (parts.size() >= 2 && ValidationUtils.notBlank(parts.get(0))) {
                    counters.put(parts.get(0).trim(), ValidationUtils.parseInt(parts.get(1), 0));
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to load ID counters: " + e.getMessage());
        }
        return counters;
    }

    private static void saveIdCounters(Map<String, Integer> counters) {
        File file = new File(DATA_DIR + ID_COUNTERS_FILE);
        try (PrintWriter writer = newUtf8Writer(file)) {
            writer.println("entity,lastIssuedId");
            for (Map.Entry<String, Integer> entry : counters.entrySet()) {
                writer.println(csvLine(entry.getKey(), String.valueOf(entry.getValue())));
            }
            secureDataFile(file);
        } catch (IOException e) {
            System.err.println("Unable to save ID counters: " + e.getMessage());
        }
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
        if (quoted) {
            System.err.println("CSV parse warning: row ended inside a quoted field and may be malformed: " + line);
        }
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
        String cleaned = neutraliseCsvFormula(value.replace('\n', ' ').replace('\r', ' '));
        boolean shouldQuote = cleaned.indexOf(',') >= 0 || cleaned.indexOf('"') >= 0;
        if (!shouldQuote) {
            return cleaned;
        }
        return "\"" + cleaned.replace("\"", "\"\"") + "\"";
    }

    private static String neutraliseCsvFormula(String value) {
        if (value.isEmpty()) {
            return value;
        }
        char first = value.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t') {
            return "'" + value;
        }
        return value;
    }

    private static void secureAllDataFiles() {
        File dir = new File(DATA_DIR);
        File[] files = dir.listFiles((parent, name) -> name.toLowerCase().endsWith(".csv"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            secureDataFile(file);
        }
    }

    private static void secureDataFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        try {
            if (shouldEncryptDataFile(file)) {
                String stored = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                if (!DataCryptoService.isEncrypted(stored)) {
                    Files.write(file.toPath(), DataCryptoService.encrypt(stored).getBytes(StandardCharsets.UTF_8));
                }
            }
            file.setReadable(false, false);
            file.setWritable(false, false);
            file.setExecutable(false, false);
            file.setReadable(true, true);
            file.setWritable(true, true);
            Set<PosixFilePermission> permissions = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(file.toPath(), permissions);
        } catch (UnsupportedOperationException ignored) {
            // Windows does not expose POSIX permissions; owner-only flags above are best effort.
        } catch (IOException | SecurityException ex) {
            System.err.println("Unable to tighten permissions for " + file.getName() + ": " + ex.getMessage());
        }
    }
}

