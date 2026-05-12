## ADDED Requirements

### Requirement: 统一 Excel 导入执行模板
系统 SHALL 在 `quickboot-common` 提供统一导入执行模板，支持以监听器回调方式逐行处理数据，并统一聚合导入统计结果（总数、成功数、失败数、失败行号）。

#### Scenario: 使用回调执行导入
- **WHEN** 业务模块调用统一导入模板并提供 `RowHandler`
- **THEN** 系统按行触发回调处理并返回统一 `ExcelImportResult`

### Requirement: 导入校验采用 validation + 业务校验并行
系统 SHALL 支持导入行模型上的 Jakarta Validation 校验，并允许业务层在回调中补充跨行或跨库规则校验。

#### Scenario: 字段约束由 validation 拦截
- **WHEN** 导入行模型字段违反 `@NotBlank` 或 `@Size`
- **THEN** 系统记录该行失败并输出校验原因

#### Scenario: 业务规则由回调抛出字段级错误
- **WHEN** 业务回调发现重复语义冲突并抛出字段级异常
- **THEN** 系统记录失败并将错误列映射到失败明细

### Requirement: 失败明细标准化输出
系统 SHALL 统一生成失败明细数据，至少包含 `错误行号`、`错误列`、`错误原因`，并支持生成失败明细文件返回给调用方。

#### Scenario: 导入存在失败行
- **WHEN** 导入过程中出现任意失败记录
- **THEN** 返回结果包含失败统计并可生成失败明细文件

### Requirement: 导入策略模型统一
系统 SHALL 提供统一导入策略模型（至少支持 `IGNORE` 与 `OVERWRITE`），并将策略透传给业务回调执行分支逻辑。

#### Scenario: 策略为 IGNORE
- **WHEN** 业务回调判定目标记录已存在且策略为 `IGNORE`
- **THEN** 系统按业务规则返回跳过或失败，不强制覆盖

#### Scenario: 策略为 OVERWRITE
- **WHEN** 业务回调判定目标记录已存在且策略为 `OVERWRITE`
- **THEN** 系统允许业务执行更新路径

### Requirement: 导出能力统一编排
系统 SHALL 在公共工具层提供统一导出入口，支持普通导出、模板导出、字典翻译导出与合并单元格导出。

#### Scenario: 模板导出
- **WHEN** 业务模块调用模板导出接口
- **THEN** 系统输出仅包含表头的 Excel 文件

#### Scenario: 字典翻译导出
- **WHEN** 导出字段配置了字典翻译注解
- **THEN** 系统将字典值转换为字典标签后写入文件

#### Scenario: 合并导出
- **WHEN** 导出模型字段配置了合并注解
- **THEN** 系统按策略对重复值进行纵向合并

### Requirement: 工具层与业务层职责边界
系统 MUST NOT 在工具层执行数据库批量写库；业务模块 SHALL 在回调结果基础上自行决定写库方式（逐条、批处理、分片等）。

#### Scenario: 业务模块需要批量写库
- **WHEN** 业务模块希望提高导入写入性能
- **THEN** 业务模块可在导入完成后对新增/更新集合自行执行批量写库
