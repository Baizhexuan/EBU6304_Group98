$ErrorActionPreference = "Stop"

if (-not (Test-Path "javadocs")) {
    New-Item -ItemType Directory -Path "javadocs" | Out-Null
}

javadoc `
    -d javadocs `
    -overview docs\javadoc_overview.html `
    -windowtitle "BUPT TA Recruitment System API" `
    -doctitle "BUPT TA Recruitment System API" `
    -Xdoclint:all,-missing `
    src\*.java
