# EBU6304 Project Work Record: L4 Admin and Advanced Features (Pair A Lead)

**Project**: BUPT International School TA Recruitment System  
**Date**: 2026-05-21, based on the final `ver_1.0` integration  
**Members**: Bai Zhexuan, Gao Weicheng  
**Stage**: L4 - Admin and Advanced Features  
**Pair A role**: Lead group

---

## 1. Objective

In the L4 task plan, Pair A led the Admin and advanced-management layer. The goal was to move beyond individual TA and MO actions and give the administrator a global view of the recruitment process.

The Admin side needed to answer three questions:

1. Which TAs have been selected?
2. How many selected working hours does each TA currently have?
3. Which TAs are near the workload limit or overloaded?

In the final `ver_1.0` product, this work is mainly visible through the Admin dashboard and the admin recommendation service.

## 2. Completed Work

### 2.1 US-7: Admin Workload Monitor

- Implemented and reviewed the workload-monitoring view.
- Displayed TA name, email, application count, selected-job count, selected hours, and workload status.
- Used three visible status labels: `OK`, `NEAR LIMIT`, and `OVERLOAD`.
- Added filtering by username, name, email, and status.
- Supported exporting a workload report for review and demonstration.

### 2.2 Global Application Management

- Implemented and reviewed the application overview page.
- Allowed Admin users to inspect all application records.
- Supported editing selected fields such as status and reviewer notes.
- Added save and undo behavior to reduce the risk of accidental edits.

### 2.3 Global Job Management

- Implemented and reviewed the job overview page.
- Allowed Admin users to inspect and edit job records.
- Added validation for role-related and numeric fields where required.
- Supported undoing unsaved changes before committing them.

### 2.4 Unsaved-Change Protection

- Added tracking for unsaved changes in Admin editing screens.
- Displayed confirmation prompts when leaving a page or closing the window with unsaved edits.
- Reduced the risk of silent data loss during admin editing.

### 2.5 Admin Recommendation and Risk Support

- Added workload risk summaries for Admin review.
- Generated replacement suggestions based on selected hours and match score.
- Included projected load, risk label, reasoning, and next-step advice in recommendation text.

---

## 3. Key Design Decisions

| Decision Area | Decision | Reason |
| :--- | :--- | :--- |
| Workload calculation | Count hours only from `SELECTED` applications | Only accepted jobs create actual TA workload. |
| Risk labels | `OK`, `NEAR LIMIT`, `OVERLOAD` | Easier for Admin users to scan than raw numbers only. |
| Admin editing | Inline table editing with save and undo | Gives Admin users control while keeping recovery options. |
| Recommendation behavior | Explainable advice, no automatic reassignment | Matches the coursework requirement for responsible AI-assisted support. |

---

## 4. Problems and Solutions

### 4.1 Workload Is Not the Same as Application Count

- **Problem**: Counting applications alone does not show real workload because pending and rejected applications do not create assigned work.
- **Solution**: The final logic counts selected applications and their related job hours.

### 4.2 Admin Editing Can Break Consistency

- **Problem**: Global editing can create invalid status values, invalid hour values, or role mismatches.
- **Solution**: Save logic includes validation and undo support.

### 4.3 Recommendation Results Need Explanation

- **Problem**: A recommendation without evidence is hard to defend during viva.
- **Solution**: Recommendation text includes match score, current load, projected load, risk level, and suggested next action.

---

## 5. Evidence

- `SystemSmokeTest` checks basic system status, scoring, notifications, and admin alert generation.
- `WorkflowRulesTest` covers workflow rules, notifications, scoring labels, and workload-related behavior.
- Admin screenshots show the workload monitor and recommendation views.
- `docs/final_requirement_checklist.md` maps the Admin workload feature to the coursework requirement.

**Review status**: Completed.
