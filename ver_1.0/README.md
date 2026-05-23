# EBU6304 Group 98 Demo Version 1.13

BUPT International School / QMUL Teaching Assistant Recruitment System.

This folder contains the stand-alone Java Swing demo for the EBU6304 group project. It implements a complete TA recruitment workflow for three roles: Teaching Assistant (TA), Module Organiser (MO), and Administrator (Admin).

## Final Submission Position

This `ver_1.0` folder is a self-contained product-demo package:

- stand-alone Java desktop application
- Java Swing GUI
- CSV text-file storage only
- no database
- role-based workflow for TA, MO, and Admin users
- explainable AI-assisted matching with offline fallback
- bilingual English / Chinese interface for BUPT and QMUL usage
- lightweight regression tests and JavaDoc scripts

## Latest Feature Scope

### TA Features

- Register or log in as a TA.
- Complete and edit a TA profile, including skills, GPA, availability, personal statement, and CV path.
- Browse open jobs with field-based search.
- View AI-assisted match scores and ranking explanations.
- Apply for selected jobs only after profile validation passes.
- Reapply to the same job after a previous application was `REJECTED` or `WITHDRAWN`.
- Track application status in `My Applications`.
- Withdraw only `PENDING` applications.
- Read notifications and messages through the top-right bell centre.
- Chat with the relevant MO after an application connects both sides.
- Send up to three messages before the MO approves the conversation.
- See reputation-related effects through future match scores.

### MO Features

- Register or log in as a Module Organiser.
- Post new TA job opportunities with weekly-hour validation.
- Manage open and closed job posts.
- Review applicants for each job.
- View current match score, missing skills, profile details, TA reputation, and workload before deciding.
- Select or reject TA applications.
- Approve TA-MO conversations from the bell centre.
- Rate completed TA work after selection.
- Trigger a reputation penalty when a TA had a high original match score but receives a low completion rating.

### Admin Features

- Monitor all TA workloads.
- Detect workload risk through `OK`, `NEAR LIMIT`, and `OVERLOAD` states.
- View AI-style replacement and reallocation recommendations.
- Ask the AI Assistant about workload, replacement, and allocation risk.
- Edit global application records with status validation.
- Review global job ownership while editing job wording, hours, location, and status.
- Export CSV workload reports with CSV-injection protection.
- Inspect notifications and message alerts through the bell centre.

### Cross-Role Features

- English / Chinese UI switching from the login page and every dashboard.
- Top-right bell centre for notifications and TA-MO messages.
- Wrapping table cells so long titles, skills, locations, and summaries are readable without `...` truncation.
- Real-time current match scores in TA, MO, and Admin views, aligned with the right-side AI ranking panel.
- AI ranking panels include a generation timestamp so stale recommendations are visible.
- Notification centre displays the latest 100 notifications to avoid UI lag on large datasets.
- CSV-backed persistence for users, profiles, jobs, applications, notifications, messages, message consent, work evaluations, TA reputation, and ID counters.

## Security and Validation Updates

Version `1.13` adds the following hardening work:

- passwords are stored as salted SHA-256 hashes instead of plain text
- old plain-text demo passwords are still accepted once and migrated after login
- `users.csv` in the submitted data has been migrated to hashed passwords
- registration rejects passwords shorter than six characters
- email validation now requires a dotted domain, for example `name@bupt.edu.cn`
- TA applications re-check email, GPA, CV path, availability, and personal statement before submission
- MO and Admin job-hour inputs cannot exceed the system workload limit of `20h`
- Admin can no longer edit a job's MO ownership field
- Admin status edits reject invalid magic strings outside the allowed status set
- CSV save/export logic neutralises cells starting with `=`, `+`, `-`, `@`, or tab
- CSV save operations are synchronized
- ID allocation uses `data/id_counters.csv` to reduce duplicate-ID risk
- CSV data files are made owner-readable/writable on a best-effort basis
- malformed quoted CSV rows print a warning instead of failing silently
- external AI calls already use connection and read timeouts, so the UI is less likely to hang on slow networks

## Demo Accounts

| Role | Username | Password | Display Name |
| --- | --- | --- | --- |
| Admin | `admin` | `admin123` | System Admin |
| TA | `ta1` | `ta123` | Li Ming |
| TA | `ta2` | `ta456` | Wang Yue |
| MO | `mo1` | `mo123` | Dr Chen |
| MO | `mo2` | `mo456` | Prof Zhao |

The visible passwords above are demo login passwords. They are stored in `data/users.csv` as salted hashes.

## Quick Start

### Windows PowerShell

From this folder:

```powershell
.\compile.ps1
.\run.ps1
```

If PowerShell blocks local scripts, use:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

Run tests:

```powershell
powershell -ExecutionPolicy Bypass -File .\test.ps1
```

### macOS / Linux

Compile:

```bash
sh compile.sh
```

Run:

```bash
sh run.sh
```

Run all lightweight regression tests:

```bash
sh test.sh
```

`run.sh` calls `sh ./compile.sh`, so it does not require `compile.sh` to have executable permission.

### Generate JavaDocs

```bash
sh javadoc.sh
```

Generated output is written to `javadocs/`.

## Matching and Ranking Behaviour

The matching score is calculated through the active `ScoringService`.

In offline mode, the local rule-based scorer:

1. Splits the TA skills and job required skills into normalized skill tokens.
2. Counts required skills matched by the TA profile.
3. Computes `matched required skills / total required skills * 100`.
4. Applies any TA reputation penalty to the current display score.
5. Produces a readable summary showing matched and missing skills.

The TA, MO, and Admin dashboards display current real-time match scores. This keeps the left-side tables consistent with the right-side AI ranking panels. The stored `matchScore` in `applications.csv` is retained as an application-time historical record and is also used by the post-work reputation mechanism.

## Message Consent Rule

TA-MO messaging is intentionally limited before consent:

- TA and MO contacts appear when an application connects both sides.
- Before conversation approval, one sender can send at most three messages to the other side for that job.
- Only the MO who owns the job can approve the conversation.
- After approval, the three-message limit is lifted for that TA-MO-job conversation.
- The bell centre clearly shows whether the conversation is approved or still waiting for MO approval.

## Reputation and Post-Work Evaluation

MO users can rate completed work for selected TA applications.

The reputation mechanism works as follows:

- Each TA starts with a default reputation score of `100`.
- If an application had a high original match score but the MO gives a low final work rating, the TA receives a reputation penalty.
- The reputation penalty reduces future current match scores.
- The system treats this as a skill-reliability signal, not an automatic misconduct decision.

This allows the demo to explain why a candidate with strong claimed skills may be ranked lower in future matching after poor delivery.

## AI Configuration

### Offline demo mode

No external key is required. If no live AI configuration is available, the application uses local explainable rule-based matching and local fallback guidance.

### Optional live `qwen-plus` mode

Copy the example file and keep the real key local:

```bash
cp config/ai.properties.example config/ai.properties
```

Then edit `config/ai.properties` with your own values. The real local config file is ignored by Git.

Equivalent environment-variable setup:

```bash
export OPENAI_API_KEY=your_bailian_key_here
export OPENAI_MODEL=qwen-plus
export OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
export AI_API_MODE=CHAT_COMPLETIONS
export AI_SCORING_MODE=AI
```

When configured, the AI Assistant can call the compatible chat-completions endpoint. If the model is unavailable or the key is missing, the system remains usable through the local fallback path.

## CSV Storage

All persistent data is stored in CSV files under `data/`:

- `users.csv`
- `profiles.csv`
- `jobs.csv`
- `applications.csv`
- `notifications.csv`
- `messages.csv`
- `message_consents.csv`
- `work_evaluations.csv`
- `ta_reputations.csv`
- `id_counters.csv`
- exported `admin_workload_report_*.csv`

No database or external persistence framework is used.

## Project Layout

- `src/`: Java source code
- `data/`: CSV data files used by the demo
- `config/ai.properties.example`: local AI configuration template
- `docs/architecture.md`: structure overview
- `docs/task_plan_alignment.md`: requirement and task-plan comparison notes
- `docs/final_requirement_checklist.md`: requirement-to-evidence mapping
- `docs/testing_strategy.md`: test strategy and workflow coverage
- `docs/user_manual.md`: final user manual with screenshot references
- `docs/ta_recruitment_viva_notes_zh.pdf`: Chinese viva preparation notes
- `screenshots/`: demo screenshots used by the manual and README
- `compile.ps1`, `run.ps1`, `test.ps1`, `javadoc.ps1`: Windows scripts
- `compile.sh`, `run.sh`, `test.sh`, `javadoc.sh`: macOS/Linux scripts

Generated files such as `bin/`, `*.class`, local AI keys, JavaDocs, and exported workload reports should not be committed.

## Regression Tests

The test suite currently includes:

- `SystemSmokeTest`
- `AuthFlowTest`
- `WorkflowRulesTest`
- `CsvPersistenceTest`
- `PostWorkFeedbackAndMessagingTest`
- `NotificationFlowTest`
- `ValidationUtilsTest`
- `MatchingServiceTest`
- `ModelStateTest`
- `ScoringServiceTest`
- `NotificationReadStateTest`
- `FileStorageLookupTest`
- `DemoMetadataTest`

These tests cover smoke loading, authentication, validation, CSV persistence, workflow rules, matching consistency, notification read-state, message consent, post-work reputation penalties, and ID allocation.

## Product Screenshots

### Login and Registration

![Login screen](screenshots/login.png)

![Registration screen](screenshots/register.png)

### TA Workflow

![TA profile](screenshots/ta_profile.png)

![TA browse jobs](screenshots/ta_browse_jobs.png)

![TA applications](screenshots/ta_applications.png)

![TA notifications](screenshots/ta_notifications.png)

### MO Workflow

![MO post job](screenshots/mo_post_job.png)

![MO applicants](screenshots/mo_applicants.png)

### Admin Workflow

![Admin workload](screenshots/admin_workload.png)

![Admin recommendations](screenshots/admin_recommendations.png)

### Latest Feature Screenshots

The following screenshots show the newer bilingual, bell-centre, conversation-approval, and reputation features.

![Bilingual UI switch](screenshots/feature_bilingual_ui.png)

![Bell centre messages](screenshots/feature_bell_centre_messages.png)

![MO conversation approval](screenshots/feature_mo_conversation_approval.png)

![Post-work rating and reputation](screenshots/feature_reputation_rating.png)

## Version Notes

- `ver_1.0`: first complete integrated demo build
- `ver_1.1`: usability-focused iteration with filtering and improved admin monitoring feedback
- `ver_1.2`: task-plan alignment update with shared dashboard base and stronger L2 authentication checks
- `ver_1.3`: stronger admin operations and AI-ready scoring abstraction
- `ver_1.4`: live AI placeholder path, admin reallocation recommendations, and UI polish
- `ver_1.5`: macOS-friendly entry screens and stronger final-demo usability
- `ver_1.6`: CSV-backed notifications and aligned multi-field search across key tables
- `ver_1.7`: expanded US-8 triggers with profile-completion reminders and job-closure alerts
- `ver_1.8`: cross-platform UI fixes, compact attribute search, and interactive AI assistant dialog
- `ver_1.9`: Admin AI Assistant supports real external model calls, including qwen-plus via DashScope compatible chat completions
- `ver_1.10`: qwen-plus local config support, plain-text AI prompt rules, copy response action, lightweight regression tests, JavaDoc generation support, and final-delivery documentation
- `ver_1.11`: faster search and page-refresh behaviour, explicit Search actions for filter toolbars, lighter refresh feedback, and reduced repeated CSV reads across TA, MO, and Admin dashboards
- `ver_1.12`: bilingual UI, bell-centre notifications and messages, MO-only conversation approval, three-message pre-approval rule, post-work ratings, TA reputation penalties, rejected-application reapply support, wrapping table cells, and real-time match/ranking consistency
- `ver_1.13`: security and validation hardening, salted password hashes, stricter email/profile checks, Admin job ownership lock, safe CSV export, synchronized CSV saves, ID counters, AI ranking timestamps, notification paging, and cross-platform script/test updates
