# EBU6304 Project Work Record: L1 Basic Architecture and Data Layer (Pair A Lead)

**Project**: BUPT International School TA Recruitment System  
**Date**: 2026-04-08; updated on 2026-05-21 against the final `ver_1.0` integration  
**Members**: Bai Zhexuan, Gao Weicheng  
**Stage**: L1 - Basic Architecture and Data Layer  
**Pair A role**: Lead group

---

## 1. Objective

The goal of this stage was to establish the technical foundation for the TA Recruitment System. Pair A focused on the core data model and file-based persistence structure so that later UI, authentication, TA, MO, Admin, and AI features could be built on stable data entities.

This work directly supported the coursework restriction that the product must use simple text-file storage and must not rely on a database.

## 2. Completed Work

### 2.1 Project Structure

- Created the early layered Java project structure under `ProjectRoot`.
- Separated responsibilities into model, UI, service, utility, and test-related areas.
- Prepared the codebase for later L2 authentication and dashboard development.

### 2.2 Core Models

- Implemented the main business entities used by the recruitment workflow:
  - `User`: account identity, username, password data, role, and display name.
  - `TAProfile`: TA profile information, including name, email, student ID, skills, GPA, CV path, availability, and statement.
  - `Job`: MO-posted job information, including title, module, description, required skills, hours, status, and location.
  - `Application`: connection between a TA and a job, including application status and review information.
- Used encapsulated fields and consistent model conversion logic to support persistence and testing.

### 2.3 CSV-Based Persistence

- Designed and implemented the early CSV storage approach.
- Supported loading and saving key model data without using a database.
- Added CSV parsing and writing support for records containing commas, quotes, and empty values.
- Established a simple data format that later versions could still inspect manually.

### 2.4 Initial Verification

- Added early model and storage checks to verify object creation, serialization, deserialization, and file round trips.
- Confirmed that the basic storage workflow could support later login, job posting, application, and admin-monitoring functions.

---

## 3. Key Design Decisions

| Decision Area | Pair A Decision | Reason |
| :--- | :--- | :--- |
| Storage medium | CSV text files | Required by the coursework brief and easy to inspect during demo. |
| Data model boundary | Separate model classes for users, profiles, jobs, and applications | Keeps later UI and workflow code easier to understand. |
| Skill list representation | Semicolon-separated skill values | Reduces conflicts with comma-based CSV fields. |
| Persistence style | Lightweight file I/O rather than database or framework | Keeps the project within the module constraints. |

---

## 4. Problems and Solutions

### 4.1 Package and Source-Root Issues

- **Problem**: In the early `ProjectRoot` stage, IDE warnings appeared when package declarations did not match physical folder paths.
- **Solution**: The folder layout and source-root assumptions were aligned so that model, utility, UI, and test code could compile consistently.

### 4.2 Text Encoding Issues

- **Problem**: Chinese console output and Java source comments could display incorrectly in Windows terminals.
- **Solution**: The team used UTF-8 terminal settings during development and later noted that Windows compilation may need explicit UTF-8 encoding.

---

## 5. Link to Final V1.0

The final `ver_1.0` product keeps the same core architecture idea:

- users, profiles, jobs, applications, and notifications are still the main records;
- the system still uses file-based persistence only;
- the final `FileStorage` implementation is a more integrated version of the early CSV storage direction;
- the data layer supports the complete TA, MO, and Admin workflow.

**Review status**: Completed. Pair C completed L1 testing and review.

