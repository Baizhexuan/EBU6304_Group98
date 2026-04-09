package com.bupt.ta.recruitment.test;

import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.service.AuthService;
import com.bupt.ta.recruitment.util.CsvStorage;

import java.io.File;
import java.util.Optional;

/**
 * L2 Pair A — 登录/注册集成测试
 * 无 JUnit 依赖版本，通过 main 方法直接运行测试。
 */
public class AuthTest {

    private static int passed = 0;
    private static int failed = 0;
    private static final String TEST_CSV_PATH = "data/test_users.csv";
    
    private static AuthService authService;
    private static CsvStorage<User> testStorage;

    public static void main(String[] args) {
        System.out.println("========== AuthTest 开始 ==========\n");

        try {
            // 用例 1：测试正常注册和正确登录 (包含密码哈希验证)
            testValidRegistrationAndLogin();

            // 用例 2：测试错误登录 - 密码错误
            testLoginWithWrongPassword();

            // 用例 3：测试错误登录 - 用户不存在
            testLoginWithNonExistentUser();

            // 用例 4：测试重复注册 (注册同名用户)
            testDuplicateRegistration();

            // 用例 5：测试用户名大小写不敏感登录
            testCaseInsensitiveLogin();

        } finally {
            // 测试结束后清理测试用的 CSV 文件
            new File(TEST_CSV_PATH).delete();
        }

        System.out.println("\n========== AuthTest 结束 ==========");
        System.out.println("通过: " + passed + "  失败: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // --- 测试前的准备工作 ---
    private static void setUp() {
        // 使用测试专用的 CSV 文件，避免污染真实数据
        testStorage = new CsvStorage<>(TEST_CSV_PATH, User::fromCsvRow);
        authService = new AuthService(testStorage);
        // 清理可能存在的旧测试文件
        new File(TEST_CSV_PATH).delete();
    }

    // ============================================================
    //  测试用例
    // ============================================================

    private static void testValidRegistrationAndLogin() {
        setUp();
        boolean isRegistered = authService.register("testuser", "password123", User.UserRole.TA);
        assertTrue("AuthTest-注册-首次注册成功", isRegistered);

        Optional<User> loggedInUser = authService.login("testuser", "password123");
        assertTrue("AuthTest-登录-正确账号密码登录成功", loggedInUser.isPresent());
        if (loggedInUser.isPresent()) {
            assertEqual("AuthTest-登录-验证用户名", "testuser", loggedInUser.get().getUsername());
            assertEqual("AuthTest-登录-验证角色", User.UserRole.TA, loggedInUser.get().getRole());
        }
    }

    private static void testLoginWithWrongPassword() {
        setUp();
        authService.register("testuser2", "correctpass", User.UserRole.MO);
        
        Optional<User> loggedInUser = authService.login("testuser2", "wrongpass");
        assertFalse("AuthTest-登录-密码错误登录失败", loggedInUser.isPresent());
    }

    private static void testLoginWithNonExistentUser() {
        setUp();
        Optional<User> loggedInUser = authService.login("ghostuser", "password123");
        assertFalse("AuthTest-登录-用户不存在登录失败", loggedInUser.isPresent());
    }

    private static void testDuplicateRegistration() {
        setUp();
        boolean firstRegistration = authService.register("duplicate_user", "pass1", User.UserRole.TA);
        assertTrue("AuthTest-注册-首次注册成功", firstRegistration);

        boolean secondRegistration = authService.register("duplicate_user", "pass2", User.UserRole.MO);
        assertFalse("AuthTest-注册-重复用户名注册失败", secondRegistration);
    }

    private static void testCaseInsensitiveLogin() {
        setUp();
        authService.register("CaseUser", "pass", User.UserRole.TA);
        
        Optional<User> loggedInUser = authService.login("caseuser", "pass");
        assertTrue("AuthTest-登录-忽略大小写登录成功", loggedInUser.isPresent());
    }

    // ============================================================
    //  简易断言工具
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
}