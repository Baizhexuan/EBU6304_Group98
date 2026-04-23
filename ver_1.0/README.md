# EBU6304 Group 98 Demo Version 1.4

BUPT International School Teaching Assistant Recruitment System.

## Implemented Features

- TA profile creation and editing
- CV path recording and local file browsing
- Job browsing, keyword filtering, and application submission
- Application status tracking and pending-application withdrawal
- MO job posting, applicant review, and shortlist decisions
- Admin workload monitoring with filters, editable overview tables, save and undo flows, and CSV export
- Admin-side overload alerts and replacement TA recommendations for reallocation decisions
- CSV file persistence without any database
- AI-ready scoring abstraction with a live API placeholder provider and a rule-based fallback
- Polished login and dashboard shell for a more presentation-ready demo

## Task Plan Alignment Check

This demo aligns most clearly with:

- L2 authentication and dashboard framework
- L3 core TA and MO workflow in a simplified integrated form
- a stronger subset of L4 admin management, reallocation support, and reporting
- an AI-ready bridge toward L5 skill matching integration

Current gaps against `task_plan.md`:

- no dedicated service-layer split such as `TAService`, `MOService`, `AdminService`
- no notification module for `US-8`
- no formal JUnit test suite / JavaDoc package delivery yet
- data persistence is still CSV-based with a flat demo layout instead of the full packaged `ProjectRoot` structure
- the live AI path is currently a placeholder integration and still needs production credentials plus a final prompt contract

## Iteration 1.4 Update

This iteration continues `ver_1.0` toward a near-final demo build and improves the admin-facing workflow while preparing the system for real AI scoring.

New updates in this version:

- added `AIModelSkillScoringProvider` as a real API-call placeholder using environment variables such as `OPENAI_API_KEY`, `OPENAI_BASE_URL`, `OPENAI_MODEL`, and `AI_SCORING_MODE`
- upgraded `SkillScoringProvider` to expose readiness and provider-status information
- updated `ScoringService` so the demo can switch between local rule-based scoring and the AI placeholder path
- added `AdminRecommendationService` for high-load TA warnings and alternative TA suggestions based on predicted fit and projected hours
- upgraded `AdminDashboard` with a recommendation side panel, selection-aware reallocation advice, and clearer summary cards
- polished the login page and shared dashboard shell to give the demo a stronger presentation-ready feel
- smoke test output now includes both AI-readiness and admin-alert summaries

## Run

```bash
./compile.sh
./run.sh
```

## Optional AI Placeholder Configuration

```bash
export AI_SCORING_MODE=AI
export OPENAI_API_KEY=your_key_here
export OPENAI_MODEL=gpt-4o-mini
export OPENAI_BASE_URL=https://api.openai.com/v1
```

If these variables are not set, the demo automatically falls back to the local rule-based scorer.

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
- `ver_1.4`: live AI placeholder path, admin reallocation recommendations, and UI polish for the next demo stage
