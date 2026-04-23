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
- Service layer: `MatchingService`, `ScoringService`, `AdminRecommendationService`, `ValidationUtils`
- AI integration seam: `SkillScoringProvider`, `RuleBasedSkillScoringProvider`, `AIModelSkillScoringProvider`, `AIIntegrationPlan`
- Persistence layer: `FileStorage`
- Verification entry: `SystemSmokeTest`

## Iteration 1.4 Additions

- `AIModelSkillScoringProvider`: performs a real HTTP placeholder call to a chat-completions style API when environment variables are configured
- `ScoringService`: now selects providers from runtime environment and reports whether the current provider is ready
- `AdminRecommendationService`: builds overload warnings and alternative-candidate recommendations for selected jobs
- `AdminDashboard`: adds a recommendation side panel, summary cards, and a more presentation-ready operations layout
- `LoginFrame` and `BaseDashboard`: refreshed visual presentation for the next demo iteration
