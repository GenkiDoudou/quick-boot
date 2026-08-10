## Context

权威产品设计见 `docs/superpowers/specs/2026-08-07-excel-dict-format-design.md`。本文件为 OpenSpec 实现向设计。

现状：`ExcelUtils` 已支持同步导入导出并注册 `ExcelBigNumberConvert`；`@ExcelDictFormat` 仅为骨架（`dictType` / `dictText`）；系统字典 `SysDictData` 已有 `listByType` + 缓存，但 Excel 链路未接入 value↔label 转换。

约束：`quickboot-common` 不得依赖 `quickboot-system`；`ExcelUtils` 为静态工具类，Converter 需通过 Holder 拿到字典实现。

## Goals / Non-Goals

**Goals:**

1. 字段标注 `@ExcelDictFormat` 后，导出写标签、导入还原键值（双向）。
2. 支持系统字典（`dictType`）与内联 `dictText`；多值分隔；未匹配 KEEP/ERROR/EMPTY。
3. common 提供注解、SPI、Converter；system 提供缓存 Lookup 并挂载。
4. 无注解 String 字段行为不变。

**Non-Goals:**

- API/VO Jackson 字典翻译。
- 枚举映射、非 String 字段。
- 批量给现有业务 VO 补注解。
- 前端字典组件。

## Decisions

### 1. 全局 EasyExcel Converter（方案 A）

- `ExcelDictConvert` 实现 `Converter<String>`，在 `ExcelUtils` 全部 write/read 入口 `registerConverter`。
- 字段无 `@ExcelDictFormat` 时原样直通，避免误伤其它 String 列。
- 备选：每字段写 `@ExcelProperty(converter=...)` → 否决（易漏、样板多）。
- 备选：导出前手工扫注解改字段 → 否决（导入路径难统一）。

### 2. common SPI + system 实现

- `DictLookup#getLabel` / `getValue`；仅服务 `dictType`。
- `DictLookupHolder` 静态 set/get；system AutoConfiguration 注入 `SysDictLookup`（内部调 `ISysDictDataService.listByType`）。
- 内联 `dictText` 由 Converter/工具类本地解析，不进 SPI。
- 备选：全部放 system → 否决（Excel 注解与 Converter 应在 common）。
- 备选：common 直接依赖字典 Service → 否决（模块环/分层破坏）。

### 3. 注解属性与优先级

- `dictType` > `dictText` > 无映射；同字段两者皆有时忽略 `dictText`。
- `separator` 默认 `,`；空串视为单值。
- `missPolicy` 默认 `KEEP`；枚举 `KEEP` / `ERROR` / `EMPTY`。

### 4. 导入混填兼容

- 子项先 label→value；失败再判断原文是否已是合法 value；仍无则 `missPolicy`。
- 便于导出再导入与用户直接填键值。

### 5. 错误类型

- `ERROR` 策略抛既有 `ExcelDataCheckException`（或等价 Excel 校验异常），文案含字段、`dictType`（若有）、原文。

## Risks / Trade-offs

- [全局 String Converter 误伤] → 无注解必须直通；单测覆盖无注解与 blank
- [Holder 未注册 + dictType] → ERROR 提示未就绪；KEEP/EMPTY 按策略，不假装命中
- [逐格打库] → SysDictLookup 必须走 listByType 缓存
- [多值 EMPTY 产生多余分隔符] → 拼接时去掉空段

## Migration Plan

1. 合入 common Converter + ExcelUtils 注册；仅内联即可在无 system 挂载时工作。
2. 合入 system SysDictLookup 挂载后，`dictType` 生效。
3. 业务 VO 按需逐步加注解（本变更不强制）。
4. 回滚：去掉 Converter 注册或注解即可恢复原行为；无库表变更。

## Open Questions

- 无（设计文档已确认）。
