## Context

权威产品设计见 `docs/superpowers/specs/2026-08-07-excel-import-template-constraints-design.md`。本文件为 OpenSpec 实现向设计。

现状：导入模板通过 `ExcelUtils.exportExcel(emptyList, ..., ImportRow.class, response)` 只写表头；无列级下拉/输入提示。字典转换能力（`@ExcelDictFormat` / `DictLookup`）与导入 Bean Validation（`ExcelListenerCallback` + `ValidatorUtils`）已存在或并行落地。

约束：不新增规则专用注解；Excel 端提示可被粘贴绕过，权威校验仍在导入；`ExcelUtils` 静态 API 需保持默认行为兼容；下拉 labels 依赖字典设计的内联解析与 `DictLookup`。

## Goals / Non-Goals

**Goals:**

1. `applyTemplateConstraints=true` 时，按 ImportRow 注解写入 Excel 下拉（Dict labels）与输入提示（Validation）。
2. 复用 `@ExcelDictFormat` + Jakarta Validation，不新建规则注解族。
3. 普通 export 默认关闭约束；各 `import/template` 打开开关。
4. Lookup 缺失 / 下拉超长时导出仍成功，并有明确降级与 warn。

**Non-Goals:**

- Excel 硬拦非法输入、用公式完整复刻 Java `@Pattern`。
- 改造 `exportTemplate(classpath 静态模板)`。
- 强制改完所有 `*ImportRow` 字段注解。
- 前端改动。

## Decisions

### 1. SheetWriteHandler + 显式开关（方案 B）

- 新增 `TemplateConstraintWriteHandler`，仅在 `applyTemplateConstraints=true` 时 `registerWriteHandler`。
- 现有 `exportExcel(...)` 默认 `false`，避免业务导出误加下拉。
- 备选：静态 xlsx 模板预埋规则 → 否决（与 ImportRow 易漂移）。
- 备选：仅加强服务端 Validation、模板不加规则 → 否决（不满足填表引导目标）。

### 2. 复用注解作单一真相

- `@ExcelDictFormat` → 下拉选项 = labels（`dictText` 或 `dictType`→`DictLookup`）；不改 Converter / missPolicy 语义。
- `@NotBlank`/`@NotNull`/`@Pattern`/`@Size`/`@Length`/`@Email` → 输入提示/批注；`message` 优先，缺省中文默认文案。
- 多条 Validation 提示按「必填 → 格式/长度 → 其它」拼接。
- 备选：新建 `@ExcelDropdown`/`@ExcelPattern` → 否决（与用户确认的复用路径冲突、重复声明）。

### 3. Excel 严格度 = 提示为主

- 使用输入提示（及可选批注），不启用强制 `showErrorBox` 拒绝输入。
- `@Pattern` 不写入 Excel 自定义公式；正则仅出现在提示文案（可缩略）。
- 可选：有 max 的 `@Size`/`@Length` 使用 POI 文本长度软约束。

### 4. 列扫描与行范围

- 仅 `@ExcelProperty` 字段；列序优先 `index`，否则声明序。
- 数据有效性/提示覆盖第 2～N 行，N 默认 2000（可配置常量或构造参数）。

### 5. 降级策略

- Lookup 未注册 + `dictType`：跳过该列下拉，warn，导出成功；可提示「字典未就绪，请按标签填写」。
- 显式列表超长：隐藏 sheet + 公式列表，或降级为仅提示；**禁止静默截断选项**。

### 6. 业务落地

- common 交付能力；system 各 `import/template` 传 `true`。
- 至少选一个样例 ImportRow（如用户）补 Dict + Validation 做验证；其余按需渐进。

## Risks / Trade-offs

- [Excel 提示可被粘贴绕过] → 文档明确；导入 Validation / Dict 仍为权威
- [WPS / 旧版 Excel 提示表现不一致] → 不特判；文档注明兼容风险
- [全局误给业务导出加下拉] → 默认开关 false；仅模板入口打开
- [Dict 与 Handler 重复解析 labels] → 复用既有 `dictText` 解析与 Lookup；保持小工具类，避免复制语义
- [下拉超长静默截断] → 强制降级路径 + warn

## Migration Plan

1. 合入 Handler + `ExcelUtils` 开关；默认关闭，行为与现网一致。
2. 打开各 `import/template`；样例 ImportRow 补注解验证。
3. 业务按需补注解。
4. 回滚：模板调用改回 `false` 或去掉 Handler 注册即可；无库表变更。

## Open Questions

- 无（产品设计已确认）。
