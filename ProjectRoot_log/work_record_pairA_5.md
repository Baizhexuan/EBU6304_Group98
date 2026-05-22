# EBU6304 Project Work Record: L5 AI-Assisted Features (Pair A Review and Testing)

**Project**: BUPT International School TA Recruitment System  
**Date**: 2026-05-21, based on the final `ver_1.0` integration  
**Members**: Bai Zhexuan, Gao Weicheng  
**Stage**: L5 - AI-Assisted Features  
**Pair A role**: Review and testing group

---

## 1. Objective

In the L5 task plan, Pair B led AI skill matching, Pair C supported workload-balancing features, and Pair A reviewed and tested the AI-assisted layer.

Pair A's objective was to confirm that the final `ver_1.0` AI-related features are:

- explainable;
- usable offline without an API key;
- supportive rather than fully automatic;
- visible in TA, MO, and Admin workflows.

## 2. Reviewed Features

### 2.1 Skill-Matching Engine

- Reviewed the matching logic that compares TA skills with job-required skills.
- Confirmed that the system returns a 0-100 match score.
- Confirmed that the explanation includes matched skills and missing skills.
- Confirmed that the logic handles basic case differences and common skill separators.

### 2.2 Scoring Provider Abstraction

- Reviewed the scoring facade and provider interface.
- Confirmed that the product supports local rule-based scoring and optional external model scoring.
- Confirmed that local scoring is the default when no API key is configured.

### 2.3 TA and MO AI Display

- Confirmed that TAs can see match scores and missing-skill hints while browsing jobs.
- Confirmed that MOs can see applicant match scores while reviewing applications.
- Confirmed that the scores are decision support only and do not automatically select or reject applicants.

### 2.4 Admin Workload Recommendation

- Reviewed the admin recommendation output.
- Confirmed that recommendation text includes current load, projected load, risk label, and next-step advice.
- Confirmed that Admin users are guided to review high-risk workload situations first.

### 2.5 AI Assistant Fallback

- Reviewed the AI assistant configuration and fallback behavior.
- Confirmed that the system still returns structured local advice when no external API key is present.
- Confirmed that external model failure does not block the core recruitment workflow.

---

## 3. Testing and Verification

Pair A used lightweight regression checks and manual review:

- `SystemSmokeTest`: checks seeded data, scoring, notification counts, and admin alert summary.
- `WorkflowRulesTest`: checks duplicate-application prevention, notifications, workload risk, and scoring-source labels.
- Manual demo review: logs into TA, MO, and Admin accounts and confirms that AI-related explanations are visible in the UI.

Verification result:

- Match score remains in the 0-100 range.
- Matching explanation shows matched and missing skills.
- Local rule-based scoring works without network access.
- External AI configuration is optional.
- Recommendations do not replace MO or Admin final decisions.

---

## 4. Review Findings

| Review Point | Result |
| :--- | :--- |
| Explainability | Passed. Results include score, matched skills, missing skills, and source labels. |
| Offline usability | Passed. The system works with local rules when no API key exists. |
| Responsibility boundary | Passed. The system gives advice but does not make final hiring decisions. |
| Test depth | Acceptable. Final tests cover major paths, but there is no separate retained 100+ record stress test. |
| External model dependency | Acceptable. The external model path is optional and does not affect the core demo. |

---

## 5. Remaining Limitations

- The original plan mentioned separate `AITest.java`, `E2ETest.java`, and 100+ data performance tests. These are not retained as separate final files in `ver_1.0`.
- Current AI matching is mostly keyword based, which is suitable for coursework demonstration but not a production hiring model.
- External model response parsing is lightweight and should be replaced with a robust JSON library in a production system.
- The final report should clearly state that AI is used as decision support only.

**Review status**: Completed.
