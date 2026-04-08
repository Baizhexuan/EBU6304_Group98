# 🛠️ EBU6304 项目开发日志：L1 基础架构与数据层

**项目名称**：BUPT International School TA Recruitment System  
**记录时间**：2026-04-09  
**参与人员**：邓博文 (Deng Bowen)  
**开发阶段**：L1 — 基础架构与数据层 (Basic Architecture & Data Layer)

---

## 1. 工作目标
作为 L1 层的评审与测试组（Pair C），本阶段目标是：对 Pair A（数据模型 + 存储引擎）和 Pair B（密码工具 + 种子数据）的全部产出进行质量把关，编写覆盖充分的测试用例，并完成代码审查记录。

## 2. 已完成任务清单

### 2.1 数据模型测试 (ModelTest.java)
- [x] 编写 `ModelTest.java`，覆盖 4 个核心模型类：`User`、`TAProfile`、`Job`、`Application`
- [x] 每个模型实现 $\geq 5$ 个测试用例，包含以下场景：
  - **构造测试**：全参构造函数字段赋值正确性验证
  - **序列化测试**：`toCsvRow()` 输出格式与预期严格匹配
  - **反序列化测试**：`fromCsvRow()` 正确还原对象各字段
  - **相等性测试**：`equals()` 基于 ID 判定，同 ID 不同字段仍相等，不同 ID 则不等
  - **异常输入测试**：字段不足、数值格式错误等情况返回 `null`
- [x] **结果**：62 个断言全部通过，确认模型层序列化逻辑正常运行。

### 2.2 CSV 存储引擎测试 (CsvStorageTest.java)
- [x] 编写 `CsvStorageTest.java`，对 `CsvStorage<T>` 的 CRUD 操作进行全覆盖测试
- [x] 测试场景分类：
  - **CRUD 基础**：`saveAll` + `loadAll`、`findById`（存在/不存在）、`update`（更新后验证 + 其他记录不受影响）、`delete`（删除后验证 + 删除不存在的 ID）
  - **边界场景**：空文件加载、空列表覆盖写入
  - **多模型类型**：分别对 `User`、`Job`、`Application`、`TAProfile` 执行存储引擎操作
  - **特殊字符**：技能字段含分号 `;`（如 `"Java;C++;Python"`）的序列化/反序列化一致性
  - **畸形数据**：手动写入空行 + 字段不足的行，验证引擎的容错能力
- [x] 使用独立临时目录 `data/test_temp_*`，测试结束后自动清理
- [x] **结果**：34 个断言全部通过，确认存储引擎 CRUD 逻辑正常运行。

### 2.3 Code Review 审查
- [x] 对 L1 层全部 9 个源文件逐一审查，产出 `L1_CodeReview_PairC.md` 审查记录表
- [x] **结果**：发现 1 个高优先级问题、5 个中优先级问题、5 个低优先级问题，10 处良好实践。

---

## 3. 关键设计决策 (Design Decisions)

| 设计点 | 决策方案 | 原因/理由 |
| :--- | :--- | :--- |
| **测试框架** | 自实现轻量断言（无 JUnit 依赖） | 项目未引入 JUnit jar，采用 `assertEqual`/`assertTrue` 等工具方法等效替代，零外部依赖。 |
| **测试隔离** | 时间戳临时目录 `data/test_temp_<ts>` | 防止测试产生的 CSV 文件污染正式 `data/` 目录，`finally` 块自动清理。 |
| **覆盖策略** | 每模型 $\geq 5$ 用例，CRUD 全覆盖 | 满足验收标准"每个模型 ≥ 3 个用例"且超额覆盖异常输入和边界场景。 |
| **审查方法** | 按文件逐一审查，按严重程度分级 | 参照业界 Code Review 标准，分 🔴高/⚠️中/ℹ️低/✅良 四级，便于开发组优先修复。 |

---

## 4. 遇到的问题与解决方案

### 4.1 BOM 编码导致编译失败
- **问题**：`PasswordUtil.java` 和 `DataSeeder.java` 含 UTF-8 BOM 标记，`javac -encoding UTF-8` 编译报"非法字符 `\ufeff`"。
- **解决**：编译时暂时排除这两个文件，测试代码不依赖它们即可独立验证。已将此问题作为 Code Review 中优先级意见反馈给 Pair B。

### 4.2 测试隔离与清理
- **问题**：CSV 存储引擎测试会在磁盘写入临时文件，需避免污染项目 `data/` 目录。
- **解决**：使用基于时间戳的独立临时目录 `data/test_temp_<timestamp>`，在 `finally` 块中递归删除，确保测试环境清洁。

---

## 5. 后续协作计划

- **反馈 Pair A**：建议 `CsvStorage` 补充引号转义解析，`Job.description` 考虑转义或限制输入。
- **反馈 Pair B**：`PasswordUtil.java` 和 `DataSeeder.java` 需去除 BOM 标记以确保 `compile.bat` 零错误。
- **衔接 L2**：测试框架已就绪，后续层次可复用断言工具方法和临时文件隔离模式。

---
**记录人**：邓博文  
**审核状态**：已完成 ✅
