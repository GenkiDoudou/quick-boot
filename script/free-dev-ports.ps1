#Requires -Version 5.1
<#
.SYNOPSIS
  释放本地开发常用端口，解决 Spring Boot / 嵌入式 Redis 启动 BindException。

.DESCRIPTION
  默认释放：
    - 9993  HTTP（quickboot-web server.port）
    - 6379  嵌入式 Redis（qc.dev.embedded-redis.port）

  用法（在仓库根目录）:
    .\script\free-dev-ports.ps1
    .\script\free-dev-ports.ps1 -Ports 9993,6379
    .\script\free-dev-ports.ps1 -WhatIf
#>
[CmdletBinding(SupportsShouldProcess = $true)]
param(
  [int[]]$Ports = @(9993, 6379)
)

$ErrorActionPreference = 'Continue'

function Get-PidsOnPort {
  param([int]$Port)
  $pids = @()
  try {
    $pids = @(
      Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique |
        Where-Object { $_ -and $_ -ne 0 }
    )
  } catch {
    # fallback: netstat
    $lines = netstat -ano | Select-String ":$Port\s+.*LISTENING"
    foreach ($line in $lines) {
      $parts = ($line.ToString() -split '\s+') | Where-Object { $_ }
      $pidText = $parts[-1]
      if ($pidText -match '^\d+$' -and [int]$pidText -ne 0) {
        $pids += [int]$pidText
      }
    }
  }
  return @($pids | Select-Object -Unique)
}

Write-Host "Freeing ports: $($Ports -join ', ')" -ForegroundColor Cyan

$killed = @{}
foreach ($port in $Ports) {
  $pids = Get-PidsOnPort -Port $port
  if (-not $pids -or $pids.Count -eq 0) {
    Write-Host "  :$port  idle" -ForegroundColor DarkGray
    continue
  }
  foreach ($procId in $pids) {
    if ($killed.ContainsKey($procId)) { continue }
    $name = '(unknown)'
    try {
      $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
      if ($proc) { $name = $proc.ProcessName }
    } catch {}
    if ($PSCmdlet.ShouldProcess("PID $procId ($name) on :$port", 'Stop-Process')) {
      try {
        Stop-Process -Id $procId -Force -ErrorAction Stop
        Write-Host "  :$port  stopped PID $procId ($name)" -ForegroundColor Yellow
        $killed[$procId] = $true
      } catch {
        Write-Host "  :$port  FAILED to stop PID $procId ($name): $_" -ForegroundColor Red
      }
    }
  }
}

Start-Sleep -Milliseconds 500

Write-Host ''
Write-Host 'Recheck:' -ForegroundColor Cyan
$busy = $false
foreach ($port in $Ports) {
  $left = Get-PidsOnPort -Port $port
  if ($left.Count -eq 0) {
    Write-Host "  :$port  free" -ForegroundColor Green
  } else {
    $busy = $true
    Write-Host "  :$port  still held by PID $($left -join ',')" -ForegroundColor Red
  }
}

if ($busy) {
  Write-Host ''
  Write-Host 'Some ports remain busy. Close the process manually or run as Administrator.' -ForegroundColor Red
  exit 1
}

Write-Host ''
Write-Host 'Done. You can start the app:' -ForegroundColor Green
Write-Host '  cd quickboot'
Write-Host '  mvn -pl quickboot-web spring-boot:run'
exit 0
