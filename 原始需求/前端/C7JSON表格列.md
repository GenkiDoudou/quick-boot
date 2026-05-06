# C7JSON表格列（C7JsonTableColumn）原始需求

## 背景
- 列表表格列渲染存在大量重复：字典标签、图片预览、超链接、slot 自定义等。

## 目标
- 通过 `columns[]` 配置生成 `ElTableColumn` 列集合，并提供通用 columnType 渲染能力。

## 功能需求
- 列过滤与排序：
  - `visible !== false` 才渲染
  - `order` 升序排序，未设置排最后
- 支持 `el-table-column` 常用字段透传：
  - `prop/label/width/minWidth/fixed/align/headerAlign/sortable/showOverflowTooltip/props`
- 列类型（columnType）：
  - `text`（默认）：支持 `formatter`，空值显示 `emptyText`
  - `tag`：用 `C7DictTag` + `dictList`
  - `image`：用 `C7Preview`（图片模式）
  - `link`：渲染 `<a>`，支持 `linkText/linkHref/linkTarget`
  - `slot`：按 `slotName` 或 `prop` 渲染具名 slot
- 支持列头 slot：`#header-[prop]`

## 验收标准
- columnType 不同渲染正确；formatter 生效；slot 可接管渲染。

