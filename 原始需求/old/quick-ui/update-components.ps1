# 组件更新脚本

# 定义组件列表
$components = @(
    'c7-cascader',
    'c7-dialog', 
    'c7-dict-tag',
    'c7-layer',
    'c7-preview',
    'c7-switch-form',
    'c7-json-table-column'
)

# 源目录和目标目录
$sourceBase = "packages\c7-plus\src\components"
$targetBase = "src\components\c7"

Write-Host "开始更新组件..." -ForegroundColor Green

# 复制普通组件
foreach ($comp in $components) {
    $source = Join-Path $sourceBase "$comp\index.vue"
    $target = Join-Path $targetBase "$comp\index.vue"
    
    if (Test-Path $source) {
        Copy-Item $source $target -Force
        Write-Host "✓ 已更新: $comp" -ForegroundColor Green
    } else {
        Write-Host "✗ 未找到: $comp" -ForegroundColor Red
    }
}

# 复制 c7-crud（包含 pagination.vue）
Write-Host "`n更新 c7-crud 组件..." -ForegroundColor Yellow
$crudSource = Join-Path $sourceBase "c7-crud\index.vue"
$crudTarget = Join-Path $targetBase "c7-crud\index.vue"
$paginationSource = Join-Path $sourceBase "c7-crud\pagination.vue"
$paginationTarget = Join-Path $targetBase "c7-crud\pagination.vue"

if (Test-Path $crudSource) {
    Copy-Item $crudSource $crudTarget -Force
    Write-Host "✓ 已更新: c7-crud/index.vue" -ForegroundColor Green
}

if (Test-Path $paginationSource) {
    Copy-Item $paginationSource $paginationTarget -Force
    Write-Host "✓ 已更新: c7-crud/pagination.vue" -ForegroundColor Green
}

Write-Host "`n所有组件更新完成！" -ForegroundColor Green
Write-Host "请检查组件是否正常工作。" -ForegroundColor Yellow

