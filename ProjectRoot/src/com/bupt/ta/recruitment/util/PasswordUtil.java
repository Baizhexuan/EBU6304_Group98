package com.bupt.ta.recruitment.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password salting, hashing, and verification.
 * It centralizes credential protection logic so that callers do not handle
 * raw hashing details directly.
 */
public class PasswordUtil {
    // 复用同一个安全随机数生成器，用于生成不可预测的盐值。
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateSalt() {
        // 为每个用户分配 16 字节的独立盐值，降低彩虹表攻击风险。
        byte[] salt = new byte[16];
        // 使用安全随机源填充盐值字节数组。
        RANDOM.nextBytes(salt);
        // 将二进制盐值编码为 Base64 字符串，便于写入 CSV 文件。
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(String password, String salt) {
        try {
            // 获取 SHA-256 摘要器，用于执行单向哈希。
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 先把 Base64 形式的盐值还原成原始字节，再混入摘要器状态。
            digest.update(Base64.getDecoder().decode(salt));
            // 使用 UTF-8 将原始密码转为字节后计算摘要。
            byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            // 将摘要结果转为 Base64，方便持久化和后续比对。
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            // 如果运行环境不支持 SHA-256，则直接抛出运行时异常暴露严重配置问题。
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static boolean verifyPassword(String rawPassword, String storedSalt, String storedHash) {
        // 重新按存储的盐值计算输入密码的哈希，并与已保存摘要进行精确比对。
        return hashPassword(rawPassword, storedSalt).equals(storedHash);
    }
}
