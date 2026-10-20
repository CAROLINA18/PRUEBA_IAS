[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$toolsRoot = Join-Path $projectRoot '.tools'
$npmCache = Join-Path $toolsRoot 'npm-cache'
$mavenHome = Join-Path $toolsRoot 'm2'

New-Item -ItemType Directory -Force $toolsRoot, $npmCache, $mavenHome | Out-Null

function Install-WithWinget {
    param(
        [Parameter(Mandatory)] [string] $PackageId,
        [Parameter(Mandatory)] [string] $DisplayName
    )

    if (-not (Get-Command winget -ErrorAction SilentlyContinue)) {
        throw "$DisplayName no está instalado y winget no está disponible. Instálalo manualmente y vuelve a ejecutar este script."
    }

    Write-Host "Instalando $DisplayName con winget..." -ForegroundColor Yellow
    winget install --id $PackageId -e --silent --accept-package-agreements --accept-source-agreements
    if ($LASTEXITCODE -ne 0) {
        throw "No fue posible instalar $DisplayName (código $LASTEXITCODE)."
    }
}

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Install-WithWinget -PackageId 'EclipseAdoptium.Temurin.21.JDK' -DisplayName 'Java 21'
    throw 'Java fue instalado. Abre una nueva terminal y vuelve a ejecutar scripts/setup.ps1 para actualizar PATH.'
}

$javaOutput = (& java -version 2>&1 | Out-String)
if ($javaOutput -notmatch 'version "(?<major>\d+)') {
    throw 'No fue posible determinar la versión de Java.'
}
$javaMajor = [int]$Matches.major
if ($javaMajor -lt 21 -or $javaMajor -gt 26) {
    throw "Se requiere Java entre 21 y 26; se encontró Java $javaMajor."
}

if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    Install-WithWinget -PackageId 'OpenJS.NodeJS.LTS' -DisplayName 'Node.js LTS'
    throw 'Node.js fue instalado. Abre una nueva terminal y vuelve a ejecutar scripts/setup.ps1 para actualizar PATH.'
}

$nodeVersion = [version]((& node --version).TrimStart('v'))
$supportedNode =
    ($nodeVersion.Major -eq 20 -and $nodeVersion -ge [version]'20.19.0') -or
    ($nodeVersion.Major -eq 22 -and $nodeVersion -ge [version]'22.12.0') -or
    ($nodeVersion.Major -eq 24)
if (-not $supportedNode) {
    throw "Angular 20 requiere Node 20.19+, 22.12+ o 24.x; se encontró $nodeVersion."
}

$npmCommand = Get-Command npm.cmd -ErrorAction SilentlyContinue
if (-not $npmCommand) {
    throw 'npm.cmd no está disponible en PATH.'
}

Write-Host 'Preparando dependencias del backend...' -ForegroundColor Cyan
$env:MAVEN_USER_HOME = $mavenHome
Push-Location (Join-Path $projectRoot 'backend')
try {
    & '.\mvnw.cmd' package -DskipTests --no-transfer-progress
    if ($LASTEXITCODE -ne 0) {
        throw "Falló la preparación del backend (código $LASTEXITCODE)."
    }
}
finally {
    Pop-Location
}

Write-Host 'Instalando dependencias del frontend...' -ForegroundColor Cyan
Push-Location (Join-Path $projectRoot 'frontend')
try {
    & $npmCommand.Source install --cache $npmCache --no-audit --no-fund
    if ($LASTEXITCODE -ne 0) {
        throw "Falló npm install (código $LASTEXITCODE)."
    }
}
finally {
    Pop-Location
}

Write-Host ''
Write-Host 'Entorno listo. Ejecuta: .\scripts\run.ps1' -ForegroundColor Green
