package com.bupt.ta.recruitment.model;

import java.util.Objects;

/**
 * Job Model - 存储助教岗位信息
 * 对应 L1 基础架构层
 */
public class Job {

    public enum JobStatus {
        OPEN,   // 开放申请
        CLOSED  // 已关闭
    }

    private String id;             // 岗位唯一 ID
    private String moId;           // 发布该岗位的 MO 用户 ID (外键)
    private String title;          // 岗位标题 (如: Java Programming TA)
    private String module;         // 所属模块/课程名
    private String description;    // 岗位详细描述
    private String requiredSkills; // 需求技能 (用分号 ';' 分隔)
    private int maxHours;          // 最大工作小时数
    private JobStatus status;      // 岗位状态

    // --- 构造函数 ---

    public Job() {}

    public Job(String id, String moId, String title, String module, String description, String requiredSkills, int maxHours, JobStatus status) {
        this.id = id;
        this.moId = moId;
        this.title = title;
        this.module = module;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.maxHours = maxHours;
        this.status = status;
    }

    public Job(String moId, String title, String module, String description, String requiredSkills, int maxHours, JobStatus status) {
        this(java.util.UUID.randomUUID().toString(), moId, title, module, description, requiredSkills, maxHours, status);
    }

    // --- Getter 和 Setter ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMoId() { return moId; }
    public void setMoId(String moId) { this.moId = moId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    public int getMaxHours() { return maxHours; }
    public void setMaxHours(int maxHours) { this.maxHours = maxHours; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    // --- 关键重写方法 ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return Objects.equals(id, job.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String toCsvRow() {
        return String.join(",", 
            id, 
            moId, 
            title, 
            module, 
            description, 
            requiredSkills, 
            String.valueOf(maxHours), 
            status.name()
        );
    }

    public static Job fromCsvRow(String csvRow) {
        String[] parts = csvRow.split(",");
        if (parts.length < 8) return null;
        
        try {
            return new Job(
                parts[0], 
                parts[1], 
                parts[2], 
                parts[3], 
                parts[4], 
                parts[5], 
                Integer.parseInt(parts[6]), 
                JobStatus.valueOf(parts[7])
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Job{" + "title='" + title + '\'' + ", module='" + module + '\'' + ", status=" + status + '}';
    }
}