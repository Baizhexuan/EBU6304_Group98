# Demo Architecture

```text
ver_1.0/
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
│   ├── AIConfig.java
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
│   ├── SystemSmokeTest.java
│   ├── AuthFlowTest.java
│   ├── WorkflowRulesTest.java
│   ├── CsvPersistenceTest.java
│   └── TestSupport.java
├── data/
│   ├── users.csv
│   ├── profiles.csv
│   ├── jobs.csv
│   ├── applications.csv
│   ├── notifications.csv
│   └── admin_workload_report_*.csv
├── config/
│   └── ai.properties.example
├── docs/
│   ├── architecture.md
│   ├── task_plan_alignment.md
│   ├── final_requirement_checklist.md
│   ├── testing_strategy.md
│   └── user_manual.md
├── screenshots/
├── compile.sh
├── run.sh
├── test.sh
├── javadoc.sh
└── README.md
```

## Layering

- UI layer: `LoginFrame`, `RegisterFrame`, `BaseDashboard`, `TADashboard`, `MODashboard`, `AdminDashboard`
- Domain layer: `User`, `TAProfile`, `Job`, `Application`, `Notification`, `MatchResult`
- Service layer: `MatchingService`, `ScoringService`, `AdminRecommendationService`, `NotificationService`, `ValidationUtils`
- AI integration seam: `SkillScoringProvider`, `RuleBasedSkillScoringProvider`, `AIModelSkillScoringProvider`, `AIConversationService`, `AIConversationDialog`, `AIConfig`, `AIIntegrationPlan`
- Persistence layer: `FileStorage`
- Verification layer: `SystemSmokeTest`, `AuthFlowTest`, `WorkflowRulesTest`, `CsvPersistenceTest`, `TestSupport`

## Final-Delivery Design Notes

- The project remains a stand-alone Java Swing application to satisfy the coursework platform constraint.
- Persistence remains CSV-only so the app stays simple, transparent, and easy to inspect during demo and viva.
- Scoring is deliberately modular through `SkillScoringProvider` so the offline explainable path and the optional external-model path can coexist safely.
- Notifications, admin workload checks, and recommendation text are implemented inside the same lightweight architecture rather than through background services or external infrastructure.
- Final-delivery evidence is distributed across source code, test scripts, JavaDoc comments, the user manual, and the requirement checklist in this folder.
