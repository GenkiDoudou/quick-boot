## 1. Handler core (common)

- [x] 1.1 新增列扫描辅助：解析带 `@ExcelProperty` 的字段列序（`index` 优先，否则声明序）
- [x] 1.2 实现 `TemplateConstraintWriteHandler`：为指定行范围写入 DataValidation 下拉与输入提示/批注
- [x] 1.3 对接 `@ExcelDictFormat`：从 `dictText` / `DictLookup` 解析 labels 生成显式列表下拉；Lookup 缺失则 skip + warn
- [x] 1.4 对接 Validation：`@NotBlank`/`@NotNull`/`@Pattern`/`@Size`/`@Length`/`@Email` → 提示文案（message 优先；多条按必填→格式/长度→其它拼接）
- [x] 1.5 下拉超长：隐藏 sheet 公式列表或降级仅提示 + warn；禁止静默截断

## 2. ExcelUtils API

- [x] 2.1 为 `exportExcel` 增加 `applyTemplateConstraints` 开关（默认 `false`，保持兼容）
- [x] 2.2 开关为 `true` 时注册 `TemplateConstraintWriteHandler`；`false` 时行为与现网一致

## 3. Business opt-in (system)

- [x] 3.1 各现有 `import/template` 调用传 `applyTemplateConstraints=true`（user / dept / dict-type / dict-data / config / role / oauth-client）
- [x] 3.2 普通业务 `export` 保持默认关闭
- [x] 3.3 至少一个样例 `ImportRow`（建议 `SysUserImportRow`）补齐 DictFormat + Validation 便于手工验证

## 4. Verification

- [x] 4.1 单测或最小验证：内联 Dict 下拉 labels；NotBlank/Pattern 提示；默认 export 无约束；Lookup 缺失不失败；超长降级路径
- [x] 4.2 后端编译通过（`mvn -pl quickboot-common,quickboot-system -am compile` 或等价）
