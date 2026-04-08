package com.bupt.ta.recruitment.model;

import java.util.Objects;

/**
 * User Model - 存储用户基础账户信息
 * 对应 L1 基础架构层
 */
public class User implements CsvSerializable{
    
    // 定义用户角色枚举，避免使用字符串导致拼写错误
    public enum UserRole {
        ADMIN,  // 管理员
        MO,     // Module Organiser (课程组织者)
        TA      // Teaching Assistant (助教申请人)
    }

    private String id;            // 唯一标识符 (建议使用 UUID)
    private String username;      // 用户名 (登录账号，需唯一)
    private String passwordHash;  // 加密后的密码哈希值 (绝对不能存明文)
    private String salt;          // 随机盐值 (用于增加哈希安全性)
    private UserRole role;        // 用户角色

    // --- 构造函数 ---

    // 无参构造函数 (某些框架或反序列化需要)
    public User() {}

    // 全参构造函数 (用于从 CSV 文件加载数据)
    public User(String id, String username, String passwordHash, String salt, UserRole role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
    }

    // 用于新用户注册的简化构造函数 (ID 和 Salt 由外部生成)
    public User(String username, String passwordHash, String salt, UserRole role) {
        this(java.util.UUID.randomUUID().toString(), username, passwordHash, salt, role);
    }

    // --- Getter 和 Setter ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    // --- 关键重写方法 ---

    /**
     * 重写 equals 方法
     * 因为在 CSV 存储引擎中，我们需要通过 ID 快速判断两个对象是否为同一个用户
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 将对象转换为 CSV 行字符串
     * 方便 CsvStorage 直接调用
     * 格式: id,username,passwordHash,salt,role
     */
    public String toCsvRow() {
        return String.join(",", 
            id, 
            username, 
            passwordHash, 
            salt, 
            role.name()
        );
    }

    /**
     * 从 CSV 行字符串恢复对象
     * @param csvRow CSV 格式的字符串
     * @return User 对象
     */
    public static User fromCsvRow(String csvRow) {
        String[] parts = csvRow.split(",");
        if (parts.length < 5) return null;
        
        return new User(
            parts[0], 
            parts[1], 
            parts[2], 
            parts[3], 
            UserRole.valueOf(parts[4])
        );
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                '}';
    }
}