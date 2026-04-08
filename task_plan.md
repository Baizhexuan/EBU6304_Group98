# EBU6304 Group 98 — 层次化任务分工（Layered Task Assignment）

> **项目名称**：BUPT International School TA Recruitment System  
> **团队规模**：6 人 = 3 组（Pair A / Pair B / Pair C，每组 2 人）  
> **分工模式**：层次化轮转（每层一个主导组，三组交替）  

---

## 一、层次总览

项目从底层到顶层分为 **6 层**，三组以轮转方式交替主导，保证每组主导 2 层、协助 2 层、评审测试 2 层，工作量对称。

| 层级 | 名称 | 主导 | 协助 | 评审+测试 |
|------|------|------|------|----------|
| **L1** | 基础架构与数据层 | Pair A | Pair B | Pair C |
| **L2** | 认证与 UI 框架层 | Pair B | Pair C | Pair A |
| **L3** | 核心业务逻辑层 | Pair C | Pair A | Pair B |
| **L4** | 管理与高级功能层 | Pair A | Pair B | Pair C |
| **L5** | AI 智能特性层 | Pair B | Pair C | Pair A |
| **L6** | 测试、文档与交付层 | Pair C | Pair A | Pair B |

---

## 二、各层详细任务

---

### L1 — 基础架构与数据层

> 主导：**Pair A** ｜ 协助：Pair B ｜ 评审+测试：Pair C

**功能目标**：搭建项目骨架，实现所有数据模型和存储引擎。

#### Pair A（主导）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 1 | 创建包结构 `model/` `ui/` `service/` `util/` `test/` | 目录 | MVC 分层清晰 |
| 2 | 实现 `User` 模型（id, username, passwordHash, salt, role），含 getter/setter/equals | `model/User.java` | 字段私有封装 |
| 3 | 实现 `TAProfile` 模型（id, userId, fullName, email, studentId, skills, gpa, cvPath） | `model/TAProfile.java` | 同上 |
| 4 | 实现 `Job` 模型（id, moId, title, module, description, requiredSkills, maxHours, status） | `model/Job.java` | 同上 |
| 5 | 实现 `Application` 模型（id, taId, jobId, status, appliedAt） | `model/Application.java` | 同上 |
| 6 | 实现 CSV 存储引擎（泛型 CRUD，支持引号转义，正式解析） | `util/CsvStorage.java` | 正确读写含逗号/引号字段 |

#### Pair B（协助）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 7 | 密码哈希工具类（SHA-256 + 随机盐值） | `util/PasswordUtil.java` | 明文密码不出现在 CSV |
| 8 | 种子数据生成器（5+ 用户 覆盖 TA/MO/ADMIN，3+ 示例岗位） | `util/DataSeeder.java` + `data/*.csv` | 一键生成可用种子 |
| 9 | 编写 `compile.bat` / `run.bat` / 初始 `README.md` | 脚本 + 文档 | 一键编译运行成功 |

#### Pair C（评审+测试）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 10 | 数据模型 JUnit 测试（构造、序列化、反序列化） | `test/ModelTest.java` | 每个模型 ≥ 3 个用例 |
| 11 | CSV 存储引擎 JUnit 测试（增删改查、特殊字符） | `test/CsvStorageTest.java` | CRUD 全覆盖 |
| 12 | Code Review 本层所有代码 | Review 记录 | 每个提交 ≥ 1 条审查意见 |

---

### L2 — 认证与 UI 框架层

> 主导：**Pair B** ｜ 协助：Pair C ｜ 评审+测试：Pair A

**功能目标**：完成登录注册系统，建立三种角色 Dashboard 的 UI 骨架和通用组件。

#### Pair B（主导）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 1 | 登录界面（用户名/密码校验、分级错误提示：空字段/用户不存在/密码错误） | `ui/LoginFrame.java` | 三级错误区分 |
| 2 | 注册界面（选择角色、用户名唯一性检查、密码确认） | `ui/RegisterFrame.java` | 注册后可立即登录 |
| 3 | Dashboard 基类（Logout 菜单、标签页框架、窗口标题模板） | `ui/BaseDashboard.java` | 三个 Dashboard 继承复用 |
| 4 | 登录成功后按 role 路由到对应 Dashboard | `ui/LoginFrame.java` | TA→TADash, MO→MODash, ADMIN→AdminDash |

#### Pair C（协助）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 5 | TADashboard 骨架（Profile / Browse Jobs / My Applications 三标签页） | `ui/TADashboard.java` | 标签切换正常 |
| 6 | MODashboard 骨架（Post Job / My Posts / Applicants 三标签页） | `ui/MODashboard.java` | 同上 |
| 7 | AdminDashboard 骨架（Workload / All Apps / All Jobs 三标签页） | `ui/AdminDashboard.java` | 同上 |
| 8 | 通用 UI 工具类（表格排序器、颜色常量、邮箱/GPA 格式验证器） | `util/UIHelper.java` | 可被各 Dashboard 复用 |

#### Pair A（评审+测试）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 9 | 登录/注册集成测试（正确登录、错误登录、重复注册） | `test/AuthTest.java` | ≥ 5 个用例 |
| 10 | Code Review 本层所有代码 | Review 记录 | 重点检查安全漏洞 |

---

### L3 — 核心业务逻辑层

> 主导：**Pair C** ｜ 协助：Pair A ｜ 评审+测试：Pair B

**功能目标**：实现 TA 和 MO 的全部核心操作（US-1 至 US-6），系统主流程跑通。

#### Pair C（主导）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 1 | **US-1/US-2**: TA 编辑个人资料（姓名、邮箱、学号、技能、GPA、CV 文件浏览） | `ui/TADashboard.java` 补全 | 邮箱格式校验、GPA 0.0-4.0、JFileChooser |
| 2 | **US-3**: TA 浏览 OPEN 岗位（表格展示 + 按模块/技能过滤 + 列排序） | `ui/TADashboard.java` 补全 | 过滤+排序可用 |
| 3 | **US-4**: TA 申请岗位（重复申请检查、时间戳记录） | `ui/TADashboard.java` 补全 | 重复申请弹窗拦截 |
| 4 | TA 申请状态视图（颜色标记：绿=Selected, 红=Rejected, 橙=Pending） | `ui/TADashboard.java` 补全 | 三色渲染正确 |

#### Pair A（协助）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 5 | **US-5**: MO 发布岗位（表单验证：非空、小时数为正整数） | `ui/MODashboard.java` 补全 | 验证拦截非法输入 |
| 6 | **US-5**: MO 管理已发布岗位（列表 + 关闭岗位 OPEN→CLOSED） | `ui/MODashboard.java` 补全 | 关闭后列表刷新 |
| 7 | **US-6**: MO 查看申请者并选择/拒绝（下拉选岗→申请者表格→操作按钮） | `ui/MODashboard.java` 补全 | 操作后状态即时更新 |
| 8 | Service 层封装（TAService, MOService, AdminService：UI 不直接调 Storage） | `service/*.java` | 业务逻辑与 UI 解耦 |

#### Pair B（评审+测试）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 9 | US-1~US-6 集成测试（每个 US ≥ 2 个用例） | `test/BusinessLogicTest.java` | 全部通过 |
| 10 | Code Review 本层所有代码 | Review 记录 | 重点检查边界条件 |

---

### L4 — 管理与高级功能层

> 主导：**Pair A** ｜ 协助：Pair B ｜ 评审+测试：Pair C

**功能目标**：完成 Admin 全面板功能、通知系统、高级编辑能力。

#### Pair A（主导）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 1 | **US-7**: Admin 工作量监控面板（TA 汇总表：总申请/已选中/总小时 + 导出 CSV + 按领域过滤） | `ui/AdminDashboard.java` 补全 | 导出文件可 Excel 打开 |
| 2 | Admin 全局申请管理（表格内联编辑 + Save Changes / Undo Changes） | `ui/AdminDashboard.java` 补全 | 编辑写入 CSV、Undo 恢复 |
| 3 | Admin 全局岗位管理（内联编辑 + 验证：MO 角色检查、数值格式） | `ui/AdminDashboard.java` 补全 | 非法编辑弹窗拒绝 |
| 4 | Admin 脏数据追踪（切换标签页/关闭窗口时提示未保存修改） | `ui/AdminDashboard.java` 补全 | 未保存弹窗确认 |

#### Pair B（协助）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 5 | **US-8**: 应用内通知系统（MO 选择/拒绝时生成通知→TA 登录可见未读消息） | `service/NotificationService.java` + `model/Notification.java` | 通知自动生成+已读标记 |
| 6 | 通知持久化（notifications.csv，CsvStorage 扩展） | `util/CsvStorage.java` 扩展 | 格式与其他 CSV 一致 |
| 7 | 工作量过载高亮（>20 小时整行标红）+ 底部统计摘要 | `ui/AdminDashboard.java` 补全 | 视觉明确区分 |

#### Pair C（评审+测试）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 8 | Admin 功能测试 + 通知系统测试 | `test/AdminTest.java` + `test/NotificationTest.java` | 编辑/保存/撤销/通知生成 |
| 9 | Code Review 本层所有代码 | Review 记录 | 重点检查数据一致性 |

---

### L5 — AI 智能特性层

> 主导：**Pair B** ｜ 协助：Pair C ｜ 评审+测试：Pair A

**功能目标**：实现 AI 技能匹配和工作量平衡推荐。

#### Pair B（主导）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 1 | **US-9**: 技能匹配引擎（TA skills vs Job requiredSkills 关键词重叠算法，返回 0-100% 匹配度） | `service/SkillMatcher.java` | 算法可解释、结果合理 |
| 2 | TA Browse Jobs 页新增"匹配度"列 + 按匹配度排序 | `ui/TADashboard.java` 增强 | 列可见、可排序 |
| 3 | MO View Applicants 页新增"推荐评分"列（匹配度越高越靠前） | `ui/MODashboard.java` 增强 | 评分列+自动排序 |

#### Pair C（协助）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 4 | **US-10**: 工作量平衡引擎（分析各 TA 已分配小时数，建议最优分配方案） | `service/WorkloadBalancer.java` | 输出建议列表，避免超载 |
| 5 | Admin 面板集成工作量平衡视图（展示建议分配 + 一键应用） | `ui/AdminDashboard.java` 增强 | 可视化+操作按钮 |

#### Pair A（评审+测试）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 6 | AI 算法正确性测试 + 端到端流程测试 + 性能边界测试（100+ 数据） | `test/AITest.java` + `test/E2ETest.java` | 全部通过、无崩溃 |
| 7 | Code Review 本层所有代码 | Review 记录 | 算法逻辑审查 |

---

### L6 — 测试、文档与交付层

> 主导：**Pair C** ｜ 协助：Pair A ｜ 评审+测试：Pair B

**功能目标**：补全测试覆盖率、完善所有文档、最终打包交付。

#### Pair C（主导）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 1 | 补全所有 JavaDoc 注释（每个 public 类和方法） | 所有 `.java` 文件 | `javadoc` 命令零警告 |
| 2 | 生成 JavaDoc HTML 站点 | `docs/javadoc/` | 浏览器可正常查看 |
| 3 | 补全 JUnit 测试至覆盖率 ≥ 60% | `test/*.java` | 覆盖率报告可出具 |

#### Pair A（协助）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 4 | UI 最终打磨（布局对齐、小屏适配 800×600、字体统一） | 所有 UI 文件 | 小屏无截断 |
| 5 | 更新 Product Backlog（所有 US 标记完成状态） | `ProductBacklog.md` | 状态明确 |
| 6 | 编写最终 README.md（运行指南 + 功能清单 + 架构图 + Demo 账号） | `README.md` | 新人 5 分钟跑通 |

#### Pair B（评审+测试）
| # | 任务 | 产出 | 验收标准 |
|---|------|------|---------|
| 7 | 编写项目报告（Scrum 过程、设计决策、UML 类图/序列图/用例图） | `Report_group98.pdf` | 含三种 UML 图 |
| 8 | 编写 Sprint Review / Retrospective 记录 | 报告附录 | 每层有总结 |
| 9 | 最终集成测试 + Bug 修复 + 打包交付（清理 bin/、校验编译运行） | 最终提交包 | 干净的包、零崩溃 |

---

## 三、各组工作量总览

| 组别 | 主导层 | 协助层 | 评审+测试层 | 核心产出 |
|------|--------|--------|------------|---------|
| **Pair A** | L1, L4 | L3, L6 | L2, L5 | 数据架构、Admin 全功能、UI 打磨 |
| **Pair B** | L2, L5 | L1, L4 | L3, L6 | 登录注册框架、AI 智能特性、项目报告 |
| **Pair C** | L3, L6 | L2, L5 | L1, L4 | TA/MO 核心业务、JavaDoc+测试+交付 |

> 每组：主导 2 层 + 协助 2 层 + 评审测试 2 层 = **完全对称**。

---

## 四、质量要求

| 检查项 | 标准 |
|--------|------|
| 编译 | `compile.bat` 零错误 |
| 运行 | `run.bat` 启动无崩溃 |
| 测试 | 该层新增测试全部通过 |
| Code Review | 所有代码至少 1 人审查通过 |
| JavaDoc | 新增 public 类/方法必须有文档注释 |
| 提交规范 | 遵循 `COMMIT_STANDARD.md` |
