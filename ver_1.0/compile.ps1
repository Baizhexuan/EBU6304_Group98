$ErrorActionPreference = "Stop"

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

javac -d bin src\*.java
