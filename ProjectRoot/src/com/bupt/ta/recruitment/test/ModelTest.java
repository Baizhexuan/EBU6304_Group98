package com.bupt.ta.recruitment.test;

import com.bupt.ta.recruitment.model.*;

/**
 * L1 Pair C — 数据模型 JUnit 风格测试
 * 
 * 覆盖四个模型类：User, TAProfile, Job, Application
 * 每个模型 ≥ 3 个用例（构造、序列化 toCsvRow、反序列化 fromCsvRow、equals）
 *
 * 因项目未引入 JUnit 依赖，采用简易断言方式运行，效果等价。
 */
public class ModelTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========== ModelTest 开始 ==========\n");

        // --- User 测试 ---
        testUserConstruction();
        testUserToCsvRow();
        testUserFromCsvRow();
        testUserEquals();
        testUserFromCsvRowInvalid();

        // --- TAProfile 测试 ---
        testTAProfileConstruction();
        testTAProfileToCsvRow();
        testTAProfileFromCsvRow();
        testTAProfileEquals();
        testTAProfileFromCsvRowInvalidGpa();

        // --- Job 测试 ---
        testJobConstruction();
        testJobToCsvRow();
        testJobFromCsvRow();
        testJobEquals();
        testJobFromCsvRowInvalid();

        // --- Application 测试 ---
        testApplicationConstruction();
        testApplicationToCsvRow();
        testApplicationFromCsvRow();
        testApplicationEquals();
        testApplicationFromCsvRowInvalid();

        System.out.println("\n========== ModelTest 结束 ==========");
        System.out.println("通过: " + passed + "  失败: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ============================================================
    //  User Tests
    // ============================================================

    private static void testUserConstruction() {
        User u = new User("id1", "alice", "hash123", "salt456", User.UserRole.TA);
        assertEqual("User-构造-id", "id1", u.getId());
        assertEqual("User-构造-username", "alice", u.getUsername());
        assertEqual("User-构造-passwordHash", "hash123", u.getPasswordHash());
        assertEqual("User-构造-salt", "salt456", u.getSalt());
        assertEqual("User-构造-role", User.UserRole.TA, u.getRole());
    }

    private static void testUserToCsvRow() {
        User u = new User("id1", "alice", "hash123", "salt456", User.UserRole.TA);
        String csv = u.toCsvRow();
        assertEqual("User-toCsvRow", "id1,alice,hash123,salt456,TA", csv);
    }

    private static void testUserFromCsvRow() {
        String csv = "id1,alice,hash123,salt456,TA";
        User u = User.fromCsvRow(csv);
        assertNotNull("User-fromCsvRow-notNull", u);
        assertEqual("User-fromCsvRow-id", "id1", u.getId());
        assertEqual("User-fromCsvRow-username", "alice", u.getUsername());
        assertEqual("User-fromCsvRow-role", User.UserRole.TA, u.getRole());
    }

    private static void testUserEquals() {
        User u1 = new User("id1", "alice", "h1", "s1", User.UserRole.TA);
        User u2 = new User("id1", "bob", "h2", "s2", User.UserRole.MO);
        User u3 = new User("id2", "alice", "h1", "s1", User.UserRole.TA);
        assertTrue("User-equals-sameId", u1.equals(u2));
        assertFalse("User-equals-diffId", u1.equals(u3));
    }

    private static void testUserFromCsvRowInvalid() {
        // 字段不足应返回 null
        User u = User.fromCsvRow("only,three,fields");
        assertNull("User-fromCsvRow-invalid", u);
    }

    // ============================================================
    //  TAProfile Tests
    // ============================================================

    private static void testTAProfileConstruction() {
        TAProfile p = new TAProfile("p1", "u1", "Alice Zhang", "alice@bupt.edu.cn",
                "2026001", "Java;Python", 3.8, "cv.pdf");
        assertEqual("TAProfile-构造-id", "p1", p.getId());
        assertEqual("TAProfile-构造-userId", "u1", p.getUserId());
        assertEqual("TAProfile-构造-fullName", "Alice Zhang", p.getFullName());
        assertEqual("TAProfile-构造-email", "alice@bupt.edu.cn", p.getEmail());
        assertEqual("TAProfile-构造-studentId", "2026001", p.getStudentId());
        assertEqual("TAProfile-构造-skills", "Java;Python", p.getSkills());
        assertTrue("TAProfile-构造-gpa", Math.abs(p.getGpa() - 3.8) < 0.001);
        assertEqual("TAProfile-构造-cvPath", "cv.pdf", p.getCvPath());
    }

    private static void testTAProfileToCsvRow() {
        TAProfile p = new TAProfile("p1", "u1", "Alice Zhang", "alice@bupt.edu.cn",
                "2026001", "Java;Python", 3.8, "cv.pdf");
        String csv = p.toCsvRow();
        assertEqual("TAProfile-toCsvRow", "p1,u1,Alice Zhang,alice@bupt.edu.cn,2026001,Java;Python,3.8,cv.pdf", csv);
    }

    private static void testTAProfileFromCsvRow() {
        String csv = "p1,u1,Alice Zhang,alice@bupt.edu.cn,2026001,Java;Python,3.8,cv.pdf";
        TAProfile p = TAProfile.fromCsvRow(csv);
        assertNotNull("TAProfile-fromCsvRow-notNull", p);
        assertEqual("TAProfile-fromCsvRow-id", "p1", p.getId());
        assertEqual("TAProfile-fromCsvRow-fullName", "Alice Zhang", p.getFullName());
        assertTrue("TAProfile-fromCsvRow-gpa", Math.abs(p.getGpa() - 3.8) < 0.001);
    }

    private static void testTAProfileEquals() {
        TAProfile p1 = new TAProfile("p1", "u1", "A", "a@b", "001", "Java", 3.0, "cv");
        TAProfile p2 = new TAProfile("p1", "u2", "B", "b@b", "002", "Python", 3.5, "cv2");
        TAProfile p3 = new TAProfile("p2", "u1", "A", "a@b", "001", "Java", 3.0, "cv");
        assertTrue("TAProfile-equals-sameId", p1.equals(p2));
        assertFalse("TAProfile-equals-diffId", p1.equals(p3));
    }

    private static void testTAProfileFromCsvRowInvalidGpa() {
        // GPA 不是数字时应返回 null
        String csv = "p1,u1,A,a@b,001,Java,NOT_A_NUMBER,cv";
        TAProfile p = TAProfile.fromCsvRow(csv);
        assertNull("TAProfile-fromCsvRow-invalidGpa", p);
    }

    // ============================================================
    //  Job Tests
    // ============================================================

    private static void testJobConstruction() {
        Job j = new Job("j1", "mo1", "Java TA", "EBU6304", "Assist labs",
                "Java;Spring", 20, Job.JobStatus.OPEN);
        assertEqual("Job-构造-id", "j1", j.getId());
        assertEqual("Job-构造-moId", "mo1", j.getMoId());
        assertEqual("Job-构造-title", "Java TA", j.getTitle());
        assertEqual("Job-构造-module", "EBU6304", j.getModule());
        assertEqual("Job-构造-description", "Assist labs", j.getDescription());
        assertEqual("Job-构造-requiredSkills", "Java;Spring", j.getRequiredSkills());
        assertEqual("Job-构造-maxHours", 20, j.getMaxHours());
        assertEqual("Job-构造-status", Job.JobStatus.OPEN, j.getStatus());
    }

    private static void testJobToCsvRow() {
        Job j = new Job("j1", "mo1", "Java TA", "EBU6304", "Assist labs",
                "Java;Spring", 20, Job.JobStatus.OPEN);
        String csv = j.toCsvRow();
        assertEqual("Job-toCsvRow", "j1,mo1,Java TA,EBU6304,Assist labs,Java;Spring,20,OPEN", csv);
    }

    private static void testJobFromCsvRow() {
        String csv = "j1,mo1,Java TA,EBU6304,Assist labs,Java;Spring,20,OPEN";
        Job j = Job.fromCsvRow(csv);
        assertNotNull("Job-fromCsvRow-notNull", j);
        assertEqual("Job-fromCsvRow-id", "j1", j.getId());
        assertEqual("Job-fromCsvRow-title", "Java TA", j.getTitle());
        assertEqual("Job-fromCsvRow-maxHours", 20, j.getMaxHours());
        assertEqual("Job-fromCsvRow-status", Job.JobStatus.OPEN, j.getStatus());
    }

    private static void testJobEquals() {
        Job j1 = new Job("j1", "mo1", "A", "M", "D", "S", 10, Job.JobStatus.OPEN);
        Job j2 = new Job("j1", "mo2", "B", "N", "E", "T", 20, Job.JobStatus.CLOSED);
        Job j3 = new Job("j2", "mo1", "A", "M", "D", "S", 10, Job.JobStatus.OPEN);
        assertTrue("Job-equals-sameId", j1.equals(j2));
        assertFalse("Job-equals-diffId", j1.equals(j3));
    }

    private static void testJobFromCsvRowInvalid() {
        // 字段不足
        Job j1 = Job.fromCsvRow("only,three");
        assertNull("Job-fromCsvRow-tooFewFields", j1);
        // maxHours 不是数字
        Job j2 = Job.fromCsvRow("j1,mo1,T,M,D,S,NaN,OPEN");
        assertNull("Job-fromCsvRow-invalidMaxHours", j2);
    }

    // ============================================================
    //  Application Tests
    // ============================================================

    private static void testApplicationConstruction() {
        Application a = new Application("a1", "ta1", "j1", Application.AppStatus.PENDING, 1700000000000L);
        assertEqual("App-构造-id", "a1", a.getId());
        assertEqual("App-构造-taId", "ta1", a.getTaId());
        assertEqual("App-构造-jobId", "j1", a.getJobId());
        assertEqual("App-构造-status", Application.AppStatus.PENDING, a.getStatus());
        assertEqual("App-构造-appliedAt", 1700000000000L, a.getAppliedAt());
    }

    private static void testApplicationToCsvRow() {
        Application a = new Application("a1", "ta1", "j1", Application.AppStatus.PENDING, 1700000000000L);
        String csv = a.toCsvRow();
        assertEqual("App-toCsvRow", "a1,ta1,j1,PENDING,1700000000000", csv);
    }

    private static void testApplicationFromCsvRow() {
        String csv = "a1,ta1,j1,SELECTED,1700000000000";
        Application a = Application.fromCsvRow(csv);
        assertNotNull("App-fromCsvRow-notNull", a);
        assertEqual("App-fromCsvRow-id", "a1", a.getId());
        assertEqual("App-fromCsvRow-taId", "ta1", a.getTaId());
        assertEqual("App-fromCsvRow-status", Application.AppStatus.SELECTED, a.getStatus());
        assertEqual("App-fromCsvRow-appliedAt", 1700000000000L, a.getAppliedAt());
    }

    private static void testApplicationEquals() {
        Application a1 = new Application("a1", "ta1", "j1", Application.AppStatus.PENDING, 100L);
        Application a2 = new Application("a1", "ta2", "j2", Application.AppStatus.SELECTED, 200L);
        Application a3 = new Application("a2", "ta1", "j1", Application.AppStatus.PENDING, 100L);
        assertTrue("App-equals-sameId", a1.equals(a2));
        assertFalse("App-equals-diffId", a1.equals(a3));
    }

    private static void testApplicationFromCsvRowInvalid() {
        Application a1 = Application.fromCsvRow("only,two");
        assertNull("App-fromCsvRow-tooFewFields", a1);
        // appliedAt 不是数字
        Application a2 = Application.fromCsvRow("a1,ta1,j1,PENDING,NOT_LONG");
        assertNull("App-fromCsvRow-invalidTimestamp", a2);
    }

    // ============================================================
    //  断言工具
    // ============================================================

    private static void assertEqual(String label, Object expected, Object actual) {
        if (expected == null && actual == null || expected != null && expected.equals(actual)) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            failed++;
            System.out.println("  [FAIL] " + label + "  expected=<" + expected + ">  actual=<" + actual + ">");
        }
    }

    private static void assertEqual(String label, long expected, long actual) {
        if (expected == actual) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            failed++;
            System.out.println("  [FAIL] " + label + "  expected=<" + expected + ">  actual=<" + actual + ">");
        }
    }

    private static void assertEqual(String label, int expected, int actual) {
        if (expected == actual) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            failed++;
            System.out.println("  [FAIL] " + label + "  expected=<" + expected + ">  actual=<" + actual + ">");
        }
    }

    private static void assertTrue(String label, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            failed++;
            System.out.println("  [FAIL] " + label + "  expected=true, got=false");
        }
    }

    private static void assertFalse(String label, boolean condition) {
        assertTrue(label, !condition);
    }

    private static void assertNotNull(String label, Object obj) {
        if (obj != null) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            failed++;
            System.out.println("  [FAIL] " + label + "  expected non-null, got null");
        }
    }

    private static void assertNull(String label, Object obj) {
        if (obj == null) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            failed++;
            System.out.println("  [FAIL] " + label + "  expected null, got=<" + obj + ">");
        }
    }
}
