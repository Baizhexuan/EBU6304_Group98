# EBU6304 Group 98 — Pair B 工作日志 (L2)

> **姓名**：蔡佳城  
> **负责层次**：L2 — 认证与 UI 框架层 (主导)  
> **日期**：2026-04-09

---

## 一、工作目标

根据项目分工，本轮由 Pair B 主导 L2 的开发。核心目标是构建系统的用户认证体系（登录与注册）和三种核心角色（Admin, MO, TA）的 UI 仪表盘（Dashboard）基础框架。

---

## 二、已完成任务清单

1.  **认证服务层 (`AuthService`) 封装**
    *   创建了 `service` 包，并添加 `AuthService.java`。
    *   将用户登录 (`login`) 和注册 (`register`) 的核心业务逻辑从 UI 层剥离，实现了与 `CsvStorage` 和 `PasswordUtil` 的交互。
    *   `login` 方法能够验证用户名和哈希密码，返回一个包含 `User` 对象的 `Optional`。
    *   `register` 方法能够检查用户名唯一性，并使用 `PasswordUtil` 创建新的加密用户记录。

2.  **登录与注册 UI 界面 (`LoginFrame` & `RegisterFrame`)**
    *   创建了 `ui` 包，并添加了 `LoginFrame.java` 和 `RegisterFrame.java`。
    *   **LoginFrame**:
        *   提供了用户名和密码输入框。
        *   实现了详细的登录失败提示（如“用户不存在”或“密码错误”），提升了用户体验。
        *   登录成功后，能够根据用户角色动态跳转到对应的 Dashboard。
    *   **RegisterFrame**:
        *   提供了用户名、密码、确认密码的输入框以及角色选择的下拉菜单。
        *   实现了密码二次确认和用户名唯一性校验。
        *   注册成功后自动跳转回登录界面。

3.  **Dashboard UI 抽象基类与骨架 (`BaseDashboard` & 子类)**
    *   创建了抽象基类 `BaseDashboard.java`，集成了通用的窗口设置、欢迎语（"Welcome, [username] ([role])"）和“登出”功能。
    *   创建了 `TADashboard.java`, `MODashboard.java`, `AdminDashboard.java` 三个子类，它们均继承自 `BaseDashboard`，为后续 L3/L4 的功能实现提供了 UI 容器。

4.  **系统启动入口与脚本更新**
    *   创建了 `Main.java` 作为程序的唯一入口，其 `main` 方法负责在 Swing 的事件调度线程中安全地启动 `LoginFrame`。
    *   修改了 `run.bat` 脚本，使其在生成种子数据后，直接编译并运行 `com.bupt.ta.recruitment.Main`，实现了 L1 到 L2 的完整流程打通。

---

## 三、关键设计决策

*   **服务层与 UI 层分离**：将认证逻辑封装在 `AuthService` 中，是典型的分层设计思想。这样做的好处是 UI 代码只负责展示和用户交互，而业务规则（如密码如何验证）则由服务层管理。这使得代码更易于维护、测试和扩展。
*   **抽象 Dashboard 基类**：通过创建 `BaseDashboard`，我们将所有角色共有的 UI 元素和行为（如登出逻辑）集中管理。这遵循了 DRY (Don't Repeat Yourself) 原则，未来如果需要修改登出功能，只需修改一处代码即可。
*   **精细化的错误提示**：在登录时，系统会区分“用户不存在”和“密码错误”，而不是笼统地提示“登录失败”。这虽然增加了少量代码，但极大地改善了最终用户的操作体验。

---

## 四、遇到的困难与解决方案

*   **困难**：最初的设想是将登录逻辑直接写在 `LoginFrame` 的按钮事件监听器中。但在编写过程中发现，代码会与文件读写、密码校验等逻辑高度耦合，非常混乱。
*   **解决方案**：果断重构，引入 `AuthService` 服务层。虽然这增加了一个类，但它让 `LoginFrame` 的职责变得非常单一和清晰，代码可读性大大提高。

---

## 五、后续计划

*   等待 Pair C 完成 L2 的协助任务（如 UI 样式美化、输入验证增强等）。
*   准备评审 Pair A 在 L3 阶段的核心业务逻辑实现。
*   为 L4 阶段由本组（Pair B）协助的高级功能（如用户管理、邮件通知等）做技术预研。
