# EBU6304 Group 98 — Pair B 工作日志（Latest）

> 姓名：蔡佳城（Caijiacheng）
> 学号：231223494
> 负责内容：ver_1.0 稳定性优化 + 测试补充
> 日期：2026-05-22

---

## 一、工作目标

在 ver_1.0 最终演示版本基础上，补强运行稳定性、数据校验与回归测试覆盖，降低异常输入导致的 UI 中断风险，并完善通知流程的自动化验证。

---

## 二、已完成工作

1. MO 面板稳定性修复（`ver_1.0/src/MODashboard.java`）
   - 将关键路径中的直接 `Integer.parseInt` 改为 `ValidationUtils.parseInt` 容错解析。
   - 对无效 `jobId` / `appId` 增加防御性判断与用户提示。
   - 在数据异常时执行安全刷新，避免流程中断。
   - 表格渲染中的工时解析改为容错，避免 `NumberFormatException`。

2. Admin 面板校验与导出增强（`ver_1.0/src/AdminDashboard.java`）
   - 应用状态保存前新增白名单校验：`PENDING / SELECTED / REJECTED / WITHDRAWN`。
   - 岗位状态保存前新增白名单校验：`OPEN / CLOSED`。
   - 统一单元格文本提取，规避空值写回风险。
   - 工作量报表导出增加 CSV 转义处理，提升逗号/引号场景兼容性。

3. 通知回归测试补充
   - 新增测试文件：`ver_1.0/src/NotificationFlowTest.java`。
   - 覆盖流程：提醒去重、决策通知、批量已读、岗位关闭通知。
   - 更新脚本：`ver_1.0/test.sh` 已加入 `NotificationFlowTest`。
   - 更新文档：`ver_1.0/README.md` 的测试清单已同步。

---

## 三、交付文件

1. `ver_1.0/src/MODashboard.java`
2. `ver_1.0/src/AdminDashboard.java`
3. `ver_1.0/src/NotificationFlowTest.java`
4. `ver_1.0/test.sh`
5. `ver_1.0/README.md`
6. `ProjectRoot_log/work_record_pairB_4.md`

---

## 四、验证情况

1. 已完成修改文件的静态错误检查，未发现语法错误。
2. 已完成测试入口接线（`test.sh`）。
3. 建议本地最终回归命令：
   - `sh compile.sh`
   - `sh test.sh`

当前测试清单：
- `SystemSmokeTest`
- `AuthFlowTest`
- `WorkflowRulesTest`
- `CsvPersistenceTest`
- `NotificationFlowTest`

---

## 五、结论

本轮主要完成了“稳定性 + 校验 + 测试覆盖”三项增强：

- 降低了异常输入对 MO/Admin 页面的崩溃风险；
- 强化了管理员侧关键字段合法性约束；
- 补齐通知系统关键流程的自动化回归测试。

---

记录人：蔡佳城（Pair B）
审核状态：待确认
