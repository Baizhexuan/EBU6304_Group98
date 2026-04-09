# L3 Pair B Review Record

## Scope

- Layer: `L3`
- Pair responsibility: `Pair B`
- Requested deliverables: `BusinessLogicTest.java` and code review notes for `US-1` to `US-6`

## What is already complete

- `TADashboard.java` already implements most of the TA-side L3 flow:
- Profile form validation for required fields, email format, and GPA range.
- OPEN-job browsing with module/skill filtering and table sorting.
- Job application creation with duplicate-application blocking.
- Application table rendering with status color mapping.

## Findings

### 1. `US-5` is not implemented yet

File: `ProjectRoot/src/com/bupt/ta/recruitment/ui/MODashboard.java`

- The `Post Job` tab only renders input widgets.
- There is no submit button, no validation logic, and no persistence call that writes new `Job` records.
- The `My Posts` tab lists existing jobs but does not provide the required `OPEN -> CLOSED` action.

Impact:
- Pair A's L3 deliverables cannot be functionally accepted yet.
- Pair B cannot produce a truthful "all US-1~US-6 passed" test report because `US-5` has no executable path.

### 2. `US-6` is not implemented yet

File: `ProjectRoot/src/com/bupt/ta/recruitment/ui/MODashboard.java`

- The `Applicants` tab only shows a read-only table.
- There is no job selector, no select/reject buttons, and no logic that updates `Application.status`.

Impact:
- MO cannot complete applicant review decisions.
- TA-side status color rendering exists, but there is no MO workflow that can drive a row from `PENDING` to `SELECTED` or `REJECTED`.

### 3. L3 service-layer requirement is still missing

Files:

- `ProjectRoot/src/com/bupt/ta/recruitment/service/AuthService.java`
- `ProjectRoot/src/com/bupt/ta/recruitment/ui/TADashboard.java`
- `ProjectRoot/src/com/bupt/ta/recruitment/ui/MODashboard.java`

- The task plan requires `TAService`, `MOService`, and `AdminService`.
- Current L3 code still lets UI classes access `CsvStorage` directly.
- That makes business logic harder to reuse and harder to test without UI reflection or duplicated logic.

Impact:
- Pair B tests can only validate the currently exposed persistence rules, not a clean service API.
- Future L4/L5 work will be more fragile because workflow rules remain embedded in Swing event handlers.

## Pair B test strategy

- Added `ProjectRoot/src/com/bupt/ta/recruitment/test/BusinessLogicTest.java`
- Executable coverage currently includes:
- `US-1/US-2`: profile validation and profile persistence
- `US-3`: OPEN-job browsing plus module/skill filtering
- `US-4`: application creation, duplicate blocking, and status persistence
- `US-5/US-6`: explicitly marked as blocked/skipped until MO actions exist

## Recommendation

1. Pair C / Pair A should first complete `MODashboard` posting, closing, and applicant-decision handlers.
2. Move TA/MO workflow rules into dedicated service classes.
3. Re-run and extend `BusinessLogicTest.java` so `US-5` and `US-6` move from `SKIP` to `PASS/FAIL`.
