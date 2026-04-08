# ProjectRoot (L1 Infrastructure Stage)

这是一个初期的架构项目原型文件夹，专门针对 **EBU6304 Group 98 第1层级 (L1) 基础架构与数据层**。在这个目录下的代码负责底层对象封装、CSV离线持久化机制以及应用的安全种子数据生成过程。

## 一、目前覆盖的任务范围

**主导组 (Pair A):**
- MVC架构创建
- `User`, `TAProfile`, `Job`, `Application` 基础业务封装与序列化接口 (`CsvSerializable`)
- 初版底层的防注入CSV存储引擎 (`CsvStorage<T>`)
- 基础流程验证 (`L1Test.java`)

**协助组 (Pair B) 本次增量:**
- `util/PasswordUtil.java`: 基于 SHA-256 + Random Salt 的强哈希密码服务支持方案构建，防止 `users.csv` 保存明文密码结构。
- `util/DataSeeder.java`: 系统全局数据种子发生器，覆盖全部实体类数据加载，初始化 Admin, MO, TA 等各类初始账号及职位申请测试态数据。
- 环境一键编译脚本 `compile.bat` 和测试/装配执行脚本 `run.bat`。

## 二、如何启动 L1 环境

只需在当前根目录下 (`ProjectRoot/`) 双击运行批处理脚本：

1. **编译代码**: 双击或在终端中运行 `compile.bat`。它将在同目录下产生 `bin/` 并加载通过所有的 `src/*.java`。
2. **初始化环境与测试**: 执行 `run.bat`，系统会自动运行 `DataSeeder` 产生带强哈希安全配置的默认 `.csv` 载体，并执行 `L1Test` 确认核心数据存取链路贯通无误。

## 三、生成的数据 (存放在 data/ 路径下)
- `users.csv`: 保存各个层级的测试账号信息。系统预设登录可用账户 (例如 TA 侧的 `ta1`/`ta2`， MO 侧的 `mo1`/`mo2` 以及 `admin`)。
- `profiles.csv`: 保存部分模拟用户的个性化简历。
- `jobs.csv` 与 `applications.csv`: 存放职位池与用户投递状态，用于后续迭代界面的展示渲染。
