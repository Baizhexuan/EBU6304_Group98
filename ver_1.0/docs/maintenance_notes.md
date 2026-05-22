# Maintenance Notes

These notes describe low-risk maintenance areas that are useful for future iterations without
changing the product workflow.

## Safe Change Areas

- Add focused console tests for existing helper methods and services.
- Expand documentation that explains how to run, verify, or demonstrate the system.
- Improve local development scripts for Windows and Unix-like terminals.
- Add reviewer-facing traceability tables that map tests to workflow requirements.
- Clarify JavaDoc comments where a method is used by multiple dashboards.

## Areas Requiring Extra Care

- Dashboard classes are large Swing files and should be edited in small, verified steps.
- CSV schema changes require updates to `FileStorage`, seed data, tests, and documentation.
- AI provider changes should preserve the offline rule-based fallback path.
- Notification changes should preserve de-duplication for unread profile reminders.
- Authentication changes should keep stored passwords hashed.

## Verification Routine

Run the PowerShell test script on Windows:

```powershell
powershell -ExecutionPolicy Bypass -File .\test.ps1
```

Run the shell test script on macOS or Linux:

```bash
sh test.sh
```

The automated checks do not replace GUI demo testing, but they quickly catch regressions in
CSV persistence, matching, notifications, authentication, and workflow rules.
