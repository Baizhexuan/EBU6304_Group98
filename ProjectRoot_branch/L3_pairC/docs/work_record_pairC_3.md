# 🛠️ EBU6304 项目开发日志：L3 核心业务逻辑层

**项目名称**：BUPT International School TA Recruitment System  
**记录时间**：2026-04-09  
**参与人员**：程嘉华 (Cheng Jiahua)  
**开发阶段**：L3 — 核心业务逻辑层 (Core Business Logic Layer)

---

## 1. 工作目标
作为 L3 层的主导开发组（Pair C），本阶段目标是：围绕 TA 角色完成核心业务闭环，将 L2 阶段已经搭建好的 `TADashboard` 骨架补充为可实际操作的业务页面，覆盖个人资料编辑、岗位浏览与筛选、岗位申请，以及申请状态跟踪四项核心功能。

## 2. 已完成任务清单

### 2.1 US-1 / US-2：TA 编辑个人资料 (`TADashboard.java`)
- [x] 将原有只读的 Profile 页面改为可编辑表单。
- [x] 支持维护以下字段：`Full Name`、`Email`、`Student ID`、`Skills`、`GPA`、`CV Path`。
- [x] 接入 `JFileChooser`，支持 TA 通过文件浏览方式选择 CV 路径。
- [x] 实现邮箱格式校验与 GPA 范围校验（`0.0 - 4.0`）。
- [x] 支持首次创建 TAProfile，也支持对已有资料进行更新并写回 `profiles.csv`。

### 2.2 US-3：TA 浏览 OPEN 岗位 (`TADashboard.java`)
- [x] 保留仅展示 `OPEN` 岗位的逻辑，避免关闭岗位进入申请视图。
- [x] 新增按 `Module` 与 `Required Skills` 的双条件过滤能力。
- [x] 通过 `UIHelper.installSorter()` 保持表格列排序可用，满足浏览岗位时的排序需求。

### 2.3 US-4：TA 申请岗位 (`TADashboard.java`)
- [x] 新增 “Apply for Selected Job” 操作按钮。
- [x] 在提交申请前检查是否已完成 TA Profile，若未完成则拦截申请。
- [x] 在提交申请前检查同一 TA 是否已对同一岗位提交过申请，防止重复申请。
- [x] 申请成功后自动记录时间戳并写入 `applications.csv`。

### 2.4 TA 申请状态视图 (`TADashboard.java`)
- [x] 在 `My Applications` 标签页中实时展示当前 TA 的全部申请记录。
- [x] 根据申请状态提供颜色区分：
  - `SELECTED` → 绿色
  - `REJECTED` → 红色
  - `PENDING` → 橙色
- [x] 保持申请列表可排序，便于后续演示与扩展。

---

## 3. 关键设计决策 (Design Decisions)

| 设计点 | 决策方案 | 原因/理由 |
| :--- | :--- | :--- |
| **功能范围** | 严格聚焦 Pair C 主导任务，仅补全 TA 侧核心逻辑 | 遵循 `task_plan.md` 中 L3 分工，避免侵入 Pair A 负责的 MO 发布与审批逻辑。 |
| **数据写回方式** | 继续复用 `CsvStorage` 的 `loadAll()` / `saveAll()` 模式 | 与当前 `ProjectRoot` 的持久化结构保持一致，减少额外重构成本。 |
| **筛选实现** | 使用 `TableRowSorter + RowFilter` 完成模块/技能过滤 | 在保持列排序能力的同时，实现对 OPEN 岗位的动态筛选。 |
| **资料创建策略** | 若用户尚无 `TAProfile`，首次保存时自动创建 | 保证新注册 TA 可以直接进入完整业务流程，不被历史空数据阻塞。 |

---

## 4. 遇到的问题与解决方案

### 4.1 L2 骨架到 L3 业务逻辑的平滑衔接
- **问题**：L2 阶段的 `TADashboard` 主要以展示为主，缺少输入控件、事件处理和数据写回流程，无法直接承接 L3 需求。
- **解决**：在不破坏现有标签页结构的前提下，将 Profile 页面改为表单式布局，并分别补充保存、筛选、申请、刷新等交互逻辑，尽量保持代码结构连续性。

### 4.2 申请前置条件与重复申请校验
- **问题**：若 TA 未完善资料或已经申请过同一岗位，系统可能产生无效或重复申请数据。
- **解决**：在申请动作中增加两层拦截：首先检查 Profile 是否存在，其次检查 `applications.csv` 中是否已存在同一 `taId + jobId` 组合，确保申请记录有效且唯一。

---

## 5. 后续协作计划

- **对接 Pair A**：由 Pair A 在后续补全 MO 端的发布岗位、关闭岗位、查看申请者与选择/拒绝逻辑。  
- **对接 Pair B**：等待 Pair B 在本层补充业务逻辑测试与边界条件 review。  
- **衔接 L4 / L5**：当前 TADashboard 已具备完整的 TA 基础业务流，后续可以继续接入通知、AI 匹配与更丰富的工作量逻辑。

---
**记录人**：程嘉华  
**审核状态**：已完成 ✅
