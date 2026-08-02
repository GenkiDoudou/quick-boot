---
name: init-agents-scaffold
description: >-
  Run or guide the tools/init-agents PowerShell scaffold to generate AGENTS.md,
  DESIGN.md, and .agents docs. Use when the user mentions init-agents, generate
  AGENTS scaffold, sync AI project norms, merge AGENTS.suggested, or refresh
  suggested convention files.
---

# init-agents scaffold

## Hard rules

1. Prefer the script for detection and file generation. Do **not** invent a full `AGENTS.md` from memory when the script can run.
2. Default run **writes files**. Use `-DryRun` first when the user has not confirmed overwrite, show the plan, then re-run without `-DryRun`.
3. Never silently overwrite a hand-maintained formal `AGENTS.md`（有正式文件时只刷新 `AGENTS.suggested.md`）.
4. Each write **overwrites** targets: `AGENTS.suggested.md`（或新建 AGENTS）、`DESIGN.md`、`AGENTS.local.md`、`.agents/generation-spec.md`、`.agents/logs/corrections.md`.
5. If `AGENTS.suggested.md` exists, merge **fact layer only** into formal `AGENTS.md` after user confirm; keep hand-written policy / Never / workflow sections unless the user explicitly asks to replace them.
6. Generated docs are Chinese, UTF-8 without BOM.

## Commands

```powershell
.\tools\init-agents\init-agents.ps1
.\tools\init-agents\init-agents.ps1 -DryRun
.\tools\init-agents\init-agents.ps1 -ProjectRoot <path>
.\tools\init-agents\init-agents.ps1 -ForceSuggested
```

Exit codes: `0` success; non-zero if root invalid or no stack recognized.

## Workflows

### Initialize / refresh

1. Prefer `-DryRun` first if overwrite risk is unclear; otherwise run without flags to write.
2. Explain `create` / `overwrite` / `*-suggested` actions.
3. For repos that already have a thick `AGENTS.md`, treat suggested files as a patch source — do not replace the formal file wholesale.

### Merge suggested

1. Diff `AGENTS.suggested.md` vs `AGENTS.md` (and DESIGN equivalents).
2. Propose a minimal merge (tech stack / commands / structure).
3. Edit only after user approval.

### Analyze project norms

When the user says "analyze project norms" (or equivalent):

1. Scan real source for implicit conventions.
2. Propose updates under `.agents/` or a patch list.
3. Do **not** silently overwrite `AGENTS.md`.

### Corrections

1. After an AI mistake is confirmed, append one row to `.agents/logs/corrections.md`.
2. Promoting a row into Never rules requires an explicit user confirm.

## Related

- `tools/init-agents/README.md`
- Design: `docs/superpowers/specs/2026-08-02-init-agents-scaffold-design.md`
