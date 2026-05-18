# ver_1.10 Task Plan Alignment

This note checks the current `ver_1.0` demo codebase against the coursework brief and the project task-plan expectations.

## Coursework Requirement Check

### Mandatory platform constraints

The current demo satisfies these mandatory requirements:

- the software is a stand-alone Java application
- all input and output data are stored in CSV text files
- no database is used

### Core product requirements currently demonstrated

The current demo demonstrates the main customer-facing workflow expected by the brief:

- TA can create and edit an applicant profile
- TA can browse available jobs
- TA can apply for jobs
- TA can check application status
- TA can receive in-app notifications when decisions are made, when profile details are missing, and when applied jobs are closed
- MO can post jobs
- MO can select or reject applicants
- Admin can check TA overall workload
- AI-assisted matching and workload support are included in an explainable, non-black-box way

## Overall Position

The current integrated demo is beyond a pure L2 skeleton. It now contains:

- most of L2 authentication and dashboard framework
- a practical subset of L3 TA and MO workflow
- a stronger subset of L4 admin monitoring, editing, notifications, and reallocation support
- an AI-backed optional extension path with an offline explainable fallback
- final-delivery documentation and lightweight regression tests inside this folder

## Matches Well

- L2 login with role-based routing
- L2 register flow with role selection and password confirmation
- L2 shared dashboard base for TA / MO / Admin
- L3 TA profile editing with email and GPA validation plus CV file browsing
- L3 TA job browsing, filtering, and application submission with duplicate checks
- L3 TA application status view with colour cues and pending withdrawal
- L3 MO posting, listing, closing, and applicant review
- stronger L4 admin workload monitoring with filters, summaries, exports, editable overview tables, and replacement suggestions
- stronger `US-8` support through in-app notifications generated from MO decisions, profile-completion reminders, and job-closure alerts
- early L5 preparation through a replaceable scoring-provider abstraction with a live API path and offline fallback
- more concrete AI explanation surfaces through missing-skills columns, projected-load reasoning, risk labels, action memos, and the Admin AI Assistant dialog
- final delivery support through README, user manual, testing strategy, and JavaDoc generation script

## Partially Met

- `ver_1.0` still uses a flat source layout instead of the fuller packaged `model/ui/service/util/test` structure described in a larger task plan
- admin workflow logic is still mostly orchestrated inside dashboard code rather than dedicated `AdminService` classes
- external AI usage is optional and intended for demo support rather than production-grade decision automation
- automated testing is lightweight regression testing rather than a full JUnit suite with coverage reports

## Not Yet Met

- dedicated service classes such as `TAService`, `MOService`, and `AdminService`
- enterprise-grade persistence, scheduling, or notification infrastructure
- a full coursework report package inside this folder alone

## Version Conclusion

`ver_1.10` is best described as:

- a product demo that satisfies the mandatory technical constraints
- a selected-core-feature prototype that covers the main TA / MO / Admin workflow plus notifications, workload support, and explainable AI assistance
- a mostly-complete L2 foundation with integrated partial implementation of L3-L4
- a final-demo-ready folder with local documentation, lightweight tests, and JavaDoc generation support
- not yet the entire final coursework package for every possible backlog item
