package com.bupt.ta.recruitment.model;

import java.util.Objects;

/**
 * TAProfile Model - 存储助教申请人的详细个人资料
 * 对应 L1 基础架构层
 */
public class TAProfile {

    private String id;            // 个人资料唯一标识符
    private String userId;        // 关联的 User ID (外键)
    private String fullName;      // 真实姓名
    private String email;         // 电子邮箱
    private String studentId;     // 学号
    private String skills;        // 技能列表 (在 CSV 中以分号 ';' 分隔, 如 "Java;Python;English")
    private double gpa;           // GPA (0.0 - 4.0)
    private String cvPath;        // CV 文件在本地的存储路径

    // --- 构造函数 ---

    public TAProfile() {}

    // 全参构造函数 (用于从 CSV 加载)
    public TAProfile(String id, String userId, String fullName, String email, String studentId, String skills, double gpa, String cvPath) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.studentId = studentId;
        this.skills = skills;
        this.gpa = gpa;
        this.cvPath = cvPath;
    }

    // 用于新创建资料的简化构造函数
    public TAProfile(String userId, String fullName, String email, String studentId, String skills, double gpa, String cvPath) {
        this(java.util.UUID.randomUUID().toString(), userId, fullName, email, studentId, skills, gpa, cvPath);
    }

    // --- Getter 和 Setter ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public String getCvPath() {
        return cvPath;
    }

    public void setCvPath(String cvPath) {
        this.cvPath = cvPath;
    }

    // --- 关键重写方法 ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TAProfile taProfile = (TAProfile) o;
        return Objects.equals(id, taProfile.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 将对象转换为 CSV 行字符串
     * 字段顺序: id,userId,fullName,email,studentId,skills,gpa,cvPath
     */
    public String toCsvRow() {
        return String.join(",", 
            id, 
            userId, 
            fullName, 
            email, 
            studentId, 
            skills, // 注意：skills 内部建议用分号分隔
            String.valueOf(gpa), 
            cvPath
        );
    }

    /**
     * 从 CSV 行字符串恢复对象
     */
    public static TAProfile fromCsvRow(String csvRow) {
        String[] parts = csvRow.split(",");
        if (parts.length < 8) return null;
        
        try {
            return new TAProfile(
                parts[0], 
                parts[1], 
                parts[2], 
                parts[3], 
                parts[4], 
                parts[5], 
                Double.parseDouble(parts[6]), 
                parts[7]
            );
        } catch (NumberFormatException e) {
            // 如果 GPA 格式错误，返回 null 或抛出异常
            return null;
        }
    }

    @Override
    public String toString() {
        return "TAProfile{" +
                "fullName='" + fullName + '\'' +
                ", studentId='" + studentId + '\'' +
                ", gpa=" + gpa +
                '}';
    }
}