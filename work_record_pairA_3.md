# EBU6304 Project Work Record: L3 Core Business Logic (Pair A Support)

**Project**: BUPT International School TA Recruitment System  
**Date**: 2026-04-12; updated on 2026-05-21 against the final `ver_1.0` integration  
**Members**: Bai Zhexuan, Gao Weicheng  
**Stage**: L3 - Core Business Logic  
**Pair A role**: Support group, mainly responsible for MO-side workflow support

---

## 1. Objective

In the L3 task plan, Pair C led the TA-side workflow, while Pair A supported the MO-side workflow. The objective of Pair A's work was to complete the Module Organiser flow so the system could support a real recruitment loop:

`TA applies for a job -> MO reviews applicants -> MO selects or rejects -> TA checks status`

In the final `ver_1.0` product, this work is mainly visible through the MO dashboard and its connection with jobs, applications, profiles, users, matching, and notifications.

## 2. Completed Work

### 2.1 US-5: MO Job Posting

- Implemented and reviewed the job-posting form for Module Organisers.
- Covered title, module, description, required skills, maximum hours, and location.
- Added basic validation to prevent empty fields and invalid hour values.
- Confirmed that newly posted jobs can appear in the MO job list and TA open-job browsing page.

### 2.2 US-5: MO Job Management

- Implemented and reviewed the `My Job Posts` workflow.
- Supported listing jobs created by the current MO.
- Supported closing and reopening jobs.
- Confirmed that closed jobs are not treated as normal open application targets.

### 2.3 US-6: MO Applicant Review

- Implemented and reviewed the applicant-review workflow.
- Allowed MOs to select a job and inspect applicants for that job.
- Displayed applicant name, email, skills, status, match score, and match explanation.
- Supported selecting or rejecting applicants.
- Connected decisions to TA-side status updates and notifications.

### 2.4 Integration with Final V1.0

- Confirmed that MO decisions generate in-app notifications through the final notification service.
- Confirmed that applicant review includes AI-assisted or rule-based match scores.
- Confirmed that the MO workflow can be demonstrated together with TA application and Admin workload monitoring.

---

## 3. Key Design Decisions

| Decision Area | Decision | Reason |
| :--- | :--- | :--- |
| MO workflow location | Integrated in the MO dashboard | Keeps the demo flow easy to navigate. |
| Application states | `PENDING`, `SELECTED`, `REJECTED`, `WITHDRAWN` | Gives TA, MO, and Admin a shared workflow vocabulary. |
| Applicant view | Aggregate application, user, profile, and job data | MOs need enough context to make a decision on one screen. |
| Decision notification | Generate notification after MO decision | Makes the recruitment process transparent for TAs. |

---

## 4. Problems and Solutions

### 4.1 Applicant Review Requires Cross-Record Data

- **Problem**: The applicant table is not a single-record view. It needs application data, job data, TA user data, and TA profile data.
- **Solution**: The dashboard refresh logic aggregates the required information before rendering the applicant table.

### 4.2 Table Sorting Can Change Visible Row Indexes

- **Problem**: Swing table sorting can make the visible row index different from the underlying model row index.
- **Solution**: Row-index conversion is used before updating the selected application.

### 4.3 Service-Layer Scope Changed in Final Integration

- **Problem**: The early task plan mentioned separate `TAService`, `MOService`, and `AdminService` classes. The final demo keeps more workflow code inside dashboards to reduce integration risk.
- **Solution**: The final version still separates core concerns where it matters most, such as matching, scoring, notifications, and admin recommendations.

---

## 5. Value Delivered

Pair A's L3 support completed the MO side of the recruitment loop. This allowed the final product to demonstrate:

- MO job posting;
- TA applications;
- MO applicant review;
- TA status updates;
- Admin workload monitoring based on selected applications.

**Review status**: Completed.

