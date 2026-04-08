package com.bupt.ta.recruitment.test;

import com.bupt.ta.recruitment.model.*;
import com.bupt.ta.recruitment.util.CsvStorage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * L1 Pair C — CSV 存储引擎 JUnit 风格测试
 *
 * 覆盖 CsvStorage 的增删改查（CRUD）操作，以及特殊字符处理场景。
 * 使用独立的临时 CSV 文件，测试结束后自动清理。
 */
public class CsvStorageTest {

    private static int passed = 0;
    private static int failed = 0;

    // 使用带时间戳的临时目录避免冲突
    private static final String TEST_DIR = "data/test_temp_" + System.currentTimeMillis();

    public static void main(String[] args) {
        System.out.println("========== CsvStorageTest 开始 ==========\n");

        new File(TEST_DIR).mkdirs();

        try {
            // CRUD 基础测试
            testSaveAndLoadAll();
            testFindById();
            testFindByIdNotFound();
            testUpdate();
            testDelete();
            testDeleteNonExistent();

            // 空文件 / 边界测试
            testLoadEmptyFile();
            testSaveEmptyList();

            // 多类型模型测试
            testJobStorage();
            testApplicationStorage();

            // 特殊字符 / 边界场景
            testSpecialCharactersInFields();
            testMalformedCsvRow();

        } finally {
            // 清理临时文件
            cleanup(new File(TEST_DIR));
        }

        System.out.println("\n========== CsvStorageTest 结束 ==========");
        System.out.println("通过: " + passed + "  失败: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ============================================================
    //  CRUD 基础测试 (以 User 为例)
    // ============================================================

    private static void testSaveAndLoadAll() {
        String path = TEST_DIR + "/users_save.csv";
        CsvStorage<User> storage = new CsvStorage<>(path, User::fromCsvRow);

        List<User> users = new ArrayList<>();
        users.add(new User("u1", "alice", "h1", "s1", User.UserRole.TA));
        users.add(new User("u2", "bob", "h2", "s2", User.UserRole.MO));
        users.add(new User("u3", "admin", "h3", "s3", User.UserRole.ADMIN));

        storage.saveAll(users);
        List<User> loaded = storage.loadAll();

        assertEqual("SaveLoad-count", 3, loaded.size());
        assertEqual("SaveLoad-user1-username", "alice", loaded.get(0).getUsername());
        assertEqual("SaveLoad-user2-role", User.UserRole.MO, loaded.get(1).getRole());
        assertEqual("SaveLoad-user3-id", "u3", loaded.get(2).getId());
    }

    private static void testFindById() {
        String path = TEST_DIR + "/users_find.csv";
        CsvStorage<User> storage = new CsvStorage<>(path, User::fromCsvRow);

        List<User> users = new ArrayList<>();
        users.add(new User("u1", "alice", "h1", "s1", User.UserRole.TA));
        users.add(new User("u2", "bob", "h2", "s2", User.UserRole.MO));
        storage.saveAll(users);

        User found = storage.findById("u2", User::getId);
        assertNotNull("FindById-found", found);
        assertEqual("FindById-username", "bob", found.getUsername());
    }

    private static void testFindByIdNotFound() {
        String path = TEST_DIR + "/users_find2.csv";
        CsvStorage<User> storage = new CsvStorage<>(path, User::fromCsvRow);
        storage.saveAll(Collections.singletonList(
                new User("u1", "alice", "h1", "s1", User.UserRole.TA)));

        User notFound = storage.findById("nonexistent", User::getId);
        assertNull("FindById-notFound", notFound);
    }

    private static void testUpdate() {
        String path = TEST_DIR + "/users_update.csv";
        CsvStorage<User> storage = new CsvStorage<>(path, User::fromCsvRow);

        List<User> users = new ArrayList<>();
        users.add(new User("u1", "alice", "h1", "s1", User.UserRole.TA));
        users.add(new User("u2", "bob", "h2", "s2", User.UserRole.MO));
        storage.saveAll(users);

        // 更新 u1 的用户名
        User updated = new User("u1", "alice_new", "h1_new", "s1", User.UserRole.ADMIN);
        storage.update(updated, User::getId);

        User retrieved = storage.findById("u1", User::getId);
        assertNotNull("Update-retrieved", retrieved);
        assertEqual("Update-newUsername", "alice_new", retrieved.getUsername());
        assertEqual("Update-newRole", User.UserRole.ADMIN, retrieved.getRole());

        // 确保其他记录不受影响
        User bob = storage.findById("u2", User::getId);
        assertNotNull("Update-otherIntact", bob);
        assertEqual("Update-otherUsername", "bob", bob.getUsername());
    }

    private static void testDelete() {
        String path = TEST_DIR + "/users_delete.csv";
        CsvStorage<User> storage = new CsvStorage<>(path, User::fromCsvRow);

        List<User> users = new ArrayList<>();
        users.add(new User("u1", "alice", "h1", "s1", User.UserRole.TA));
        users.add(new User("u2", "bob", "h2", "s2", User.UserRole.MO));
        users.add(new User("u3", "charlie", "h3", "s3", User.UserRole.ADMIN));
        storage.saveAll(users);

        storage.delete("u2", User::getId);

        List<User> remaining = storage.loadAll();
        assertEqual("Delete-count", 2, remaining.size());
        assertNull("Delete-removed", storage.findById("u2", User::getId));
        assertNotNull("Delete-u1-intact", storage.findById("u1", User::getId));
        assertNotNull("Delete-u3-intact", storage.findById("u3", User::getId));
    }

    private static void testDeleteNonExistent() {
        String path = TEST_DIR + "/users_delnon.csv";
        CsvStorage<User> storage = new CsvStorage<>(path, User::fromCsvRow);

        List<User> users = new ArrayList<>();
        users.add(new User("u1", "alice", "h1", "s1", User.UserRole.TA));
        storage.saveAll(users);

        // 删除不存在的 ID，列表应保持不变
        storage.delete("nonexistent", User::getId);
        List<User> remaining = storage.loadAll();
        assertEqual("DeleteNonExistent-count", 1, remaining.size());
    }

    // ============================================================
    //  空文件 / 边界测试
    // ============================================================

    private static void testLoadEmptyFile() {
        String path = TEST_DIR + "/empty.csv";
        CsvStorage<User> storage = new CsvStorage<>(path, User::fromCsvRow);

        List<User> loaded = storage.loadAll();
        assertNotNull("EmptyFile-notNull", loaded);
        assertEqual("EmptyFile-count", 0, loaded.size());
    }

    private static void testSaveEmptyList() {
        String path = TEST_DIR + "/save_empty.csv";
        CsvStorage<User> storage = new CsvStorage<>(path, User::fromCsvRow);

        // 先写入数据再用空列表覆盖
        storage.saveAll(Collections.singletonList(
                new User("u1", "alice", "h1", "s1", User.UserRole.TA)));
        storage.saveAll(new ArrayList<>());

        List<User> loaded = storage.loadAll();
        assertEqual("SaveEmpty-count", 0, loaded.size());
    }

    // ============================================================
    //  多类型模型测试
    // ============================================================

    private static void testJobStorage() {
        String path = TEST_DIR + "/jobs.csv";
        CsvStorage<Job> storage = new CsvStorage<>(path, Job::fromCsvRow);

        List<Job> jobs = new ArrayList<>();
        jobs.add(new Job("j1", "mo1", "Java TA", "EBU6304", "Assist labs", "Java;Spring", 20, Job.JobStatus.OPEN));
        jobs.add(new Job("j2", "mo2", "ML TA", "EBU6001", "ML labs", "Python;ML", 25, Job.JobStatus.CLOSED));
        storage.saveAll(jobs);

        List<Job> loaded = storage.loadAll();
        assertEqual("JobStorage-count", 2, loaded.size());
        assertEqual("JobStorage-j1-title", "Java TA", loaded.get(0).getTitle());
        assertEqual("JobStorage-j2-status", Job.JobStatus.CLOSED, loaded.get(1).getStatus());

        // 更新
        Job updated = new Job("j1", "mo1", "Java TA (Updated)", "EBU6304", "New desc", "Java", 30, Job.JobStatus.CLOSED);
        storage.update(updated, Job::getId);
        Job retrieved = storage.findById("j1", Job::getId);
        assertEqual("JobStorage-updated-title", "Java TA (Updated)", retrieved.getTitle());
        assertEqual("JobStorage-updated-hours", 30, retrieved.getMaxHours());
    }

    private static void testApplicationStorage() {
        String path = TEST_DIR + "/applications.csv";
        CsvStorage<Application> storage = new CsvStorage<>(path, Application::fromCsvRow);

        long now = 1700000000000L;
        List<Application> apps = new ArrayList<>();
        apps.add(new Application("a1", "ta1", "j1", Application.AppStatus.PENDING, now));
        apps.add(new Application("a2", "ta2", "j1", Application.AppStatus.SELECTED, now + 1000));
        apps.add(new Application("a3", "ta1", "j2", Application.AppStatus.REJECTED, now + 2000));
        storage.saveAll(apps);

        List<Application> loaded = storage.loadAll();
        assertEqual("AppStorage-count", 3, loaded.size());

        // 删除
        storage.delete("a2", Application::getId);
        loaded = storage.loadAll();
        assertEqual("AppStorage-afterDelete", 2, loaded.size());
        assertNull("AppStorage-a2-gone", storage.findById("a2", Application::getId));
    }

    // ============================================================
    //  特殊字符 / 边界场景
    // ============================================================

    private static void testSpecialCharactersInFields() {
        // 技能内使用分号（在同一 CSV 字段内），验证不影响读写
        String path = TEST_DIR + "/profiles_special.csv";
        CsvStorage<TAProfile> storage = new CsvStorage<>(path, TAProfile::fromCsvRow);

        TAProfile p = new TAProfile("p1", "u1", "Alice", "a@b.com", "001", "Java;C++;Python", 3.9, "cv.pdf");
        storage.saveAll(Collections.singletonList(p));

        List<TAProfile> loaded = storage.loadAll();
        assertEqual("SpecialChar-count", 1, loaded.size());
        assertEqual("SpecialChar-skills", "Java;C++;Python", loaded.get(0).getSkills());
        assertEqual("SpecialChar-gpa-roundtrip", 3.9, loaded.get(0).getGpa());
    }

    private static void testMalformedCsvRow() {
        // 在 CSV 文件中手动写入有问题的行，验证存储引擎的健壮性
        String path = TEST_DIR + "/malformed.csv";
        try {
            // 手动写入混合数据：一行正常、一行空、一行字段不足
            PrintWriter pw = new PrintWriter(new FileWriter(path));
            pw.println("u1,alice,h1,s1,TA");
            pw.println("");
            pw.println("only,two");
            pw.println("u2,bob,h2,s2,MO");
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        CsvStorage<User> storage = new CsvStorage<>(path, User::fromCsvRow);
        List<User> loaded = storage.loadAll();
        // 空行被跳过，字段不足的行 fromCsvRow 返回 null 也被跳过
        assertEqual("Malformed-validCount", 2, loaded.size());
        assertEqual("Malformed-first", "alice", loaded.get(0).getUsername());
        assertEqual("Malformed-second", "bob", loaded.get(1).getUsername());
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

    private static void assertEqual(String label, int expected, int actual) {
        if (expected == actual) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            failed++;
            System.out.println("  [FAIL] " + label + "  expected=<" + expected + ">  actual=<" + actual + ">");
        }
    }

    private static void assertEqual(String label, double expected, double actual) {
        if (Math.abs(expected - actual) < 0.001) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            failed++;
            System.out.println("  [FAIL] " + label + "  expected=<" + expected + ">  actual=<" + actual + ">");
        }
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

    /**
     * 递归删除临时测试目录
     */
    private static void cleanup(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    cleanup(child);
                }
            }
        }
        dir.delete();
    }
}
