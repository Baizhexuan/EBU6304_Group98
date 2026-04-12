# 🛠️ EBU6304 项目开发日志：L3 业务逻辑与 UI 集成 (Pair A)

**项目名称**：BUPT International School TA Recruitment System  
**记录时间**：2026-04-12  
**参与人员**：[你的名字]  
**开发阶段**：L3 — 业务逻辑封装与前端集成 (Business Logic & UI Integration)

---

## 1. 工作目标
本阶段的核心任务是完成 Module Organiser (MO) 的管理工作台 (`MODashboard`) 界面开发，涵盖岗位发布、岗位管理以及申请者审批的完整流程。同时，启动 Service 层架构重构，通过创建 `MOService` 类，旨在将复杂的业务逻辑（如跨表数据聚合、状态流转校验）从 UI 代码中剥离，实现 UI 与数据持久化层的解耦。

## 2. 已完成任务清单

### 2.1 Service 层初步构建 (MOService)
- [x] **创建 `MOService.java`**：封装了 MO 角色的核心底层操作接口。
- [x] **业务逻辑封装**：
  - 实现了 `getJobsByMo()`：基于 Stream API 过滤查询当前 MO 发布的所有岗位。
  - 实现了 `postJob()` 与 `closeJob()`：统一处理岗位的持久化写入与状态变更。
  - 实现了 `updateApplicationStatus()`：将 UI 层的状态指令映射为底层枚举模型，确保数据一致性。

### 2.2 MO 岗位发布与管理前端集成 (US-5)
- [x] **岗位发布 (Post Job)**：利用 `GridBagLayout` 实现了多字段输入表单，涵盖标题、模块、工时及描述，并为后续的表单拦截验证预留了组件句柄。
- [x] **岗位列表 (My Posts)**：实现了基于 `DefaultTableModel` 的岗位展示视图，并集成 `UIHelper.installSorter` 增强了数据检索的便利性。

### 2.3 MO 申请审批前端集成 (US-6, US-7)
- [x] **申请者管理 (Applicants)**：实现了“多表聚合”显示，通过后台逻辑将 `Application`、`User`、`TAProfile` 和 `Job` 的数据关联，在前端展示完整的申请人画像。
- [x] **交互审批逻辑**：
  - 集成了审批（Approve）与拒绝（Reject）动作监听器。
  - 加入了 `JOptionPane` 二次确认机制，防止关键业务操作的误触发。
- [x] **状态即时刷新**：在审批完成后，通过 `model.setValueAt()` 实现 UI 局部刷新，无需重新加载整个表格即可反馈最新的操作结果。

---

## 3. 关键设计决策 (Design Decisions)

| 设计点 | 决策方案 | 原因/理由 |
| :--- | :--- | :--- |
| **视图层数据聚合** | 内存 Map 映射缓存 | 在渲染申请者列表时，预先将 User 和 Profile 加载进 HashMap。这避免了在表格循环中频繁执行文件 IO，将查询效率从 $O(N^2)$ 优化至近乎 $O(N)$。 |
| **表格行映射** | 引入 `convertRowIndexToModel` | 解决因表格排序（Sorting）导致的视觉索引与底层数据模型索引不一致问题，确保 MO 点击“批准”时，对应的是正确的申请记录。 |
| **状态防重复校验** | 前置状态对比 | 在提交更新前对比 `currentStatus` 与 `newStatus`，若状态未发生改变则拦截请求并提示用户，减少无效的磁盘写入。 |

---
**记录人**：[高炜程]
