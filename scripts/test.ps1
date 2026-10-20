[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$env:MAVEN_USER_HOME = Join-Path $projectRoot '.tools\m2'

Write-Host 'Ejecutando pruebas reactivas del backend...' -ForegroundColor Cyan
Push-Location (Join-Path $projectRoot 'backend')
try {
    & '.\mvnw.cmd' test --no-transfer-progress
    if ($LASTEXITCODE -ne 0) {
        throw 'Fallaron las pruebas del backend.'
    }
}
finally {
    Pop-Location
}

Write-Host 'Compilando frontend para producción...' -ForegroundColor Cyan
& node (Join-Path $projectRoot 'frontend\node_modules\@angular\cli\bin\ng.js') build
if ($LASTEXITCODE -ne 0) {
    throw 'Falló la compilación del frontend.'
}

Write-Host 'Validación completa.' -ForegroundColor Green
