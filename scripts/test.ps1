[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$env:MAVEN_USER_HOME = Join-Path $projectRoot '.tools\m2'

Write-Host 'Ejecutando pruebas unitarias e integracion del backend...' -ForegroundColor Cyan
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

Write-Host 'Ejecutando pruebas unitarias del frontend...' -ForegroundColor Cyan
Push-Location (Join-Path $projectRoot 'frontend')
try {
    & npm.cmd run test:ci
    if ($LASTEXITCODE -ne 0) {
        throw 'Fallaron las pruebas unitarias del frontend.'
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
