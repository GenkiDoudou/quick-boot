#Requires -Version 5.1
<#
.SYNOPSIS
  Resolve output paths and write (or dry-run) agents documents.
#>

function Get-Utf8NoBomEncoding {
    return New-Object System.Text.UTF8Encoding $false
}

function Resolve-DocPlan {
    param(
        [string]$ProjectRoot,
        [string]$FormalRelative,
        [string]$SuggestedRelative,
        [ValidateSet('SuggestedIfExists','AlwaysOverwrite')]
        [string]$Mode,
        [bool]$ForceSuggested
    )

    $formalFull = Join-Path $ProjectRoot ($FormalRelative -replace '/', [IO.Path]::DirectorySeparatorChar)
    $suggestedFull = $null
    if ($SuggestedRelative) {
        $suggestedFull = Join-Path $ProjectRoot ($SuggestedRelative -replace '/', [IO.Path]::DirectorySeparatorChar)
    }

    $formalExists = Test-Path -LiteralPath $formalFull

    # AlwaysOverwrite: every -Write refreshes this target path (create or overwrite).
    if ($Mode -eq 'AlwaysOverwrite') {
        $action = if ($formalExists) { 'overwrite' } else { 'create' }
        return [pscustomobject]@{
            Key = $FormalRelative
            Action = $action
            TargetPath = $formalFull
            RelativeTarget = $FormalRelative
        }
    }

    # SuggestedIfExists: protect formal AGENTS/DESIGN/generation-spec; refresh suggested or formal.
    if ($ForceSuggested) {
        $sugExists = Test-Path -LiteralPath $suggestedFull
        return [pscustomobject]@{
            Key = $FormalRelative
            Action = $(if ($sugExists) { 'overwrite-suggested' } else { 'create-suggested' })
            TargetPath = $suggestedFull
            RelativeTarget = $SuggestedRelative
        }
    }
    if ($formalExists) {
        $sugExists = Test-Path -LiteralPath $suggestedFull
        return [pscustomobject]@{
            Key = $FormalRelative
            Action = $(if ($sugExists) { 'overwrite-suggested' } else { 'create-suggested' })
            TargetPath = $suggestedFull
            RelativeTarget = $SuggestedRelative
        }
    }
    return [pscustomobject]@{
        Key = $FormalRelative
        Action = 'create'
        TargetPath = $formalFull
        RelativeTarget = $FormalRelative
    }
}

function Build-WritePlan {
    param(
        [string]$ProjectRoot,
        [bool]$ForceSuggested,
        $Documents
    )

    $items = New-Object System.Collections.Generic.List[object]

    $agentsPlan = Resolve-DocPlan -ProjectRoot $ProjectRoot -FormalRelative 'AGENTS.md' -SuggestedRelative 'AGENTS.suggested.md' -Mode SuggestedIfExists -ForceSuggested:$ForceSuggested
    $agentsPlan | Add-Member NoteProperty Content $Documents.Agents -Force
    [void]$items.Add($agentsPlan)

    $codeFormaterPlan = Resolve-DocPlan -ProjectRoot $ProjectRoot -FormalRelative 'code_formater.md' -SuggestedRelative 'code_formater.suggested.md' -Mode SuggestedIfExists -ForceSuggested:$ForceSuggested
    $codeFormaterPlan | Add-Member NoteProperty Content $Documents.CodeFormater -Force
    [void]$items.Add($codeFormaterPlan)

    # DESIGN / generation-spec / local / corrections: refresh target every -Write
    $designPlan = Resolve-DocPlan -ProjectRoot $ProjectRoot -FormalRelative 'DESIGN.md' -SuggestedRelative 'DESIGN.suggested.md' -Mode AlwaysOverwrite -ForceSuggested:$ForceSuggested
    if ($ForceSuggested) {
        $designPlan = Resolve-DocPlan -ProjectRoot $ProjectRoot -FormalRelative 'DESIGN.md' -SuggestedRelative 'DESIGN.suggested.md' -Mode SuggestedIfExists -ForceSuggested:$true
    }
    $designPlan | Add-Member NoteProperty Content $Documents.Design -Force
    [void]$items.Add($designPlan)

    $localPlan = Resolve-DocPlan -ProjectRoot $ProjectRoot -FormalRelative 'AGENTS.local.md' -SuggestedRelative '' -Mode AlwaysOverwrite -ForceSuggested:$false
    $localPlan | Add-Member NoteProperty Content $Documents.Local -Force
    [void]$items.Add($localPlan)

    $genPlan = Resolve-DocPlan -ProjectRoot $ProjectRoot -FormalRelative '.agents/generation-spec.md' -SuggestedRelative '.agents/generation-spec.suggested.md' -Mode AlwaysOverwrite -ForceSuggested:$ForceSuggested
    if ($ForceSuggested) {
        $genPlan = Resolve-DocPlan -ProjectRoot $ProjectRoot -FormalRelative '.agents/generation-spec.md' -SuggestedRelative '.agents/generation-spec.suggested.md' -Mode SuggestedIfExists -ForceSuggested:$true
    }
    $genPlan | Add-Member NoteProperty Content $Documents.GenerationSpec -Force
    [void]$items.Add($genPlan)

    $corrPlan = Resolve-DocPlan -ProjectRoot $ProjectRoot -FormalRelative '.agents/logs/corrections.md' -SuggestedRelative '' -Mode AlwaysOverwrite -ForceSuggested:$false
    $corrPlan | Add-Member NoteProperty Content $Documents.Corrections -Force
    [void]$items.Add($corrPlan)

    $gitignoreAction = 'none'
    $gitignorePath = Join-Path $ProjectRoot '.gitignore'
    # Ensure ignore rule when we will write local (create or overwrite)
    if ($localPlan.Action -eq 'create' -or $localPlan.Action -eq 'overwrite') {
        if (Test-Path -LiteralPath $gitignorePath) {
            $gi = Get-Content -LiteralPath $gitignorePath -Raw -Encoding UTF8
            if ($gi -notmatch '(?m)^\s*AGENTS\.local\.md\s*$') {
                $gitignoreAction = 'append'
            } else {
                $gitignoreAction = 'already-ignored'
            }
        } else {
            $gitignoreAction = 'hint-missing-gitignore'
        }
    }

    return [pscustomobject]@{
        Items = $items.ToArray()
        GitignorePath = $gitignorePath
        GitignoreAction = $gitignoreAction
    }
}

function Write-Utf8NoBomFile {
    param([string]$Path, [string]$Content)
    $dir = Split-Path -Parent $Path
    if ($dir -and -not (Test-Path -LiteralPath $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
    $enc = Get-Utf8NoBomEncoding
    [System.IO.File]::WriteAllText($Path, $Content, $enc)
}

function Invoke-WritePlan {
    param(
        $Plan,
        [bool]$Write
    )

    $results = New-Object System.Collections.Generic.List[object]
    foreach ($item in @($Plan.Items)) {
        if (-not $Write) {
            [void]$results.Add([pscustomobject]@{ RelativeTarget = $item.RelativeTarget; Action = $item.Action; Applied = $false })
            continue
        }
        # create / overwrite / create-suggested / overwrite-suggested: always refresh target
        Write-Utf8NoBomFile -Path $item.TargetPath -Content $item.Content
        [void]$results.Add([pscustomobject]@{ RelativeTarget = $item.RelativeTarget; Action = $item.Action; Applied = $true })
    }

    $giApplied = $false
    if ($Write -and $Plan.GitignoreAction -eq 'append') {
        $nl = [Environment]::NewLine
        $append = $nl + '# AI personal preferences (do not commit)' + $nl + 'AGENTS.local.md' + $nl
        [System.IO.File]::AppendAllText($Plan.GitignorePath, $append, (Get-Utf8NoBomEncoding))
        $giApplied = $true
    }

    return [pscustomobject]@{
        Results = $results.ToArray()
        GitignoreAction = $Plan.GitignoreAction
        GitignoreApplied = $giApplied
    }
}