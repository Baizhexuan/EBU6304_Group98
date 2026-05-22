# Manual QA Checklist

Use this checklist after running the automated tests and before a final demo or submission.

## Startup

- Compile the project successfully.
- Launch the Swing application from `Main`.
- Confirm the login window opens without console errors.
- Confirm the title/about text shows the expected version label.

## Authentication

- Log in as `admin / admin123`.
- Log in as `ta1 / ta123`.
- Log in as `mo1 / mo123`.
- Try an incorrect password and confirm login is rejected.
- Try a duplicate username during registration and confirm it is blocked.

## TA Workflow

- Open the TA profile page and confirm seeded profile data loads.
- Save a complete profile and confirm no reminder remains unread.
- Attempt to apply with an incomplete profile and confirm the workflow blocks it.
- Browse open jobs and confirm closed jobs are excluded.
- Apply for a job and confirm match score and summary are visible.
- Withdraw an application and confirm re-application is possible.

## MO Workflow

- Open the MO job list and confirm owned jobs are visible.
- Create a new job using required skills and weekly hours.
- Close a job and confirm active applicants receive a closure notification.
- Review a pending application and select or reject the applicant.
- Confirm the TA receives a decision notification.

## Admin Workflow

- Open workload monitoring and confirm selected-hour totals are visible.
- Review high-risk overload summaries when a TA exceeds the safe limit.
- Open the recommendation view and confirm matched/missing skills are explainable.
- Export a workload report and confirm a CSV file appears in `data/`.
- Open the AI assistant and confirm either live provider status or local fallback status is visible.

## Data Files

- Review `data/users.csv` for hashed passwords rather than plain text.
- Review `data/jobs.csv` to confirm new jobs are saved as CSV rows.
- Review `data/applications.csv` to confirm application status transitions persist.
- Review `data/notifications.csv` to confirm read/unread state persists.
