# User Manual

## Purpose

This stand-alone Java application supports BUPT International School Teaching Assistant recruitment for three roles:

- `TA`: create a profile, browse jobs, apply, track status, and read notifications
- `MO`: post jobs, review applicants, and update application decisions
- `Admin`: monitor workload, export reports, edit overview tables, inspect recommendations, and ask the AI assistant for decision support

## Prerequisites

- Java JDK installed locally
- terminal access to run shell scripts
- optional internet access and a local API key if you want to demonstrate the live `qwen-plus` assistant

## Compile and Run

```bash
./compile.sh
./run.sh
```

If the scripts are not executable in your environment, run:

```bash
sh compile.sh
sh run.sh
```

## Demo Accounts

- `admin / admin123`
- `ta1 / ta123`
- `ta2 / ta456`
- `mo1 / mo123`
- `mo2 / mo456`

## TA Workflow

### 1. Log in as a TA

Use one of the TA accounts from the login screen.

Reference screenshot:

![Login screen](../screenshots/login.png)

### 2. Complete or update the profile

Open `My Profile` and fill in:

- full name
- email
- student ID
- skills
- GPA
- CV file, which is copied into local demo storage
- availability
- personal statement

If profile details are missing, the system can generate an in-app reminder.

Reference screenshot:

![TA profile](../screenshots/ta_profile.png)

### 3. Browse jobs and apply

Open `Browse Jobs` to:

- review open jobs only
- use the compact filter toolbar and aligned field filters
- inspect the explainable AI match summary and missing-skills output
- submit an application

Reference screenshot:

![TA browse jobs](../screenshots/ta_browse_jobs.png)

### 4. Track application status

Open `My Applications` to:

- review status
- read reviewer notes
- withdraw pending applications when appropriate

Reference screenshot:

![TA applications](../screenshots/ta_applications.png)

### 5. Review notifications

Open `Notifications` to:

- read application decision updates
- read profile reminders
- read job-closure alerts
- mark notifications as read

Reference screenshot:

![TA notifications](../screenshots/ta_notifications.png)

## MO Workflow

### 1. Log in as an MO

Use `mo1 / mo123` or `mo2 / mo456`.

### 2. Post a job

Open `Post Job` and fill in:

- title
- module
- required skills
- max hours
- location
- description

Reference screenshot:

![MO post job](../screenshots/mo_post_job.png)

### 3. Review applicants

Open `Applicants` to:

- switch between your jobs
- filter applicants by name, email, skills, or status
- inspect match score and match summary
- select or reject applicants

Decision updates generate TA notifications automatically.

Reference screenshot:

![MO applicants](../screenshots/mo_applicants.png)

## Admin Workflow

### 1. Monitor workload

Open `Workload Monitor` to:

- review all TAs
- filter by username, name, email, and status
- inspect `OK`, `NEAR LIMIT`, or `OVERLOAD` states
- export a timestamped CSV report

Reference screenshot:

![Admin workload](../screenshots/admin_workload.png)

### 2. Review recommendations and AI support

The admin page provides:

- workload alerts
- replacement recommendations
- projected load reasoning
- an `Ask AI Assistant` dialog for additional decision support

Reference screenshot:

![Admin recommendations](../screenshots/admin_recommendations.png)

## AI Configuration and Fallback

### Offline demo mode

No API key is required for the core workflow. The system falls back to local explainable logic when no external configuration is present.

### Live `qwen-plus` mode

Copy the example file and keep the real key local:

```bash
cp config/ai.properties.example config/ai.properties
```

Then edit `config/ai.properties` with your own values. The file is ignored by Git.

When configured, the Admin AI Assistant uses the compatible chat-completions endpoint and still keeps the rule-based fallback available.

## Common Issues

- `Registration password is rejected`: enter at least 6 characters and repeat the same value in the confirmation field.
- `CV upload fails`: choose a readable local file. The app copies it into `data/cv_uploads/` for the standalone demo.
- `Buttons do not show text clearly`: re-run with the local JDK used during development. The project already uses stable Swing button styling for macOS, Windows, and Linux.
- `No AI answer appears`: check `config/ai.properties` or your environment variables. The dialog should still work in fallback mode.
- `Data looks changed after testing`: restore the original CSV files from version control or re-seed the app by removing the core CSV files and running `./run.sh` again.
- `Admin report not found`: exported files are written to the `data/` folder with a timestamp in the filename.
