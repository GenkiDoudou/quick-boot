#Requires -Version 5.1
<#
.SYNOPSIS
  Detect project stack facts for init-agents templates.
#>

function Get-PackageJsonDepsText {
    param([string]$PackageJsonPath)
    if (-not (Test-Path -LiteralPath $PackageJsonPath)) { return "" }
    try {
        $raw = Get-Content -LiteralPath $PackageJsonPath -Raw -Encoding UTF8
        return ($raw -replace '\s', '')
    } catch {
        return ""
    }
}

function Get-PkgMgr {
    param([string]$Dir)
    if (Test-Path -LiteralPath (Join-Path $Dir "pnpm-lock.yaml")) { return "pnpm" }
    if (Test-Path -LiteralPath (Join-Path $Dir "yarn.lock")) { return "yarn" }
    if (Test-Path -LiteralPath (Join-Path $Dir "bun.lockb")) { return "bun" }
    if (Test-Path -LiteralPath (Join-Path $Dir "package-lock.json")) { return "npm" }
    $pkgPath = Join-Path $Dir "package.json"
    if (Test-Path -LiteralPath $pkgPath) {
        $pkgText = Get-Content -LiteralPath $pkgPath -Raw -Encoding UTF8
        if ($pkgText -match '"packageManager"\s*:\s*"pnpm') { return "pnpm" }
        if ($pkgText -match '"packageManager"\s*:\s*"yarn') { return "yarn" }
        if ($pkgText -match '"packageManager"\s*:\s*"bun') { return "bun" }
    }
    return "npm"
}

function Get-PathAliases {
    param([string]$Dir)
    $aliases = New-Object System.Collections.Generic.List[string]
    foreach ($name in @("tsconfig.json", "tsconfig.app.json", "jsconfig.json")) {
        $p = Join-Path $Dir $name
        if (-not (Test-Path -LiteralPath $p)) { continue }
        $t = Get-Content -LiteralPath $p -Raw -Encoding UTF8
        if ($t -match '"@/\*"') { [void]$aliases.Add('@/* -> src/*') }
        if ($t -match '"@@/\*"') { [void]$aliases.Add('@@/* -> src/.umi/*') }
    }
    return $aliases
}

function Get-ScriptsInfo {
    param([string]$PackageJsonPath)
    $info = [ordered]@{ DevKey = "dev"; Dev = ""; Build = ""; Lint = "" }
    if (-not (Test-Path -LiteralPath $PackageJsonPath)) { return [pscustomobject]$info }
    try {
        $pkg = Get-Content -LiteralPath $PackageJsonPath -Raw -Encoding UTF8 | ConvertFrom-Json
    } catch {
        return [pscustomobject]$info
    }
    if (-not $pkg.scripts) { return [pscustomobject]$info }
    $scripts = $pkg.scripts
    foreach ($key in @("dev", "serve", "start")) {
        if ($scripts.PSObject.Properties.Name -contains $key) {
            $info.DevKey = $key
            $info.Dev = [string]$scripts.$key
            break
        }
    }
    if ($scripts.PSObject.Properties.Name -contains "build") { $info.Build = [string]$scripts.build }
    elseif ($scripts.PSObject.Properties.Name -contains "build:prod") { $info.Build = [string]$scripts.'build:prod' }
    if ($scripts.PSObject.Properties.Name -contains "lint") { $info.Lint = [string]$scripts.lint }
    return [pscustomobject]$info
}

function Detect-FrontendFromPackage {
    param(
        [string]$PackageJsonPath,
        [string]$ProjectRoot
    )
    $dir = Split-Path -Parent $PackageJsonPath
    $rel = if ($dir -eq $ProjectRoot) { "." } else { $dir.Substring($ProjectRoot.Length).TrimStart('\', '/') -replace '\\', '/' }
    $deps = Get-PackageJsonDepsText -PackageJsonPath $PackageJsonPath
    if ([string]::IsNullOrWhiteSpace($deps)) { return $null }

    $framework = "Unknown"
    $version = ""
    if ($deps -match '"next"') { $framework = "Next.js" }
    elseif ($deps -match '"nuxt"') { $framework = "Nuxt" }
    elseif ($deps -match '"@angular/core"') { $framework = "Angular" }
    elseif ($deps -match '"svelte"') { $framework = "Svelte" }
    elseif ($deps -match '"vue"') { $framework = "Vue" }
    elseif ($deps -match '"react"') { $framework = "React" }

    if ($deps -match '"umi"|"@umijs/"|"@ali/ppx"') {
        if ($framework -eq "Unknown") { $framework = "Umi" }
        else { $framework = "$framework + Umi" }
    }

    $name = Split-Path -Leaf $dir
    try {
        $pkg = Get-Content -LiteralPath $PackageJsonPath -Raw -Encoding UTF8 | ConvertFrom-Json
        if ($pkg.name) { $name = [string]$pkg.name }
        $allDeps = @{}
        if ($pkg.dependencies) { $pkg.dependencies.PSObject.Properties | ForEach-Object { $allDeps[$_.Name] = [string]$_.Value } }
        if ($pkg.devDependencies) { $pkg.devDependencies.PSObject.Properties | ForEach-Object { $allDeps[$_.Name] = [string]$_.Value } }
        foreach ($key in @("vue", "react", "next", "@angular/core")) {
            if ($allDeps.ContainsKey($key) -and $allDeps[$key] -match '(\d+)') {
                $version = $Matches[1]
                break
            }
        }
    } catch { }

    $uiLib = "none"
    if ($deps -match '"element-plus"') { $uiLib = "Element Plus" }
    elseif ($deps -match '"element-ui"') { $uiLib = "Element UI" }
    elseif ($deps -match '"antd"|"@ant-design/') { $uiLib = "Ant Design" }
    elseif ($deps -match '"@arco-design/') { $uiLib = "Arco Design" }
    elseif ($deps -match '"@douyinfe/semi-ui"') { $uiLib = "Semi Design" }
    elseif ($deps -match '"vant"') { $uiLib = "Vant" }
    elseif ($deps -match '"@mui/material"|"@material-ui/') { $uiLib = "Material UI" }

    $css = "CSS"
    if ($deps -match '"tailwindcss"') { $css = "Tailwind CSS" }
    elseif ($deps -match '"styled-components"' -or $deps -match '"@emotion/') { $css = "CSS-in-JS" }
    elseif ($deps -match '"less"') { $css = "Less + CSS Modules" }
    elseif ($deps -match '"sass"|"node-sass"') { $css = "Sass/SCSS" }

    $lang = "JavaScript"
    if ($deps -match '"typescript"' -or (Test-Path -LiteralPath (Join-Path $dir "tsconfig.json"))) {
        $lang = "TypeScript"
    }

    $scripts = Get-ScriptsInfo -PackageJsonPath $PackageJsonPath
    $aliases = Get-PathAliases -Dir $dir
    $fwLabel = if ($version) { "$framework $version" } else { $framework }

    return [pscustomobject]@{
        Path             = $rel
        Name             = $name
        Framework        = $fwLabel
        FrameworkBase    = $framework
        FrameworkVersion = $version
        UiLib            = $uiLib
        CssScheme        = $css
        PkgMgr           = (Get-PkgMgr -Dir $dir)
        Lang             = $lang
        Scripts          = $scripts
        PathAliases      = @($aliases)
    }
}

function Detect-BackendMaven {
    param(
        [string]$PomPath,
        [string]$ProjectRoot
    )
    $dir = Split-Path -Parent $PomPath
    $rel = if ($dir -eq $ProjectRoot) { "." } else { $dir.Substring($ProjectRoot.Length).TrimStart('\', '/') -replace '\\', '/' }
    $text = Get-Content -LiteralPath $PomPath -Raw -Encoding UTF8
    $java = "Unknown"
    if ($text -match '<maven\.compiler\.(?:release|source)>\s*([^<]+)\s*<') { $java = $Matches[1].Trim() }
    elseif ($text -match '<java\.version>\s*([^<]+)\s*<') { $java = $Matches[1].Trim() }
    $boot = "Unknown"
    if ($text -match '<spring-boot\.version>\s*([^<]+)\s*<') { $boot = $Matches[1].Trim() }
    $modules = @()
    if ($text -match '(?s)<modules>(.*?)</modules>') {
        $modBlock = $Matches[1]
        $modules = [regex]::Matches($modBlock, '<module>\s*([^<]+)\s*</module>') | ForEach-Object { $_.Groups[1].Value.Trim() }
    }
    $artifact = "unknown"
    if ($text -match '<artifactId>\s*([^<]+)\s*</artifactId>') { $artifact = $Matches[1].Trim() }

    return [pscustomobject]@{
        Path              = $rel
        BuildTool         = "Maven"
        ArtifactId        = $artifact
        JavaVersion       = $java
        SpringBootVersion = $boot
        Modules           = @($modules)
        SuggestedCommands = @(
            'mvn clean install -DskipTests',
            'mvn -pl <module> spring-boot:run'
        )
    }
}

function Detect-BackendGradle {
    param(
        [string]$GradlePath,
        [string]$ProjectRoot
    )
    $dir = Split-Path -Parent $GradlePath
    $rel = if ($dir -eq $ProjectRoot) { "." } else { $dir.Substring($ProjectRoot.Length).TrimStart('\', '/') -replace '\\', '/' }
    $text = Get-Content -LiteralPath $GradlePath -Raw -Encoding UTF8
    $java = "Unknown"
    if ($text -match 'JavaVersion\.VERSION_(\d+)') { $java = $Matches[1] }
    elseif ($text -match 'sourceCompatibility\s*=\s*[''"]?(\d+)') { $java = $Matches[1] }
    $boot = "Unknown"
    if ($text -match 'springframework\.boot[^0-9]*([0-9]+\.[0-9]+(?:\.[0-9]+)?)') {
        $boot = $Matches[1]
    }
    return [pscustomobject]@{
        Path              = $rel
        BuildTool         = "Gradle"
        ArtifactId        = (Split-Path -Leaf $dir)
        JavaVersion       = $java
        SpringBootVersion = $boot
        Modules           = @()
        SuggestedCommands = @('./gradlew build', './gradlew bootRun')
    }
}

function Get-UiTheme {
    param([string]$UiLib)
    switch ($UiLib) {
        "Ant Design" { return @{ Color = "#1677ff"; Radius = "6" } }
        "Element Plus" { return @{ Color = "#409EFF"; Radius = "4" } }
        "Element UI" { return @{ Color = "#409EFF"; Radius = "4" } }
        "Arco Design" { return @{ Color = "#165DFF"; Radius = "4" } }
        "Semi Design" { return @{ Color = "#0077FA"; Radius = "6" } }
        "Vant" { return @{ Color = "#1989fa"; Radius = "4" } }
        "Material UI" { return @{ Color = "#1976d2"; Radius = "4" } }
        default { return @{ Color = "#1677ff"; Radius = "6" } }
    }
}

function Get-ProjectStack {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ProjectRoot
    )

    $root = (Resolve-Path -LiteralPath $ProjectRoot).Path
    $frontends = New-Object System.Collections.Generic.List[object]
    $backends = New-Object System.Collections.Generic.List[object]
    $docs = New-Object System.Collections.Generic.List[object]
    $hints = New-Object System.Collections.Generic.List[string]

    $pkgCandidates = New-Object System.Collections.Generic.List[string]
    $rootPkg = Join-Path $root "package.json"
    if (Test-Path -LiteralPath $rootPkg) { [void]$pkgCandidates.Add($rootPkg) }

    Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue | ForEach-Object {
        $p = Join-Path $_.FullName "package.json"
        if (Test-Path -LiteralPath $p) { [void]$pkgCandidates.Add($p) }
    }
    foreach ($sub in @("packages", "apps")) {
        $base = Join-Path $root $sub
        if (-not (Test-Path -LiteralPath $base)) { continue }
        Get-ChildItem -LiteralPath $base -Directory -ErrorAction SilentlyContinue | ForEach-Object {
            $p = Join-Path $_.FullName "package.json"
            if (Test-Path -LiteralPath $p) { [void]$pkgCandidates.Add($p) }
        }
    }

    $seenPkg = @{}
    foreach ($pkgPath in $pkgCandidates) {
        $key = $pkgPath.ToLowerInvariant()
        if ($seenPkg.ContainsKey($key)) { continue }
        $seenPkg[$key] = $true
        $relDir = Split-Path -Parent $pkgPath
        $docsRoot = Join-Path $root "docs"
        $isDocs = ($relDir -eq $docsRoot)
        $fe = Detect-FrontendFromPackage -PackageJsonPath $pkgPath -ProjectRoot $root
        if ($null -eq $fe) { continue }
        if ($isDocs) {
            $deps = Get-PackageJsonDepsText -PackageJsonPath $pkgPath
            if ($deps -match '"vitepress"') {
                [void]$docs.Add([pscustomobject]@{
                        Path    = "docs"
                        Tool    = "VitePress"
                        PkgMgr  = (Get-PkgMgr -Dir $relDir)
                        Scripts = (Get-ScriptsInfo -PackageJsonPath $pkgPath)
                    })
            }
            continue
        }
        if ($fe.FrameworkBase -eq "Unknown" -and $fe.UiLib -eq "none") { continue }
        [void]$frontends.Add($fe)
    }

    $pomCandidates = @()
    $rootPom = Join-Path $root "pom.xml"
    if (Test-Path -LiteralPath $rootPom) { $pomCandidates += $rootPom }
    Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue | ForEach-Object {
        $p = Join-Path $_.FullName "pom.xml"
        if (Test-Path -LiteralPath $p) { $pomCandidates += $p }
    }
    foreach ($pom in $pomCandidates) {
        [void]$backends.Add((Detect-BackendMaven -PomPath $pom -ProjectRoot $root))
    }

    $gradleFiles = @()
    foreach ($name in @("build.gradle", "build.gradle.kts")) {
        $gp = Join-Path $root $name
        if (Test-Path -LiteralPath $gp) { $gradleFiles += $gp }
        Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue | ForEach-Object {
            $p = Join-Path $_.FullName $name
            if (Test-Path -LiteralPath $p) { $gradleFiles += $p }
        }
    }
    foreach ($gf in $gradleFiles) {
        [void]$backends.Add((Detect-BackendGradle -GradlePath $gf -ProjectRoot $root))
    }

    foreach ($hint in @("sdd", "openspec/project.md", ".cursor/rules")) {
        $hp = Join-Path $root ($hint -replace '/', [IO.Path]::DirectorySeparatorChar)
        if (Test-Path -LiteralPath $hp) { [void]$hints.Add($hint) }
    }

    $isMonorepo = $false
    if (Test-Path -LiteralPath (Join-Path $root "pnpm-workspace.yaml")) { $isMonorepo = $true }
    if (Test-Path -LiteralPath (Join-Path $root "lerna.json")) { $isMonorepo = $true }
    if (Test-Path -LiteralPath $rootPkg) {
        $rt = Get-Content -LiteralPath $rootPkg -Raw -Encoding UTF8 -ErrorAction SilentlyContinue
        if ($rt -and $rt -match '"workspaces"') { $isMonorepo = $true }
    }
    $modSum = 0
    foreach ($b in $backends) { $modSum += @($b.Modules).Count }
    if ($modSum -gt 0) { $isMonorepo = $true }
    if (($frontends.Count + $backends.Count + $docs.Count) -gt 1) { $isMonorepo = $true }

    $primaryFe = $null
    $rootFe = @($frontends | Where-Object { $_.Path -eq "." }) | Select-Object -First 1
    $uiNamed = @($frontends | Where-Object { $_.Name -match 'ui' -and $_.UiLib -ne "none" }) | Select-Object -First 1
    $anyUi = @($frontends | Where-Object { $_.UiLib -ne "none" }) | Select-Object -First 1
    if ($rootFe -and $rootFe.UiLib -ne "none") { $primaryFe = $rootFe }
    elseif ($uiNamed) { $primaryFe = $uiNamed }
    elseif ($anyUi) { $primaryFe = $anyUi }
    elseif ($frontends.Count -gt 0) { $primaryFe = $frontends[0] }

    $uiLib = if ($primaryFe) { $primaryFe.UiLib } else { "none" }
    $theme = Get-UiTheme -UiLib $uiLib

    $projectName = Split-Path -Leaf $root
    if ($rootFe -and $rootFe.Name) { $projectName = $rootFe.Name }
    elseif ($backends.Count -gt 0) { $projectName = $backends[0].ArtifactId }
    elseif ($frontends.Count -gt 0 -and $frontends[0].Name) { $projectName = $frontends[0].Name }

    $fwParts = New-Object System.Collections.Generic.List[string]
    foreach ($f in $frontends) { [void]$fwParts.Add("$($f.Framework) ($($f.Path))") }
    foreach ($b in $backends) {
        [void]$fwParts.Add("$($b.BuildTool) Java $($b.JavaVersion) / Spring Boot $($b.SpringBootVersion) ($($b.Path))")
    }
    foreach ($d in $docs) { [void]$fwParts.Add("$($d.Tool) ($($d.Path))") }

    $summary = "Unknown"
    if ($fwParts.Count -gt 0) { $summary = ($fwParts -join "; ") }

    $result = New-Object psobject
    $result | Add-Member NoteProperty ProjectName $projectName
    $result | Add-Member NoteProperty ProjectRoot $root
    $result | Add-Member NoteProperty IsMonorepo $isMonorepo
    $result | Add-Member NoteProperty Frontends $frontends.ToArray()
    $result | Add-Member NoteProperty Backends $backends.ToArray()
    $result | Add-Member NoteProperty Docs $docs.ToArray()
    $result | Add-Member NoteProperty SpecHints $hints.ToArray()
    $result | Add-Member NoteProperty PrimaryUiLib $uiLib
    $result | Add-Member NoteProperty PrimaryColor $theme.Color
    $result | Add-Member NoteProperty BorderRadius $theme.Radius
    $result | Add-Member NoteProperty PrimaryFrontend $primaryFe
    $result | Add-Member NoteProperty FrameworkSummary $summary
    $result | Add-Member NoteProperty HasFrontend ($frontends.Count -gt 0)
    $result | Add-Member NoteProperty HasBackend ($backends.Count -gt 0)
    $result | Add-Member NoteProperty HasDocs ($docs.Count -gt 0)
    $result | Add-Member NoteProperty HasJavaSpring ($backends.Count -gt 0)
    return $result
}

function Test-ProjectStackRecognized {
    param($Stack)
    return ($Stack.Frontends.Count -gt 0 -or $Stack.Backends.Count -gt 0 -or $Stack.Docs.Count -gt 0)
}