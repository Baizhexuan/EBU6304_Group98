# EBU6304 Project Work Record: L6 Testing, Documentation, and Delivery (Pair A Support)

**Project**: BUPT International School TA Recruitment System  
**Date**: 2026-05-21, based on the final `ver_1.0` integration  
**Members**: Bai Zhexuan, Gao Weicheng  
**Stage**: L6 - Testing, Documentation, and Delivery  
**Pair A role**: Support group

---

## 1. Objective

In the L6 task plan, Pair C led JavaDoc and test-coverage completion, while Pair A supported final delivery quality. Pair A's goal was to help make the final `ver_1.0` product easy to run, explain, demonstrate, and assess.

The support work focused on:

- final UI review;
- README and setup guidance;
- user-manual and screenshot checks;
- requirement-completion alignment against the coursework brief.

## 2. Completed Work

### 2.1 Final UI Review

- Reviewed the login, registration, TA dashboard, MO dashboard, and Admin dashboard screens.
- Checked that the main TA, MO, and Admin pages support a continuous recruitment demo.
- Reviewed table layout, filtering controls, refresh behavior, and key action buttons.
- Paid special attention to Admin workload and recommendation views because these are important final-assessment features.

### 2.2 README and Running Instructions

- Helped organize the final `ver_1.0/README.md`.
- Confirmed that the README identifies `ver_1.0` as the final demo folder.
- Confirmed that the README includes product scope, demo accounts, compile/run/test instructions, AI configuration, and version notes.
- Confirmed that the README explains the product as a Java Swing desktop application using text-file persistence.

### 2.3 User Manual and Screenshots

- Reviewed the user manual for TA, MO, and Admin workflows.
- Confirmed that screenshots cover the major screens:
  - login;
  - registration;
  - TA profile;
  - TA job browsing;
  - TA applications;
  - TA notifications;
  - MO job posting;
  - MO applicant review;
  - Admin workload;
  - Admin recommendations.
- Confirmed that the user manual can be used as a script for demo-video preparation.

### 2.4 Requirement Completion Alignment

- Helped check the final requirement checklist.
- Helped align the final product with the layered task plan and coursework brief.
- Recorded limitations such as manual GUI testing, optional external AI, and the need to assemble the final report separately.

---

## 3. Final Delivery Checkpoints

Pair A reviewed the final product from a demo perspective:

1. TA logs in and completes a profile.
2. TA browses jobs and checks match scores.
3. TA applies for a job.
4. MO logs in and selects or rejects the applicant.
5. TA checks application status and notifications.
6. Admin checks workload, exports a report, and reviews recommendations.
7. The system still works without an external AI key through local rule-based scoring.

Result:

- Core recruitment loop is demonstrable.
- TA, MO, and Admin boundaries are clear.
- Documentation supports final report writing and viva preparation.
- Screenshots cover the main frames.
- Test and JavaDoc scripts are documented in the README.

---

## 4. Issues Found and Handling

### 4.1 Final Version Differs from Early `ProjectRoot`

- **Issue**: The early `ProjectRoot` uses a package-based structure, while final `ver_1.0` uses a flatter source layout.
- **Handling**: README and alignment notes identify `ver_1.0` as the final demo folder and treat earlier directories as process evidence.

### 4.2 Product Backlog File Is Not Retained in `ver_1.0`

- **Issue**: The task plan mentions updating `ProductBacklog.md`, but the final demo folder does not contain a separate product-backlog file.
- **Handling**: Requirement status is documented through `final_requirement_checklist.md` and `task_plan_alignment.md`. If QMPlus still requires the original Excel backlog, the group should update that separate submission file.

### 4.3 Windows Compilation Encoding

- **Issue**: Some Java comments contain UTF-8 characters, and Windows `javac` may default to GBK.
- **Handling**: For Windows verification, use `javac -encoding UTF-8 -d bin src\*.java`, or add this note to the final README.

---

## 5. Delivery Value

Pair A's L6 support helped move the project from implementation to final submission readiness:

- the UI is easier to demonstrate;
- the README is easier for new users to follow;
- the user manual supports demo-video preparation;
- the requirement checklist supports assessment against the PDF brief;
- the final report can reference these documents as design, testing, and delivery evidence.

**Review status**: Completed.
