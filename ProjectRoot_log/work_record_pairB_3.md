# EBU6304 Group 98 — Pair B 工作日志（L3）

> 姓名：蔡佳城  刘一帆
> 负责层次：L3 — 核心业务逻辑层（评审 + 测试）  
> 日期：2026-04-09

---

## 一、工作目标

根据任务分工，L3 阶段 Pair B 的职责不是直接主导功能开发，而是对 US-1 至 US-6 的核心业务流程进行测试验证与代码评审，确认当前仓库中的实现是否达到验收标准，并形成可交付的测试与审查记录。

---

## 二、已完成任务

1. 新增集成测试文件 BusinessLogicTest.java
   - 放置于主项目测试目录，覆盖 TA 端当前已经具备可执行路径的 L3 业务流程。
   - 测试采用与项目现有测试一致的无 JUnit main 方法执行方式，避免额外引入依赖。

2. 完成 US-1 / US-2 测试验证
   - 验证 TA 个人资料编辑时的邮箱格式校验。
   - 验证 GPA 超出 0.0 到 4.0 范围时被拒绝。
   - 验证资料首次保存与再次编辑更新均能正确持久化。

3. 完成 US-3 测试验证
   - 验证 TA 只能浏览状态为 OPEN 的岗位。
   - 验证按模块关键字过滤与按技能关键字过滤逻辑可正常工作。

4. 完成 US-4 测试验证
   - 验证 TA 在已完善 Profile 的情况下能够提交岗位申请。
   - 验证申请状态初始化为 PENDING。
   - 验证重复申请会被系统拦截，不会产生重复数据。
   - 验证 PENDING、SELECTED、REJECTED 三种申请状态在持久化后能被正确读取。

5. 完成 L3 代码评审记录
   - 对 TADashboard 与 MODashboard 当前实现进行检查。
   - 明确指出当前仓库中 US-5 与 US-6 尚未真正落地，不能被视为已通过验收。

---

## 三、评审发现

### 1. US-5 尚未完成

对应文件：ProjectRoot/src/com/bupt/ta/recruitment/ui/MODashboard.java

- Post Job 页面目前只有表单输入控件，没有提交按钮。
- 没有岗位发布校验逻辑，也没有写入 jobs.csv 的保存操作。
- My Posts 页面虽然能列出岗位，但没有实现 OPEN 到 CLOSED 的关闭动作。

结论：
US-5 目前只能认定为界面骨架，不满足任务计划中的功能验收标准。

### 2. US-6 尚未完成

对应文件：ProjectRoot/src/com/bupt/ta/recruitment/ui/MODashboard.java

- Applicants 页面当前为只读表格。
- 没有选岗下拉框，没有 Select / Reject 操作按钮。
- 没有更新 Application.status 的业务逻辑。

结论：
US-6 当前未形成完整业务闭环，TA 侧虽然已经支持状态展示，但 MO 侧无法驱动状态变化。

### 3. Service 层封装要求仍未达成

对应文件：
- ProjectRoot/src/com/bupt/ta/recruitment/ui/TADashboard.java
- ProjectRoot/src/com/bupt/ta/recruitment/ui/MODashboard.java
- ProjectRoot/src/com/bupt/ta/recruitment/service/AuthService.java

- 任务计划要求在 L3 补充 TAService、MOService、AdminService。
- 当前仓库里 TA 与 MO 的业务规则仍然直接写在 Swing UI 类中，并直接访问 CsvStorage。

结论：
这会降低复用性与可测试性，也导致 Pair B 的测试只能覆盖当前暴露出来的行为，而无法基于独立服务层做更稳定的业务验证。

---

## 四、测试策略说明

由于当前仓库尚未提供完整的 MO 业务处理入口，BusinessLogicTest 采取如下策略：

- 对已具备真实实现路径的 US-1 至 US-4 进行可执行测试。
- 对尚未实现的 US-5、US-6 明确标记为跳过，不伪造通过结果。
- 使用独立测试目录 data/test_l3_pairb 存放临时数据，避免污染正式数据文件。

这样可以保证 Pair B 的测试结论真实反映当前仓库状态，而不是给出失真的“全部通过”报告。

---

## 五、本轮交付物

1. ProjectRoot/src/com/bupt/ta/recruitment/test/BusinessLogicTest.java
2. ProjectRoot_log/work_record_pairB_3.md

---

## 六、后续建议

1. 由负责实现的同学尽快补全 MODashboard 中的岗位发布、关闭岗位、选择申请者、拒绝申请者等交互与持久化逻辑。
2. 将 TA 与 MO 的业务规则从 UI 中抽离到独立 Service 层，便于后续 L4、L5 复用。
3. 在 US-5 与 US-6 完成后，扩展 BusinessLogicTest，将当前的 SKIP 项转为真实的 PASS 或 FAIL。

---

记录人：蔡佳城（Pair B）  
审核状态：待确认