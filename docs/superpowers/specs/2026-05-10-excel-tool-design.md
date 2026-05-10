# Excel 工具类设计方案（quickboot-common）

## 1. 目标与边界

### 1.1 目标
1. 对标 RuoYi/quick-boot 的完整 Excel 能力：监听器、校验、合并、模板、字典翻译、失败明细、策略模型。
2. 能力统一下沉至 `quickboot-common`，供后续所有业务模块复用。

### 1.2 边界
1. 工具层不负责批量写库，由业务层决定并实现。
2. 重复语义不在工具层硬编码，由业务层定义（如库内重复、文件内重复、组合唯一等）。
3. 继续沿用当前技术路线：`EasyExcel + Jakarta Validation`。

## 2. 模块结构设计

建议在 `quickboot-common/src/main/java/io/github/genkidoudou/common/excel` 下按职责拆分：

1. `core`
- `EasyExcelSupport`：读写入口、响应输出、模板输出、字节流输出。
- `ExcelImportEngine`：统一导入执行器（策略驱动）。

2. `model`
- `ExcelImportContext`：导入上下文（策略、扩展参数、校验开关等）。
- `ExcelImportStrategy`：统一策略枚举（如 `IGNORE` / `OVERWRITE`）。
- `ExcelImportResult`：总数、成功数、失败数、失败行号、失败文件等。
- `ExcelImportErrorRow`：错误行号、错误列、错误原因。

3. `listener`
- `ExcelListener<T>`：监听器统一接口。
- `DefaultExcelListener<T>`：默认监听器（转换异常、validation 异常收集）。
- `DefaultExcelListenerCallback<T>`：逐行回调监听器。

4. `annotation`
- `ExcelDictFormat`：字典翻译注解（已存在，继续使用）。
- `CellMerge`：合并列注解。

5. `convert`
- `ExcelDictConverter`：字典值/标签双向转换。

6. `merge`
- `CellMergeStrategy`：按注解进行纵向合并。

7. `exception`
- `ExcelDataCheckException`：业务校验异常。
- `ExcelImportFieldException`：字段级错误（用于精确列名错误）。

## 3. 导入流程设计

1. 读取 `MultipartFile`，执行 `readSheetResult`。
2. Listener 层处理：
- 单元格类型转换异常。
- Bean Validation 异常（你确认采用 validation + 业务校验并行）。
3. 对每行调用业务 `RowHandler<T>`。
4. 业务回调只做规则判断与分类，不做工具层批量写库。
5. 引擎统一聚合结果并生成失败明细文件（可选）。

## 4. 导出流程设计

1. 普通导出：响应流写出。
2. 模板导出：仅表头。
3. 字典翻译导出：借助 `ExcelDictConverter`。
4. 合并导出：注册 `CellMergeStrategy`。

## 5. 职责分层

### 5.1 工具层职责（common）
1. Excel 解析、监听、基础校验。
2. 错误聚合、失败明细生成。
3. 统一策略模型与导出能力。

### 5.2 业务层职责（web）
1. 重复语义定义。
2. 新增/更新分类后的落库策略（单条/批量/分片）。
3. 业务字段映射与领域异常语义。

## 6. 关键接口（语义）

1. `ExcelImportEngine.importByCallback(...)`
- 入参：`file`、`rowClass`、`context`、`rowHandler`、`columnLabelMapper`、`blankChecker`
- 出参：`ExcelImportResult`

2. `RowHandler<T>`
- 入参：`row`、`analysisContext`、`stats`
- 约定：可抛 `ExcelImportFieldException(field, message)`。

3. `ExcelImportContext`
- `strategy`：`IGNORE` / `OVERWRITE`
- `validate`：是否启用 Bean Validation
- `ext`：扩展参数（如 `dictType`）
- `errorFileName`：失败文件名

## 7. 错误处理与失败明细

1. 解析异常：记录行号、列号、表头、异常原因。
2. validation 异常：记录行号和校验消息。
3. 业务异常：
- 抛 `ExcelImportFieldException` 标注字段。
- 通过 `columnLabelMapper` 映射中文列名。
4. 失败明细统一 sheet：`导入失败明细`，字段为：
- `错误行号`
- `错误列`
- `错误原因`

## 8. 字典模块接入策略

1. `dict type` / `dict data` 作为首个接入样板。
2. Service 保留：
- 行级规则判断。
- 行分类（新增/更新/失败）。
- 导入完成后业务后处理（如刷新缓存）。
3. 与现有对外接口保持兼容。

## 9. 测试策略

### 9.1 common 单测
1. listener 校验路径：类型转换、validation、业务异常。
2. `ExcelImportResult` 聚合正确性。
3. 失败明细文件生成正确性。
4. 字典转换器双向一致性。
5. 合并策略边界行为。

### 9.2 web 集成测试
1. `IGNORE` / `OVERWRITE` 策略分支。
2. 失败明细中行号/列/原因准确。
3. 模板下载与导出链路可用。

## 10. 迁移与落地顺序

1. 在 `quickboot-common` 固化完整工具能力。
2. 字典模块完成接入与回归验证。
3. 逐步迁移其它业务模块导入导出。
4. 过渡期可保留旧方法并标记废弃，迁移完成后统一收敛。

## 11. 验收标准

1. `quickboot-common` 提供完整可复用 Excel 导入导出工具。
2. 字典模块接入后满足既有功能与新增要求（失败明细、字段级错误列、策略模型）。
3. 工具层不做批量写库，业务层可自由实现批量策略。
4. 能力文档齐全，后续业务可按模板快速接入。
