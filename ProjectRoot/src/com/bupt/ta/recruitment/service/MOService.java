package com.bupt.ta.recruitment.service;

import com.bupt.ta.recruitment.model.Application;
import com.bupt.ta.recruitment.model.Job;
import com.bupt.ta.recruitment.model.TAProfile;
import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.util.CsvStorage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Module Organiser (MO) operations.
 * Handles the business logic for Task 5, 6, and 7.
 */
public class MOService {
    // ==========================================
    // 1. 数据存储层依赖注入 (Storage Dependencies)
    // ==========================================
    
    // 初始化四个核心数据表的存储引擎。
    // 使用泛型 <T> 保证类型安全，并传入对应 Model 类的 fromCsvRow 静态方法引用，用于自动反序列化。
    private final CsvStorage<Job> jobStorage = new CsvStorage<>("data/jobs.csv", Job::fromCsvRow);
    private final CsvStorage<Application> applicationStorage = new CsvStorage<>("data/applications.csv", Application::fromCsvRow);
    private final CsvStorage<User> userStorage = new CsvStorage<>("data/users.csv", User::fromCsvRow);
    private final CsvStorage<TAProfile> profileStorage = new CsvStorage<>("data/profiles.csv", TAProfile::fromCsvRow);

    // ==========================================
    // 2. 岗位管理相关业务 (Job Management)
    // ==========================================

    /**
     * 获取指定 MO 账号发布的所有岗位记录。
     * * @param moId 当前登录的 MO 的用户 ID
     * @return 该 MO 发布的所有 Job 列表
     */
    public List<Job> getJobsByMo(String moId) {
        return jobStorage.loadAll().stream()
                .filter(job -> moId.equals(job.getMoId()))
                .collect(Collectors.toList());
    }

    /**
     * 发布新的 TA 招聘岗位。
     * * @param job UI 层封装好的、包含完整岗位信息的 Job 对象
     * @throws Exception 文件读写异常
     */
    public void postJob(Job job) throws Exception {
        List<Job> jobs = jobStorage.loadAll();// 步骤 1：从 CSV 文件中全量加载当前所有的岗位数据
        jobs.add(job);// 步骤 2：将新创建的岗位添加到内存列表的末尾
        jobStorage.saveAll(jobs);// 步骤 3：将更新后的列表整体覆盖写回 CSV 文件，完成持久化
    }

    // ==========================================
    // 3. 申请者审批相关业务 (Application Management)
    // ==========================================

    /**
     * 关闭指定的岗位（将状态变更为 CLOSED）。
     * * @param jobId 需要关闭的岗位 ID
     * @throws Exception 文件读写异常
     */
    public void closeJob(String jobId) throws Exception {
        List<Job> jobs = jobStorage.loadAll();
        for (Job job : jobs) {
            if (job.getId().equals(jobId)) {
                // 使用 Job.JobStatus 枚举
                job.setStatus(Job.JobStatus.CLOSED);
                break;
            }
        }
        jobStorage.saveAll(jobs);
    }

    // ==========================================
    // 3. 申请者审批相关业务 (Application Management)
    // ==========================================

    /**
     * 根据岗位 ID 获取该岗位下的所有申请记录。
     * * @param jobId 目标岗位 ID
     * @return 针对该岗位的 Application 列表
     */
    public List<Application> getApplicationsByMoJob(String jobId) {
        return applicationStorage.loadAll().stream()
                .filter(app -> app.getJobId().equals(jobId))
                .collect(Collectors.toList());
    }

    /**
     * 根据 User ID 获取用户的底层账号信息（如 Username 等）。
     * * @param userId 目标用户 ID
     * @return User 对象
     */
    public User getUserById(String userId) {
        return userStorage.findById(userId, User::getId);
    }

    /**
     * 根据 User ID 获取 TA 的个人简历资料（包含 Skills, Email 等）。
     * * @param userId 目标 TA 的用户 ID
     * @return TAProfile 对象，若未找到则返回 null
     */
    public TAProfile getProfileByUserId(String userId) {
        return profileStorage.loadAll().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst().orElse(null);
    }

    /**
     * 更新某条申请记录的审核状态（如 批准 / 拒绝）。
     * * @param appId  目标申请记录的 ID
     * @param status 期望变更为的新状态（字符串形式，需与枚举名一致）
     * @throws Exception 文件读写异常或枚举转换异常
     */
    public void updateApplicationStatus(String appId, String status) throws Exception {
        List<Application> apps = applicationStorage.loadAll();
        for (Application app : apps) {
            if (app.getId().equals(appId)) {
                // 将字符串转换为 Application.AppStatus 枚举
                app.setStatus(Application.AppStatus.valueOf(status));
                break;
            }
        }
        applicationStorage.saveAll(apps);
    }
}
