[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$runRoot = Join-Path $projectRoot '.run'

foreach ($name in @('backend', 'frontend')) {
    $pidFile = Join-Path $runRoot "$name.pid"
    if (-not (Test-Path -LiteralPath $pidFile)) {
        continue
    }

    $processId = [int](Get-Content -LiteralPath $pidFile -Raw).Trim()
    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if ($process) {
        Stop-Process -Id $processId
        Start-Sleep -Seconds 1
        if (Get-Process -Id $processId -ErrorAction SilentlyContinue) {
            Stop-Process -Id $processId -Force
        }
        Write-Host "$name detenido (PID $processId)."
    }

    Remove-Item -LiteralPath $pidFile -Force
}
