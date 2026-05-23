package com.bupt.ta.recruitment.model;

import java.util.Objects;

/**
 * Application Model - 存储 TA 对岗位的申请记录
 * 对应 L1 基础架构层
 */
public class Application implements CsvSerializable{

    public enum AppStatus {
        PENDING,   // 待审核 (橙色)
        SELECTED,  // 已选中 (绿色)
        REJECTED   // 已拒绝 (红色)
    }

    private String id;            // 申请记录唯一 ID
    private String taId;          // 申请人的 User ID (外键)
    private String jobId;         // 申请岗位的 Job ID (外键)
    private AppStatus status;     // 审核状态
    private long appliedAt;       // 申请时间戳 (使用 long 类型存储 System.currentTimeMillis())

    // --- 构造函数 ---

    public Application() {}

    public Application(String id, String taId, String jobId, AppStatus status, long appliedAt) {
        this.id = id;
        this.taId = taId;
        this.jobId = jobId;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    public Application(String taId, String jobId, AppStatus status) {
        this(java.util.UUID.randomUUID().toString(), taId, jobId, status, System.currentTimeMillis());
    }

    // --- Getter 和 Setter ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTaId() { return taId; }
    public void setTaId(String taId) { this.taId = taId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public AppStatus getStatus() { return status; }
    public void setStatus(AppStatus status) { this.status = status; }

    public long getAppliedAt() { return appliedAt; }
    public void setAppliedAt(long appliedAt) { this.appliedAt = appliedAt; }

    // --- 关键重写方法 ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String toCsvRow() {
        return String.join(",", 
            id, 
            taId, 
            jobId, 
            status.name(), 
            String.valueOf(appliedAt)
        );
    }

    public static Application fromCsvRow(String csvRow) {
        String[] parts = csvRow.split(",");
        if (parts.length < 5) return null;
        
        try {
            return new Application(
                parts[0], 
                parts[1], 
                parts[2], 
                AppStatus.valueOf(parts[3]), 
                Long.parseLong(parts[4])
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Application{" + "taId='" + taId + '\'' + ", jobId='" + jobId + '\'' + ", status=" + status + '}';
    }
}