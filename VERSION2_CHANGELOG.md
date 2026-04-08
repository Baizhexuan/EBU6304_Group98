# Version2 修改说明（L1 基础架构与数据层）

> 更新日期：2026-04-08

---

## 新增文件

### `PasswordUtil.java`
密码安全工具类。使用 Java 标准库的 `SHA-256` 算法对密码进行哈希处理，每个密码配合一个 16 字节的随机盐值（Base64 编码）。提供三个静态方法：生成盐值、哈希密码、验证密码。确保明文密码永远不会出现在 CSV 文件中。

---

## 修改文件

### `User.java`
用户模型升级。新增 `passwordHash`（哈希值）和 `salt`（盐值）两个字段，替代原来的明文 `password` 字段。新增 5 参数构造器用于 Version2 格式的用户创建。添加 `checkPassword()` 方法，内部调用 `PasswordUtil` 进行哈希比对。旧的 `password` 字段标记为 `@Deprecated`，保留仅用于向后兼容。

### `FileStorage.java`
存储引擎升级。`users.csv` 的列格式从 `id,username,password,role`（4列）变更为 `id,username,passwordHash,salt,role`（5列）。首次运行生成种子数据时，通过 `PasswordUtil` 对每个默认账号密码进行哈希后再写入。`loadUsers()` 方法同时兼容新旧两种格式（5字段优先，4字段回退），`saveUsers()` 统一输出新格式。

### `LoginFrame.java`
登录验证升级。密码校验从原来的 `matchedUser.password.equals(password)` 明文比较，改为调用 `matchedUser.checkPassword(password)` 进行哈希比对。底部提示文字从 "Version1" 改为 "Version2"。注释中的版本标记同步更新。

### `Main.java`
添加 Version2 的 JavaDoc 文档注释，说明种子数据已改为哈希存储。

### `data/users.csv` 和 `src/data/users.csv`
两份种子数据文件均替换为预生成的哈希格式。文件中不再包含任何明文密码，只有 Base64 编码的哈希值和盐值。Demo 登录密码本身不变（如 admin/admin123），只是存储方式变了。

### `compile.bat`
编译脚本新增 `PasswordUtil.java` 到 `javac` 编译列表中，确保一键编译包含新文件。

### `README.md`
添加 Version2 更新章节，记录所有变更内容：密码哈希机制说明、新文件列表、CSV 格式变更、升级注意事项（需删除旧 `users.csv`）。项目结构树和数据格式表同步更新。

---

## 变更文件清单

| 状态 | 文件路径 |
|------|---------|
| 新增 | `src/PasswordUtil.java` |
| 修改 | `src/User.java` |
| 修改 | `src/FileStorage.java` |
| 修改 | `src/LoginFrame.java` |
| 修改 | `src/Main.java` |
| 修改 | `data/users.csv` |
| 修改 | `src/data/users.csv` |
| 修改 | `compile.bat` |
| 修改 | `README.md` |
