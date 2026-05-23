# EBU6304 Group 98 Demo Version 1.13 中文说明

BUPT International School / QMUL Teaching Assistant Recruitment System  
北京邮电大学国际学院 / QMUL 助教招聘系统

本项目是 EBU6304 小组作业的 Java Swing 桌面端演示系统，主要面向 TA 招聘流程，包含三类用户角色：

- `TA`：Teaching Assistant，助教申请者
- `MO`：Module Organiser，模块负责人
- `Admin`：系统管理员

系统支持 TA 申请岗位、MO 发布岗位和审核申请、Admin 监控工作量，并加入了 AI matching、通知中心、TA-MO 消息、信誉评分、安全校验等功能。

## 项目定位

当前 `ver_1.0` 文件夹是一个可独立运行的 Java Swing demo，符合课程要求：

- 独立 Java 桌面应用
- 使用 CSV 文件存储数据
- 不使用数据库
- 支持 TA / MO / Admin 三角色工作流
- 支持本地可解释 AI matching
- 可选外部 AI API
- 支持英文 / 中文界面切换
- 提供测试脚本、JavaDoc 脚本和交付文档

## 最新功能概览

### TA 功能

- 注册或登录 TA 账号
- 编辑个人资料，包括姓名、邮箱、学号、技能、GPA、CV path、可工作时间和个人陈述
- 浏览开放岗位
- 使用搜索功能筛选岗位
- 查看 AI 匹配分数和岗位 ranking
- 申请选中的岗位
- 申请前会重新校验 email、GPA、CV path、availability 和 statement
- 查看自己的申请状态
- 撤回 `PENDING` 状态的申请
- 被 `REJECTED` 或 `WITHDRAWN` 后可以重新申请同一个岗位
- 通过右上角铃铛查看通知和消息
- 与相关 MO 进行 TA-MO 对话
- 在 MO 未同意前最多发送 3 条消息
- 查看 reputation / 信誉分对后续 matching 的影响

### MO 功能

- 注册或登录 MO 账号
- 发布新的 TA 岗位
- 发布岗位时会校验每周工作时长上限
- 管理自己发布的岗位
- 打开或关闭岗位
- 查看每个岗位的申请者
- 查看 TA 的技能、匹配度、缺失技能、信誉分和当前工作量
- 录用或拒绝 TA 申请
- 在铃铛消息中心同意 TA-MO 对话
- 对已完成工作的 TA 进行评分
- 当 TA 原始匹配分很高但最终完成评分很低时，触发 reputation penalty

### Admin 功能

- 查看全部 TA 的工作量
- 判断 TA 是否处于 `OK`、`NEAR LIMIT` 或 `OVERLOAD` 状态
- 查看系统推荐和调配建议
- 使用 AI Assistant 询问工作量风险和候选人替换建议
- 查看并编辑全部申请记录
- 申请状态编辑会校验合法状态值
- 查看岗位归属，但不能直接修改 Job 的 MO 所有权
- 可以编辑岗位标题、模块、技能、小时数、地点和状态
- 导出 CSV workload report，并防止 CSV injection
- 通过铃铛查看通知和消息提醒

### 跨角色功能

- 支持 `English / 中文` 一键切换
- 顶部右侧铃铛统一显示通知和消息
- 表格长文本自动换行，不再用 `...` 截断
- TA、MO、Admin 页面中的 matching 分数均使用实时计算逻辑
- 左侧表格 matching 和右侧 ranking 使用同一套算法，显示结果保持一致
- AI ranking 面板显示生成时间，方便判断推荐是否过期
- 通知中心最多渲染最新 100 条通知，避免通知过多导致卡顿
- 所有数据使用 CSV 文件存储

## 安全性和校验更新

`ver_1.13` 重点修复了截图中提到的安全性、输入校验和稳定性问题：

- 密码不再明文存储，改为 salted SHA-256 hash
- 旧明文密码仍能登录一次，并会在登录后自动迁移为 hash
- 提交版 `data/users.csv` 已经迁移为 hash 密码
- 注册密码最短 6 位，不能设置 `"a"` 这种弱密码
- Email 正则更严格，必须有带点的 domain，例如 `name@bupt.edu.cn`
- TA 申请岗位时会再次校验 email、GPA、CV path、availability、statement
- GPA 必须在 `0.0 - 4.0`
- MO 发布岗位和 Admin 编辑岗位时，每周小时数不能超过 `20h`
- Admin 不能修改 Job 的 MO ownership，避免岗位归属混乱
- Admin 修改申请状态或岗位状态时，只允许合法状态值
- CSV 保存和导出会中和以 `=`, `+`, `-`, `@`, tab 开头的单元格，降低 Excel 公式注入风险
- CSV 写入方法加了 `synchronized`，降低并发写入损坏文件的风险
- 新增 `data/id_counters.csv`，用计数器减少重复 ID 风险
- CSV 文件会尽量设置为当前用户可读写
- CSV 解析遇到格式异常会输出 warning，不再完全静默跳过
- 外部 AI API 请求已有连接和读取超时，网络慢时不容易一直卡住 UI

## 演示账号

| 角色 | 用户名 | 密码 | 显示名称 |
| --- | --- | --- | --- |
| Admin | `admin` | `admin123` | System Admin |
| TA | `ta1` | `ta123` | Li Ming |
| TA | `ta2` | `ta456` | Wang Yue |
| MO | `mo1` | `mo123` | Dr Chen |
| MO | `mo2` | `mo456` | Prof Zhao |

上表中的密码是演示登录密码；实际 `data/users.csv` 中保存的是 salted hash。

也可以在登录页点击 `Register / 注册` 创建新的 TA 或 MO 账号。

## 如何运行

### Windows PowerShell

进入 `ver_1.0` 项目目录后运行：

编译并运行：

```powershell
.\compile.ps1
.\run.ps1
```

如果 PowerShell 阻止脚本运行：

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

### macOS / Linux

编译：

```bash
sh compile.sh
```

运行：

```bash
sh run.sh
```

`run.sh` 内部使用 `sh ./compile.sh`，所以不会因为 `compile.sh` 没有执行权限而报 `Permission denied`。

## 如何测试

### Windows PowerShell

```powershell
powershell -ExecutionPolicy Bypass -File .\test.ps1
```

### macOS / Linux

```bash
sh test.sh
```

当前测试包含：

- `SystemSmokeTest`
- `AuthFlowTest`
- `WorkflowRulesTest`
- `CsvPersistenceTest`
- `PostWorkFeedbackAndMessagingTest`
- `NotificationFlowTest`
- `ValidationUtilsTest`
- `MatchingServiceTest`
- `ModelStateTest`
- `ScoringServiceTest`
- `NotificationReadStateTest`
- `FileStorageLookupTest`
- `DemoMetadataTest`

这些测试覆盖登录注册、输入校验、CSV 持久化、TA/MO/Admin 工作流、matching、通知、消息限制、完成工作评分、reputation penalty 和 ID 分配。

## Matching 算法说明

系统通过 `ScoringService` 统一计算 TA 和 Job 的匹配度。

在默认离线模式下，本地规则算法逻辑为：

1. 读取 TA profile 中的 `skills`
2. 读取 Job 中的 `requiredSkills`
3. 将技能按 `;`、`,`、`/`、`|` 等分隔符拆分
4. 全部转成小写并去除空格
5. 统计岗位所需技能中有多少被 TA 覆盖
6. 计算公式：

```text
match score = matched required skills / total required skills * 100
```

例如：

```text
TA skills: Java;OOP;Communication
Job required skills: Java;OOP;Communication
Match = 3 / 3 * 100 = 100%
```

如果：

```text
TA skills: Python;Data Structures
Job required skills: Python;Data Structures;Teamwork
Match = 2 / 3 * 100 = 67%
```

系统会同时生成解释文本：

```text
Matched: python, data structures
Missing: teamwork
```

如果 TA reputation 被降低，当前展示的 matching 分数会受到惩罚。

## Matching 和 Ranking 为什么现在一致

之前系统中曾经存在一个容易误解的问题：

- 左侧表格显示的是申请提交时保存的旧 `matchScore`
- 右侧 ranking 显示的是根据当前 TA profile 实时重新计算的分数

现在已经修正：

- TA 页面
- MO 页面
- Admin 页面
- 右侧 AI ranking 面板

都会使用当前 `ScoringService.evaluate()` 实时计算显示分数，因此左侧表格和右侧 ranking 保持一致。

CSV 中旧的 `matchScore` 仍然保留，用作申请提交当时的历史记录，也用于 reputation penalty 判断。

## TA-MO 消息和三条限制

TA 和 MO 可以通过右上角铃铛进入消息中心。

规则如下：

- 只有当 TA 申请了某个 MO 的岗位后，双方才会出现在联系人列表中
- 在 MO 同意对话前，一方最多只能发送 3 条消息
- 第 4 条消息会被系统拦截
- 只有该岗位的 MO 可以点击 `Approve / 同意对话`
- TA 不能同意对话
- MO 同意后，该 TA-MO-job 对话不再受 3 条消息限制

界面中会显示：

```text
Waiting for MO approval. Pre-approval messages left: 2
```

或：

```text
Conversation approved
```

## Reputation 信誉分机制

系统新增了完成工作后的评分机制。

逻辑如下：

- 每个 TA 默认 reputation 为 `100`
- MO 可以对已录用 TA 的完成工作进行评分
- 如果 TA 原始 match score 很高，但 MO 给出的完成评分很低
- 系统会认为 TA 的技能可靠性存在风险
- TA reputation 会被扣分
- 后续 matching 分数会因为 reputation 下降而受到惩罚

重点解释：

```text
这个功能不是自动判定 TA 作弊，而是防止 TA 在 skills 中虚写能力。
没有 reputation penalty 时，TA 可以随便把技能写得很强，系统会一直给高 matching；
加入 MO 最终评分后，如果实际完成质量很低，系统会降低其信誉分，并影响未来 matching。
```

## AI 配置

### 默认离线模式

系统不需要 AI key 也能运行。

如果没有配置外部 AI，系统会使用本地规则型匹配和本地 fallback explanation。

### 可选 qwen-plus 模式

复制配置文件：

```bash
cp config/ai.properties.example config/ai.properties
```

然后在 `config/ai.properties` 中填写自己的 key。

API 申请 URL：

```text
https://bailian.console.aliyun.com/cn-beijing?tab=model#/api-key
```

也可以使用环境变量：

```bash
export OPENAI_API_KEY=your_bailian_key_here
export OPENAI_MODEL=qwen-plus
export OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
export AI_API_MODE=CHAT_COMPLETIONS
export AI_SCORING_MODE=AI
```

如果 key 不可用或模型请求失败，系统仍会自动回到本地可解释逻辑。

## CSV 数据文件

系统所有持久化数据都保存在 `data/` 目录：

- `users.csv`：用户账号，密码为 salted hash
- `profiles.csv`：TA 个人资料
- `jobs.csv`：岗位信息
- `applications.csv`：申请记录
- `notifications.csv`：通知
- `messages.csv`：TA-MO 消息
- `message_consents.csv`：对话同意状态
- `work_evaluations.csv`：完成工作评分
- `ta_reputations.csv`：TA 信誉分
- `id_counters.csv`：ID 计数器，降低重复 ID 风险

Admin 导出的工作量报告格式为：

```text
admin_workload_report_*.csv
```

项目不使用数据库。

## 项目结构

```text
src/                         Java 源代码
data/                        CSV 数据文件
config/ai.properties.example AI 配置模板
docs/                        项目文档
screenshots/                 截图
compile.ps1                  Windows 编译脚本
run.ps1                      Windows 运行脚本
test.ps1                     Windows 测试脚本
javadoc.ps1                  Windows JavaDoc 脚本
compile.sh                   macOS/Linux 编译脚本
run.sh                       macOS/Linux 运行脚本
test.sh                      macOS/Linux 测试脚本
javadoc.sh                   macOS/Linux JavaDoc 生成脚本
README.md                    英文 README
README_zh.md                 中文 README
```

不要上传或提交这些生成文件：

```text
bin/
*.class
javadocs/
config/ai.properties
data/admin_workload_report*.csv
```

## 界面截图

下面这些图片对应当前最新 GUI 界面，包含基础角色流程和新增功能。

### 登录和注册

![最新登录页](screenshots/login.png)

![最新注册页](screenshots/register.png)

### TA 工作流

![TA 我的资料](screenshots/ta_profile.png)

![TA 浏览岗位](screenshots/ta_browse_jobs.png)

![TA 我的申请](screenshots/ta_applications.png)

![TA 通知中心](screenshots/ta_notifications.png)

### MO 工作流

![MO 发布岗位](screenshots/mo_post_job.png)

![MO 申请者页面](screenshots/mo_applicants.png)

### Admin 工作流

![Admin 工作量监控](screenshots/admin_workload.png)

![Admin AI 推荐和系统建议](screenshots/admin_recommendations.png)

### 新增功能截图

### 中英文界面切换

![中英文界面切换](screenshots/feature_bilingual_ui.png)

### 铃铛通知和 TA-MO 消息中心

![铃铛消息中心](screenshots/feature_bell_centre_messages.png)

### MO 同意对话

![MO 同意对话](screenshots/feature_mo_conversation_approval.png)

### 完成工作评分和信誉分

![完成工作评分和信誉分](screenshots/feature_reputation_rating.png)

## 主要源码文件说明

- `Main.java`：程序入口
- `LoginFrame.java`：登录界面，支持旧密码迁移到 hash
- `RegisterFrame.java`：注册界面，包含密码长度校验
- `BaseDashboard.java`：TA/MO/Admin dashboard 公共 UI 基类
- `TADashboard.java`：TA 工作台，包含申请前二次校验
- `MODashboard.java`：MO 工作台，包含岗位小时数校验和完成工作评分
- `AdminDashboard.java`：Admin 工作台，包含状态校验、MO ownership 锁定和安全 CSV 导出
- `I18n.java`：中英文界面切换
- `NotificationCenterDialog.java`：铃铛通知和消息中心
- `MessageService.java`：TA-MO 消息和三条限制逻辑
- `NotificationService.java`：通知生成、读取和关闭岗位通知处理
- `ScoringService.java`：统一 matching 调度
- `RuleBasedSkillScoringProvider.java`：本地规则匹配算法
- `ReputationService.java`：信誉分和惩罚逻辑
- `PasswordService.java`：salted SHA-256 密码 hash 和校验
- `FileStorage.java`：CSV 读写、ID 计数器和安全保存

## 版本记录

- `ver_1.0`：第一个完整集成 demo
- `ver_1.1`：加入筛选和 Admin 监控优化
- `ver_1.2`：共享 dashboard 基类和认证检查
- `ver_1.3`：加强 Admin 操作和 AI-ready scoring 抽象
- `ver_1.4`：AI placeholder、Admin reallocation recommendation 和 UI 优化
- `ver_1.5`：优化登录、注册和最终演示体验
- `ver_1.6`：CSV-backed notifications 和多字段搜索
- `ver_1.7`：完善 profile reminder 和 job closure alerts
- `ver_1.8`：跨平台 UI 修复和 AI Assistant dialog
- `ver_1.9`：Admin AI Assistant 支持外部模型调用
- `ver_1.10`：qwen-plus 配置、测试脚本、JavaDoc 和交付文档
- `ver_1.11`：搜索、刷新和 dashboard 响应体验优化
- `ver_1.12`：中英文 UI、铃铛消息中心、MO 同意对话、三条消息限制、完成工作评分、reputation penalty、rejected 后重新申请、表格换行和实时 matching/ranking 一致性
- `ver_1.13`：安全和校验加固、salted password hash、申请前二次校验、Admin MO ownership 锁定、安全 CSV 导出、同步 CSV 写入、ID counter、AI ranking 时间戳、通知分页和跨平台脚本更新
