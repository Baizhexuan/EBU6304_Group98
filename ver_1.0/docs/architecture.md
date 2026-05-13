# Demo Architecture

```text
software_engineering/
├── src/
│   ├── Main.java
│   ├── DemoMetadata.java
│   ├── LoginFrame.java
│   ├── RegisterFrame.java
│   ├── BaseDashboard.java
│   ├── TADashboard.java
│   ├── MODashboard.java
│   ├── AdminDashboard.java
│   ├── AdminRecommendationService.java
│   ├── AIConversationService.java
│   ├── AIConversationDialog.java
│   ├── FilterToolbar.java
│   ├── Notification.java
│   ├── NotificationService.java
│   ├── FileStorage.java
│   ├── MatchingService.java
│   ├── SkillScoringProvider.java
│   ├── RuleBasedSkillScoringProvider.java
│   ├── AIModelSkillScoringProvider.java
│   ├── ScoringService.java
│   ├── AIIntegrationPlan.java
│   ├── MatchResult.java
│   ├── ValidationUtils.java
│   ├── User.java
│   ├── TAProfile.java
│   ├── Job.java
│   ├── Application.java
│   └── SystemSmokeTest.java
├── data/
│   ├── users.csv
│   ├── profiles.csv
│   ├── jobs.csv
│   ├── applications.csv
│   ├── notifications.csv
│   └── admin_workload_report_*.csv
├── docs/
│   ├── architecture.md
│   └── task_plan_alignment.md
├── compile.sh
├── run.sh
└── README.md
```

## Layering

- UI layer: `LoginFrame`, `RegisterFrame`, `BaseDashboard`, `TADashboard`, `MODashboard`, `AdminDashboard`
- Domain layer: `User`, `TAProfile`, `Job`, `Application`, `Notification`, `MatchResult`
- Service layer: `MatchingService`, `ScoringService`, `AdminRecommendationService`, `NotificationService`, `ValidationUtils`
- AI integration seam: `SkillScoringProvider`, `RuleBasedSkillScoringProvider`, `AIModelSkillScoringProvider`, `AIIntegrationPlan`
- Persistence layer: `FileStorage`
- Verification entry: `SystemSmokeTest`

## Iteration 1.6 Additions

- `Notification` and `NotificationService`: implement an in-app notification path for TA status updates
- `FileStorage`: now manages `notifications.csv` in the same text-based persistence style as the rest of the demo
- `TADashboard`: adds a notification tab, unread tracking, aligned per-column search fields, and clearer missing-skills AI output
- `MODashboard`: adds aligned per-column filters and automatically generates notifications when applicant decisions are made
- `AdminDashboard`: replaces single keyword search bars with aligned field-by-field filters and expands AI explanation visibility
- `LoginFrame` and `BaseDashboard`: strengthen visible button outlines and final-demo interaction clarity


## Iteration 1.7 Notification Extension

- `NotificationService.notifyProfileRequired` adds a deduplicated unread reminder when a TA profile is missing or incomplete.
- `NotificationService.markProfileReminderResolved` clears the reminder after a complete profile is saved.
- `NotificationService.notifyJobClosed` sends job-closure alerts to TAs with active applications when MO or Admin closes a job.
- Notifications remain stored in `data/notifications.csv`, preserving the no-database coursework constraint.


## Iteration 1.8 UI and AI Assistant Update

- `BaseDashboard` and `RegisterFrame` use a basic Swing button UI for more reliable text and border rendering across macOS, Windows, and Linux.
- `FilterToolbar` adds a compact attribute selector plus keyword input while preserving aligned per-column filters.
- `AIConversationDialog` and `AIConversationService` add an interactive Admin AI assistant with external model support and local explainable fallback.
- `AdminRecommendationService` now includes risk labels, projected-load reasoning, and suggested next actions.
