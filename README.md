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
# EBU6304 Group 98

BUPT International School Teaching Assistant Recruitment System.

This repository contains the development history of Group 98 for the TA recruitment project. The project is a Java-based desktop system built with Swing and file-based persistence, following the coursework constraint of not using a database.

## Project Overview

The system supports three main roles:

- TA: create a profile, record CV information, browse available jobs, submit applications, and check application status
- MO: publish TA job posts, review applicants, and decide whether to select or reject them
- Admin: monitor TA workload across modules and export overview reports

The current demo implementation stores data in CSV files and includes a lightweight skill-matching mechanism to simulate AI-assisted screening in a simple and explainable way.

## Architecture

The repository mainly follows a layered structure:

- UI layer: Swing windows and dashboards for login, TA, MO, and Admin workflows
- Domain layer: data models such as `User`, `TAProfile`, `Job`, and `Application`
- Service layer: validation and skill-matching logic
- Persistence layer: file storage utilities that read and write CSV files

In the more complete demo version, the system is organised around these main components:

- `Main`: application entry point
- `LoginFrame` and `RegisterFrame`: authentication and demo account setup
- `TADashboard`: TA-side profile, jobs, and application management
- `MODashboard`: MO-side job posting and applicant review
- `AdminDashboard`: workload monitoring and reporting
- `FileStorage`: CSV-based persistence and seed data handling
- `MatchingService`: skill overlap scoring for demo matching

## Repository Structure

### `src/`

This folder represents the first demo / early prototype stage of the project. It contains the initial Java source code that was used to establish the basic direction of the system before the more complete packaged version was prepared.

### `ver_1.0/`

This folder is the main demo build for the later iteration. It contains the more complete and presentation-ready implementation, including:

- full Swing UI flow
- CSV data files
- architecture notes
- compile and run scripts
- smoke test entry for quick verification

The latest pushed update continues this demo into `ver_1.1`, adding search/filter tools for TA and MO workflows plus clearer Admin workload export/reporting.

If you want to run the more complete demo, this is the folder that should be used.

### `ver_1.0_tess_picture/`

This folder contains screenshot assets for the `ver_1.0` demo. These images are used to document the interface and user flow, such as login, registration, TA dashboard views, profile validation, and successful submission states.

## Main Features in `ver_1.0`

- Login with seeded demo accounts
- Register additional TA or MO demo accounts
- TA profile creation and editing
- CV path recording
- Job browsing and application submission
- Application status tracking
- MO job publishing and applicant review
- Admin workload overview with overload warning logic
- CSV report export
- File-based persistence without a database
- Simple skill match scoring for demo purposes

## Latest Iteration Update

The latest `ver_1.0` follow-up iteration adds:

- TA job search/filter support
- MO search/filter support for job posts and applicant review
- Admin workload export with timestamped filenames
- clearer workload warnings for near-limit and overload cases
- updated demo metadata and version notes in documentation

## How to Run the Complete Demo

Run the project from the `ver_1.0/` folder:

```bash
cd ver_1.0
./compile.sh
./run.sh
```

## Demo Accounts

The prepared demo accounts in `ver_1.0` are:

- `admin / admin123`
- `ta1 / ta123`
- `ta2 / ta456`
- `mo1 / mo123`
- `mo2 / mo456`

## Notes

- The repository includes multiple development stages, so not every folder represents the same iteration level.
- Local CSV files may change during testing because the application writes runtime data back to disk.
- The intended final presentation/demo version in this repository is `ver_1.0`.
