# EBU6304 Group 98 Demo Version 1.5

BUPT International School Teaching Assistant Recruitment System.

## Product Scope

This demo implements the core recruitment workflow required for a stand-alone Java application:

- TA can create and edit an applicant profile
- TA can browse open jobs and apply for them
- TA can check application status and withdraw pending applications
- MO can post jobs, manage their own posts, and review applicants
- Admin can monitor TA workload, edit global application and job records, export reports, and inspect replacement recommendations
- AI-assisted scoring is included through an explainable rule-based engine and an API-ready placeholder provider

## Requirement Check

### Basic requirements covered

- stand-alone Java desktop application
- all data stored in CSV text files
- TA profile creation
- CV path selection through a local file chooser
- available job browsing
- job application flow
- application status checking
- MO job posting
- MO applicant selection and rejection
- Admin workload monitoring

### AI-assisted functions currently covered

- matching skills between jobs and applicants
- identifying missing skills in match summaries
- workload balancing support through admin-side replacement recommendations and load warnings

### Current project position

This `ver_1.0` folder now meets the mandatory platform and storage restrictions and demonstrates a selected set of core features as required by the coursework brief. It is still an iterative demo build rather than the full final coursework package, because the broader Agile evidence, formal test suite, JavaDoc delivery, and complete report package belong to the wider repository work rather than only this folder.

## Iteration 1.5 Update

This iteration focuses on final-demo usability, macOS compatibility, and clearer product presentation.

New updates in this version:

- enlarged and rebalanced the login screen for macOS-friendly text and input rendering
- redesigned the registration screen with wider fields, clearer spacing, and easier onboarding
- removed feature-heavy landing-page copy and replaced it with a cleaner product-style entry experience
- improved TA and MO dashboards with clearer summaries, sortable tables, and more presentation-ready feedback
- strengthened admin recommendation explanations with an operational checklist, action memos, and projected-load guidance
- improved shared dashboard behaviour for smaller windows through better minimum sizing and tab handling

## Run

```bash
./compile.sh
./run.sh
```

## Optional AI Placeholder Configuration

```bash
export AI_SCORING_MODE=AI
export OPENAI_API_KEY=your_key_here
export OPENAI_MODEL=gpt-4o-mini
export OPENAI_BASE_URL=https://api.openai.com/v1
```

If these variables are not set, the demo automatically falls back to the local rule-based scorer.

## Demo Accounts

- `admin / admin123`
- `ta1 / ta123`
- `ta2 / ta456`
- `mo1 / mo123`
- `mo2 / mo456`

## Project Layout

- `src/`: Java source code
- `data/`: CSV data files
- `docs/architecture.md`: structure overview
- `docs/task_plan_alignment.md`: requirement and task-plan comparison notes
- `compile.sh`: compile script
- `run.sh`: run script

## Version Notes

- `ver_1.0`: first complete integrated demo build
- `ver_1.1`: usability-focused iteration with filtering and improved admin monitoring feedback
- `ver_1.2`: task-plan alignment update with shared dashboard base and stronger L2 authentication checks
- `ver_1.3`: stronger admin operations and AI-ready scoring abstraction for the next integration stage
- `ver_1.4`: live AI placeholder path, admin reallocation recommendations, and UI polish for the next demo stage
- `ver_1.5`: macOS-friendly entry screens, cleaner final-product styling, and stronger final-demo usability
