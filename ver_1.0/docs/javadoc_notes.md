# JavaDoc Notes

The generated JavaDoc output is stored in `javadocs/` for submission review. The source of
the overview page is `docs/javadoc_overview.html` so the generated documentation can be
rebuilt consistently.

## Generate Documentation

macOS or Linux:

```bash
sh javadoc.sh
```

Windows PowerShell:

```powershell
.\javadoc.ps1
```

Both scripts include the same overview, window title, and document title.
They keep JavaDoc structural checks enabled while suppressing missing-comment noise from
large Swing private fields.

## Review Focus

- `Main` explains the application launch path.
- `FileStorage` explains CSV-backed persistence helpers.
- `NotificationService` explains workflow event notifications.
- `MatchingService` and `ScoringService` explain matching and provider delegation.
- `SkillScoringProvider` explains the local/AI scoring abstraction.
- `*Test` classes describe the lightweight regression checks used for final delivery.

For a quick generated-output checklist, see `docs/javadoc_review_checklist.md`.
