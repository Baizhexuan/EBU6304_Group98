#  BUPT International School TA Recruitment System - Prototype Description (Group 98)

## 1. Prototype Overview
This prototype adopts a **Low-Fidelity Wireframe** style. By removing complex colors and visual distractions (utilizing basic black, white, gray, placeholders, and hand-drawn style fonts), the focus is entirely directed toward the core functional flow and information architecture of the system.

The prototype utilizes a Single Page Application (SPA) tab structure, integrating the three core user perspectives of the system: **TA View**, **MO View**, and **Admin View**. Yellow "Annotations" have been specifically incorporated into the design to directly map the key User Stories and technical constraints from our Product Backlog.

---

## 2. Core Views and Feature Analysis

### 2.1 TA View (Student Perspective)
This interface is designed to provide applicants with a seamless profile management and job search experience.
* **Profile and CV Management (US01/US02):**
  * Provides a basic personal information display area.
  * Includes an `Upload CV (.txt/.pdf)` button. In compliance with the system's "no-database" constraint, structured user data will be converted into **JSON format** text files for local storage.
* **Job Board and Application Tracking (US08):**
  * The `Job Search` module allows TAs to browse and apply for open positions.
  * The `Application Status` table provides a transparent tracking mechanism for TAs (displaying module name, current status such as Pending/Accepted, and notes), which directly addresses the pain point of information opacity in the existing manual process (aligning with the survey result where 80% of students expressed frustration).

### 2.2 MO View (Module Organiser Perspective)
This interface provides professors and module organisers with an efficient recruitment management tool.
* **Job Posting (US05/US06):**
  * Features a prominent `+ Create New Job Post` button at the top for quick publication of new requirements.
* **Smart Applicant Screening (US09 - AI Matching Feature):**
  * In the `Current Applicants` list, the prototype demonstrates the core highlight of the system: **AI Skill Match Visualization**.
  * The system intuitively presents the matching score of each applicant (e.g., `AI Match: 92% (Java, Python)` or `45% (Missing SQL)`). This significantly reduces the MO's screening effort and aligns with the 70% of students who wished for proactive skill matching.

### 2.3 Admin View (Administrator Perspective)
This interface directly responds to the core feedback provided by the mock "Client" during our early research regarding "preventing high-performing students from being over-allocated."
* **Global Workload Monitor (US07):**
  * The `System Workload Overview` table lists all TAs' IDs, the number of modules they have taken, and their total working hours.
  * **Overload Warning Mechanism:** When a TA's total working hours exceed a safe threshold (e.g., TA #1002 in the prototype reaching 28 hours), the status bar automatically triggers an `[OVERLOAD!]` visual warning. This helps administrators intervene in a timely manner to prevent a single high-performing student from being over-allocated.
* **Data Persistence Guarantee:**
  * Provides an `Export Report (CSV)` button. Given the restriction of not using traditional relational databases, adopting the **CSV format** ensures data readability and backward compatibility (can be opened directly via Microsoft Excel) in the event of a system failure, directly responding to the client's request for system transparency.

---

## 3. Alignment with Agile Development
This prototype strictly follows the principles of Agile development:
1. **Embracing Constraints:** The UI annotations explicitly state the text storage strategy using JSON and CSV, proving that hard technical constraints have been considered during the design phase.
2. **Value-Driven:** The prototype focuses on showcasing high-priority (Must-Have) features (such as application status tracking and workload monitoring) as well as features that bring significant business value (Should-Have) (such as AI skill matching).
