# Final Requirement Checklist

This checklist reviews the current `ver_1.0` demo against the coursework brief and the final demo scope.

## Mandatory Constraints

- Stand-alone Java application: Implemented with Java Swing desktop UI.
- Simple text-file storage: Implemented with CSV files in `data/`.
- No database: Satisfied. No SQL database or external persistence framework is used.
- Cross-platform target: UI uses standard Swing components and avoids OS-specific paths in the main workflow.

## Core Recruitment Workflow

- TA can create applicant profile: Implemented in TA Dashboard / My Profile.
- TA can upload or record CV: Implemented through local CV path selection with `JFileChooser`.
- TA can find available jobs: Implemented in Browse Jobs with open-job filtering.
- TA can apply for jobs: Implemented with duplicate-application checks and AI match summary.
- TA can check application status: Implemented in My Applications with status colours and MO notes.
- MO can post jobs: Implemented in MO Dashboard / Post Job.
- MO can select or reject applicants: Implemented in Applicants tab with notification trigger.
- Admin can check TA workload: Implemented in Workload Monitor with overload and near-limit warnings.

## US-8 Notification Coverage

- Application decision notifications: Implemented for selected/rejected decisions.
- Profile-completion reminders: Implemented when TA profile is missing or incomplete.
- Job-closure alerts: Implemented when MO or Admin closes a job with active applications.
- Read-state management: Implemented with mark selected/all as read.

## AI-Assisted Features

- Skill matching: Implemented with local rule-based scoring and optional external model scoring.
- Missing skill explanation: Implemented in TA and MO tables.
- Workload balancing support: Implemented through Admin reallocation advice and risk summaries.
- Interactive AI assistant: Implemented in Admin Dashboard with qwen-plus / DashScope compatible-mode support.
- Responsible AI explanation: Implemented through plain-text recommendation, evidence, risk, and next-action prompt rules.
- Safe key handling: API keys are read from environment variables or local `config/ai.properties`; keys are not stored in source code.

## UI and Usability

- Login and registration screens: Mac-friendly sizing and visible buttons.
- TA dashboard: Profile, job browsing, applications, and notifications are separated into clear tabs.
- MO dashboard: Job posting, job management, and applicant review are separated into clear tabs.
- Admin dashboard: Workload, applications, and jobs are separated into operation-focused tabs.
- Search and filtering: Compact attribute selector plus aligned per-column filters are implemented.
- Cross-platform button rendering: Shared button styling uses a stable Swing UI for macOS, Windows, and Linux.

## Remaining Limitations

- Full JUnit coverage is not included in this demo folder.
- JavaDoc generation is not included in this demo folder.
- The external model requires a valid local API key and internet access.
- The demo remains a course prototype, not a production recruitment system.
