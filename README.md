# TA Recruitment System Prototype

It focuses on the iteration-2 recruitment workflow while still including the basic
iteration-1 TA profile functionality needed to make the workflow runnable.

## Covered functions

TA creates and updates a profile
TA browses available jobs
TA applies for a selected job
TA checks application status
Module Organiser posts a new job
Module Organiser reviews applicants
Module Organiser marks an applicant as `Selected`, `Rejected`, or `Pending`

## Technical choices

Language: Python 3
GUI: `tkinter` from the standard library
Storage: CSV files inside [`data`]
External dependencies: none


## How to run

```powershell
python app.py
```

## Data files

[`data/profiles.csv`](data\profiles.csv)
[`data/jobs.csv`](data\jobs.csv)
[`data/applications.csv`](data\applications.csv)


## Suggested flow

1. Open the TA portal and save a TA profile.
2. Select one of the available jobs and submit an application.
3. Open the MO portal and review the application.
4. Mark the applicant as selected or rejected.
5. Return to the TA portal and refresh the status table.
