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
 * L3 Pair B deliverable.
 *
 * <p>This project does not yet expose the L3 flows through a dedicated service layer, so this
 * test exercises the same persistence and validation rules currently implemented in the dashboard
 * classes. TA-side flows are executable; MO-side flows are reported as blocked because the current
 * repository only contains UI skeletons for them.
 */
public class BusinessLogicTest {
    private static int passed = 0;
    private static int failed = 0;
    private static int skipped = 0;

    private static File tempDir;
    private static CsvStorage<TAProfile> profileStorage;
    private static CsvStorage<Job> jobStorage;
    private static CsvStorage<Application> applicationStorage;

    public static void main(String[] args) {
        System.out.println("========== BusinessLogicTest (L3 Pair B) ==========");
        try {
            testProfileRejectsInvalidEmail();
            testProfileRejectsInvalidGpa();
            testProfileSaveCreatesAndUpdatesProfile();
            testBrowseOnlyReturnsOpenJobs();
            testBrowseSupportsModuleAndSkillFilters();
            testApplyCreatesPendingApplication();
            testApplyRejectsDuplicateApplication();
            testApplicationStatusViewDataKeepsAllThreeStates();
            skipBlockedMoPostingFlow();
            skipBlockedMoDecisionFlow();
        } finally {
            tearDown();
        }

        System.out.println();
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Skipped: " + skipped);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void setUp() {
        tearDown();
        tempDir = new File("data/test_l3_pairb");
        tempDir.mkdirs();
        profileStorage = new CsvStorage<>(new File(tempDir, "profiles.csv").getPath(), TAProfile::fromCsvRow);
        jobStorage = new CsvStorage<>(new File(tempDir, "jobs.csv").getPath(), Job::fromCsvRow);
        applicationStorage = new CsvStorage<>(new File(tempDir, "applications.csv").getPath(), Application::fromCsvRow);
        profileStorage.saveAll(new ArrayList<>());
        jobStorage.saveAll(new ArrayList<>());
        applicationStorage.saveAll(new ArrayList<>());
    }

    private static void tearDown() {
        if (tempDir != null && tempDir.exists()) {
            deleteRecursively(tempDir);
            tempDir = null;
        }
    }

    private static void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
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
        skipped++;
        System.out.println("  [SKIP] US-5 MO posting flow is blocked: MODashboard has no publish/close action or service-layer API yet.");
    }

    private static void skipBlockedMoDecisionFlow() {
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
        if (isBlank(fullName) || isBlank(email) || isBlank(studentId) || isBlank(skills) || isBlank(gpaText) || isBlank(cvPath)) {
            return ValidationResult.failure("All profile fields are required.");
        }
        if (!UIHelper.isValidEmail(email)) {
            return ValidationResult.failure("Invalid email.");
        }
        if (!UIHelper.isValidGpa(gpaText)) {
            return ValidationResult.failure("Invalid GPA.");
        }
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
        ValidationResult validation = validateProfile(fullName, email, studentId, skills, gpaText, cvPath);
        if (!validation.success) {
            return validation;
        }

        List<TAProfile> profiles = profileStorage.loadAll();
        TAProfile target = null;
        for (TAProfile profile : profiles) {
            if (userId.equals(profile.getUserId())) {
                target = profile;
                break;
            }
        }
        if (target == null) {
            target = new TAProfile();
            target.setId(java.util.UUID.randomUUID().toString());
            target.setUserId(userId);
            profiles.add(target);
        }

        target.setFullName(fullName);
        target.setEmail(email);
        target.setStudentId(studentId);
        target.setSkills(skills);
        target.setGpa(Double.parseDouble(gpaText));
        target.setCvPath(cvPath);
        profileStorage.saveAll(profiles);
        return ValidationResult.success();
    }

    private static List<Job> browseJobs(String moduleKeyword, String skillKeyword) {
        List<Job> result = new ArrayList<>();
        String normalizedModule = moduleKeyword == null ? "" : moduleKeyword.trim().toLowerCase();
        String normalizedSkill = skillKeyword == null ? "" : skillKeyword.trim().toLowerCase();

        for (Job job : jobStorage.loadAll()) {
            if (job.getStatus() != Job.JobStatus.OPEN) {
                continue;
            }
            boolean moduleMatched = normalizedModule.isEmpty() || job.getModule().toLowerCase().contains(normalizedModule);
            boolean skillMatched = normalizedSkill.isEmpty() || job.getRequiredSkills().toLowerCase().contains(normalizedSkill);
            if (moduleMatched && skillMatched) {
                result.add(job);
            }
        }
        return result;
    }

    private static ApplyResult applyForJob(String taId, String jobId) {
        boolean hasProfile = false;
        for (TAProfile profile : profileStorage.loadAll()) {
            if (taId.equals(profile.getUserId())) {
                hasProfile = true;
                break;
            }
        }
        if (!hasProfile) {
            return ApplyResult.failure("Profile required.");
        }

        for (Application application : applicationStorage.loadAll()) {
            if (taId.equals(application.getTaId()) && jobId.equals(application.getJobId())) {
                return ApplyResult.failure("Duplicate application.");
            }
        }

        List<Application> applications = applicationStorage.loadAll();
        applications.add(new Application(taId, jobId, Application.AppStatus.PENDING));
        applicationStorage.saveAll(applications);
        return ApplyResult.success();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void assertEquals(String label, Object expected, Object actual) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        if (ok) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            failed++;
            System.out.println("  [FAIL] " + label + " expected=<" + expected + "> actual=<" + actual + ">");
        }
    }

    private static void assertTrue(String label, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            failed++;
            System.out.println("  [FAIL] " + label + " expected=true actual=false");
        }
    }

    private static void assertFalse(String label, boolean condition) {
        assertTrue(label, !condition);
    }

    private static final class ValidationResult {
        private final boolean success;
        private final String message;

        private ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        private static ValidationResult success() {
            return new ValidationResult(true, "OK");
        }

        private static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }
    }

    private static final class ApplyResult {
        private final boolean success;
        private final String message;

        private ApplyResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        private static ApplyResult success() {
            return new ApplyResult(true, "OK");
        }

        private static ApplyResult failure(String message) {
            return new ApplyResult(false, message);
        }
    }
}
