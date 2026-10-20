[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$runRoot = Join-Path $projectRoot '.run'
$backendJar = Join-Path $projectRoot 'backend\target\preapproved-api-0.0.1-SNAPSHOT.jar'
$angularCli = Join-Path $projectRoot 'frontend\node_modules\@angular\cli\bin\ng.js'
$mavenHome = Join-Path $projectRoot '.tools\m2'

New-Item -ItemType Directory -Force $runRoot | Out-Null

if (-not (Test-Path -LiteralPath $angularCli)) {
    throw 'Faltan dependencias del frontend. Ejecuta primero .\scripts\setup.ps1.'
}

Write-Host 'Cerrando una instancia anterior del proyecto, si existe...' -ForegroundColor DarkGray
& (Join-Path $PSScriptRoot 'stop.ps1')

Write-Host 'Compilando backend...' -ForegroundColor Cyan
$env:MAVEN_USER_HOME = $mavenHome
Push-Location (Join-Path $projectRoot 'backend')
try {
    & '.\mvnw.cmd' package -DskipTests --no-transfer-progress
    if ($LASTEXITCODE -ne 0) {
        throw 'No fue posible compilar el backend.'
    }
}
finally {
    Pop-Location
}

$backendOut = Join-Path $runRoot 'backend.out.log'
$backendErr = Join-Path $runRoot 'backend.err.log'
$frontendOut = Join-Path $runRoot 'frontend.out.log'
$frontendErr = Join-Path $runRoot 'frontend.err.log'

$backend = Start-Process -FilePath 'java.exe' `
    -ArgumentList @('-jar', $backendJar) `
    -WorkingDirectory (Join-Path $projectRoot 'backend') `
    -WindowStyle Hidden `
    -RedirectStandardOutput $backendOut `
    -RedirectStandardError $backendErr `
    -PassThru

$frontend = Start-Process -FilePath 'node.exe' `
    -ArgumentList @($angularCli, 'serve', '--host', '127.0.0.1', '--port', '4200') `
    -WorkingDirectory (Join-Path $projectRoot 'frontend') `
    -WindowStyle Hidden `
    -RedirectStandardOutput $frontendOut `
    -RedirectStandardError $frontendErr `
    -PassThru

Set-Content -LiteralPath (Join-Path $runRoot 'backend.pid') -Value $backend.Id
Set-Content -LiteralPath (Join-Path $runRoot 'frontend.pid') -Value $frontend.Id

function Wait-ForUrl {
    param([string] $Url, [string] $Name)

    $deadline = (Get-Date).AddSeconds(90)
    do {
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 2
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
                Write-Host "$Name listo: $Url" -ForegroundColor Green
                return
            }
        }
        catch {
            Start-Sleep -Seconds 1
        }
    } while ((Get-Date) -lt $deadline)

    throw "$Name no inició a tiempo. Revisa los archivos .run/*.log."
}

try {
    Wait-ForUrl -Url 'http://localhost:8080/actuator/health' -Name 'Backend'
    Wait-ForUrl -Url 'http://127.0.0.1:4200' -Name 'Frontend'
    Write-Host ''
    Write-Host 'Aplicación disponible en http://127.0.0.1:4200' -ForegroundColor Cyan
    Write-Host 'Para detenerla: .\scripts\stop.ps1'
}
catch {
    & (Join-Path $PSScriptRoot 'stop.ps1')
    throw
}
