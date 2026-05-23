# EBU6304 Group 98 Demo Version 1.12 中文说明

BUPT International School / QMUL Teaching Assistant Recruitment System  
北京邮电大学国际学院 / QMUL 助教招聘系统

本项目是 EBU6304 小组作业的 Java Swing 桌面端演示系统，主要面向 TA 招聘流程，包含三类用户角色：

- `TA`：Teaching Assistant，助教申请者
- `MO`：Module Organiser，模块负责人
- `Admin`：系统管理员

系统支持 TA 申请岗位、MO 发布岗位和审核申请、Admin 监控工作量，并加入了 AI matching、通知中心、TA-MO 消息、信誉评分等功能。

## 项目定位

当前 `ver_1.0` 文件夹是一个可独立运行的 Java Swing demo，符合课程要求：

- 独立 Java 桌面应用
- 使用 CSV 文件存储数据
- 不使用数据库
- 支持 TA / MO / Admin 三角色工作流
- 支持本地可解释 AI matching
- 可选外部 AI API
- 支持英文 / 中文界面切换

## 最新功能概览

### TA 功能

- 注册或登录 TA 账号
- 编辑个人资料，包括姓名、邮箱、学号、技能、GPA、CV path、可工作时间和个人陈述
- 浏览开放岗位
- 使用搜索功能筛选岗位
- 查看 AI 匹配分数和岗位 ranking
- 申请选中的岗位
- 查看自己的申请状态
- 撤回 `PENDING` 状态的申请
- 被 `REJECTED` 后可以重新申请同一个岗位
- 通过右上角铃铛查看通知和消息
- 与相关 MO 进行 TA-MO 对话
- 在 MO 未同意前最多发送 3 条消息
- 查看 reputation / 信誉分对后续匹配的影响

### MO 功能

- 注册或登录 MO 账号
- 发布新的 TA 岗位
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
- 查看并编辑全部岗位记录
- 导出 CSV workload report
- 通过铃铛查看通知和消息提醒

### 跨角色功能

- 支持 `English / 中文` 一键切换
- 顶部右侧铃铛统一显示通知和消息
- 表格长文本自动换行，不再用 `...` 截断
- TA、MO、Admin 页面中的 matching 分数均使用实时计算逻辑
- 左侧表格 matching 和右侧 ranking 使用同一套算法，显示结果保持一致
- 所有数据使用 CSV 文件存储

## 演示账号

| 角色 | 用户名 | 密码 | 显示名称 |
| --- | --- | --- | --- |
| Admin | `admin` | `admin123` | System Admin |
| TA | `ta1` | `ta123` | Li Ming |
| TA | `ta2` | `ta456` | Wang Yue |
| MO | `mo1` | `mo123` | Dr Chen |
| MO | `mo2` | `mo456` | Prof Zhao |

也可以在登录页点击 `Register / 注册` 创建新的 TA 或 MO 账号。

## 如何运行

### Windows PowerShell

进入项目目录：

```powershell
cd "C:\Users\lenovo\Desktop\软件工程\Project\EBU6304_Group98-main\ver_1.0"
```

编译：

```powershell
javac -encoding UTF-8 -d bin src\*.java
```

运行：

```powershell
java -cp bin Main
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

## 如何测试

### Windows PowerShell

```powershell
javac -encoding UTF-8 -d bin src\*.java
java -cp bin SystemSmokeTest
java -cp bin AuthFlowTest
java -cp bin WorkflowRulesTest
java -cp bin CsvPersistenceTest
java -cp bin PostWorkFeedbackAndMessagingTest
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

其中 `PostWorkFeedbackAndMessagingTest` 用于测试新增的消息限制、MO 同意对话、完成工作评分和 reputation penalty 功能。

## 推荐演示流程

答辩或课堂展示时，可以按以下顺序演示：

1. 打开系统，在登录页切换 `English / 中文`
2. 使用 `ta1 / ta123` 登录 TA
3. 在 `我的资料` 页面填写并保存 TA profile
4. 在 `浏览岗位` 页面搜索 `Java`
5. 查看右侧 `AI 匹配排名`
6. 选中一个岗位并点击 `申请选中岗位`
7. 在 `我的申请` 页面查看新申请状态
8. 打开右上角铃铛，进入 `Messages`
9. TA 连续发送 3 条消息，再发送第 4 条，展示未同意时会被限制
10. 使用相关 MO 账号登录，在铃铛消息中心点击 `Approve / 同意对话`
11. TA 再次发送消息，展示限制解除
12. MO 发布一个新岗位
13. MO 查看申请者，录用或拒绝 TA
14. MO 对已录用 TA 的完成工作进行评分
15. 展示低评分如何影响 TA reputation 和后续 matching
16. Admin 登录，查看 workload monitor
17. Admin 查看 AI recommendation，编辑申请和岗位记录
18. Admin 导出 CSV report

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

注意：

```text
这不是自动判定 TA 造假或违规，而是作为后续 matching 的可靠性信号。
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

- `users.csv`：用户账号
- `profiles.csv`：TA 个人资料
- `jobs.csv`：岗位信息
- `applications.csv`：申请记录
- `notifications.csv`：通知
- `messages.csv`：TA-MO 消息
- `message_consents.csv`：对话同意状态
- `work_evaluations.csv`：完成工作评分
- `ta_reputations.csv`：TA 信誉分

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
compile.sh                   编译脚本
run.sh                       运行脚本
test.sh                      测试脚本
javadoc.sh                   JavaDoc 生成脚本
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

## 主要源码文件说明

- `Main.java`：程序入口
- `LoginFrame.java`：登录界面
- `RegisterFrame.java`：注册界面
- `BaseDashboard.java`：TA/MO/Admin dashboard 公共 UI 基类
- `TADashboard.java`：TA 工作台
- `MODashboard.java`：MO 工作台
- `AdminDashboard.java`：Admin 工作台
- `I18n.java`：中英文界面切换
- `NotificationCenterDialog.java`：铃铛通知和消息中心
- `MessageService.java`：TA-MO 消息和三条限制逻辑
- `NotificationService.java`：通知生成与读取
- `ScoringService.java`：统一 matching 调度
- `RuleBasedSkillScoringProvider.java`：本地规则匹配算法
- `ReputationService.java`：信誉分和惩罚逻辑
- `FileStorage.java`：CSV 读写

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

