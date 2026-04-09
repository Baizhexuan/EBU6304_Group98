# 🛠️ EBU6304 项目开发日志：L2 认证与 UI 框架层

**项目名称**：BUPT International School TA Recruitment System  
**记录时间**：2026-04-09  
**参与人员**：程嘉华 (Cheng Jiahua)  
**开发阶段**：L2 — 认证与 UI 框架层 (Authentication & UI Framework Layer)

---

## 1. 工作目标
作为 L2 层的协助开发组（Pair C），本阶段目标是：在 Pair B 已完成登录、注册与基础 Dashboard 框架的前提下，补齐三类角色页面的标签页骨架，并提供可复用的 UI 工具类，为后续 L3 核心业务逻辑接入做好界面层准备。

## 2. 已完成任务清单

### 2.1 TA Dashboard 骨架实现 (`TADashboard.java`)
- [x] 建立 `Profile / Browse Jobs / My Applications` 三个标签页结构。
- [x] 对接 `profiles.csv`、`jobs.csv`、`applications.csv` 中的已有数据，保证页面能在 L2 阶段直接展示基础内容。
- [x] 在 `Profile` 标签页中展示 TA 的姓名、邮箱、学号、技能、GPA 和 CV 路径。
- [x] 在 `Browse Jobs` 标签页中仅展示状态为 `OPEN` 的岗位信息。
- [x] 在 `My Applications` 标签页中展示当前 TA 的申请记录，并按状态提供颜色区分。

### 2.2 MO Dashboard 骨架实现 (`MODashboard.java`)
- [x] 建立 `Post Job / My Posts / Applicants` 三个标签页结构。
- [x] 在 `Post Job` 标签页中提供岗位标题、模块编号、技能需求、工时和描述区域的表单骨架。
- [x] 在 `My Posts` 标签页中展示当前 MO 发布的岗位数据。
- [x] 在 `Applicants` 标签页中联动读取岗位、申请记录、TA 用户和 TA Profile 数据，形成基础申请者总览表。

### 2.3 Admin Dashboard 骨架实现 (`AdminDashboard.java`)
- [x] 建立 `Workload / All Apps / All Jobs` 三个标签页结构。
- [x] 在 `Workload` 标签页中统计每位 TA 的已录用岗位数量与当前总工时。
- [x] 在 `All Apps` 标签页中展示系统全部申请记录。
- [x] 在 `All Jobs` 标签页中展示系统全部岗位信息，形成管理员总览视图。

### 2.4 通用 UI 工具类实现 (`UIHelper.java`)
- [x] 添加邮箱格式校验方法 `isValidEmail()`。
- [x] 添加 GPA 范围校验方法 `isValidGpa()`。
- [x] 提供状态颜色常量，供后续表格渲染复用。
- [x] 实现表格排序器安装方法 `installSorter()`，使多个 Dashboard 可以直接复用排序能力。

---

## 3. 关键设计决策 (Design Decisions)

| 设计点 | 决策方案 | 原因/理由 |
| :--- | :--- | :--- |
| **界面层定位** | 仅实现 L2 所需页面骨架与数据展示 | 严格遵循 `task_plan.md` 中 Pair C 的职责，不提前侵入 L3 业务逻辑层。 |
| **数据来源** | 直接复用 L1 的 `CsvStorage` 与 `DataSeeder` | 保持与已有仓库结构一致，确保登录后可以立即演示三类 Dashboard。 |
| **通用能力抽取** | 新增 `UIHelper` 统一封装校验与排序 | 减少多个 Dashboard 中重复实现邮箱校验、GPA 校验、表格排序等逻辑。 |
| **展示优先策略** | 页面先做到“可切换、可展示、可扩展” | 满足 L2 的 UI 框架目标，同时为 L3 的表单提交、筛选、审批等功能预留挂接点。 |

---

## 4. 遇到的问题与解决方案

### 4.1 Pair B 提交与底层接口不完全兼容
- **问题**：在与 `main` 分支中已有的 `CsvStorage`、`User`、`PasswordUtil` 等类整合时，发现 Pair B 提交中的部分调用方式与当前底层实现不一致，直接合并会导致编译失败。
- **解决**：在合并 Pair C 内容到 `main` 时，同时完成必要的兼容性调整，确保 `AuthService`、`LoginFrame`、`RegisterFrame` 与现有 L1 数据层正确衔接，保证整个 `ProjectRoot` 能成功编译运行。

### 4.2 Dashboard 骨架与后续业务边界划分
- **问题**：如果在 L2 阶段直接补充过多交互逻辑，会与 `task_plan.md` 中 L3/L4 的职责边界冲突。
- **解决**：本次实现仅保留标签页布局、基础数据展示和通用 UI 支撑，不加入超出 L2 范围的复杂审批、编辑或通知逻辑。

---

## 5. 后续协作计划

- **对接 Pair B**：确保当前登录/注册流程与 Dashboard 骨架能稳定衔接，作为 L2 完整成果交付。  
- **衔接 L3**：后续可在当前 TA / MO / Admin 三类标签页中继续补充编辑资料、申请岗位、审批申请等核心业务逻辑。  
- **支持测试与文档**：为后续 Pair A / Pair B 的测试与代码评审提供明确的页面结构和文件划分依据。

---
**记录人**：程嘉华  
**审核状态**：已完成 ✅
