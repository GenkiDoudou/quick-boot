# Skills 仓 → Cursor Skills 增量同步 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在独立 `skills.git` 仓落地 `skills/` + `tools/` 骨架与 `Sync-CursorSkills.ps1`，按名字把共享 Skill 以 Windows Junction 增量链接到目标项目 `.cursor/skills/`，不影响本地独有 Skill，且通过 `SKILLS_ROOT` 调用共享 `tools/`。

**Architecture:** 本机独立 clone（不进 quickboot git）。同步脚本解析 `skills/` 一级子目录为共享名单，对目标项目 `.cursor/skills/<name>` 创建/修正 Junction；真实目录默认跳过，`-Force` 才备份后改链。共享工具留在 clone 的 `tools/`，Skill 文案约定 `$env:SKILLS_ROOT`。

**Tech Stack:** PowerShell 5.1+、Windows Directory Junction（`New-Item -ItemType Junction` / `mklink /J`）、独立 git 仓 `https://git.su60.com/luyanan/skills.git`

**Spec:** `docs/superpowers/specs/2026-08-02-skills-repo-sync-design.md`（quickboot 仓库内）

**工作目录约定：**

| 别名 | 含义 | 示例 |
|------|------|------|
| `SKILLS_REPO` | skills.git 的本机 clone 根（即 `SKILLS_ROOT`） | `E:\workspace\luyanan\skills` |
| `QUICKBOOT` | 验证用目标项目 | `E:\workspace\luyanan\quickboot` |

实现与 commit **默认发生在 `SKILLS_REPO`**；仅 Task 0 / 可选 Task 7 触及 `QUICKBOOT`。

---

## File Structure

```text
SKILLS_REPO/
├── README.md                          # SKILLS_ROOT、同步用法、tools 约定
├── .gitignore                         # 常见忽略（若需要）
├── skills/
│   └── _example-echo/                 # 验证用示例 Skill（可保留或后续删除）
│       └── SKILL.md
├── tools/
│   └── example-echo/
│       └── echo-info.ps1              # 示例工具：打印 SKILLS_ROOT 与参数
├── sync/
│   ├── Sync-CursorSkills.ps1          # CLI 入口
│   ├── lib/
│   │   ├── Resolve-SkillsRoot.ps1
│   │   ├── Get-SharedSkillNames.ps1
│   │   ├── Test-DirectoryJunction.ps1
│   │   └── Invoke-SkillSync.ps1       # 核心同步逻辑（可被测试直接 dot-source）
│   └── tests/
│       └── Sync-CursorSkills.Tests.ps1  # 临时目录自测（无 Pester 依赖）
```

责任边界：

- `Resolve-SkillsRoot.ps1`：解析 `SKILLS_ROOT`（环境变量 → 参数 → 可选配置文件）。
- `Get-SharedSkillNames.ps1`：列举 `skills/` 一级子目录（排除以 `_` 开头的可选；**首版不排除**，`_example-echo` 也参与链接，便于验证）。
- `Test-DirectoryJunction.ps1`：判断路径是否为 Junction，并读取目标。
- `Invoke-SkillSync.ps1`：对单个/批量 Skill 执行 §5 冲突策略，返回结果对象。
- `Sync-CursorSkills.ps1`：参数解析、调用核心、打印摘要；默认 dry-run，`-Apply` 落盘。

---

### Task 0: Clone 空仓并确认工作树

**Files:**

- Create（git）：`SKILLS_REPO` 工作树

- [ ] **Step 1: Clone skills 仓**

```powershell
# 若目录已存在且为 git 仓则跳过 clone
$SKILLS_REPO = 'E:\workspace\luyanan\skills'   # 可按本机调整
if (-not (Test-Path -LiteralPath $SKILLS_REPO)) {
  git clone https://git.su60.com/luyanan/skills.git $SKILLS_REPO
}
Set-Location $SKILLS_REPO
git status
git remote -v
```

Expected: 空仓或已有极少文件；`origin` 指向 `git.su60.com/luyanan/skills.git`。

- [ ] **Step 2: 将规格状态改为已确认（在 QUICKBOOT，可选）**

Modify: `QUICKBOOT/docs/superpowers/specs/2026-08-02-skills-repo-sync-design.md` 第 4 行：

```markdown
**状态：** 已确认（实现按 plans/2026-08-02-skills-repo-sync.md）
```

- [ ] **Step 3: Commit 规格状态（仅当用户要求提交 QUICKBOOT 时再做；默认跳过）**

本 Task 在 `SKILLS_REPO` 无文件可提交则跳过 commit。

---

### Task 1: 解析 `SKILLS_ROOT` 与 Junction 探测（可测函数）

**Files:**

- Create: `SKILLS_REPO/sync/lib/Resolve-SkillsRoot.ps1`
- Create: `SKILLS_REPO/sync/lib/Test-DirectoryJunction.ps1`
- Create: `SKILLS_REPO/sync/tests/Sync-CursorSkills.Tests.ps1`（先写这两项的断言）

- [ ] **Step 1: 写入失败测试（`Resolve-SkillsRoot` / Junction）**

Create `sync/tests/Sync-CursorSkills.Tests.ps1`:

```powershell
#Requires -Version 5.1
$ErrorActionPreference = 'Stop'
$lib = Join-Path (Split-Path $PSScriptRoot -Parent) 'lib'

function Assert-True($cond, $msg) {
  if (-not $cond) { throw "ASSERT FAIL: $msg" }
}

# --- Resolve-SkillsRoot ---
. (Join-Path $lib 'Resolve-SkillsRoot.ps1')

$prev = $env:SKILLS_ROOT
try {
  $env:SKILLS_ROOT = 'C:\fake-skills-root-from-env'
  $r = Resolve-SkillsRoot -SkillsRoot ''
  Assert-True ($r -eq 'C:\fake-skills-root-from-env') 'env SKILLS_ROOT should win when -SkillsRoot empty'

  $r2 = Resolve-SkillsRoot -SkillsRoot 'C:\fake-skills-root-from-param'
  Assert-True ($r2 -eq 'C:\fake-skills-root-from-param') '-SkillsRoot param should override env'
}
finally {
  if ($null -eq $prev) { Remove-Item Env:SKILLS_ROOT -ErrorAction SilentlyContinue }
  else { $env:SKILLS_ROOT = $prev }
}

# --- Test-DirectoryJunction ---
. (Join-Path $lib 'Test-DirectoryJunction.ps1')

$tmp = Join-Path $env:TEMP ("skills-junc-test-" + [guid]::NewGuid().ToString('N'))
$target = Join-Path $tmp 'target'
$link = Join-Path $tmp 'link'
$real = Join-Path $tmp 'real'
New-Item -ItemType Directory -Path $target, $real -Force | Out-Null
New-Item -ItemType Junction -Path $link -Target $target | Out-Null

$info = Get-DirectoryJunctionInfo -Path $link
Assert-True ($info.IsJunction -eq $true) 'link should be junction'
Assert-True ($info.Target.TrimEnd('\') -eq (Resolve-Path $target).Path.TrimEnd('\')) 'junction target mismatch'

$info2 = Get-DirectoryJunctionInfo -Path $real
Assert-True ($info2.IsJunction -eq $false) 'real dir should not be junction'

Remove-Item -LiteralPath $tmp -Recurse -Force
Write-Host 'PASS: Resolve-SkillsRoot + Junction tests'
```

- [ ] **Step 2: 运行测试，确认失败**

```powershell
Set-Location $SKILLS_REPO
powershell -NoProfile -File .\sync\tests\Sync-CursorSkills.Tests.ps1
```

Expected: FAIL（找不到 `Resolve-SkillsRoot.ps1` 或函数未定义）。

- [ ] **Step 3: 实现 `Resolve-SkillsRoot.ps1`**

```powershell
#Requires -Version 5.1
function Resolve-SkillsRoot {
  <#
  .SYNOPSIS
    按 参数 > 环境变量 > 可选配置文件 解析 SKILLS_ROOT。
  #>
  param(
    [string]$SkillsRoot = ''
  )
  if (-not [string]::IsNullOrWhiteSpace($SkillsRoot)) {
    return $SkillsRoot.TrimEnd('\', '/')
  }
  if (-not [string]::IsNullOrWhiteSpace($env:SKILLS_ROOT)) {
    return $env:SKILLS_ROOT.TrimEnd('\', '/')
  }
  $cfg = Join-Path $env:USERPROFILE '.config\luyanan-skills\root.txt'
  if (Test-Path -LiteralPath $cfg -PathType Leaf) {
    $line = (Get-Content -LiteralPath $cfg -TotalCount 1 -ErrorAction Stop).Trim()
    if (-not [string]::IsNullOrWhiteSpace($line)) {
      return $line.TrimEnd('\', '/')
    }
  }
  throw 'SKILLS_ROOT not set. Pass -SkillsRoot, set env SKILLS_ROOT, or create %USERPROFILE%\.config\luyanan-skills\root.txt'
}
```

- [ ] **Step 4: 实现 `Test-DirectoryJunction.ps1`**

```powershell
#Requires -Version 5.1
function Get-DirectoryJunctionInfo {
  <#
  .SYNOPSIS
    判断路径是否为 Directory Junction，并返回目标路径。
  .OUTPUTS
    PSCustomObject: IsJunction (bool), Target (string|null), Path (string)
  #>
  param(
    [Parameter(Mandatory)]
    [string]$Path
  )
  if (-not (Test-Path -LiteralPath $Path)) {
    return [pscustomobject]@{ Path = $Path; IsJunction = $false; Target = $null; Exists = $false }
  }
  $item = Get-Item -LiteralPath $Path -Force
  # Junction: LinkType 为 Junction；部分环境用 Attributes ReparsePoint + 无 SymLink
  $isJunc = $false
  $target = $null
  if ($item.LinkType -eq 'Junction') {
    $isJunc = $true
    $target = $item.Target
    if ($target -is [array]) { $target = $target[0] }
  }
  elseif ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
    # 回退：用 cmd dir 解析（少见）
    $isJunc = $true
    $target = $null
  }
  return [pscustomobject]@{
    Path       = $Path
    Exists     = $true
    IsJunction = [bool]$isJunc
    Target     = $target
  }
}
```

- [ ] **Step 5: 再跑测试**

```powershell
powershell -NoProfile -File .\sync\tests\Sync-CursorSkills.Tests.ps1
```

Expected: `PASS: Resolve-SkillsRoot + Junction tests`

- [ ] **Step 6: Commit（在 SKILLS_REPO）**

```powershell
Set-Location $SKILLS_REPO
git add sync/lib/Resolve-SkillsRoot.ps1 sync/lib/Test-DirectoryJunction.ps1 sync/tests/Sync-CursorSkills.Tests.ps1
git commit -m "feat(sync): add SkillsRoot resolver and junction probe"
```

---

### Task 2: 共享名单与核心同步逻辑（TDD）

**Files:**

- Create: `SKILLS_REPO/sync/lib/Get-SharedSkillNames.ps1`
- Create: `SKILLS_REPO/sync/lib/Invoke-SkillSync.ps1`
- Modify: `SKILLS_REPO/sync/tests/Sync-CursorSkills.Tests.ps1`（追加用例）

- [ ] **Step 1: 追加失败测试——名单列举 + 同步策略**

在 `Sync-CursorSkills.Tests.ps1` 末尾、`Write-Host PASS` 之前追加：

```powershell
. (Join-Path $lib 'Get-SharedSkillNames.ps1')
. (Join-Path $lib 'Invoke-SkillSync.ps1')

$root = Join-Path $env:TEMP ("skills-sync-test-" + [guid]::NewGuid().ToString('N'))
$skillsDir = Join-Path $root 'skills'
$toolsDir = Join-Path $root 'tools'
$projSkills = Join-Path $root 'project\.cursor\skills'
New-Item -ItemType Directory -Path (Join-Path $skillsDir 'alpha'), (Join-Path $skillsDir 'beta'), $toolsDir, $projSkills -Force | Out-Null
Set-Content -LiteralPath (Join-Path $skillsDir 'alpha\SKILL.md') -Value "# alpha`n" -Encoding utf8
Set-Content -LiteralPath (Join-Path $skillsDir 'beta\SKILL.md') -Value "# beta`n" -Encoding utf8

# 本地独有
New-Item -ItemType Directory -Path (Join-Path $projSkills 'local-only') -Force | Out-Null
Set-Content -LiteralPath (Join-Path $projSkills 'local-only\SKILL.md') -Value "# local`n" -Encoding utf8

$names = @(Get-SharedSkillNames -SkillsRoot $root)
Assert-True ($names -contains 'alpha' -and $names -contains 'beta' -and $names.Count -eq 2) 'shared names should be alpha,beta'

# dry-run：不应创建 junction
$plan = Invoke-SkillSync -SkillsRoot $root -ProjectRoot (Join-Path $root 'project') -Apply:$false -Force:$false
Assert-True (-not (Test-Path (Join-Path $projSkills 'alpha'))) 'dry-run must not create alpha'

# apply：创建 junction，保留 local-only
$applied = Invoke-SkillSync -SkillsRoot $root -ProjectRoot (Join-Path $root 'project') -Apply:$true -Force:$false
Assert-True ((Get-DirectoryJunctionInfo -Path (Join-Path $projSkills 'alpha')).IsJunction) 'alpha should be junction'
Assert-True (Test-Path (Join-Path $projSkills 'local-only\SKILL.md')) 'local-only must remain'

# 真实目录同名：默认跳过
New-Item -ItemType Directory -Path (Join-Path $projSkills 'gamma-src') -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $skillsDir 'gamma') -Force | Out-Null
Set-Content (Join-Path $skillsDir 'gamma\SKILL.md') "# gamma`n" -Encoding utf8
# 先造一个真实目录占位 gamma
New-Item -ItemType Directory -Path (Join-Path $projSkills 'gamma') -Force | Out-Null
Set-Content (Join-Path $projSkills 'gamma\SKILL.md') "# local gamma`n" -Encoding utf8
$skip = Invoke-SkillSync -SkillsRoot $root -ProjectRoot (Join-Path $root 'project') -Apply:$true -Force:$false
$gammaInfo = Get-DirectoryJunctionInfo -Path (Join-Path $projSkills 'gamma')
Assert-True ($gammaInfo.IsJunction -eq $false) 'real gamma should be skipped without -Force'
$content = Get-Content (Join-Path $projSkills 'gamma\SKILL.md') -Raw
Assert-True ($content -match 'local gamma') 'local gamma content must stay'

# -Force：备份后改链
$forced = Invoke-SkillSync -SkillsRoot $root -ProjectRoot (Join-Path $root 'project') -Apply:$true -Force:$true
$gammaInfo2 = Get-DirectoryJunctionInfo -Path (Join-Path $projSkills 'gamma')
Assert-True ($gammaInfo2.IsJunction -eq $true) 'gamma should become junction with -Force'
$bak = Get-ChildItem -LiteralPath $projSkills -Directory | Where-Object { $_.Name -like 'gamma.bak-*' }
Assert-True ($null -ne $bak) 'backup dir gamma.bak-* should exist'

Remove-Item -LiteralPath $root -Recurse -Force
Write-Host 'PASS: all Sync-CursorSkills tests'
```

- [ ] **Step 2: 跑测试，确认新断言失败**

```powershell
powershell -NoProfile -File .\sync\tests\Sync-CursorSkills.Tests.ps1
```

Expected: FAIL（缺少 `Get-SharedSkillNames` / `Invoke-SkillSync`）。

- [ ] **Step 3: 实现 `Get-SharedSkillNames.ps1`**

```powershell
#Requires -Version 5.1
function Get-SharedSkillNames {
  <#
  .SYNOPSIS
    返回 SkillsRoot/skills 下一级子目录名（共享名单）。
  #>
  param(
    [Parameter(Mandatory)]
    [string]$SkillsRoot
  )
  $skills = Join-Path $SkillsRoot 'skills'
  if (-not (Test-Path -LiteralPath $skills -PathType Container)) {
    throw "skills directory missing: $skills"
  }
  Get-ChildItem -LiteralPath $skills -Directory |
    Select-Object -ExpandProperty Name |
    Sort-Object
}
```

- [ ] **Step 4: 实现 `Invoke-SkillSync.ps1`**

```powershell
#Requires -Version 5.1
# 依赖：Get-SharedSkillNames、Get-DirectoryJunctionInfo（调用方先 dot-source）

function Invoke-SkillSync {
  <#
  .SYNOPSIS
    将共享 skills 以 Junction 增量同步到项目 .cursor/skills。
  .OUTPUTS
    结果对象数组：Name, Action (link|skip|repoint|backup-link|noop|error), Detail, Applied
  #>
  param(
    [Parameter(Mandatory)][string]$SkillsRoot,
    [Parameter(Mandatory)][string]$ProjectRoot,
    [switch]$Apply,
    [switch]$Force
  )

  $skillsRoot = $SkillsRoot.TrimEnd('\', '/')
  $projectRoot = (Resolve-Path -LiteralPath $ProjectRoot).Path
  $tools = Join-Path $skillsRoot 'tools'
  if (-not (Test-Path -LiteralPath $tools -PathType Container)) {
    throw "tools directory missing: $tools"
  }

  $destRoot = Join-Path $projectRoot '.cursor\skills'
  if ($Apply -and -not (Test-Path -LiteralPath $destRoot)) {
    New-Item -ItemType Directory -Path $destRoot -Force | Out-Null
  }

  $results = @()
  foreach ($name in @(Get-SharedSkillNames -SkillsRoot $skillsRoot)) {
    $src = Join-Path $skillsRoot "skills\$name"
    $dest = Join-Path $destRoot $name
    $srcFull = (Resolve-Path -LiteralPath $src).Path

    $entry = [pscustomobject]@{ Name = $name; Action = 'noop'; Detail = ''; Applied = $false }

    if (-not (Test-Path -LiteralPath $dest)) {
      $entry.Action = 'link'
      $entry.Detail = "create junction -> $srcFull"
      if ($Apply) {
        if (-not (Test-Path -LiteralPath $destRoot)) {
          New-Item -ItemType Directory -Path $destRoot -Force | Out-Null
        }
        New-Item -ItemType Junction -Path $dest -Target $srcFull | Out-Null
        $entry.Applied = $true
      }
      $results += $entry
      continue
    }

    $info = Get-DirectoryJunctionInfo -Path $dest
    if ($info.IsJunction) {
      $current = $null
      if ($info.Target) {
        $current = ([string]$info.Target).TrimEnd('\')
      }
      $want = $srcFull.TrimEnd('\')
      if ($current -and ($current -eq $want)) {
        $entry.Action = 'noop'
        $entry.Detail = 'already linked to correct target'
      }
      else {
        $entry.Action = 'repoint'
        $entry.Detail = "repoint junction -> $srcFull"
        if ($Apply) {
          # 删除 junction 不删目标内容
          cmd /c "rmdir `"$dest`""
          New-Item -ItemType Junction -Path $dest -Target $srcFull | Out-Null
          $entry.Applied = $true
        }
      }
      $results += $entry
      continue
    }

    # 真实目录或其它
    if ($Force) {
      $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
      $bak = Join-Path $destRoot ($name + '.bak-' + $stamp)
      $entry.Action = 'backup-link'
      $entry.Detail = "rename to $bak then junction -> $srcFull"
      if ($Apply) {
        Rename-Item -LiteralPath $dest -NewName (Split-Path $bak -Leaf)
        New-Item -ItemType Junction -Path $dest -Target $srcFull | Out-Null
        $entry.Applied = $true
      }
    }
    else {
      $entry.Action = 'skip'
      $entry.Detail = 'real directory exists; use -Force to backup and replace'
    }
    $results += $entry
  }

  return $results
}
```

- [ ] **Step 5: 跑测试通过**

```powershell
powershell -NoProfile -File .\sync\tests\Sync-CursorSkills.Tests.ps1
```

Expected: `PASS: all Sync-CursorSkills tests`

- [ ] **Step 6: Commit**

```powershell
git add sync/lib/Get-SharedSkillNames.ps1 sync/lib/Invoke-SkillSync.ps1 sync/tests/Sync-CursorSkills.Tests.ps1
git commit -m "feat(sync): incremental junction sync with skip/force backup"
```

---

### Task 3: CLI 入口 `Sync-CursorSkills.ps1`

**Files:**

- Create: `SKILLS_REPO/sync/Sync-CursorSkills.ps1`

- [ ] **Step 1: 实现入口脚本**

```powershell
#Requires -Version 5.1
<#
.SYNOPSIS
  将本仓 skills/ 以 Junction 增量同步到目标项目 .cursor/skills。

.EXAMPLE
  .\sync\Sync-CursorSkills.ps1 -ProjectRoot D:\work\quickboot
  .\sync\Sync-CursorSkills.ps1 -ProjectRoot D:\work\quickboot -Apply
  .\sync\Sync-CursorSkills.ps1 -ProjectRoot D:\work\quickboot -Apply -Force
#>
[CmdletBinding()]
param(
  [Parameter(Mandatory)]
  [string]$ProjectRoot,
  [string]$SkillsRoot = '',
  [switch]$Apply,
  [switch]$Force
)

$ErrorActionPreference = 'Stop'
$lib = Join-Path $PSScriptRoot 'lib'
. (Join-Path $lib 'Resolve-SkillsRoot.ps1')
. (Join-Path $lib 'Test-DirectoryJunction.ps1')
. (Join-Path $lib 'Get-SharedSkillNames.ps1')
. (Join-Path $lib 'Invoke-SkillSync.ps1')

$root = Resolve-SkillsRoot -SkillsRoot $SkillsRoot
# 若未传 SkillsRoot 且脚本位于仓内，可用仓库根作默认（脚本在 sync/ 下）
if ([string]::IsNullOrWhiteSpace($SkillsRoot) -and [string]::IsNullOrWhiteSpace($env:SKILLS_ROOT)) {
  $repoGuess = Split-Path $PSScriptRoot -Parent
  if ((Test-Path (Join-Path $repoGuess 'skills')) -and (Test-Path (Join-Path $repoGuess 'tools'))) {
    $root = $repoGuess
  }
}

if (-not (Test-Path -LiteralPath $ProjectRoot -PathType Container)) {
  Write-Error "ProjectRoot not found: $ProjectRoot"
  exit 1
}

Write-Host "SKILLS_ROOT : $root"
Write-Host "ProjectRoot : $((Resolve-Path $ProjectRoot).Path)"
Write-Host "Mode        : $(if ($Apply) { 'APPLY' } else { 'DRY-RUN' })"
Write-Host ""

$results = Invoke-SkillSync -SkillsRoot $root -ProjectRoot $ProjectRoot -Apply:$Apply -Force:$Force

foreach ($r in $results) {
  $flag = if ($r.Applied) { 'DONE' } else { 'PLAN' }
  Write-Host ("  [{0}] {1,-12} {2}  {3}" -f $flag, $r.Action, $r.Name, $r.Detail)
}

Write-Host ""
if (-not $Apply) {
  Write-Host 'Dry-run only. Re-run with -Apply to create/update junctions.'
} else {
  Write-Host 'Sync complete.'
}
exit 0
```

注意：上面「未设 env 时用仓库根」与 `Resolve-SkillsRoot` 抛错可能冲突。实现时调整为：

1. 若 `-SkillsRoot` 有值 → 用它；
2. 否则若 `$env:SKILLS_ROOT` 有值 → 用它；
3. 否则若 `%USERPROFILE%\.config\luyanan-skills\root.txt` 有值 → 用它；
4. 否则若 `Split-Path $PSScriptRoot -Parent` 同时含 `skills/` 与 `tools/` → 用仓库根；
5. 否则抛错。

把该顺序写进 `Resolve-SkillsRoot` 的可选参数 `-FallbackRepoRoot`，由 CLI 传入 `Split-Path $PSScriptRoot -Parent`，避免测试行为变化。

- [ ] **Step 2: Dry-run 冒烟**

```powershell
# 先确保 skills/ tools/ 目录存在（Task 4 会建示例；此处可先建空目录）
New-Item -ItemType Directory -Path .\skills,.\tools -Force | Out-Null
powershell -NoProfile -File .\sync\Sync-CursorSkills.ps1 -ProjectRoot $QUICKBOOT
```

Expected: 打印 DRY-RUN；若 `skills/` 为空则无链接行；不改 `QUICKBOOT/.cursor/skills`。

- [ ] **Step 3: Commit**

```powershell
git add sync/Sync-CursorSkills.ps1 sync/lib/Resolve-SkillsRoot.ps1
git commit -m "feat(sync): add Sync-CursorSkills CLI with dry-run default"
```

---

### Task 4: 示例 Skill + 示例 tool + README

**Files:**

- Create: `SKILLS_REPO/skills/_example-echo/SKILL.md`
- Create: `SKILLS_REPO/tools/example-echo/echo-info.ps1`
- Create: `SKILLS_REPO/README.md`
- Create: `SKILLS_REPO/.gitignore`（可选）

- [ ] **Step 1: 示例 tool**

`tools/example-echo/echo-info.ps1`:

```powershell
#Requires -Version 5.1
param([string]$Message = 'ok')
Write-Host "SKILLS_ROOT=$env:SKILLS_ROOT"
Write-Host "Message=$Message"
exit 0
```

- [ ] **Step 2: 示例 Skill**

`skills/_example-echo/SKILL.md`:

```markdown
---
name: example-echo
description: Demo skill that runs tools/example-echo via SKILLS_ROOT. Use when verifying skills-repo sync.
---

# example-echo

## 前置

确认环境变量 `SKILLS_ROOT` 指向本 skills 仓 clone 根目录。

## 执行

```powershell
& "$env:SKILLS_ROOT\tools\example-echo\echo-info.ps1" -Message 'hello-from-skill'
```

禁止假定工具位于业务项目根目录的 `tools/` 下。
```

- [ ] **Step 3: README.md**

```markdown
# skills

共享 Cursor Agent Skills（`skills/`）与可执行工具（`tools/`）。

## 目录

- `skills/<name>/SKILL.md` — Cursor 项目 Skill 源
- `tools/` — 供 Skill 调用的脚本；**不要**拷进业务仓库的 `tools/`
- `sync/Sync-CursorSkills.ps1` — 增量 Junction 同步到目标项目 `.cursor/skills`

## 本机设置

```powershell
$env:SKILLS_ROOT = 'E:\workspace\luyanan\skills'   # 改成你的 clone 路径
# 或写入 %USERPROFILE%\.config\luyanan-skills\root.txt 一行路径
```

## 同步到项目

```powershell
cd $env:SKILLS_ROOT
.\sync\Sync-CursorSkills.ps1 -ProjectRoot E:\workspace\luyanan\quickboot          # dry-run
.\sync\Sync-CursorSkills.ps1 -ProjectRoot E:\workspace\luyanan\quickboot -Apply # 落盘
# 本地已有同名真实目录需要改链时：
.\sync\Sync-CursorSkills.ps1 -ProjectRoot ... -Apply -Force
```

规则：只处理本仓 `skills/` 下一级目录；目标项目中**不在名单内**的 Skill 一律不动。

## 修改共享 Skill

在业务项目里编辑 `.cursor/skills/<name>`（Junction 指向本仓）→ 回到本仓 `git commit` / `git push`。

## 测试

```powershell
powershell -NoProfile -File .\sync\tests\Sync-CursorSkills.Tests.ps1
```
```

- [ ] **Step 4: Commit**

```powershell
git add skills tools README.md .gitignore
git commit -m "docs: add example skill/tool and sync README"
```

---

### Task 5: 在 quickboot 上端到端验证

**Files:** 无新增（仅本机 junction）；不提交 quickboot 内 junction（`.cursor/skills` 若被 git 跟踪需注意——**不要**把 junction 当普通目录 commit；若当前 skills 已在 git 中，验证用**新名字** `_example-echo` 或临时目录）。

- [ ] **Step 1: 记录验证前本地 Skill 列表**

```powershell
Get-ChildItem $QUICKBOOT\.cursor\skills -Directory | Select-Object Name | Sort-Object Name
$before = @(Get-ChildItem $QUICKBOOT\.cursor\skills -Directory | ForEach-Object Name)
```

- [ ] **Step 2: Dry-run 再 Apply**

```powershell
$env:SKILLS_ROOT = $SKILLS_REPO
Set-Location $SKILLS_REPO
.\sync\Sync-CursorSkills.ps1 -ProjectRoot $QUICKBOOT
.\sync\Sync-CursorSkills.ps1 -ProjectRoot $QUICKBOOT -Apply
```

Expected: `_example-echo`（或其它共享名）显示为 DONE/link；既有本地 Skill 名称仍在。

- [ ] **Step 3: 确认本地独有未改**

```powershell
$after = @(Get-ChildItem $QUICKBOOT\.cursor\skills -Directory | ForEach-Object Name)
# 每个 $before 中的名字仍存在
Compare-Object $before $after
# _example-echo 应为 Junction
(Get-Item $QUICKBOOT\.cursor\skills\_example-echo).LinkType
```

Expected: `LinkType = Junction`；`before` 中条目仍在。

- [ ] **Step 4: 编辑落在 clone**

```powershell
Add-Content $QUICKBOOT\.cursor\skills\_example-echo\SKILL.md "`n<!-- sync-verify -->`n"
Set-Location $SKILLS_REPO
git status
git diff -- skills/_example-echo/SKILL.md
```

Expected: skills 仓显示该文件变更。

- [ ] **Step 5: 执行示例 tool**

```powershell
& "$env:SKILLS_ROOT\tools\example-echo\echo-info.ps1" -Message 'e2e'
```

Expected: 打印 `SKILLS_ROOT=...` 与 `Message=e2e`。

- [ ] **Step 6: 还原验证用编辑（可选 commit 或 discard）**

```powershell
Set-Location $SKILLS_REPO
git checkout -- skills/_example-echo/SKILL.md
# 若要保留示例则保留文件，仅去掉 sync-verify 标记
```

- [ ] **Step 7: Push skills 仓（仅当用户明确要求 push 时）**

```powershell
git push -u origin HEAD
```

---

### Task 6（可选）: 在 `AGENTS.local.md` 记录 `SKILLS_ROOT`

**Files:**

- Modify: `QUICKBOOT/AGENTS.local.md`（个人文件，应已被 gitignore）

- [ ] **Step 1: 追加一行**

```markdown
## Skills 仓

本机共享 Skills 根目录：`SKILLS_ROOT=E:\workspace\luyanan\skills`（按实际路径修改）。
同步：`& "$env:SKILLS_ROOT\sync\Sync-CursorSkills.ps1" -ProjectRoot <本仓库根> -Apply`
```

- [ ] **Step 2: 确认未被 git 跟踪**

```powershell
Set-Location $QUICKBOOT
git check-ignore -v AGENTS.local.md
git status -- AGENTS.local.md
```

Expected: 被 ignore，或不出现在待提交列表。

---

### Task 7: 规格自检对照（无代码）

- [ ] **Step 1: 对照 spec 逐条打勾**

| Spec 要求 | Task |
|-----------|------|
| 独立 clone，不进本仓 submodule | Task 0、README |
| Junction 增量，名单外不动 | Task 2、5 |
| 真实目录默认 skip，`-Force` 备份 | Task 2 |
| 默认 dry-run，`-Apply` 落盘 | Task 3 |
| tools 留在 clone，`SKILLS_ROOT` | Task 4、5 |
| 不碰项目 `tools/init-agents` | Task 5 验证未改该路径 |

- [ ] **Step 2: 更新 QUICKBOOT 规格状态为「已实现」**（实现全部通过后）

仅改状态行；若用户要求再 commit。

---

## Self-Review（计划作者）

1. **Spec coverage:** §3–§8 均有对应 Task；迁入现有 quickboot skills 明确在范围外（Task 7 / spec §9）。
2. **Placeholder scan:** 无 TBD；Task 3 对 `Resolve-SkillsRoot` 与仓库根回退的冲突已写明用 `-FallbackRepoRoot` 解决——实现时必须改函数签名并更新 Task 1 测试（增加「仅 Fallback」用例），避免抛错。
3. **Type consistency:** 结果字段统一为 `Name, Action, Detail, Applied`；Junction API 统一 `Get-DirectoryJunctionInfo`。

**实现 Task 3 前必做小修正（并入 Task 1 或 Task 3）：**

```powershell
function Resolve-SkillsRoot {
  param(
    [string]$SkillsRoot = '',
    [string]$FallbackRepoRoot = ''
  )
  # 1) -SkillsRoot 2) env 3) root.txt 4) FallbackRepoRoot 若含 skills+tools 5) throw
}
```

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-08-02-skills-repo-sync.md`.

**两种执行方式：**

1. **Subagent-Driven（推荐）** — 每任务派一个新子代理，任务间复查，迭代快  
2. **Inline Execution** — 本会话按 executing-plans 连续执行，设检查点  

要选哪一种？
