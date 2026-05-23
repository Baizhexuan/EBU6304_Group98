package com.bupt.ta.recruitment.util;

import com.bupt.ta.recruitment.model.Application;
import com.bupt.ta.recruitment.model.Job;
import com.bupt.ta.recruitment.model.TAProfile;
import com.bupt.ta.recruitment.model.User;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class DataSeeder {
    private DataSeeder() {
    }

    public static void main(String[] args) {
        seedIfNeeded();
        System.out.println("Data seeding completed.");
    }

    public static void seedIfNeeded() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        CsvStorage<User> userStorage = new CsvStorage<>("data/users.csv", User::fromCsvRow);
        if (!userStorage.loadAll().isEmpty()) {
            ensureOptionalFiles();
            return;
        }

        CsvStorage<TAProfile> profileStorage = new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);
        CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
        CsvStorage<Application> appStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);

        List<User> users = new ArrayList<>();
        users.add(createUser("user-admin-1", "admin", "admin123", User.UserRole.ADMIN));
        users.add(createUser("user-mo-1", "mo1", "mo123", User.UserRole.MO));
        users.add(createUser("user-mo-2", "mo2", "mo456", User.UserRole.MO));
        users.add(createUser("user-ta-1", "ta1", "ta123", User.UserRole.TA));
        users.add(createUser("user-ta-2", "ta2", "ta456", User.UserRole.TA));
        users.add(createUser("user-ta-3", "ta3", "ta789", User.UserRole.TA));
        userStorage.saveAll(users);

        List<TAProfile> profiles = new ArrayList<>();
        profiles.add(new TAProfile("prof-1", "user-ta-1", "Alice Zhang", "alice@bupt.edu.cn", "TA2026001", "Java;Spring;Communication", 3.8, "alice_cv.pdf"));
        profiles.add(new TAProfile("prof-2", "user-ta-2", "Bob Lee", "bob@bupt.edu.cn", "TA2026002", "Python;Machine Learning;Excel", 3.6, "bob_cv.pdf"));
        profiles.add(new TAProfile("prof-3", "user-ta-3", "Charlie Wang", "charlie@bupt.edu.cn", "TA2026003", "C++;Algorithms;Java", 3.9, "charlie_cv.docx"));
        profileStorage.saveAll(profiles);

        List<Job> jobs = new ArrayList<>();
        jobs.add(new Job("job-1", "user-mo-1", "Java Software Engineering TA", "EBU6304", "Assist with labs and marking for Java software engineering.", "Java;Spring", 20, Job.JobStatus.OPEN));
        jobs.add(new Job("job-2", "user-mo-1", "Database Systems TA", "EBU5302", "Support database assignments and tutorial Q&A.", "SQL;Java", 15, Job.JobStatus.OPEN));
        jobs.add(new Job("job-3", "user-mo-2", "Machine Learning TA", "EBU6001", "Help with Python ML practical labs.", "Python;Machine Learning", 25, Job.JobStatus.OPEN));
        jobs.add(new Job("job-4", "user-mo-2", "Algorithm Design TA", "EBU5001", "Support algorithm worksheet sessions.", "C++;Algorithms", 10, Job.JobStatus.CLOSED));
        jobStorage.saveAll(jobs);

        List<Application> applications = new ArrayList<>();
        applications.add(new Application("app-1", "user-ta-1", "job-1", Application.AppStatus.PENDING, System.currentTimeMillis() - 86400000L));
        applications.add(new Application("app-2", "user-ta-2", "job-3", Application.AppStatus.SELECTED, System.currentTimeMillis() - 172800000L));
        applications.add(new Application("app-3", "user-ta-3", "job-1", Application.AppStatus.REJECTED, System.currentTimeMillis() - 43200000L));
        appStorage.saveAll(applications);

        ensureOptionalFiles();
    }

    private static User createUser(String id, String username, String password, User.UserRole role) {
        String salt = PasswordUtil.generateSalt();
        return new User(id, username, PasswordUtil.hashPassword(password, salt), salt, role);
    }

    private static void ensureOptionalFiles() {
        new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);
        new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
        new CsvStorage<>("data/applications.csv", Application::fromCsvRow);
    }
}
