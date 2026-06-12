# PostgreSQL + pgvector 一键部署（Windows PowerShell）
# 用法：在 docker/pgvector 目录下执行  .\deploy.ps1
# 可选：.\deploy.ps1 -Down  停止并删除容器（保留数据卷）

param(
    [switch]$Down,
    [switch]$Logs
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

$EnvFile = Join-Path $ScriptDir ".env"
$EnvExample = Join-Path $ScriptDir ".env.example"

if (-not (Test-Path $EnvFile)) {
    if (Test-Path $EnvExample) {
        Copy-Item $EnvExample $EnvFile
        Write-Host "已生成 .env（来自 .env.example），可按需修改密码与端口。" -ForegroundColor Yellow
    }
}

function Invoke-Compose {
    param([string[]]$Args)
    docker compose --env-file $EnvFile @Args
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

if ($Down) {
    Write-Host "停止并移除容器（数据卷 pgvector_data 保留）..." -ForegroundColor Cyan
    Invoke-Compose @("down")
    exit 0
}

if ($Logs) {
    Invoke-Compose @("logs", "-f", "pgvector")
    exit 0
}

Write-Host "拉取镜像并启动 PostgreSQL + pgvector ..." -ForegroundColor Cyan
Invoke-Compose @("pull")
Invoke-Compose @("up", "-d")

Write-Host ""
Write-Host "等待健康检查 ..." -ForegroundColor Cyan
$maxWait = 60
$elapsed = 0
while ($elapsed -lt $maxWait) {
    $status = docker inspect --format='{{.State.Health.Status}}' quickboot-pgvector 2>$null
    if ($status -eq "healthy") {
        Write-Host "容器已就绪 (healthy)" -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 2
    $elapsed += 2
}

Write-Host ""
Write-Host "连接信息（与 application-dev.yml 对齐）：" -ForegroundColor Green
Get-Content $EnvFile | ForEach-Object {
    if ($_ -match '^(POSTGRES_|PGVECTOR_)') { Write-Host "  $_" }
}
Write-Host "  JDBC: jdbc:postgresql://127.0.0.1:5433/quickboot_vector"
Write-Host ""
Write-Host "常用命令：" -ForegroundColor Gray
Write-Host "  查看日志:  .\deploy.ps1 -Logs"
Write-Host "  停止服务:  .\deploy.ps1 -Down"
Write-Host "  进入 psql: docker exec -it quickboot-pgvector psql -U vector -d quickboot_vector"
