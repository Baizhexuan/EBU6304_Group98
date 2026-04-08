# TA Recruitment System Prototype

This prototype was created for the second assessment of the EBU6304 group project.
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
Storage: CSV files inside [`data`](C:\Users\ASUS\Documents\New project\data)
External dependencies: none

The project deliberately avoids databases and third-party packages so it remains
aligned with the assignment restriction and easy to run on another machine.

## How to run

```powershell
python app.py
```

## Data files

The application will create these files automatically on first run:

[`data/profiles.csv`](C:\Users\ASUS\Documents\New project\data\profiles.csv)
[`data/jobs.csv`](C:\Users\ASUS\Documents\New project\data\jobs.csv)
[`data/applications.csv`](C:\Users\ASUS\Documents\New project\data\applications.csv)

Two sample jobs are also inserted automatically so the TA workflow can be tested immediately.

## Suggested demo flow

1. Open the TA portal and save a TA profile.
2. Select one of the available jobs and submit an application.
3. Open the MO portal and review the application.
4. Mark the applicant as selected or rejected.
5. Return to the TA portal and refresh the status table.
