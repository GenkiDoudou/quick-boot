## Why

当前项目已在多个模块零散实现 Excel 导入导出能力，存在重复实现、错误处理不一致、策略语义不统一、业务接入成本高的问题。随着字典模块等场景持续扩展，需要在 `quickboot-common` 建立统一、可复用且可扩展的 Excel 基础能力。

## What Changes

- 在 `quickboot-common` 建立统一 Excel 工具体系，覆盖导入监听、校验、模板、导出、字典翻译、失败明细、合并策略等完整能力。
- 统一导入策略模型（如 `IGNORE` / `OVERWRITE`）与导入上下文模型，规范工具层与业务层协作边界。
- 提供标准化失败明细输出（错误行号、错误列、错误原因）与失败文件生成能力。
- 提供可插拔的逐行回调机制，支持业务层定义重复语义与领域校验规则。
- 以字典模块作为首个接入样板，验证共性能力可复用性并保持对外接口兼容。
- 明确工具层不负责批量写库，批量策略由业务层按场景实现。

## Capabilities

### New Capabilities
- `common-excel-tooling`: 提供统一的 Excel 导入导出基础设施（监听器、校验、策略、模板、失败明细、字典翻译、合并策略、扩展回调）。

### Modified Capabilities
- `common-response-paging`: 导入类接口返回结构补充失败文件与失败明细语义（在统一 `R` 响应契约下扩展导入结果字段约定）。

## Impact

- 影响后端模块：`quickboot-common`、`quickboot-web`（字典模块首批接入）。
- 影响代码分层：新增/调整 `excel/core|model|listener|annotation|convert|merge|exception` 等公共包结构。
- 影响接口行为：导入接口在原有统计基础上新增失败明细文件相关字段与统一策略语义。
- 影响测试范围：需要新增 common 单测与 web 集成测试，覆盖解析异常、validation、业务校验、失败明细生成与策略分支。
