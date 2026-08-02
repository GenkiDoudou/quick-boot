#Requires -Version 5.1
<#
.SYNOPSIS
  Render AGENTS/DESIGN/local/generation-spec/corrections from stack + templates.
  This file MUST be saved as UTF-8 with BOM so Windows PowerShell 5.1 parses Chinese correctly.
#>

function Get-TemplateText {
    param([string]$TemplatesDir, [string]$Name)
    $p = Join-Path $TemplatesDir $Name
    if (-not (Test-Path -LiteralPath $p)) { throw "Missing template: $p" }
    return [System.IO.File]::ReadAllText($p, [System.Text.Encoding]::UTF8)
}

function Expand-Template {
    param([string]$Template, [hashtable]$Map)
    $out = $Template
    foreach ($key in $Map.Keys) {
        $token = '{{' + $key + '}}'
        $out = $out.Replace($token, [string]$Map[$key])
    }
    return $out
}

function Build-ProjectStructure {
    param($Stack)
    $lines = New-Object System.Collections.Generic.List[string]
    [void]$lines.Add(($Stack.ProjectName) + '/')
    foreach ($f in @($Stack.Frontends)) {
        [void]$lines.Add('|-- ' + $f.Path + '/   # 前端')
    }
    foreach ($b in @($Stack.Backends)) {
        $modHint = ''
        if (@($b.Modules).Count -gt 0) { $modHint = ' 模块: ' + ($b.Modules -join ', ') }
        [void]$lines.Add('|-- ' + $b.Path + '/   # ' + $b.BuildTool + $modHint)
    }
    foreach ($d in @($Stack.Docs)) {
        [void]$lines.Add('|-- ' + $d.Path + '/   # ' + $d.Tool)
    }
    [void]$lines.Add('`-- ...')
    return ($lines -join "`n")
}

function Build-BuildCommands {
    param($Stack)
    $sb = New-Object System.Text.StringBuilder
    foreach ($f in @($Stack.Frontends)) {
        [void]$sb.AppendLine('### 前端 (' + $f.Path + ')')
        [void]$sb.AppendLine('```bash')
        $devKey = $f.Scripts.DevKey
        if (-not $devKey) { $devKey = 'dev' }
        $devComment = ''
        if ($f.Scripts.Dev) { $devComment = '  # ' + $f.Scripts.Dev }
        [void]$sb.AppendLine($f.PkgMgr + ' ' + $devKey + $devComment)
        if ($f.Scripts.Build) {
            [void]$sb.AppendLine($f.PkgMgr + ' run build  # ' + $f.Scripts.Build)
        } else {
            [void]$sb.AppendLine($f.PkgMgr + ' run build')
        }
        if ($f.Scripts.Lint) {
            [void]$sb.AppendLine($f.PkgMgr + ' run lint  # ' + $f.Scripts.Lint)
        }
        [void]$sb.AppendLine('```')
        [void]$sb.AppendLine('')
        if (@($f.PathAliases).Count -gt 0) {
            [void]$sb.AppendLine('路径别名: ' + ($f.PathAliases -join '; '))
            [void]$sb.AppendLine('')
        }
    }
    foreach ($b in @($Stack.Backends)) {
        [void]$sb.AppendLine('### 后端 (' + $b.Path + ')')
        [void]$sb.AppendLine('```bash')
        foreach ($c in @($b.SuggestedCommands)) { [void]$sb.AppendLine($c) }
        [void]$sb.AppendLine('```')
        [void]$sb.AppendLine('')
    }
    foreach ($d in @($Stack.Docs)) {
        [void]$sb.AppendLine('### 文档 (' + $d.Path + ')')
        [void]$sb.AppendLine('```bash')
        [void]$sb.AppendLine('cd ' + $d.Path)
        [void]$sb.AppendLine($d.PkgMgr + ' i')
        [void]$sb.AppendLine($d.PkgMgr + ' dev')
        [void]$sb.AppendLine('```')
        [void]$sb.AppendLine('')
    }
    if ($sb.Length -eq 0) { [void]$sb.AppendLine('_未探测到可用命令。_') }
    return $sb.ToString().TrimEnd()
}

function Build-CodingStyle {
    param($Stack)
    $parts = New-Object System.Collections.Generic.List[string]
    $fe = $Stack.PrimaryFrontend
    if ($Stack.HasFrontend -and $fe) {
        if ($fe.FrameworkBase -match 'Vue|Nuxt') {
            [void]$parts.Add('**组件（Vue）**')
            [void]$parts.Add('- 单文件组件 `.vue`，Composition API + `<script setup>`')
            [void]$parts.Add('- Props 使用 `defineProps<T>()`')
            [void]$parts.Add('- 样式方案: ' + $fe.CssScheme)
        } elseif ($fe.FrameworkBase -match 'React|Next') {
            [void]$parts.Add('**组件（React）**')
            [void]$parts.Add('- PascalCase，目录推荐 `index.tsx`')
            [void]$parts.Add('- Props 明确类型（`interface` / 函数组件）')
            [void]$parts.Add('- 样式方案: ' + $fe.CssScheme)
        } else {
            [void]$parts.Add('**组件**')
            [void]$parts.Add('- PascalCase；类型明确；样式方案: ' + $fe.CssScheme)
        }
    } else {
        [void]$parts.Add('_未探测到前端，跳过 UI 组件风格。_')
    }
    return ($parts -join "`n")
}

function Build-ComponentApi {
    param($Stack)
    if (-not $Stack.HasFrontend) { return '_未探测到前端。_' }
    $fe = $Stack.PrimaryFrontend
    $lines = @(
        '- 通用组件放在 `' + $fe.Path + '/src/components/`（若项目目录不同请按实际调整）',
        '- API 封装放在 `services/` 或 `api/`，页面内禁止直接裸调 fetch/axios',
        '- 类型定义放在模块旁或 `types/` / `typings/`'
    )
    return ($lines -join "`n")
}

function Build-BackendBlock {
    param($Stack)
    if (-not $Stack.HasBackend) { return '_未探测到后端。_' }
    $lines = @(
        '- 保持 controller / service / mapper（或 repository）/ entity / dto / vo 分层',
        '- 新增修改删除类接口优先 `@PostMapping`（除非项目已有统一约定）',
        '- 请求模型接入 Jakarta Validation',
        '- 对外 REST 补充 `@Tag` / `@Operation`（必要时 `@Parameter`）',
        '- 库表与实体的是否类字段禁止 boolean，统一 String + CHAR(1)，取值 `0`/`1`（若项目已采用该约定）',
        '- 新接口 JSON 字段使用 camelCase'
    )
    return ($lines -join "`n")
}

function Build-NeverRules {
    param($Stack)
    $rules = New-Object System.Collections.Generic.List[string]
    [void]$rules.Add('- 禁止提交密钥、token 或 `.env` 内容')
    [void]$rules.Add('- 禁止修改 `node_modules/`、构建产物（`dist/`、`build/`、`target/`）或 `.git` 内部')
    [void]$rules.Add('- 禁止静默整文件覆盖手写维护的 `AGENTS.md` / `DESIGN.md`；应使用 suggested 或先征得确认')
    [void]$rules.Add('- 非琐碎多文件改动前，禁止跳过假设 / 成功标准 / 最小计划')
    if ($Stack.HasFrontend) {
        [void]$rules.Add('- 禁止在可写明确类型时使用 `any`')
        [void]$rules.Add('- 禁止静态 UI 使用内联 style；遵循项目样式方案')
        [void]$rules.Add('- 存在 services/api 封装时，禁止在组件内直接调用 fetch/axios')
        [void]$rules.Add('- 列表渲染禁止省略 `key`')
    }
    if ($Stack.HasJavaSpring) {
        [void]$rules.Add('- 存在 Service 层时，禁止把业务逻辑只写在 Controller')
        [void]$rules.Add('- 禁止用 IllegalArgumentException 作为主要业务失败信号；使用项目自定义异常')
        [void]$rules.Add('- 禁止在实体/库表是否语义上引入 boolean（应使用 char `0`/`1` 约定）')
    }
    return ($rules -join "`n")
}

function Build-GenerationBlocks {
    param($Stack)
    $sb = New-Object System.Text.StringBuilder
    if ($Stack.HasFrontend) {
        $fe = $Stack.PrimaryFrontend
        if ($fe.FrameworkBase -match 'Vue|Nuxt') {
            [void]$sb.AppendLine('## Vue SFC')
            [void]$sb.AppendLine('```vue')
            [void]$sb.AppendLine('<script setup lang="ts">')
            [void]$sb.AppendLine('// defineProps / defineEmits / composables')
            [void]$sb.AppendLine('</script>')
            [void]$sb.AppendLine('<template>')
            [void]$sb.AppendLine('  <!-- 模板 -->')
            [void]$sb.AppendLine('</template>')
            [void]$sb.AppendLine('```')
            [void]$sb.AppendLine('')
        }
        if ($fe.FrameworkBase -match 'React|Next') {
            [void]$sb.AppendLine('## React 组件')
            [void]$sb.AppendLine('```tsx')
            [void]$sb.AppendLine('export interface DemoProps { title: string }')
            [void]$sb.AppendLine('export default function Demo({ title }: DemoProps) {')
            [void]$sb.AppendLine('  return <div>{title}</div>')
            [void]$sb.AppendLine('}')
            [void]$sb.AppendLine('```')
            [void]$sb.AppendLine('')
        }
    }
    if ($Stack.HasJavaSpring) {
        [void]$sb.AppendLine('## Java Controller / Service / Mapper')
        [void]$sb.AppendLine('- Controller：REST + 校验 + OpenAPI 注解')
        [void]$sb.AppendLine('- Service：事务与业务规则')
        [void]$sb.AppendLine('- Mapper/Repository：仅持久化')
        [void]$sb.AppendLine('- Entity/DTO/VO：映射清晰；是否语义字段避免 boolean')
        [void]$sb.AppendLine('')
    }
    if ($sb.Length -eq 0) { [void]$sb.AppendLine('_可随项目演进补充各技术栈骨架。_') }
    return $sb.ToString().TrimEnd()
}

function Build-TechStackBullets {
    param($Stack)
    $lines = New-Object System.Collections.Generic.List[string]
    foreach ($f in @($Stack.Frontends)) {
        [void]$lines.Add('- 前端 (`' + $f.Path + '`)：' + $f.Framework + '，UI=' + $f.UiLib + '，样式=' + $f.CssScheme + '，包管理=' + $f.PkgMgr + '，语言=' + $f.Lang)
    }
    foreach ($b in @($Stack.Backends)) {
        [void]$lines.Add('- 后端 (`' + $b.Path + '`)：' + $b.BuildTool + '，Java ' + $b.JavaVersion + '，Spring Boot ' + $b.SpringBootVersion)
    }
    foreach ($d in @($Stack.Docs)) {
        [void]$lines.Add('- 文档 (`' + $d.Path + '`)：' + $d.Tool)
    }
    return ($lines -join "`n")
}

function Build-SpecPathIndex {
    param($Stack)
    if (@($Stack.SpecHints).Count -eq 0) {
        return '_未探测到本地规范线索路径（`sdd/`、`openspec/project.md`、`.cursor/rules`）。_'
    }
    $lines = @($Stack.SpecHints | ForEach-Object { '- `' + $_ + '`' })
    return ($lines -join "`n")
}

function New-AgentsDocumentSet {
    param(
        [Parameter(Mandatory = $true)]$Stack,
        [Parameter(Mandatory = $true)][string]$TemplatesDir
    )

    $autoGen = '- 禁止编辑框架自动生成缓存目录（若存在：`.next/`、`.nuxt/`、`.umi/`、`.angular/`）'
    $monoText = '否'
    if ($Stack.IsMonorepo) { $monoText = '是' }

    $map = @{
        ProjectName        = $Stack.ProjectName
        FrameworkSummary   = $Stack.FrameworkSummary
        IsMonorepoText     = $monoText
        TechStackBullets   = (Build-TechStackBullets -Stack $Stack)
        ProjectStructure   = (Build-ProjectStructure -Stack $Stack)
        AutoGenNever       = $autoGen
        BuildCommands      = (Build-BuildCommands -Stack $Stack)
        CodingStyleBlock   = (Build-CodingStyle -Stack $Stack)
        ComponentApiBlock  = (Build-ComponentApi -Stack $Stack)
        BackendBlock       = (Build-BackendBlock -Stack $Stack)
        NeverRulesBlock    = (Build-NeverRules -Stack $Stack)
        SpecPathIndex      = (Build-SpecPathIndex -Stack $Stack)
        PrimaryColor       = $Stack.PrimaryColor
        BorderRadius       = $Stack.BorderRadius
        PrimaryUiLib       = $Stack.PrimaryUiLib
        GenerationBlocks   = (Build-GenerationBlocks -Stack $Stack)
    }

    $agents = Expand-Template -Template (Get-TemplateText $TemplatesDir 'AGENTS.md.tmpl') -Map $map
    $design = Expand-Template -Template (Get-TemplateText $TemplatesDir 'DESIGN.md.tmpl') -Map $map
    $local = Get-TemplateText $TemplatesDir 'AGENTS.local.md.tmpl'
    $gen = Expand-Template -Template (Get-TemplateText $TemplatesDir 'generation-spec.md.tmpl') -Map $map
    $corr = Get-TemplateText $TemplatesDir 'corrections.md.tmpl'

    return [pscustomobject]@{
        Agents         = $agents
        Design         = $design
        Local          = $local
        GenerationSpec = $gen
        Corrections    = $corr
    }
}