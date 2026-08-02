---
name: example-echo
description: Use when verifying that a shared skill can invoke a tool from the configured SKILLS_ROOT.
---

# Example Echo

Run the shared tool through `SKILLS_ROOT`:

```powershell
& "$env:SKILLS_ROOT\tools\example-echo\echo-info.ps1" -Message "hello from example-echo"
```

Do not assume the tool exists under the current project's `tools/` directory. Always resolve it from `$env:SKILLS_ROOT`.
