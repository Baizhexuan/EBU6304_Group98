# ver_1.8 Task Plan Alignment

This note checks the current `ver_1.0` demo codebase against `/Users/yukikana0108/Downloads/task_plan.md` and the coursework brief.

## Coursework Requirement Check

### Mandatory platform constraints

The current demo satisfies these mandatory requirements:

- the software is a stand-alone Java application
- all input and output data are stored in CSV text files
- no database is used

### Core product requirements currently demonstrated

The current demo now demonstrates the main customer-facing workflow expected by the brief:

- TA can create and edit an applicant profile
- TA can browse available jobs
- TA can apply for jobs
- TA can check application status
- TA can receive in-app notifications when decisions are made, when profile details are missing, and when applied jobs are closed
- MO can post jobs
- MO can select or reject applicants
- Admin can check TA overall workload
- AI-assisted matching and workload support are included in an explainable, non-black-box way

From a product perspective, this means the demo already covers the selected set of core functions required for a prototype build and moves closer to the higher-level coursework requirements.

## Overall Position

The current integrated demo is beyond a pure L2 skeleton. It now contains:

- most of L2 authentication and dashboard framework
- a practical subset of L3 TA and MO workflow
- a stronger subset of L4 admin monitoring, editing, notifications, and reallocation support
- an AI placeholder integration seam for later L5 model-backed scoring work

At the same time, it does not yet fully satisfy the complete task plan for L1-L6 as a whole coursework package.

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
- early L5 preparation through a replaceable scoring-provider abstraction with a live API placeholder path
- more concrete AI explanation surfaces through missing-skills columns, projected-load reasoning, risk labels, action memos, and the Admin AI Assistant dialog

## Partially Met

- L1 architecture exists, but `ver_1.0` still uses a flat source layout instead of the packaged `model/ui/service/util/test` structure required by the plan
- L4 admin management is stronger than before, but it is still implemented inside the dashboard layer rather than separated into a dedicated `AdminService`
- L5 AI features now have a real transport placeholder, but there is not yet a production prompt contract, workload-balancing model, or final recommendation engine
- L6 delivery is partly covered by README and smoke testing, but not by JavaDoc generation or coverage reporting

## Not Yet Met

- dedicated service classes such as `TAService`, `MOService`, and `AdminService`
- JUnit-based business, admin, notification, AI, and end-to-end test suites
- JavaDoc site generation
- full product backlog and final report alignment inside this demo folder

## Version Conclusion

`ver_1.8` is best described as:

- a product demo that satisfies the mandatory technical constraints
- a selected-core-feature prototype that covers the main TA / MO / Admin workflow plus expanded notification support, cross-platform UI polish, and richer AI-assisted decision support
- a mostly-complete L2 foundation with an integrated partial implementation of L3-L4
- an AI-placeholder bridge into later L5 integration work with clearer explainability than earlier builds
- not yet the entire final coursework package for all L1-L6 deliverables
