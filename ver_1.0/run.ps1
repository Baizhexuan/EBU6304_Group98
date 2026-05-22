$ErrorActionPreference = "Stop"

.\compile.ps1
java -cp bin Main
