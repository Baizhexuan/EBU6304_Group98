# EBU6304 Project Work Record: L2 Authentication and UI Framework (Pair A Review)

**Project**: BUPT International School TA Recruitment System  
**Date**: 2026-04-09; updated on 2026-05-21 against the final `ver_1.0` integration  
**Members**: Bai Zhexuan, Gao Weicheng  
**Stage**: L2 - Authentication and UI Framework  
**Pair A role**: Review and testing group

---

## 1. Objective

According to the layered task plan, Pair B led the login, registration, and dashboard framework, while Pair C assisted with dashboard skeletons. Pair A was responsible for reviewing and testing this layer.

The goal was to confirm that users could enter the system through a stable authentication flow and then be routed to the correct dashboard based on role: TA, MO, or Admin.

## 2. Completed Work

### 2.1 Authentication Review

- Reviewed the login flow for normal login, empty input, unknown users, and incorrect credentials.
- Reviewed the registration flow for role selection, password confirmation, and duplicate username prevention.
- Checked that role-based routing opens the correct dashboard after login.

### 2.2 Integration Testing

- Added and reviewed authentication-oriented tests in the early project stage.
- Covered key scenarios:
  - successful login;
  - failed login with wrong password;
  - failed login with unknown username;
  - duplicate registration prevention;
  - basic new-user persistence.
- In the final `ver_1.0` version, these concerns are represented by `AuthFlowTest`.

### 2.3 UI Framework Review

- Reviewed the shared dashboard structure used by TA, MO, and Admin screens.
- Confirmed that the main dashboards expose separate tabs for each role's workflow.
- Checked that the final `ver_1.0` login and registration screens are suitable for classroom demonstration.

---

## 3. Key Design Decisions

| Review Area | Decision or Standard | Reason |
| :--- | :--- | :--- |
| Test approach | Lightweight Java main-method tests | Avoids extra dependencies and keeps the project easy to compile. |
| UI testing boundary | Review UI manually and test logic separately | Swing UI automation is heavy for this coursework scope. |
| Role routing | Route by user role after login | Matches the three-user structure required by the project brief. |
| Security focus | Check duplicate users and credential validation | Login is the entry point for all later workflows. |

---

## 4. Problems and Solutions

### 4.1 External Test Dependencies

- **Problem**: A full JUnit setup would require extra configuration and could make the coursework package harder to run.
- **Solution**: The team used lightweight executable Java tests with custom assertions.

### 4.2 UI and Business Logic Separation

- **Problem**: Directly testing Swing click behavior is time-consuming and brittle.
- **Solution**: Pair A focused automated tests on authentication rules and used manual review for UI rendering and navigation.

---

## 5. Link to Final V1.0

The final `ver_1.0` product includes:

- login and registration screens;
- role-based navigation into TA, MO, and Admin dashboards;
- demo accounts for each role;
- `AuthFlowTest` as authentication regression evidence.

**Review status**: Completed.

