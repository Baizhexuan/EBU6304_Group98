# Demo Architecture

```text
software_engine/
├── src/
│   ├── Main.java
│   ├── DemoMetadata.java
│   ├── LoginFrame.java
│   ├── RegisterFrame.java
│   ├── BaseDashboard.java
│   ├── TADashboard.java
│   ├── MODashboard.java
│   ├── AdminDashboard.java
│   ├── FileStorage.java
│   ├── MatchingService.java
│   ├── SkillScoringProvider.java
│   ├── RuleBasedSkillScoringProvider.java
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
- Domain layer: `User`, `TAProfile`, `Job`, `Application`, `MatchResult`
- Service layer: `MatchingService`, `ScoringService`, `ValidationUtils`
- AI integration seam: `SkillScoringProvider`, `RuleBasedSkillScoringProvider`, `AIIntegrationPlan`
- Persistence layer: `FileStorage`
- Verification entry: `SystemSmokeTest`

## Iteration 1.3 Additions

- `AdminDashboard`: expanded from read-only monitoring into filtered admin operations with editable applications and jobs, save and undo actions, and unsaved-change warnings
- `SkillScoringProvider`: interface for future external AI or LLM-backed recommendation logic
- `RuleBasedSkillScoringProvider`: current default provider that wraps the existing rule-based matcher
- `ScoringService`: single entry point so UI code stays stable when the scoring backend changes later
- `AIIntegrationPlan`: lightweight readiness summary used in the admin UI and smoke test output
- `TADashboard`: now reads scores through `ScoringService` rather than binding directly to `MatchingService`
- `FileStorage`: added helper lookup by display name to support admin-side job reassignment
