## Why

导入模板目前只导出表头，列上无填写引导（枚举下拉、必填/格式提示），填错成本高。EasyExcel 可用 POI 写入数据有效性与输入提示；应复用已有 `@ExcelDictFormat` 与 Jakarta Validation，避免再造一套规则注解，且不削弱服务端权威校验。

## What Changes

- 新增 `TemplateConstraintWriteHandler`：扫描 `*ImportRow` 的 `@ExcelProperty` 字段，将 `@ExcelDictFormat` 投影为 Excel 下拉（labels），将 Validation 注解投影为输入提示/批注。
- `ExcelUtils.exportExcel` 增加 `applyTemplateConstraints` 开关（默认 `false` 保持兼容）；导入模板下载传 `true`，普通业务导出默认关闭。
- 对接常用 Validation：`@NotBlank`/`@NotNull`/`@Pattern`/`@Size`/`@Length`/`@Email`；日期/手机号以提示为主，不把 Java 正则写入 Excel 自定义公式，不硬拦非法输入。
- 各业务 `import/template` 入口打开开关；`*ImportRow` 按需补 DictFormat / Validation（本版不强制改完所有字段）。
- 不改造 `exportTemplate(classpath 静态模板)`；不新建 `@ExcelDropdown` / `@ExcelPattern` 等规则注解族。

参考设计：`docs/superpowers/specs/2026-08-07-excel-import-template-constraints-design.md`。

## Capabilities

### New Capabilities

- `excel-template-constraints`: 导入模板导出时按注解写入列级下拉与输入提示；开关控制；与字典转换/导入校验协作边界。

### Modified Capabilities

- （无；主 specs 目录尚无既有能力需改需求。）

## Impact

- 后端：`quickboot-common`（Handler、提示组装、`ExcelUtils` 开关）；`quickboot-system`（各 `import/template` 调用打开开关；样例/按需 `*ImportRow` 注解）。
- 前端：无。
- 依赖：EasyExcel 3.3.4 / POI DataValidation；复用 `@ExcelDictFormat`、`DictLookup`/`dictText` 取 labels；Jakarta Validation 注解元数据。
- 行为：权威校验仍在导入侧；Excel 端仅 UX 提示，可被粘贴绕过。
