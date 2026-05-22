# JavaDoc Review Checklist

Use this checklist after regenerating `javadocs/`.

## Generated Pages To Check

- `javadocs/index.html` opens the generated documentation.
- `javadocs/overview-summary.html` shows the project overview from `docs/javadoc_overview.html`.
- `javadocs/allclasses-index.html` lists both production classes and regression tests.
- `javadocs/FileStorage.html` documents CSV persistence helpers.
- `javadocs/NotificationService.html` documents notification workflow events.
- `javadocs/MatchingService.html` documents deterministic skill matching.
- `javadocs/ScoringService.html` documents scoring-provider delegation.

## Newly Covered Regression Test Pages

- `javadocs/ValidationUtilsTest.html`
- `javadocs/MatchingServiceTest.html`
- `javadocs/ModelStateTest.html`
- `javadocs/ScoringServiceTest.html`
- `javadocs/NotificationReadStateTest.html`
- `javadocs/FileStorageLookupTest.html`
- `javadocs/DemoMetadataTest.html`

## Regeneration Command

```powershell
powershell -ExecutionPolicy Bypass -File .\javadoc.ps1
```

The generation should complete without warnings after the missing-comment doclint noise is
suppressed by the script.
