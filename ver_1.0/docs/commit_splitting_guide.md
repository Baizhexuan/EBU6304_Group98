# Commit Splitting Guide

The current maintenance work can be split into small, meaningful commits. Each item below
is independent and should keep the application behaviour unchanged.

## Suggested Commit Groups

1. Fix source encoding for notification regression test
2. Add validation utility regression tests
3. Add matching service regression tests
4. Add model helper regression tests
5. Add scoring service regression tests
6. Add notification read-state regression tests
7. Add CSV lookup and ID allocation regression tests
8. Add demo metadata regression tests
9. Extend shell test runner with the new test programs
10. Add Windows PowerShell compile, run, and test scripts
11. Add Windows PowerShell JavaDoc generation script
12. Add JavaDoc overview and generation notes
13. Add JavaDoc review checklist
14. Regenerate JavaDocs with expanded test coverage pages
15. Document Windows development workflow
16. Expand testing strategy with the new coverage areas
17. Add regression test matrix for reviewer traceability
18. Update README build and test instructions
19. Add repository-level ignore rules for IDE and generated files

## Example Manual Flow

Use the IDE Git panel or terminal to stage one group at a time. After each group, run:

```powershell
powershell -ExecutionPolicy Bypass -File .\test.ps1
```

For documentation-only groups, running the test script is still a useful sanity check because
it confirms no accidental source edits were included.
