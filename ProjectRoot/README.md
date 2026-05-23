# ProjectRoot (L2 Authentication and UI Framework Stage)

This folder extends the packaged L1 foundation into the L2 stage for the BUPT International School TA Recruitment System.

## L2 Scope Completed

### Pair B
- Login window with empty-field, unknown-user, and wrong-password feedback
- Registration window with role selection, unique username checking, and password confirmation
- Base dashboard superclass with shared logout menu, tabbed layout, and title template
- Role-based routing after successful login

### Pair C
- TA dashboard skeleton with `Profile`, `Browse Jobs`, and `My Applications`
- MO dashboard skeleton with `Post Job`, `My Posts`, and `Applicants`
- Admin dashboard skeleton with `Workload`, `All Apps`, and `All Jobs`
- Shared `UIHelper` utility with validation helpers, colors, and table sorter setup

## Packages

- `com.bupt.ta.recruitment.model`: domain models from L1
- `com.bupt.ta.recruitment.util`: CSV storage, seeding, password hashing, UI helper
- `com.bupt.ta.recruitment.ui`: login, registration, base dashboard, role dashboards
- `com.bupt.ta.recruitment.test`: L1 tests plus `L2SmokeTest`

## Run

1. Run `compile.bat`
2. Run `run.bat`

## Demo Accounts

- `admin / admin123`
- `mo1 / mo123`
- `mo2 / mo456`
- `ta1 / ta123`
- `ta2 / ta456`
- `ta3 / ta789`

## Notes

- This is the L2 framework layer, so dashboards are currently structured skeletons rather than full business implementations.
- The code follows the packaged structure used in `main/ProjectRoot` for easier team integration.
