package com.bupt.ta.recruitment.test;

import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.service.AuthService;
import com.bupt.ta.recruitment.util.CsvStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTest {

    private static final String TEST_CSV_PATH = "data/test_users.csv";
    private AuthService authService;
    private CsvStorage<User> testStorage;

    @BeforeEach
    public void setUp() {
        // 使用测试专用的 CSV 文件，避免污染真实数据
        testStorage = new CsvStorage<>(TEST_CSV_PATH, User::fromCsvRow);
        authService = new AuthService(testStorage);
        // 清理可能存在的旧测试文件
        new File(TEST_CSV_PATH).delete();
    }

    @AfterEach
    public void tearDown() {
        // 测试结束后清理测试文件
        new File(TEST_CSV_PATH).delete();
    }

    // 用例 1：测试正常注册和正确登录 (包含密码哈希验证)
    @Test
    public void testValidRegistrationAndLogin() {
        boolean isRegistered = authService.register("testuser", "password123", User.UserRole.TA);
        assertTrue(isRegistered, "用户注册应该成功");

        Optional<User> loggedInUser = authService.login("testuser", "password123");
        assertTrue(loggedInUser.isPresent(), "使用正确的账号密码应该登录成功");
        assertEquals("testuser", loggedInUser.get().getUsername());
        assertEquals(User.UserRole.TA, loggedInUser.get().getRole());
    }

    // 用例 2：测试错误登录 - 密码错误
    @Test
    public void testLoginWithWrongPassword() {
        authService.register("testuser2", "correctpass", User.UserRole.MO);
        
        Optional<User> loggedInUser = authService.login("testuser2", "wrongpass");
        assertFalse(loggedInUser.isPresent(), "使用错误的密码应该登录失败");
    }

    // 用例 3：测试错误登录 - 用户不存在
    @Test
    public void testLoginWithNonExistentUser() {
        Optional<User> loggedInUser = authService.login("ghostuser", "password123");
        assertFalse(loggedInUser.isPresent(), "不存在的用户应该登录失败");
    }

    // 用例 4：测试重复注册 (注册同名用户)
    @Test
    public void testDuplicateRegistration() {
        boolean firstRegistration = authService.register("duplicate_user", "pass1", User.UserRole.TA);
        assertTrue(firstRegistration, "首次注册应该成功");

        boolean secondRegistration = authService.register("duplicate_user", "pass2", User.UserRole.MO);
        assertFalse(secondRegistration, "注册已存在的用户名应该失败");
    }

    // 用例 5：测试用户名大小写不敏感登录 (验证 AuthService 里的 equalsIgnoreCase)
    @Test
    public void testCaseInsensitiveLogin() {
        authService.register("CaseUser", "pass", User.UserRole.TA);
        
        Optional<User> loggedInUser = authService.login("caseuser", "pass");
        assertTrue(loggedInUser.isPresent(), "用户名验证应忽略大小写");
    }
}