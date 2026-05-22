# 🛠️ EBU6304 项目开发日志：安全加固与测试补全

**项目名称**：BUPT International School TA Recruitment System  
**记录时间**：2026-05-22  
**参与人员**：邓博文 (Deng Bowen)  
**开发阶段**：L6 后续 — 安全加固 (P1) + 回归测试补全 (P2)

---

## 1. 工作目标

在 L6 JavaDoc 交付完成的基础上，针对项目代码审查中发现的两处明确缺陷进行修复：

1. **P1 — 密码安全加固**：将用户密码由明文 CSV 存储改为 SHA-256 哈希存储，消除 OWASP A02 敏感数据暴露风险。
2. **P2 — 回归测试补全**：在 `WorkflowRulesTest` 中补充 4 个关键业务场景测试用例，提升核心逻辑的测试覆盖率。

---

## 2. 已完成任务清单

### 2.1 P1 — 密码 SHA-256 哈希加固

#### 变更动机

原始代码在 `data/users.csv` 中以明文存储用户密码（如 `admin123`、`ta123`），任何能读取文件的人均可直接获取所有账号凭据，属于严重安全缺陷。

#### 修改文件清单

| 文件 | 修改内容 |
| :--- | :--- |
| `ver_1.0/src/FileStorage.java` | 新增 `public static String hashPassword(String plainText)` 静态方法，使用 `java.security.MessageDigest` 实现 SHA-256 哈希；`ensureUsers()` 种子数据改为调用该方法生成哈希值写入 CSV |
| `ver_1.0/src/LoginFrame.java` | `attemptLogin()` 登录验证改为：`matched.password.equals(FileStorage.hashPassword(password))` |
| `ver_1.0/src/RegisterFrame.java` | `registerUser()` 注册写入改为：`FileStorage.hashPassword(password)` 存储，确保新注册账号密码同样不以明文保存 |
| `ver_1.0/src/AuthFlowTest.java` | 测试辅助方法 `authenticate()` 同步更新为哈希比对；注册测试用例改为存储哈希密码，维持测试与业务逻辑一致性 |
| `ver_1.0/data/users.csv` | 所有 6 个账号（含 Yuki 账号）的密码字段替换为对应的 64 位小写十六进制 SHA-256 哈希值 |

#### 技术细节

- 使用 `SHA-256` 算法，编码为 `UTF-8`，输出 64 位小写十六进制字符串
- `hashPassword()` 设计为无状态静态方法，SHA-256 在所有 Java SE 环境中始终可用，`NoSuchAlgorithmException` 包装为 `RuntimeException`
- 本次不引入随机 salt，以保持 demo 系统的简洁性和可复现性；在生产系统中应进一步使用 bcrypt 或 PBKDF2

#### Demo 账号对照表（哈希后）

| 用户名 | 原明文 | SHA-256 哈希（前16位） |
| ------ | ------ | ---------------------- |
| admin | admin123 | 240be518fabd2724... |
| ta1 | ta123 | 21557b9d977113c8... |
| ta2 | ta456 | 074f45cf0ec916a2... |
| mo1 | mo123 | a65accd68f911330... |
| mo2 | mo456 | 4bbdb0dd38976748... |
| Yuki | 123456 | 8d969eef6ecad3c2... |

---

### 2.2 P2 — WorkflowRulesTest 测试补全

在 `ver_1.0/src/WorkflowRulesTest.java` 末尾新增 4 个测试用例：

| # | 测试名称 | 验收标准 |
| - | -------- | -------- |
| 1 | 密码哈希确定性 | 同一明文两次哈希结果相同，输出长度恰好 64 字符 |
| 2 | 密码哈希安全性 | 存储值不等于明文；不同明文产生不同哈希 |
| 3 | MO 关闭岗位持久化 | 将 OPEN 岗位状态改为 CLOSED 并保存后，重新加载确认 `isOpen()` 返回 `false` |
| 4 | 申请撤回后可重申 | TA 将申请状态改为 WITHDRAWN 后，`canApply()` 检查恢复为 `true` |

---

## 3. 测试结果

所有回归测试均通过：

```
Smoke test passed.
AuthFlowTest passed.
WorkflowRulesTest passed.
CsvPersistenceTest passed.
```

- `SystemSmokeTest` ✅ — 系统初始化、评分、通知、管理员摘要
- `AuthFlowTest` ✅ — 登录验证（含哈希）、注册流程、大小写不敏感查找
- `WorkflowRulesTest` ✅ — 业务流程（含 4 条新增用例）
- `CsvPersistenceTest` ✅ — CSV 读写、特殊字符转义

---

## 4. 遗留说明

- 本次哈希不含随机 salt，如需进一步提升安全性可在未来版本中引入 `SecureRandom` + salt 列
- `ver_1.0/src/models/` 子目录存在与根目录重名的 Model 类（`User.java`、`Job.java` 等），编译时需排除该子目录，建议后续清理
- Demo 账号密码（如 `admin123`）仍展示在 `LoginFrame` 的提示文字和 `README.md` 中，属于演示需要，不影响存储安全性
