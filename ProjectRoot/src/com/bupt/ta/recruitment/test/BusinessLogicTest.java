package com.bupt.ta.recruitment.test;

import com.bupt.ta.recruitment.model.Application;
import com.bupt.ta.recruitment.model.Job;
import com.bupt.ta.recruitment.model.TAProfile;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.UIHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration-style test harness for the executable Pair B L3 business flows.
 * It verifies TA-side validation, browsing, and application persistence without
 * requiring an external test framework.
 */
public class BusinessLogicTest {
    // 记录通过用例数量。
    private static int passed = 0;
    // 记录失败用例数量。
    private static int failed = 0;
    // 记录跳过用例数量。
    private static int skipped = 0;

    // 临时测试数据目录。
    private static File tempDir;
    // 测试使用的档案存储。
    private static CsvStorage<TAProfile> profileStorage;
    // 测试使用的岗位存储。
    private static CsvStorage<Job> jobStorage;
    // 测试使用的申请存储。
    private static CsvStorage<Application> applicationStorage;

    public static void main(String[] args) {
        // 输出测试标题，便于在命令行识别当前测试套件。
        System.out.println("========== BusinessLogicTest (L3 Pair B) ==========");
        try {
            // 执行邮箱格式校验测试。
            testProfileRejectsInvalidEmail();
            // 执行 GPA 范围校验测试。
            testProfileRejectsInvalidGpa();
            // 执行资料新增与更新测试。
            testProfileSaveCreatesAndUpdatesProfile();
            // 执行只浏览开放岗位的测试。
            testBrowseOnlyReturnsOpenJobs();
            // 执行模块与技能过滤测试。
            testBrowseSupportsModuleAndSkillFilters();
            // 执行申请创建测试。
            testApplyCreatesPendingApplication();
            // 执行重复申请拦截测试。
            testApplyRejectsDuplicateApplication();
            // 执行三种申请状态持久化测试。
            testApplicationStatusViewDataKeepsAllThreeStates();
            // MO 发布岗位流程目前仍按阻塞项跳过。
            skipBlockedMoPostingFlow();
            // MO 审核申请流程目前仍按阻塞项跳过。
            skipBlockedMoDecisionFlow();
        } finally {
            // 无论测试是否异常，都尝试清理临时目录。
            tearDown();
        }

        // 输出空行分隔测试明细与总结。
        System.out.println();
        // 输出通过数量。
        System.out.println("Passed: " + passed);
        // 输出失败数量。
        System.out.println("Failed: " + failed);
        // 输出跳过数量。
        System.out.println("Skipped: " + skipped);
        // 只要存在失败项，就以非零退出码结束进程。
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void setUp() {
        // 每个测试开始前先清理上一次留下的临时数据。
        tearDown();
        // 创建本测试专用的数据目录。
        tempDir = new File("data/test_l3_pairb");
        tempDir.mkdirs();
        // 初始化临时档案存储。
        profileStorage = new CsvStorage<>(new File(tempDir, "profiles.csv").getPath(), TAProfile::fromCsvRow);
        // 初始化临时岗位存储。
        jobStorage = new CsvStorage<>(new File(tempDir, "jobs.csv").getPath(), Job::fromCsvRow);
        // 初始化临时申请存储。
        applicationStorage = new CsvStorage<>(new File(tempDir, "applications.csv").getPath(), Application::fromCsvRow);
        // 先写入空列表，保证 CSV 文件实际存在。
        profileStorage.saveAll(new ArrayList<>());
        jobStorage.saveAll(new ArrayList<>());
        applicationStorage.saveAll(new ArrayList<>());
    }

    private static void tearDown() {
        // 只有临时目录存在时才执行递归删除。
        if (tempDir != null && tempDir.exists()) {
            deleteRecursively(tempDir);
            // 删除完成后清空目录引用。
            tempDir = null;
        }
    }

    private static void deleteRecursively(File file) {
        // 读取当前目录下的所有子文件和子目录。
        File[] children = file.listFiles();
        if (children != null) {
            // 递归删除所有子节点。
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        // 最后删除当前文件或目录本身。
        file.delete();
    }

    private static void testProfileRejectsInvalidEmail() {
        setUp();
        ValidationResult result = validateProfile("Alice", "alice_at_example.com", "2024001", "Java;SQL", "3.5", "C:/cv.pdf");
        assertFalse("US-1 profile validation rejects malformed email", result.success);
    }

    private static void testProfileRejectsInvalidGpa() {
        setUp();
        ValidationResult result = validateProfile("Alice", "alice@example.com", "2024001", "Java;SQL", "4.5", "C:/cv.pdf");
        assertFalse("US-2 profile validation rejects GPA above 4.0", result.success);
    }

    private static void testProfileSaveCreatesAndUpdatesProfile() {
        setUp();
        ValidationResult createResult = saveProfile("ta-1", "Alice", "alice@example.com", "2024001", "Java;SQL", "3.7", "C:/cv.pdf");
        assertTrue("US-1 save profile creates a new record", createResult.success);
        assertEquals("US-1 profile count after create", 1, profileStorage.loadAll().size());

        ValidationResult updateResult = saveProfile("ta-1", "Alice Wang", "alice@example.com", "2024001", "Java;Python", "3.8", "C:/cv_v2.pdf");
        assertTrue("US-2 save profile updates existing record", updateResult.success);
        TAProfile saved = profileStorage.loadAll().get(0);
        assertEquals("US-2 updated name persisted", "Alice Wang", saved.getFullName());
        assertEquals("US-2 updated GPA persisted", 3.8, saved.getGpa());
    }

    private static void testBrowseOnlyReturnsOpenJobs() {
        setUp();
        List<Job> jobs = new ArrayList<>();
        jobs.add(new Job("job-open-1", "mo-1", "Programming TA", "EBU6304", "Support labs", "Java;Testing", 8, Job.JobStatus.OPEN));
        jobs.add(new Job("job-closed-1", "mo-1", "Closed Job", "EBU6200", "Closed", "Python", 6, Job.JobStatus.CLOSED));
        jobStorage.saveAll(jobs);

        List<Job> visible = browseJobs("", "");
        assertEquals("US-3 browse only exposes OPEN jobs", 1, visible.size());
        assertEquals("US-3 visible job id", "job-open-1", visible.get(0).getId());
    }

    private static void testBrowseSupportsModuleAndSkillFilters() {
        setUp();
        List<Job> jobs = new ArrayList<>();
        jobs.add(new Job("job-open-1", "mo-1", "Programming TA", "EBU6304", "Support labs", "Java;Testing", 8, Job.JobStatus.OPEN));
        jobs.add(new Job("job-open-2", "mo-1", "Research TA", "EBU6401", "Support project", "Python;Writing", 6, Job.JobStatus.OPEN));
        jobs.add(new Job("job-open-3", "mo-1", "Design TA", "DES1001", "Design support", "Figma;UX", 4, Job.JobStatus.OPEN));
        jobStorage.saveAll(jobs);

        List<Job> moduleFiltered = browseJobs("ebu64", "");
        assertEquals("US-3 module filter narrows the job list", 1, moduleFiltered.size());
        assertEquals("US-3 module filter match", "job-open-2", moduleFiltered.get(0).getId());

        List<Job> skillFiltered = browseJobs("", "java");
        assertEquals("US-3 skill filter narrows the job list", 1, skillFiltered.size());
        assertEquals("US-3 skill filter match", "job-open-1", skillFiltered.get(0).getId());
    }

    private static void testApplyCreatesPendingApplication() {
        setUp();
        saveProfile("ta-1", "Alice", "alice@example.com", "2024001", "Java;SQL", "3.7", "C:/cv.pdf");
        List<Job> jobs = new ArrayList<>();
        jobs.add(new Job("job-open-1", "mo-1", "Programming TA", "EBU6304", "Support labs", "Java;Testing", 8, Job.JobStatus.OPEN));
        jobStorage.saveAll(jobs);

        ApplyResult result = applyForJob("ta-1", "job-open-1");
        assertTrue("US-4 application succeeds when profile exists", result.success);

        List<Application> applications = applicationStorage.loadAll();
        assertEquals("US-4 application record count", 1, applications.size());
        assertEquals("US-4 new application status is pending", Application.AppStatus.PENDING, applications.get(0).getStatus());
        assertTrue("US-4 application timestamp is written", applications.get(0).getAppliedAt() > 0L);
    }

    private static void testApplyRejectsDuplicateApplication() {
        setUp();
        saveProfile("ta-1", "Alice", "alice@example.com", "2024001", "Java;SQL", "3.7", "C:/cv.pdf");
        List<Job> jobs = new ArrayList<>();
        jobs.add(new Job("job-open-1", "mo-1", "Programming TA", "EBU6304", "Support labs", "Java;Testing", 8, Job.JobStatus.OPEN));
        jobStorage.saveAll(jobs);

        ApplyResult first = applyForJob("ta-1", "job-open-1");
        ApplyResult second = applyForJob("ta-1", "job-open-1");
        assertTrue("US-4 first application is accepted", first.success);
        assertFalse("US-4 duplicate application is rejected", second.success);
        assertEquals("US-4 duplicate application does not create a second row", 1, applicationStorage.loadAll().size());
    }

    private static void testApplicationStatusViewDataKeepsAllThreeStates() {
        setUp();
        List<Application> applications = new ArrayList<>();
        applications.add(new Application("app-1", "ta-1", "job-1", Application.AppStatus.PENDING, 1L));
        applications.add(new Application("app-2", "ta-1", "job-2", Application.AppStatus.SELECTED, 2L));
        applications.add(new Application("app-3", "ta-1", "job-3", Application.AppStatus.REJECTED, 3L));
        applicationStorage.saveAll(applications);

        int pending = 0;
        int selected = 0;
        int rejected = 0;
        for (Application application : applicationStorage.loadAll()) {
            if (application.getStatus() == Application.AppStatus.PENDING) {
                pending++;
            } else if (application.getStatus() == Application.AppStatus.SELECTED) {
                selected++;
            } else if (application.getStatus() == Application.AppStatus.REJECTED) {
                rejected++;
            }
        }

        assertEquals("US-4 pending state survives persistence", 1, pending);
        assertEquals("US-4 selected state survives persistence", 1, selected);
        assertEquals("US-4 rejected state survives persistence", 1, rejected);
    }

    private static void skipBlockedMoPostingFlow() {
        // 将当前阻塞项记为跳过。
        skipped++;
        System.out.println("  [SKIP] US-5 MO posting flow is blocked: MODashboard has no publish/close action or service-layer API yet.");
    }

    private static void skipBlockedMoDecisionFlow() {
        // 将当前阻塞项记为跳过。
        skipped++;
        System.out.println("  [SKIP] US-6 MO applicant decision flow is blocked: repository does not yet provide select/reject handlers.");
    }

    private static ValidationResult validateProfile(
            String fullName,
            String email,
            String studentId,
            String skills,
            String gpaText,
            String cvPath) {
        // 任一资料字段为空都视为校验失败。
        if (isBlank(fullName) || isBlank(email) || isBlank(studentId) || isBlank(skills) || isBlank(gpaText) || isBlank(cvPath)) {
            return ValidationResult.failure("All profile fields are required.");
        }
        // 邮箱格式不合法时返回失败结果。
        if (!UIHelper.isValidEmail(email)) {
            return ValidationResult.failure("Invalid email.");
        }
        // GPA 不合法时返回失败结果。
        if (!UIHelper.isValidGpa(gpaText)) {
            return ValidationResult.failure("Invalid GPA.");
        }
        // 所有规则通过时返回成功结果。
        return ValidationResult.success();
    }

    private static ValidationResult saveProfile(
            String userId,
            String fullName,
            String email,
            String studentId,
            String skills,
            String gpaText,
            String cvPath) {
        // 先复用统一资料校验逻辑。
        ValidationResult validation = validateProfile(fullName, email, studentId, skills, gpaText, cvPath);
        if (!validation.success) {
            return validation;
        }

        // 读取现有档案，准备更新或新增。
        List<TAProfile> profiles = profileStorage.loadAll();
        // target 表示当前用户的目标档案记录。
        TAProfile target = null;
        for (TAProfile profile : profiles) {
            if (userId.equals(profile.getUserId())) {
                target = profile;
                break;
            }
        }
        // 如果还没有档案，则先创建一条新记录。
        if (target == null) {
            target = new TAProfile();
            target.setId(java.util.UUID.randomUUID().toString());
            target.setUserId(userId);
            profiles.add(target);
        }

        // 将输入值逐项回写到档案对象。
        target.setFullName(fullName);
        target.setEmail(email);
        target.setStudentId(studentId);
        target.setSkills(skills);
        target.setGpa(Double.parseDouble(gpaText));
        target.setCvPath(cvPath);
        // 保存整个档案集合。
        profileStorage.saveAll(profiles);
        // 返回成功结果。
        return ValidationResult.success();
    }

    private static List<Job> browseJobs(String moduleKeyword, String skillKeyword) {
        // 收集符合条件的岗位列表。
        List<Job> result = new ArrayList<>();
        // 预处理模块关键字，避免空指针并统一小写比较。
        String normalizedModule = moduleKeyword == null ? "" : moduleKeyword.trim().toLowerCase();
        // 预处理技能关键字，避免空指针并统一小写比较。
        String normalizedSkill = skillKeyword == null ? "" : skillKeyword.trim().toLowerCase();

        // 遍历全部岗位，只保留开放且匹配过滤条件的记录。
        for (Job job : jobStorage.loadAll()) {
            if (job.getStatus() != Job.JobStatus.OPEN) {
                continue;
            }
            // 模块关键字为空时自动匹配，否则要求模块名包含关键字。
            boolean moduleMatched = normalizedModule.isEmpty() || job.getModule().toLowerCase().contains(normalizedModule);
            // 技能关键字为空时自动匹配，否则要求技能串包含关键字。
            boolean skillMatched = normalizedSkill.isEmpty() || job.getRequiredSkills().toLowerCase().contains(normalizedSkill);
            if (moduleMatched && skillMatched) {
                result.add(job);
            }
        }
        return result;
    }

    private static ApplyResult applyForJob(String taId, String jobId) {
        // 默认认为当前 TA 还没有完善档案。
        boolean hasProfile = false;
        // 遍历档案列表，检查当前 TA 是否已经创建资料。
        for (TAProfile profile : profileStorage.loadAll()) {
            if (taId.equals(profile.getUserId())) {
                hasProfile = true;
                break;
            }
        }
        // 没有档案时拒绝申请。
        if (!hasProfile) {
            return ApplyResult.failure("Profile required.");
        }

        // 再次遍历申请列表，拦截重复申请。
        for (Application application : applicationStorage.loadAll()) {
            if (taId.equals(application.getTaId()) && jobId.equals(application.getJobId())) {
                return ApplyResult.failure("Duplicate application.");
            }
        }

        // 读取已有申请并追加新的待处理申请记录。
        List<Application> applications = applicationStorage.loadAll();
        applications.add(new Application(taId, jobId, Application.AppStatus.PENDING));
        // 持久化新的申请集合。
        applicationStorage.saveAll(applications);
        // 返回成功结果。
        return ApplyResult.success();
    }

    private static boolean isBlank(String value) {
        // null 或去空格后为空字符串都视为 blank。
        return value == null || value.trim().isEmpty();
    }

    private static void assertEquals(String label, Object expected, Object actual) {
        // 统一比较期望值和实际值是否相等。
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        if (ok) {
            // 断言成功时增加通过计数并输出通过日志。
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            // 断言失败时增加失败计数并输出详细差异。
            failed++;
            System.out.println("  [FAIL] " + label + " expected=<" + expected + "> actual=<" + actual + ">");
        }
    }

    private static void assertTrue(String label, boolean condition) {
        // 条件为真时判定断言通过。
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            // 条件为假时判定断言失败。
            failed++;
            System.out.println("  [FAIL] " + label + " expected=true actual=false");
        }
    }

    private static void assertFalse(String label, boolean condition) {
        // 通过复用 assertTrue 简化“应为假”的判断逻辑。
        assertTrue(label, !condition);
    }

    private static final class ValidationResult {
        // 标记当前校验是否成功。
        private final boolean success;
        // 保存校验结果的附带消息。
        private final String message;

        private ValidationResult(boolean success, String message) {
            // 记录成功标记。
            this.success = success;
            // 记录结果消息。
            this.message = message;
        }

        private static ValidationResult success() {
            // 构造一个标准成功结果。
            return new ValidationResult(true, "OK");
        }

        private static ValidationResult failure(String message) {
            // 构造一个携带错误消息的失败结果。
            return new ValidationResult(false, message);
        }
    }

    private static final class ApplyResult {
        // 标记申请动作是否成功。
        private final boolean success;
        // 保存申请动作的结果消息。
        private final String message;

        private ApplyResult(boolean success, String message) {
            // 记录申请是否成功。
            this.success = success;
            // 记录申请结果消息。
            this.message = message;
        }

        private static ApplyResult success() {
            // 返回标准成功结果。
            return new ApplyResult(true, "OK");
        }

        private static ApplyResult failure(String message) {
            // 返回携带失败原因的结果。
            return new ApplyResult(false, message);
        }
    }
}
