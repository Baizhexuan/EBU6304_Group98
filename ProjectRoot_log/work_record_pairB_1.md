# 🛠️ EBU6304 项目开发日志：L1 基础架构与数据层 (协助与补充)

**项目名称**：BUPT International School TA Recruitment System  
**记录时间**：2026-04-09  
**参与人员**：蔡佳城 (Cai Jiacheng)  
**开发阶段**：L1 — 基础架构与数据层 (Basic Architecture & Data Layer) - Pair B 协助部分

---

## 1. 工作目标
在本阶段（L1）中，Pair B 的主要目标是协助 Pair A 完善基础架构，提供系统级的密码安全加密支持（Security Utilities），编写自动化测试数据生成器（Data Seeder），并配置项目的自动化构建运行脚本与项目说明文档，为后续 L2 登录认证模块的开发做好完全的准备。

## 2. 已完成任务清单

### 2.1 密码哈希安全模块 (Password Utilities)
- [x] **`PasswordUtil.java`**: 实现密码加密机制，避免在 CSV 数据源中明文存储用户密码。
  - 采用 **SHA-256** 加密算法。
  - 引入了 `SecureRandom` 提取**随机 Salt（盐值）**并通过 Base64 编码。
  - 实现了 `generateSalt()`, `hashPassword()` 和 `verifyPassword()` 核心鉴权辅助方法。

### 2.2 种子数据集生成器 (Data Seeder)
- [x] **`DataSeeder.java`**: 编写自动化的测试数据生成流程。
  - **用户流装配**：自动创建 6 名预设角色的用户，包含 1 名 Admin（系统管理员）、2 名 MO（模块负责人）、3 名 TA（助教候选人）。
  - **档案流装配**：为生成的 TA 用户自动配置关联的详情档案（`TAProfile`），包含生成的学号、GPA 和擅长技能。
  - **业务流装配**：生成 4 个典型的 Job（职位发布）以及关联的 4 条 Application（申请记录）。
  - 所有数据无缝对接 Pair A 编写的 `CsvStorage`，一件持久化至 `data/*.csv` 文件中。

### 2.3 自动化构建与运行脚本 (Build & Run Scripts)
- [x] **`compile.bat`**: 编写 Windows 批处理编译脚本。
  - 自动清理并构建输出目录 `bin/`。
  - 收集并编译 `src` 目录下所有的 `.java` 源码，同时指定了 `-encoding UTF-8` 保证跨平台中文字符集兼容。
- [x] **`run.bat`**: 编写一键执行逻辑脚本。
  - 集成执行 `DataSeeder` 初始化运行环境。
  - 同步调用 `L1Test` 完成 L1 层的端到端流程验证。

### 2.4 文档编撰与规范化
- [x] **`README.md`**: 在项目根目录（`ProjectRoot`）下独立撰写系统的引导性说明。
  - 记录了本次项目基于无数据库的 MVC 架构。
  - 明晰了当前的运行方式。

---

## 3. 关键设计决策 (Design Decisions)

| 设计点 | 决策方案 | 原因/理由 |
| :--- | :--- | :--- |
| **密码加密机制** | SHA-256 + 独立 Salt 值 | 仅使用 MD5 或通用共享盐容易被彩虹表碰撞。为每个用户分别生成独立 Salt，保证同样的密码生成不同的摘要，符合业界通用安全规范。 |
| **测试数据构成** | 硬编码与动态关联结合 | 保证每次环境初始化时均能获得一致的 Admin/MO 身份来进行 L2 的登录测试，同时动态关联 UUID 确保 `Application` 表中的逻辑参照不失效。 |
| **脚本批处理方案** | 构建独立 `bin` 取代即时编译 | 避免开发阶段源码互相穿插污染；方便没有 IDE 的环境下直接使用 CMD 或 PowerShell 双击一键演示评阅。 |

---

## 4. 遇到的问题与解决方案

### 4.1 数据外键引用冲突 (Foreign Key Integrity)
- **问题**：在生成 `Application` 记录时，找不到对应的 `userId` 和 `jobId`，导致生成孤立的数据脏行。
- **解决**：在 `DataSeeder` 中严格调整了对象的实例化顺序（先持久化 User $\rightarrow$ 再持久化 Profile 和 Job $\rightarrow$ 最后通过返回的对象强引用 ID 来创建 Application），确保了业务外键关联逻辑。

### 4.2 GitHub 仓库合并覆写丢失 (Git Branch Conflict / Overwrite)
- **问题**：由于直接强拉主分支并在错误的分支进行了开发，导致之前的代码节点被覆盖丢失。
- **解决**：引入标准的 Git 分支工作流模型。重新通过 `GitHub Desktop` 拉取 `main` 节点代码至独立分支 `dev_caijiacheng` 后进行了重构还原，保证开发进程相互隔离且安全。

---

## 5. 后续协作计划

- **对接 Pair C**：向主导测试的 C 组提交上述生成的测试数据集，配合他们开展 L1 模块的交叉评审以及深度的健壮性测试。
- **辅助 Pair A**：继续跟进和补充底层模型可能在 L2 开发期间暴露出的一些属性缺失情况。
- **推进 L2 主导工作**：基于目前已完备的底层 API 与校验体系（如 `verifyPassword` 和泛型查找），**Pair B 将开始全面主导 L2 (认证与 UI 框架层)** 的登录鉴权及各个角色的基础 Dashboard 视窗搭建。

---
**记录人**：蔡佳城 (Pair B)  
**审核状态**：待 Pair C 评审 $\square$