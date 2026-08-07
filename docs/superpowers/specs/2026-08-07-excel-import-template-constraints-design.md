# Excel 导入模板列约束（注解驱动提示）

日期：2026-08-07  
状态：已定稿（待实现）  
来源：头脑风暴；复用 `@ExcelDictFormat` 与 Jakarta Validation；依赖既有 `ExcelUtils` / EasyExcel 3.3.4

## 背景与目标

下载导入模板时，希望在列上体现填写规则（如性别仅男/女、手机号格式、日期格式、非空等），降低填错成本。

EasyExcel 本身无「列规则」开箱注解；可通过 `SheetWriteHandler` + Apache POI `DataValidation` / 输入提示写入。Excel 端能力有限（尤其不支持完整 Java 正则），且可被粘贴绕过，**不能**替代服务端导入校验。

本设计在**不新增规则专用注解族**的前提下：扫描 `*ImportRow` 上已有的 `@ExcelDictFormat` 与 Jakarta Validation，自动投影为模板下拉与输入提示。

### 已确认决策

| 项 | 选择 |
| --- | --- |
| 阶段 | 先定稿设计；实现另开计划 |
| 方案 | 注解 + 公共 `SheetWriteHandler`（方案 B） |
| 规则来源 | 复用 `@ExcelDictFormat` + Jakarta Validation，不新建 `@ExcelDropdown` / `@ExcelPattern` |
| 覆盖 | 能力对所有导入模板入口可复用；业务 `*ImportRow` 按需补注解 |
| 规则类型 | 下拉 + 非空提示 + 日期/手机号等格式提示（`@Pattern` 等） |
| Excel 严格度 | 提示为主（输入提示/批注）；不硬拦非法输入 |
| 权威校验 | 仍在导入：`ExcelDictConvert` + `ExcelListenerCallback` / `ValidatorUtils` |

### 非目标

- 新建一套模板规则注解
- Excel 端用公式完整复刻 Java `@Pattern`
- 强制拒绝非法输入（`showErrorBox` 硬拦）
- 改造 `exportTemplate(classpath 静态模板)` 与注解混用
- 默认给普通业务数据导出加下拉
- 前端改动、异步导入导出中心
- 本版强制改完所有 `*ImportRow` 字段注解（能力就绪即可；业务按需补）

## 架构与职责

```text
ImportRow 字段注解（单一真相）
  ├─ @ExcelDictFormat     → 字典转换（已有）+ 模板下拉 labels（本能力消费）
  └─ @NotBlank/@Pattern… → 导入 Bean Validation（已有）+ 模板提示文案（本能力消费）

ExcelUtils.exportExcel(..., applyTemplateConstraints=true)
  └─ TemplateConstraintWriteHandler
        ├─ 扫 @ExcelProperty 列序
        ├─ DataValidation：显式列表（dict labels）
        └─ 输入提示 / 批注：必填、格式说明

导入（主路径不变）
  ExcelDictConvert + ExcelListenerCallback → ValidatorUtils
```

| 组件 | 做 | 不做 |
| --- | --- | --- |
| `@ExcelDictFormat` | 继续只管 value↔label；模板侧只读 labels 做下拉 | 不新增「模板校验」语义字段 |
| Validation 注解 | 继续服务端校验；模板侧投影为提示 | 不要求 Excel 执行完整 Java 正则 |
| `TemplateConstraintWriteHandler` | 写 POI 下拉与 prompt | 不替代导入校验、不改 Converter |
| 业务 `*ImportRow` | 按需补 DictFormat / Validation | 不手写各业务 Handler |

与 [Excel 字典转换设计](./2026-08-07-excel-dict-format-design.md) 的关系：本能力是字典设计的**旁路消费**（取 labels），不修改 `missPolicy` 与双向转换契约。依赖 `DictLookup` / 内联 `dictText` 解析取标签列表。

## 注解 → Excel 规则映射

**扫描范围**：仅带 `@ExcelProperty` 的字段；列索引优先 `index`，否则声明序。数据行默认第 2～N 行（N 建议默认 2000，可配置）。

| 注解 / 线索 | Excel 端行为 | 提示文案来源 | 备注 |
| --- | --- | --- | --- |
| `@ExcelDictFormat` | **显式下拉**：选项 = 字典 **labels** | 可选「请从下拉选择」 | `dictText` 或 `dictType`→`DictLookup`；多值分隔时仍提供单值下拉引导，提示说明用 separator 拼接 |
| `@NotBlank` / `@NotNull` | **输入提示/批注**：标明必填 | `message`，缺省「该列不能为空」 | 不写强制非空 DataValidation |
| `@Pattern` | **输入提示**：格式要求 | 优先 `message`；可附带 regexp 缩略 | **不**把 Java 正则写入 Excel 自定义公式 |
| `@Size` / `@Length` | 提示长度；可选 POI 文本长度软约束 | `message` 或生成长度范围说明 | 有 max 时长度约束最有用 |
| `@Email` | 提示填写邮箱 | `message` | 不做完整邮箱公式 |
| `LocalDate` / `Date` 或日期类 `@Pattern` | 提示日期格式（如 `yyyy-MM-dd`） | `message` 或约定默认说明 | 可选单元格日期格式；不承诺硬拦 |
| 手机号 | 同 `@Pattern` | 业务挂 `@Pattern` + `message` | 不单独发明手机号注解 |
| 无上述注解 | 不写约束 | — | 与现状一致 |

**组合规则**

1. 同一字段可同时有 Dict + Validation：下拉与提示并存。
2. 多条 Validation：提示按「必填 → 格式/长度 → 其它」拼接，分号分隔。
3. `dictType` 且 `DictLookup` 未就绪：跳过该列下拉并 warn，导出仍成功；提示可写「字典未就绪，请按标签填写」。
4. 下拉选项过长（POI 显式列表上限）：隐藏 sheet + 公式列表，或降级为「仅提示、无下拉」并 warn；**禁止静默截断**。

## 导出流程与 API

### 触发入口

| 入口 | 启用 Handler | 说明 |
| --- | --- | --- |
| 导入模板下载（空列表 + `*ImportRow`） | **是** | 主场景 |
| 普通业务数据导出 | **默认否** | 避免误加下拉 |
| `exportTemplate(classpath 静态文件)` | **本版不做** | 与注解扫描不混用 |

### API 约定

- 现有 `exportExcel(list, sheetName, clazz, response)` **保持兼容**，默认 `applyTemplateConstraints=false`。
- 增加可选开关（重载或布尔参数 `applyTemplateConstraints`）。
- 各业务 `import/template` 调用处传 `true`；普通 `export` 不动。

### 处理流程

```text
1. EasyExcel.write(os, ImportRow.class)
2. registerConverter（既有 BigNumber / DictConvert）
3. applyTemplateConstraints=true 时 registerWriteHandler(TemplateConstraintWriteHandler)
4. doWrite(list) → 写出表头（模板多为空列表）
5. Handler：扫字段 → 下拉 + 输入提示/批注
6. 响应输出 xlsx
```

## 错误与边界

| 场景 | 行为 |
| --- | --- |
| 无 Dict、无 Validation | 不写任何约束 |
| Lookup 未注册 + `dictType` | 跳过下拉，warn；导出成功 |
| 下拉选项超长 | 隐藏 sheet 或降级仅提示，并 warn |
| `message` 为空 | 内置中文默认文案 |
| 粘贴非法值 / 清格 | 允许；导入时校验 |
| WPS / 旧版 Excel 提示差异 | 文档注明；不做多端特判 |
| 与字典 Converter 并存 | 互不干扰 |

## 模块与落地范围

```text
quickboot-common
  TemplateConstraintWriteHandler   （拟新增）
  注解扫描 / 提示文案组装辅助       （拟新增，保持小而专注）
  ExcelUtils                       增加开关并按需注册 Handler

quickboot-system（及后续模块）
  各 import/template 调用打开开关
  *ImportRow 按需补 @ExcelDictFormat / Validation
```

- **能力一次交付**于 common；业务注解补齐可渐进。
- 实现阶段验证：Dict 列有 label 下拉；`@NotBlank`/`@Pattern` 有提示；普通 export 默认无下拉；Lookup 缺失不拖垮导出；导入权威校验行为不变。

## 与「能否在模板列上加规则」的结论

| 规则诉求 | 结论 |
| --- | --- |
| 性别男/女等枚举 | **能**：`@ExcelDictFormat` → Excel 下拉（labels） |
| 非空 | **提示能**：`@NotBlank` → 输入提示；硬拦不做 |
| 日期格式 | **提示能**：类型/`@Pattern` → 提示；硬校验靠导入 |
| 手机号正则 | **提示能**：`@Pattern` → 提示；Excel 不执行完整 Java 正则 |

## 实现顺序（建议，供 writing-plans）

1. `TemplateConstraintWriteHandler`：列扫描 + 下拉 + 输入提示
2. `ExcelUtils` 增加 `applyTemplateConstraints` 开关
3. 对接 `@ExcelDictFormat` labels（内联 + Lookup）
4. 对接常用 Validation（NotBlank/NotNull/Pattern/Size/Email）
5. 各 `import/template` 打开开关；样例 `ImportRow` 补注解验证
6. 下拉超长降级与 Lookup 缺失路径验证
