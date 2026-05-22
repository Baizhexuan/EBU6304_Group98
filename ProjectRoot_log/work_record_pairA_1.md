# 🛠️ EBU6304 项目开发日志：L1 基础架构与数据层

**项目名称**：BUPT International School TA Recruitment System  
**记录时间**：2026-04-08  
**参与人员**：白哲煊 (Bai Zhexuan)、高炜程 (Gao Weicheng)  
**开发阶段**：L1 — 基础架构与数据层 (Basic Architecture & Data Layer)

---

## 1. 工作目标
本阶段目标是搭建整个系统的底层骨架，建立一个无需数据库、基于 CSV 文件的轻量级持久化存储方案，并定义核心业务数据模型，为后续 L2 (UI) 和 L3 (业务逻辑) 的开发提供稳固的数据支撑。

## 2. 已完成任务清单

### 2.1 项目骨架搭建
- [x] 构建标准 Java 包结构，实现职责分离：
  - `com.bupt.ta.recruitment.model` (模型层)
  - `com.bupt.ta.recruitment.util` (工具层)
  - `com.bupt.ta.recruitment.test` (测试层)
- [x] 规范化 Source Root 结构，解决 IDE 包路径与物理路径不匹配问题。

### 2.2 数据模型实现 (Models)
实现了 4 个核心实体类，所有类均采用私有字段封装，并重写了 `equals()` 和 `hashCode()` 以确保在集合操作中能通过唯一 ID 正确识别：
- [x] **`User.java`**: 存储账户基础信息，包含角色枚举 (`ADMIN`, `MO`, `TA`) 及安全相关的 `salt` 字段。
- [x] **`TAProfile.java`**: 通过 `userId` 关联用户，存储学号、GPA、CV 路径及技能列表。
- [x] **`Job.java`**: 存储岗位需求、最大小时数及状态 (`OPEN`, `CLOSED`)。
- [x] **`Application.java`**: 实现 TA 与 Job 的中间关联，记录申请时间戳与审核状态 (`PENDING`, `SELECTED`, `REJECTED`)。

### 2.3 泛型存储引擎开发 (Generic Storage Engine)
- [x] **接口定义**：创建 `CsvSerializable` 接口，强制要求所有模型类实现 `toCsvRow()` 方法，确保序列化标准统一。
- [x] **通用引擎实现**：开发 `CsvStorage<T extends CsvSerializable>` 泛型类。
  - 实现基于 `Function` 映射的动态反序列化机制。
  - 提供完整的 CRUD 接口：`loadAll()`, `saveAll()`, `findById()`, `update()`, `delete()`。
  - 实现了文件系统自动检查与 `data/` 目录初始化逻辑。

### 2.4 验证与测试
- [x] 编写 `L1Test.java` 进行端到端功能验证。
- [x] **测试场景**：实例化存储引擎 $\rightarrow$ 创建测试用户 $\rightarrow$ 写入物理文件 $\rightarrow$ 重启读取 $\rightarrow$ 验证字段一致性。
- [x] **结果**：所有测试用例通过，确认持久化逻辑正常运行。

---

## 3. 关键设计决策 (Design Decisions)

| 设计点 | 决策方案 | 原因/理由 |
| :--- | :--- | :--- |
| **存储介质** | 基于 CSV 的文本文件 | 严格遵守 PDF 强制要求 $\rightarrow$ “Do not use a database”。 |
| **存储架构** | 泛型存储类 $\text{CsvStorage}\langle T \rangle$ | 避免为每个 Model 重复编写读写逻辑，极大降低代码冗余，提高可维护性。 |
| **关联关系** | 使用 String ID (UUID) 作为外键 | 在无数据库自增 ID 的环境下，UUID 可确保跨文件、跨记录的唯一性。 |
| **列表存储** | 技能列表使用分号 (`;`) 分隔 | 防止技能内容中包含逗号导致 CSV 字段解析错位，保证数据结构稳固。 |
| **类型安全** | 引入 `CsvSerializable` 接口 | 通过接口约束泛型 $\text{T}$，确保任何传入 `CsvStorage` 的类都具备转换为 CSV 行的能力。 |

---

## 4. 遇到的问题与解决方案

### 4.1 包名不匹配 (Package Mismatch)
- **问题**：VS Code 报错 `The declared package does not match the expected package`。
- **解决**：重新梳理 `src` 下的文件夹层级，确保物理路径与代码中的 `package` 声明完全一致，并执行 `Clean Java Language Server Workspace`。

### 4.2 终端乱码问题 (Encoding Issue)
- **问题**：控制台输出中文时出现乱码。
- **解决**：分析为 Java UTF-8 与 Windows PowerShell GBK 编码冲突，通过执行 `chcp 65001` 将终端切换为 UTF-8 编码。

---

## 5. 后续协作计划

- **对接 Pair B**：指导其基于 `CsvStorage` 实现 `PasswordUtil` (SHA-256 加密) 和 `DataSeeder` (初始化种子数据)。
- **对接 Pair C**：将 L1 代码推送到 GitHub，邀请其编写 JUnit 测试用例并进行 Code Review。
- **衔接 L2**：为 UI 组提供 `CsvStorage` 的 API 调用指南，确保登录模块能快速对接用户模型。

---
**记录人**：白哲煊、高炜程  
**审核状态**：待 Pair C 评审 $\square$
