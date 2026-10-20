[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$runRoot = Join-Path $projectRoot '.run'
$backendJar = Join-Path $projectRoot 'backend\target\preapproved-api-0.0.1-SNAPSHOT.jar'
$angularCli = Join-Path $projectRoot 'frontend\node_modules\@angular\cli\bin\ng.js'
$stoppedProcesses = [System.Collections.Generic.HashSet[int]]::new()

function Stop-ProjectProcess {
    param(
        [Parameter(Mandatory)] [int] $TargetPid,
        [Parameter(Mandatory)] [string] $Name
    )

    $process = Get-Process -Id $TargetPid -ErrorAction SilentlyContinue
    if (-not $process) {
        return
    }

    Stop-Process -Id $TargetPid -ErrorAction SilentlyContinue

    $deadline = (Get-Date).AddSeconds(3)
    while ((Get-Process -Id $TargetPid -ErrorAction SilentlyContinue) -and
            (Get-Date) -lt $deadline) {
        Start-Sleep -Milliseconds 200
    }

    if (Get-Process -Id $TargetPid -ErrorAction SilentlyContinue) {
        Stop-Process -Id $TargetPid -Force -ErrorAction SilentlyContinue
        $deadline = (Get-Date).AddSeconds(5)
        while ((Get-Process -Id $TargetPid -ErrorAction SilentlyContinue) -and
                (Get-Date) -lt $deadline) {
            Start-Sleep -Milliseconds 200
        }
    }

    if (Get-Process -Id $TargetPid -ErrorAction SilentlyContinue) {
        throw "No fue posible detener $Name (PID $TargetPid)."
    }

    [void]$stoppedProcesses.Add($TargetPid)
    Write-Host "$Name detenido (PID $TargetPid)."
}

foreach ($name in @('backend', 'frontend')) {
    $pidFile = Join-Path $runRoot "$name.pid"
    if (-not (Test-Path -LiteralPath $pidFile)) {
        continue
    }

    $targetPid = [int](Get-Content -LiteralPath $pidFile -Raw).Trim()
    Stop-ProjectProcess -TargetPid $targetPid -Name $name

    Remove-Item -LiteralPath $pidFile -Force
}

# Recupera procesos huérfanos cuando los archivos PID ya no existen. Se compara
# la ruta completa para no afectar otras aplicaciones Java o Angular del equipo.
try {
    $projectProcesses = @()
    $projectProcesses += Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" |
        Where-Object { $_.CommandLine -like "*$backendJar*" }
    $projectProcesses += Get-CimInstance Win32_Process -Filter "Name = 'node.exe'" |
        Where-Object { $_.CommandLine -like "*$angularCli*" }

    foreach ($projectProcess in $projectProcesses) {
        $targetPid = [int]$projectProcess.ProcessId
        if (-not $stoppedProcesses.Contains($targetPid)) {
            $name = if ($projectProcess.Name -eq 'java.exe') { 'backend' } else { 'frontend' }
            Stop-ProjectProcess -TargetPid $targetPid -Name $name
        }
    }
}
catch {
    Write-Warning 'No fue posible buscar procesos huérfanos; usa el Administrador de tareas si el JAR continúa bloqueado.'
}
