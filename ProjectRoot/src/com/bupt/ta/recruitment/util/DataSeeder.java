package com.bupt.ta.recruitment.util;

import com.bupt.ta.recruitment.model.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 演示数据生成器 (Task 8)
 * 预设 6 个用户 (Admin, MO, TA)、3 个 TA Profile、4 个 Job 和 4 个 Application。
 * 用于项目初期的快速测试和集成验证。
 */
public class DataSeeder {

    public static void main(String[] args) {
        System.out.println("====== 开始生成基础演示数据 (Data Seeder) ======");

        // 确保 data 目录存在
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // 1. 初始化 Storage
        CsvStorage<User> userStorage = new CsvStorage<>("data/users.csv", User::fromCsvRow);
        CsvStorage<TAProfile> profileStorage = new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);
        CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
        CsvStorage<Application> appStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);

        // 2. 生成 User (6个)
        List<User> users = new ArrayList<>();
        // Admin
        String saltAdmin = PasswordUtil.generateSalt();
        User admin = new User("user-admin-1", "admin", PasswordUtil.hashPassword("admin123", saltAdmin), saltAdmin, User.UserRole.ADMIN);
        users.add(admin);

        // MOs
        String saltMo1 = PasswordUtil.generateSalt();
        User mo1 = new User("user-mo-1", "mo1", PasswordUtil.hashPassword("mo123", saltMo1), saltMo1, User.UserRole.MO);
        
        String saltMo2 = PasswordUtil.generateSalt();
        User mo2 = new User("user-mo-2", "mo2", PasswordUtil.hashPassword("mo456", saltMo2), saltMo2, User.UserRole.MO);
        
        users.add(mo1);
        users.add(mo2);

        // TAs
        String saltTa1 = PasswordUtil.generateSalt();
        User ta1 = new User("user-ta-1", "ta1", PasswordUtil.hashPassword("ta123", saltTa1), saltTa1, User.UserRole.TA);
        
        String saltTa2 = PasswordUtil.generateSalt();
        User ta2 = new User("user-ta-2", "ta2", PasswordUtil.hashPassword("ta456", saltTa2), saltTa2, User.UserRole.TA);

        String saltTa3 = PasswordUtil.generateSalt();
        User ta3 = new User("user-ta-3", "ta3", PasswordUtil.hashPassword("ta789", saltTa3), saltTa3, User.UserRole.TA);

        users.add(ta1);
        users.add(ta2);
        users.add(ta3);

        userStorage.saveAll(users);
        System.out.println("-> Users.csv 生成完毕! (含 hash 密码)");

        // 3. 生成 TAProfile
        List<TAProfile> profiles = new ArrayList<>();
        profiles.add(new TAProfile("prof-1", "user-ta-1", "Alice Zhang", "alice@bupt.edu.cn", "TA2026001", "Java;Spring;English", 3.8, "alice_cv.pdf"));
        profiles.add(new TAProfile("prof-2", "user-ta-2", "Bob Lee", "bob@bupt.edu.cn", "TA2026002", "Python;Machine Learning", 3.6, "bob_cv.pdf"));
        profiles.add(new TAProfile("prof-3", "user-ta-3", "Charlie Wang", "charlie@bupt.edu.cn", "TA2026003", "C++;Java;Algorithms", 3.9, "charlie_cv.docx"));
        
        profileStorage.saveAll(profiles);
        System.out.println("-> TAProfiles.csv 生成完毕!");

        // 4. 生成 Job 岗位
        List<Job> jobs = new ArrayList<>();
        Job job1 = new Job("job-1", "user-mo-1", "Java Software Engineering TA", "EBU6304", "Assist with labs and marking for Java SW Engineering", "Java;Spring", 20, Job.JobStatus.OPEN);
        Job job2 = new Job("job-2", "user-mo-1", "Database Systems TA", "EBU5302", "Manage database assignments", "SQL;Java", 15, Job.JobStatus.OPEN);
        Job job3 = new Job("job-3", "user-mo-2", "Machine Learning TA", "EBU6001", "Assist in ML Python labs", "Python;Machine Learning", 25, Job.JobStatus.OPEN);
        Job job4 = new Job("job-4", "user-mo-2", "Algorithm Design TA", "EBU5001", "Marking for Algo tests", "C++;Algorithms", 10, Job.JobStatus.CLOSED);

        jobs.add(job1);
        jobs.add(job2);
        jobs.add(job3);
        jobs.add(job4);

        jobStorage.saveAll(jobs);
        System.out.println("-> Jobs.csv 生成完毕!");

        // 5. 生成 Application 记录
        List<Application> apps = new ArrayList<>();
        // TA1 -> Job1 (待审核)
        apps.add(new Application("app-1", "user-ta-1", "job-1", Application.AppStatus.PENDING, System.currentTimeMillis() - 86400000L));
        // TA2 -> Job3 (被录用)
        apps.add(new Application("app-2", "user-ta-2", "job-3", Application.AppStatus.SELECTED, System.currentTimeMillis() - 172800000L));
        // TA3 -> Job1 (被拒绝)
        apps.add(new Application("app-3", "user-ta-3", "job-1", Application.AppStatus.REJECTED, System.currentTimeMillis() - 43200000L));
        // TA3 -> Job4 (已申请但岗位关闭)
        apps.add(new Application("app-4", "user-ta-3", "job-4", Application.AppStatus.PENDING, System.currentTimeMillis() - 10000L));

        appStorage.saveAll(apps);
        System.out.println("-> Applications.csv 生成完毕!");

        System.out.println("====== 所有演示数据生成配置已成功写入文件! ======");
    }
}
