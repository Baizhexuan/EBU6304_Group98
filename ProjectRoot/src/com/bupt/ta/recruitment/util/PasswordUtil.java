package com.bupt.ta.recruitment.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码哈希工具类 (Task 7)
 * 采用 SHA-256 + 随机盐值的方式加密密码，避免在CSV中明文存储
 */
public class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;

    /**
     * 生成安全的随机盐值
     * @return Base64编码的字符串盐值
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 对密码进行哈希加密
     * @param password 明文密码
     * @param salt Base64编码的盐值
     * @return Base64编码的密码哈希片段
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            // 这里将普通的盐字符串解码回字节
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            md.update(saltBytes);
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("系统不支持 SHA-256 哈希算法", e);
        }
    }

    /**
     * 验证用户输入的密码是否正确
     * @param inputPassword 用户刚刚输入的明文密码
     * @param storedSalt 注册时保存在User对象/CSV中的盐值
     * @param storedHash 注册时保存在User对象/CSV中的哈希密码
     * @return true 如果匹配，否则 false
     */
    public static boolean verifyPassword(String inputPassword, String storedSalt, String storedHash) {
        String newHash = hashPassword(inputPassword, storedSalt);
        return newHash.equals(storedHash);
    }
}
