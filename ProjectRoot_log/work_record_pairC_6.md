# 🛠️ EBU6304 项目开发日志：代码质量与安全修复层

**项目名称**：BUPT International School TA Recruitment System  
**记录时间**：2026-05-23  
**参与人员**：邓博文 (Deng Bowen)  
**开发阶段**：代码质量与安全修复 (Code Quality & Security Hardening)

---

## 1. 工作目标

在对 `ver_1.0` 进行系统性代码质量审查后，本阶段针对发现的以下四类高优先级问题进行修复：

1. **UI 线程阻塞**：`TADashboard` 在 Swing EDT 上同步调用 AI 评分接口，当外部模型启用时会导致 UI 冻结
2. **CSV 导出注入漏洞**：`AdminDashboard` 导出报告时未对字段值进行消毒，Excel 打开时可能执行公式注入命令
3. **Job 所有权被篡改**：`AdminDashboard` 职位表格允许 Admin 直接编辑 MO 字段，可能导致职位归属错乱
4. **Email 正则过于宽松**：`ValidationUtils.isEmail()` 允许无点号域名（如 `a@b` 被视为合法邮箱）

---

## 2. 已完成修复清单

### 2.1 TADashboard - refreshJobs 使用 SwingWorker 异步执行

**文件**：`ver_1.0/src/TADashboard.java`

**问题**：`refreshJobs()` 方法在 Swing EDT（事件分发线程）上同步为每个职位调用 `ScoringService.evaluate()`。当外部 AI 评分提供器启用时，每次 HTTP 请求最多阻塞 UI 长达 9 秒（3s 连接超时 + 6s 读超时），多职位循环下界面完全冻结。

**修复**：
- 新增 `import javax.swing.SwingWorker`
- 将原来的同步 `try/finally` 刷新逻辑重构为 `SwingWorker<List<Object[]>, Void>`
  - `doInBackground()`：在后台线程完成所有 `ScoringService.evaluate()` 调用和行数据组装
  - `done()`：回到 EDT 将结果填充到 `jobsModel`，并更新 `jobInsightLabel` 和 `jobAiRankingArea`
- 保留原有过滤器逻辑（title/module/skills/location 四字段过滤）
- 异常情况在 `done()` 中捕获并显示到 `jobInsightLabel`，避免静默失败

**效果**：TA 浏览职位时 UI 保持响应，AI 评分在后台完成后自动刷新列表。

---

### 2.2 AdminDashboard - CSV 导出公式注入防护

**文件**：`ver_1.0/src/AdminDashboard.java`

**问题**：`exportWorkloadReport()` 将表格单元格值直接写入 CSV，若字段值以 `=`、`+`、`-`、`@`、`|`、`%` 开头，Microsoft Excel、LibreOffice 等电子表格应用会将其解释为公式并执行（CSV Injection / Formula Injection，对应 OWASP A03:2021 注入类漏洞）。

**修复**：
- 新增私有方法 `sanitizeCsvExportField(String value)`：检测字段首字符是否为公式触发符，若是则在前方添加单引号 `'`，强制电子表格将其作为文本处理
- 在 `exportWorkloadReport()` 的数据行写入处，对所有 6 个列值调用 `sanitizeCsvExportField()` 后再传入 `csvLine()`

**覆盖的触发字符**：`=` `+` `-` `@` `|` `%`

---

### 2.3 AdminDashboard - Job 表格 MO 字段设为只读

**文件**：`ver_1.0/src/AdminDashboard.java`

**问题**：Jobs Overview 表格的 `isCellEditable()` 实现为 `column >= 1`，意味着 Admin 可以直接编辑 MO（第 1 列）字段，将任意职位转移给其他 MO，造成职位所有权混乱，影响 MO 工作流的数据一致性。

**修复**：
- 将条件由 `column >= 1` 改为 `column >= 2`
- 新增行内注释说明：`column 0 = Job ID (read-only), column 1 = MO ownership (read-only to prevent ownership hijack)`
- Admin 仍可编辑 Title、Module、Skills、Hours、Location、Status 六个字段，MO 字段不可修改

---

### 2.4 ValidationUtils - Email 正则表达式修复

**文件**：`ver_1.0/src/ValidationUtils.java`

**问题**：原正则 `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$` 不要求域名中包含点号，导致 `a@b`、`user@localhost` 等格式被视为合法邮箱，与实际业务需求不符。

**修复**：
- 新正则：`^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-])*\.[A-Za-z]{2,}$`
- 要求域名至少包含一个点号，且顶级域名（TLD）长度不少于 2 个字母
- 仍保留原有的本地部分规则（支持 `+`、`_`、`.`、`-`）
- 新增行内注释说明修复意图

---

## 3. 修改文件汇总

| 文件 | 修改类型 | 关键变更 |
| :--- | :--- | :--- |
| `ver_1.0/src/TADashboard.java` | 功能重构 | `refreshJobs()` 改为 SwingWorker 异步执行；新增 `import javax.swing.SwingWorker` |
| `ver_1.0/src/AdminDashboard.java` | 安全修复 + 权限修复 | 新增 `sanitizeCsvExportField()`；CSV 导出调用消毒；MO 列 `isCellEditable` 限制为只读 |
| `ver_1.0/src/ValidationUtils.java` | 输入验证修复 | `isEmail()` 正则更新，要求域名含点号和合法 TLD |

---

## 4. 测试验证建议

| 验证项 | 操作步骤 | 预期结果 |
| :--- | :--- | :--- |
| SwingWorker 异步加载 | 登录 TA 账号，点击 Browse Jobs 标签页 | 界面不冻结，职位列表在后台加载后自动显示 |
| CSV 注入防护 | Admin 登录，在工作量表格手动制造含 `=SUM(1+1)` 的数据，导出 CSV | 导出文件中对应字段值以 `'` 开头，Excel 不执行公式 |
| Job MO 只读 | Admin 登录，打开 Jobs Overview，尝试双击 MO 列 | 单元格不可编辑，无法修改 MO 字段 |
| Email 格式验证 | 注册时输入 `user@localhost` 或 `a@b` | 提示邮箱格式无效，无法注册 |

---

## 5. 关联 Git 变更

分支：`pairC-L6-javadoc`  
提交说明：`fix: SwingWorker async job refresh, CSV injection guard, Job MO read-only, email regex`
