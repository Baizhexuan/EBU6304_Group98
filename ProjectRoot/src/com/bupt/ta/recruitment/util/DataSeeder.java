package com.bupt.ta.recruitment.util;

import com.bupt.ta.recruitment.model.Application;
import com.bupt.ta.recruitment.model.Job;
import com.bupt.ta.recruitment.model.TAProfile;
import com.bupt.ta.recruitment.model.User;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Seed generator for preparing demo users, profiles, jobs, and applications.
 * It bootstraps the CSV data files so the system can run with predictable
 * sample data in fresh environments.
 */
public class DataSeeder {
    public static void main(String[] args) {
        // 定位项目根目录下的数据文件夹。
        File dataDir = new File("data");
        // 如果数据目录不存在，则先创建目录结构。
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // 初始化用户数据存储对象，用于读写 users.csv。
        CsvStorage<User> userStorage = new CsvStorage<>("data/users.csv", User::fromCsvRow);
        // 初始化 TA 档案数据存储对象，用于读写 profiles.csv。
        CsvStorage<TAProfile> profileStorage = new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);
        // 初始化岗位数据存储对象，用于读写 jobs.csv。
        CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
        // 初始化申请数据存储对象，用于读写 applications.csv。
        CsvStorage<Application> appStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);

        // 如果用户数据已经存在，说明环境已经初始化过，本次直接结束。
        if (!userStorage.loadAll().isEmpty()) {
            return;
        }

        // 组装基础用户列表，覆盖管理员、模块负责人和助教候选人三类角色。
        List<User> users = new ArrayList<>();
        users.add(createUser("user-admin-1", "admin", "admin123", User.UserRole.ADMIN));
        users.add(createUser("user-mo-1", "mo1", "mo123", User.UserRole.MO));
        users.add(createUser("user-mo-2", "mo2", "mo456", User.UserRole.MO));
        users.add(createUser("user-ta-1", "ta1", "ta123", User.UserRole.TA));
        users.add(createUser("user-ta-2", "ta2", "ta456", User.UserRole.TA));
        users.add(createUser("user-ta-3", "ta3", "ta789", User.UserRole.TA));
        // 一次性保存全部用户，保证后续 profile 和 job 能引用这些固定 ID。
        userStorage.saveAll(users);

        // 组装示例 TA 档案数据，用于展示资料维护与申请功能。
        List<TAProfile> profiles = new ArrayList<>();
        profiles.add(new TAProfile("prof-1", "user-ta-1", "Alice Zhang", "alice@bupt.edu.cn", "TA2026001", "Java;Spring;English", 3.8, "alice_cv.pdf"));
        profiles.add(new TAProfile("prof-2", "user-ta-2", "Bob Lee", "bob@bupt.edu.cn", "TA2026002", "Python;Machine Learning", 3.6, "bob_cv.pdf"));
        profiles.add(new TAProfile("prof-3", "user-ta-3", "Charlie Wang", "charlie@bupt.edu.cn", "TA2026003", "C++;Java;Algorithms", 3.9, "charlie_cv.docx"));
        // 持久化示例档案，使 TA 登录后可以直接看到预置资料。
        profileStorage.saveAll(profiles);

        // 组装示例岗位数据，覆盖开放岗位和关闭岗位两种状态。
        List<Job> jobs = new ArrayList<>();
        jobs.add(new Job("job-1", "user-mo-1", "Java Software Engineering TA", "EBU6304", "Assist with labs and marking for Java software engineering.", "Java;Spring", 20, Job.JobStatus.OPEN));
        jobs.add(new Job("job-2", "user-mo-1", "Database Systems TA", "EBU5302", "Support database assignments and tutorial Q&A.", "SQL;Java", 15, Job.JobStatus.OPEN));
        jobs.add(new Job("job-3", "user-mo-2", "Machine Learning TA", "EBU6001", "Help with Python ML practical labs.", "Python;Machine Learning", 25, Job.JobStatus.OPEN));
        jobs.add(new Job("job-4", "user-mo-2", "Algorithm Design TA", "EBU5001", "Support algorithm worksheet sessions.", "C++;Algorithms", 10, Job.JobStatus.CLOSED));
        // 保存岗位数据，为岗位浏览与申请流程提供可用数据。
        jobStorage.saveAll(jobs);

        // 组装示例申请数据，覆盖待处理、已录用和已拒绝三种状态。
        List<Application> applications = new ArrayList<>();
        applications.add(new Application("app-1", "user-ta-1", "job-1", Application.AppStatus.PENDING, System.currentTimeMillis() - 86400000L));
        applications.add(new Application("app-2", "user-ta-2", "job-3", Application.AppStatus.SELECTED, System.currentTimeMillis() - 172800000L));
        applications.add(new Application("app-3", "user-ta-3", "job-1", Application.AppStatus.REJECTED, System.currentTimeMillis() - 43200000L));
        // 最后写入申请数据，保证前面引用的用户和岗位都已经存在。
        appStorage.saveAll(applications);
    }

    private static User createUser(String id, String username, String password, User.UserRole role) {
        // 为当前用户生成独立盐值。
        String salt = PasswordUtil.generateSalt();
        // 将原始密码哈希后与盐值一起封装进 User 对象。
        return new User(id, username, PasswordUtil.hashPassword(password, salt), salt, role);
    }
}
