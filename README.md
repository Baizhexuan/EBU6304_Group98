# EBU6304 Group 98 — International School Teaching Assistant Recruitment System

A stand-alone Java Swing application for supporting Teaching Assistant recruitment at BUPT International School.

The system provides role-based workflows for Teaching Assistant applicants, Module Organisers, and Administrators. It replaces the previous form/Excel-based process with a lightweight desktop application using CSV text-file storage, while keeping the design simple, modular, and suitable for the EBU6304 Software Engineering Group Project.

---

## 1. Project Overview

BUPT International School recruits Teaching Assistants each semester to support academic modules and school activities such as invigilation. The original process depends heavily on forms and Excel files, which makes application tracking, workload checking, and applicant-job matching difficult to manage.

This project implements a selected set of core recruitment features:

- TA profile creation and editing
- CV path recording
- Job browsing and application submission
- Application status checking
- MO job posting and applicant review
- Admin workload monitoring
- In-app notifications
- Explainable AI-assisted applicant-job matching and workload recommendations

The current integrated product is located in:

```bash
ver_1.0/
```

---

## 2. Coursework Compliance

This project follows the mandatory EBU6304 constraints:

| Requirement | Project Implementation |
| --- | --- |
| Application type | Stand-alone Java desktop application |
| UI technology | Java Swing |
| Data storage | CSV text files under `data/` |
| Database usage | No database is used |
| Framework usage | No Spring Boot or heavy external framework |
| Core users | TA, MO, Admin |
| AI support | Explainable rule-based matching with optional external model configuration |
| Final evidence | Source code, tests, JavaDoc script, user manual, screenshots, testing strategy, requirement checklist |

---

## 3. Main Features

### 3.1 Teaching Assistant Features

Teaching Assistant users can:

- log in with a TA account
- create or update their applicant profile
- record skills, GPA, availability, personal statement, and CV path
- browse available jobs
- filter/search job listings
- view match score and missing-skill explanation
- apply for jobs
- check application status
- withdraw pending applications
- receive notifications about application decisions, profile reminders, and job-closure events

### 3.2 Module Organiser Features

Module Organiser users can:

- log in with an MO account
- post new TA jobs
- manage their own job posts
- close or reopen jobs
- review applicants for their jobs
- inspect applicant match scores and explanations
- select or reject applicants
- trigger status notifications for TAs

### 3.3 Administrator Features

Administrator users can:

- log in with an Admin account
- monitor TA workload
- identify `OK`, `NEAR LIMIT`, and `OVERLOAD` workload states
- view global application and job records
- edit administrative overview tables
- export workload reports as CSV files
- inspect replacement recommendations
- use the Admin AI Assistant for recruitment-support questions

### 3.4 AI-Assisted Features

The project includes explainable AI-assisted support for:

- matching TA skills with job requirements
- identifying missing skills
- producing match summaries
- warning about workload risk
- suggesting replacement candidates
- supporting Admin decision-making through an AI Assistant dialog

The system does not depend on a live AI key for the demo. If no external model is configured, it falls back to local explainable rule-based logic.

---

## 21. Contributors

Group 98 — EBU6304 Software Engineering Group Project

---

## 22. Licence

This repository is for EBU6304 coursework and educational demonstration purposes.
