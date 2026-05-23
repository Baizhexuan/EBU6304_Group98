# Regression Test Matrix

This matrix maps the lightweight Java test programs to the behaviours they protect.
It is intended to make manual review and commit splitting easier.

| Area | Test program | Protected behaviour |
| --- | --- | --- |
| Startup data | `SystemSmokeTest` | Seed users, jobs, applications, notifications, scoring summary, and admin alert preview load successfully |
| Authentication | `AuthFlowTest` | Login checks, blank input rejection, short-password rejection, case-insensitive username lookup, and new-user persistence |
| Workflow rules | `WorkflowRulesTest` | Profile readiness, open-job browsing, duplicate application prevention, MO decisions, TA notifications, overload recommendations |
| CSV storage | `CsvPersistenceTest` | Save/load round trips for commas, quotes, empty values, and text-file persistence |
| Notification flow | `NotificationFlowTest` | Decision messages, profile reminder de-duplication, mark-all-read behaviour, and job-closure alerts |
| Validation helpers | `ValidationUtilsTest` | Blank detection, email checks, registration password length checks, integer parsing fallback, and decimal parsing fallback |
| Matching logic | `MatchingServiceTest` | Skill tokenisation, duplicate removal, score rounding, and matched/missing explanations |
| Model helpers | `ModelStateTest` | Profile completeness, job open-state checks, notification read-state checks, and safe display names |
| Scoring facade | `ScoringServiceTest` | Provider delegation, offline readiness, mode labels, source labels, and null-provider guard |
| Notification state | `NotificationReadStateTest` | Profile reminder resolution, single-notification read marking, and persisted read-state reloads |
| Storage lookup | `FileStorageLookupTest` | Case-insensitive lookup, display-name lookup, entity lookup, and next-ID allocation |
| Build metadata | `DemoMetadataTest` | Version label, application title, and about-message readiness text |

## Recommended Local Verification

Use one of the following commands before committing:

```bash
sh test.sh
```

```powershell
.\test.ps1
```

The PowerShell script mirrors the shell script so Windows IDE users can verify the same
regression scope without changing the project layout.
