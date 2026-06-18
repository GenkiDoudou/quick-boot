# 一次性：将 MySQL 8 dump 转为 MariaDB 兼容并导入本地嵌入式库（需后端已用 dev-embedded 启动过至少一次）
# 用法（仓库根目录）：
#   1. 先启动后端 dev-embedded，确认 3307 可连
#   2. .\scripts\mariadb4j-import-mysql-dump.ps1
#
# 导入完成后，后续换机只需拷贝 data/mariadb4j/ 目录。

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$SourceDump = Join-Path $Root "docs\-2026_06_17_23_26_05-dump.sql"
$PreparedDump = Join-Path $Root "data\mariadb4j-import-prepared.sql"

if (-not (Test-Path $SourceDump)) {
    Write-Error "找不到 dump：$SourceDump"
}

Write-Host "预处理 MySQL 8 collation -> MariaDB 兼容 ..." -ForegroundColor Cyan
$dir = Split-Path $PreparedDump -Parent
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }

$content = [System.IO.File]::ReadAllText($SourceDump)
$content = $content -replace 'utf8mb4_0900_bin', 'utf8mb4_bin'
$content = $content -replace 'utf8mb4_0900_ai_ci', 'utf8mb4_unicode_ci'
[System.IO.File]::WriteAllText($PreparedDump, $content, [System.Text.UTF8Encoding]::new($false))

$mysql = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysql) {
    Write-Host ""
    Write-Host "未找到 mysql 客户端。请安装 MySQL/MariaDB 客户端后执行：" -ForegroundColor Yellow
    Write-Host "  mysql -h127.0.0.1 -P3307 -uroot qc2 < `"$PreparedDump`""
    Write-Host ""
    Write-Host "预处理文件已生成：$PreparedDump" -ForegroundColor Green
    exit 0
}

Write-Host "导入到 127.0.0.1:3307/qc2（约 1-3 分钟）..." -ForegroundColor Cyan
Get-Content $PreparedDump -Raw | & mysql -h127.0.0.1 -P3307 -uroot qc2
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "导入完成。可停止后端并备份 data/mariadb4j/ 目录。" -ForegroundColor Green
