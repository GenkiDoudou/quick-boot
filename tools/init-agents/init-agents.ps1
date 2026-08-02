#Requires -Version 5.1
<#
.SYNOPSIS
  Detect project stack and generate AGENTS/DESIGN/.agents docs (writes by default).

.EXAMPLE
  .\tools\init-agents\init-agents.ps1
  .\tools\init-agents\init-agents.ps1 -DryRun
  .\tools\init-agents\init-agents.ps1 -ProjectRoot D:\app -ForceSuggested
#>
[CmdletBinding()]
param(
    [string]$ProjectRoot = (Get-Location).Path,
    [switch]$DryRun,
    # Kept for compatibility; default is already write. Ignored unless -DryRun is also set.
    [switch]$Write,
    [switch]$ForceSuggested
)

$ErrorActionPreference = 'Stop'
$here = $PSScriptRoot
. (Join-Path $here 'lib\Detect-Stack.ps1')
. (Join-Path $here 'lib\New-AgentsDocs.ps1')
. (Join-Path $here 'lib\Write-AgentsDocs.ps1')

if (-not (Test-Path -LiteralPath $ProjectRoot -PathType Container)) {
    Write-Error "ProjectRoot is not an existing directory: $ProjectRoot"
    exit 1
}

$doWrite = -not [bool]$DryRun

$root = (Resolve-Path -LiteralPath $ProjectRoot).Path
$stack = Get-ProjectStack -ProjectRoot $root
if (-not (Test-ProjectStackRecognized -Stack $stack)) {
    Write-Host "ERROR: No recognizable frontend/backend/docs project under: $root"
    exit 2
}

Write-Host "Project: $($stack.ProjectName)"
Write-Host "Detect: $($stack.FrameworkSummary)"
Write-Host ("UI: {0}  color={1}  radius={2}px" -f $stack.PrimaryUiLib, $stack.PrimaryColor, $stack.BorderRadius)
if ($stack.SpecHints.Count -gt 0) {
    Write-Host ("SpecHints: " + ($stack.SpecHints -join ', '))
}
Write-Host ""

$templatesDir = Join-Path $here 'templates'
$docs = New-AgentsDocumentSet -Stack $stack -TemplatesDir $templatesDir
$plan = Build-WritePlan -ProjectRoot $root -ForceSuggested:([bool]$ForceSuggested) -Documents $docs
$result = Invoke-WritePlan -Plan $plan -Write:$doWrite

Write-Host "Actions:"
foreach ($r in @($result.Results)) {
    $flag = if ($r.Applied) { 'WROTE' } else { 'PLAN' }
    Write-Host ("  [{0}] {1,-18} {2}" -f $flag, $r.Action, $r.RelativeTarget)
}

switch ($result.GitignoreAction) {
    'append' {
        if ($result.GitignoreApplied) { Write-Host '  [WROTE] gitignore append   AGENTS.local.md' }
        else { Write-Host '  [PLAN]  gitignore append   AGENTS.local.md' }
    }
    'hint-missing-gitignore' { Write-Host '  [HINT]  no .gitignore; consider ignoring AGENTS.local.md' }
    'already-ignored' { Write-Host '  [OK]    AGENTS.local.md already in .gitignore' }
}

Write-Host ""
if ($DryRun) {
    Write-Host "Dry-run only. No files were written. Re-run without -DryRun to apply."
} else {
    Write-Host "Write complete."
}

exit 0
