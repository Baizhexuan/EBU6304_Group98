# Testing Strategy

## Goal

The final demo needs evidence that the core TA, MO, Admin, notification, CSV, and AI-support workflows behave reliably. This folder uses lightweight console-based Java tests rather than a heavier JUnit stack so the build remains compatible with the coursework constraints and the current shell-script workflow.

## How to Run

```bash
./test.sh
```

On Windows PowerShell:

```powershell
.\test.ps1
```

For a reviewer-friendly coverage map, see `docs/regression_test_matrix.md`.

This script performs:

1. `sh ./compile.sh`
2. `java -cp bin SystemSmokeTest`
3. `java -cp bin AuthFlowTest`
4. `java -cp bin WorkflowRulesTest`
5. `java -cp bin CsvPersistenceTest`
6. `java -cp bin NotificationFlowTest`
7. `java -cp bin ValidationUtilsTest`
8. `java -cp bin MatchingServiceTest`
9. `java -cp bin ModelStateTest`
10. `java -cp bin ScoringServiceTest`
11. `java -cp bin NotificationReadStateTest`
12. `java -cp bin FileStorageLookupTest`
13. `java -cp bin DemoMetadataTest`

## Automated Test Coverage

| Test program | Main purpose | Evidence produced |
| --- | --- | --- |
| `SystemSmokeTest` | Basic sanity check for seeded data, scoring, notifications, and admin alert generation | Confirms the system starts in a valid seeded state |
| `AuthFlowTest` | Login credential lookup, blank-input rejection logic, duplicate username detection, and new-user persistence | Confirms registration/login assumptions used by the UI |
| `WorkflowRulesTest` | Profile completeness, open-job browsing assumptions, application creation, duplicate prevention, MO decision notifications, admin overload alerts, explainable fallback scoring | Confirms the core recruitment workflow and US-8 triggers |
| `CsvPersistenceTest` | CSV save/load round trips for commas, quotes, and empty values | Confirms the storage layer remains text-based but more robust |
| `NotificationFlowTest` | Profile-reminder de-duplication, application-decision messaging, read-state transitions, and job-closure alerts | Confirms notification events stay visible without duplicate unread reminders |
| `ValidationUtilsTest` | Blank checks, email validation, and numeric parse fallback behaviour | Confirms shared form-validation helpers keep stable edge-case behaviour |
| `MatchingServiceTest` | Skill tokenisation, duplicate removal, deterministic scoring, and matched/missing summary text | Confirms explainable matching remains deterministic and easy to inspect |
| `ModelStateTest` | Helper methods on core model objects such as profile completeness, job openness, notification read state, and safe display names | Confirms dashboard-facing state helpers stay stable |
| `ScoringServiceTest` | Active scoring-provider mode, delegation, readiness summary, and null-provider guard behaviour | Confirms scoring integration remains explainable and offline-safe |
| `NotificationReadStateTest` | Profile-reminder resolution, single notification read marking, and persisted read-state reloads | Confirms notification state transitions survive CSV round trips |
| `FileStorageLookupTest` | Case-insensitive user lookup, display-name lookup, entity lookup, and next-ID allocation | Confirms CSV lookup helpers and ID allocation stay deterministic |
| `DemoMetadataTest` | Version label, app title, and about-message readiness text | Confirms UI support metadata remains populated |

## Manual Demo Checks

### Login and registration

- open the app and confirm the login screen renders correctly
- log in with a seeded demo account
- open registration and confirm all fields are visible
- try a duplicate username and confirm the UI blocks it

### TA workflow

- open `My Profile`, update fields, and save
- confirm incomplete profiles trigger reminders
- open `Browse Jobs`, use both compact and aligned filters, and apply for a valid job
- confirm duplicate application attempts are blocked
- open `My Applications` and verify status colours and notes
- open `Notifications` and confirm unread/read behaviour

### MO workflow

- post a new job
- close or reopen a job from the MO job list
- review applicants for one job
- select or reject an applicant
- confirm TA notifications are generated

### Admin workflow

- inspect workload rows and summary cards
- export a report and confirm a timestamped CSV file is generated
- review a TA-specific recommendation
- edit application/job overview rows and save successfully
- open `Ask AI Assistant` and verify either external-model mode or explainable fallback mode

## Edge Cases Covered

- blank login input is treated as invalid
- duplicate usernames are identified case-insensitively
- incomplete profiles are rejected by workflow rules
- duplicate active applications are blocked
- notifications are generated for application decisions and profile reminders
- workload alerts are generated when selected hours exceed the safe limit
- CSV rows preserve commas, quotes, and empty values after a save/load cycle
- helper validation methods keep predictable fallbacks for malformed input
- skill matching normalises case, removes duplicates, and reports missing skills
- notification read-state helpers persist changes across reloads
- model helper methods keep dashboard assumptions stable
- scoring metadata remains available for offline demonstration

## Remaining Limitations

- These tests are lightweight regression checks rather than full JUnit suites.
- GUI assertions are still manual because Swing UI behaviour is demonstrated interactively.
- External model calls are not forced during automated tests because the final demo must also remain valid offline.
