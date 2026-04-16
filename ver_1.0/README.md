# EBU6304 Group 98 Demo Version 1.3

BUPT International School Teaching Assistant Recruitment System.

## Implemented Features

- TA profile creation and editing
- CV path recording and local file browsing
- Job browsing, keyword filtering, and application submission
- Application status tracking and pending-application withdrawal
- MO job posting, applicant review, and shortlist decisions
- Admin workload monitoring with filters, editable overview tables, save and undo flows, and CSV export
- CSV file persistence without any database
- AI-ready scoring abstraction with a rule-based provider as the current default

## Task Plan Alignment Check

This demo aligns most clearly with:

- L2 authentication and dashboard framework
- L3 core TA and MO workflow in a simplified integrated form
- a stronger subset of L4 admin management and reporting
- an AI-ready foundation for L5 skill matching integration

Current gaps against `task_plan.md`:

- no dedicated service-layer split such as `TAService`, `MOService`, `AdminService`
- no notification module for `US-8`
- no formal JUnit test suite / JavaDoc package delivery yet
- data persistence is still CSV-based with a flat demo layout instead of the full packaged `ProjectRoot` structure
- no external AI model is connected yet; the provider layer is prepared for that next step

## Iteration 1.3 Update

This iteration continues `ver_1.0` toward a near-final demo build and improves the admin-facing workflow while preparing the system for future AI integration.

New updates in this version:

- added an AI-ready scoring layer with `SkillScoringProvider`, `RuleBasedSkillScoringProvider`, and `ScoringService`
- updated TA matching calls to use the central scoring service instead of calling the rule-based matcher directly
- added `AIIntegrationPlan` so the current readiness state can be surfaced in the UI and smoke test output
- upgraded `AdminDashboard` with workload search and status filters
- added admin summary labels for visible TA count, allocated hours, overload cases, and current scoring provider readiness
- made the admin applications table editable for status and reviewer notes, with save and undo support
- made the admin jobs table editable for MO assignment, title, module, skills, hours, location, and status, with validation plus save and undo support
- added unsaved-change warnings when the admin tries to log out or close the dashboard
- workload export files now include the active scoring provider for auditability
- README and architecture notes updated to reflect the `ver_1.3` build

## Run

```bash
./compile.sh
./run.sh
```

## Demo Accounts

- `admin / admin123`
- `ta1 / ta123`
- `ta2 / ta456`
- `mo1 / mo123`
- `mo2 / mo456`

## Project Layout

- `src/`: Java source code
- `data/`: CSV data files
- `docs/architecture.md`: structure overview
- `docs/task_plan_alignment.md`: task plan comparison notes
- `compile.sh`: compile script
- `run.sh`: run script

## Version Notes

- `ver_1.0`: first complete integrated demo build
- `ver_1.1`: usability-focused iteration with filtering and improved admin monitoring feedback
- `ver_1.2`: task-plan alignment update with shared dashboard base and stronger L2 authentication checks
- `ver_1.3`: stronger admin operations and AI-ready scoring abstraction for the next integration stage
