# EBU6304 Group 98 Demo Version 1.1

BUPT International School Teaching Assistant Recruitment System.

## Implemented Features

- TA profile creation and editing
- CV path recording
- Job browsing and application submission
- Application status tracking
- MO job posting and applicant review
- Admin workload monitoring and CSV export
- CSV file persistence without any database
- Simple skill-match scoring for demo use

## Iteration 1.1 Update

This iteration continues the `ver_1.0` demo and focuses on usability improvements for the intermediate product build.

New updates in this version:

- TA job board search/filter for faster browsing
- MO job list search/filter
- MO applicant filtering by name, email, skills, status, or match summary
- Admin workload export files now include a timestamp in the filename
- Admin workload status is clearer, with `OK`, `NEAR LIMIT`, and `OVERLOAD - review allocation immediately`
- Login/about metadata updated to reflect the newer demo iteration more clearly

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
- `compile.sh`: compile script
- `run.sh`: run script

## Version Notes

- `ver_1.0`: first complete integrated demo build
- `ver_1.1`: usability-focused iteration with filtering and improved admin monitoring feedback
