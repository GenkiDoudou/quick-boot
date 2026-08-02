#Requires -Version 5.1
<#
.SYNOPSIS
  先释放 9993/6379，再启动 quickboot-web。

.DESCRIPTION
  用法（仓库根目录）:
    .\script\start-web.ps1
    .\script\start-web.ps1 -SkipInstall
#>
[CmdletBinding()]
param(
  [switch]$SkipInstall
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot
$FreeScript = Join-Path $PSScriptRoot 'free-dev-ports.ps1'
$Quickboot = Join-Path $Root 'quickboot'

if (-not (Test-Path $FreeScript)) {
  throw "Missing $FreeScript"
}
if (-not (Test-Path $Quickboot)) {
  throw "Missing $Quickboot"
}

& $FreeScript
if ($LASTEXITCODE -ne 0) {
  throw "Failed to free ports (exit $LASTEXITCODE)"
}

Set-Location $Quickboot
if (-not $SkipInstall) {
  Write-Host 'mvn -pl quickboot-web -am -DskipTests install' -ForegroundColor Cyan
  mvn -pl quickboot-web -am -DskipTests install
}
Write-Host 'mvn -pl quickboot-web spring-boot:run' -ForegroundColor Cyan
mvn -pl quickboot-web spring-boot:run
