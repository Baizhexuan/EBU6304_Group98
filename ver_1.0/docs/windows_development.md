# Windows Development Notes

This project can be compiled and tested from PowerShell without changing the original
macOS/Linux shell scripts.

## PowerShell Commands

Compile:

```powershell
.\compile.ps1
```

Run the Swing application:

```powershell
.\run.ps1
```

Run the regression suite:

```powershell
.\test.ps1
```

Generate JavaDocs:

```powershell
.\javadoc.ps1
```

Generated JavaDocs include the overview from `docs/javadoc_overview.html`.

If PowerShell blocks local scripts on a lab machine, run the script with a temporary
execution-policy override:

```powershell
powershell -ExecutionPolicy Bypass -File .\test.ps1
```

## IDE Notes

- Set the project SDK to a JDK that supports standard Swing and `javac`.
- Use `ver_1.0` as the working directory when running scripts from the IDE terminal.
- Keep generated `.class` files under `bin/`; source files stay under `src/`.
- CSV files under `data/` are part of the demo state and should be reviewed before committing.
