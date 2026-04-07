# Demo Architecture

```text
software_engine/
├── src/
│   ├── Main.java
│   ├── LoginFrame.java
│   ├── RegisterFrame.java
│   ├── TADashboard.java
│   ├── MODashboard.java
│   ├── AdminDashboard.java
│   ├── FileStorage.java
│   ├── MatchingService.java
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
│   └── admin_workload_report.csv
├── docs/
│   └── architecture.md
├── compile.sh
├── run.sh
└── README.md
```

## Layering

- UI layer: `LoginFrame`, `RegisterFrame`, `TADashboard`, `MODashboard`, `AdminDashboard`
- Domain layer: `User`, `TAProfile`, `Job`, `Application`, `MatchResult`
- Service layer: `MatchingService`, `ValidationUtils`
- Persistence layer: `FileStorage`
- Verification entry: `SystemSmokeTest`
