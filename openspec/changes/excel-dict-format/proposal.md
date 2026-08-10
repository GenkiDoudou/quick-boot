## Why

Excel 导入导出已落地，但缺少统一的字典值/标签转换：业务 VO 只能存原始 `dictValue`，导出对人不可读，导入也无法把中文标签还原为键值。现有 `ExcelDictFormat` 仅为骨架，需补齐常规字典转换能力。

## What Changes

- 完善 `@ExcelDictFormat`：`dictType`、内联 `dictText`、`separator`、`missPolicy`（KEEP/ERROR/EMPTY）。
- 新增 EasyExcel 全局 `ExcelDictConvert`：导出 value→label，导入 label→value；支持多值与混填兼容。
- `quickboot-common` 提供 `DictLookup` SPI + `DictLookupHolder`；内联映射本地解析。
- `quickboot-system` 实现 `SysDictLookup`（复用 `listByType` 缓存）并 AutoConfiguration 挂载。
- `ExcelUtils` 所有读写入口注册该 Converter；无注解 String 字段原样直通。
- 本版不改业务 VO、不加 API/Jackson 字典翻译。

参考设计：`docs/superpowers/specs/2026-08-07-excel-dict-format-design.md`。

## Capabilities

### New Capabilities

- `excel-dict-format`: Excel 字段字典转换（注解契约、双向转换、多值、未匹配策略、SPI 与系统字典挂载）。

### Modified Capabilities

- （无；不修改既有主 specs 能力需求。）

## Impact

- 后端：`quickboot-common`（注解、SPI、Converter、ExcelUtils）；`quickboot-system`（SysDictLookup + AutoConfiguration）。
- 前端：无。
- 依赖：EasyExcel、现有 `ISysDictDataService.listByType` 缓存；common 不直接依赖 system。
- 行为：仅标注 `@ExcelDictFormat` 的 String 字段参与转换；未注册 Lookup 时按 `missPolicy` 处理 `dictType` 字段。
