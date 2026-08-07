# Excel 字典转换（`@ExcelDictFormat`）

日期：2026-08-07  
状态：已定稿（待实现）  
来源：头脑风暴；参考现有骨架 `ExcelDictFormat` 与 `ExcelUtils` / `SysDictData` 缓存

## 背景与目标

为 Excel 导入/导出提供常规字典转换能力：字段标注后，导出将字典**值**写成**标签**，导入将**标签**还原为**值**。支持系统字典类型与注解内联映射；支持多值分隔；未匹配策略可配置。

### 已确认决策

| 项 | 选择 |
| --- | --- |
| 范围 | 仅 Excel（不做 API/VO Jackson 翻译） |
| 数据来源 | `dictType` 查系统字典；否则用内联 `dictText` |
| 方向 | 导出 value→label + 导入 label→value（双向） |
| 未匹配 | 注解可配 `KEEP` / `ERROR` / `EMPTY`，默认 `KEEP` |
| 多值 | 支持，分隔符可配，默认 `,` |
| 模块边界 | `common`：注解 + SPI + Converter；`system`：缓存实现并挂载 |
| 实现路径 | 全局注册 EasyExcel `Converter`（方案 A） |

### 非目标

- API/VO 自动字典翻译、响应增强注解
- 枚举类映射、非 `String` 字段字典转换
- 前端字典下拉封装
- 批量给现有业务 VO 补注解（本版只交付能力）
- 异步导入导出中心

## 架构

```text
quickboot-common
  @ExcelDictFormat
  DictMissPolicy            KEEP | ERROR | EMPTY
  DictLookup                SPI：getLabel / getValue
  DictLookupHolder          静态挂载，供 ExcelUtils 静态路径取实现
  ExcelDictConvert          EasyExcel Converter<String>
  ExcelUtils                全局 registerConverter(ExcelDictConvert)

quickboot-system
  SysDictLookup             基于 ISysDictDataService.listByType 缓存
  AutoConfiguration         启动时 DictLookupHolder.set(SysDictLookup)
```

## 注解契约

`@ExcelDictFormat`（字段级，`RUNTIME`）：

| 属性 | 含义 | 默认 |
| --- | --- | --- |
| `dictType` | 系统字典类型；非空则查 `DictLookup` | `""` |
| `dictText` | 内联项，形如 `"0=男","1=女"` | `{}` |
| `separator` | 多值分隔符 | `","` |
| `missPolicy` | 未匹配策略 | `KEEP` |

**解析优先级**

1. `dictType` 非空 → `DictLookup`（忽略同字段 `dictText`）
2. 否则 `dictText` 非空 → 本地解析内联表
3. 两者皆空 → 无字典映射，非空原文按 `missPolicy` 处理（通常等价 KEEP 原样）

**用法示意**

```java
@ExcelProperty("状态")
@ExcelDictFormat(dictType = "sys_normal_disable")
private String status;

@ExcelProperty("性别")
@ExcelDictFormat(dictText = {"0=男", "1=女", "2=未知"})
private String sex;
```

字段 Java 类型以 **String** 为准。

## 转换语义

### 导出（Java → Excel）：value → label

- 写出字典标签；blank 不查字典，保持空
- 多值：按 `separator` 拆分 → 逐项转换 → 同分隔符拼回
- `separator` 为空串：整格按单值处理

### 导入（Excel → Java）：label → value

- 按标签解析为字典键值写入字段；blank 保持空
- 多值规则同导出
- **混填兼容**：子项先按 label 查；查不到再按 value 判断是否已是合法键值；仍无则走 `missPolicy`（便于「导出再导入」或用户直接填 value）

### `missPolicy`（按子项）

| 策略 | 行为 |
| --- | --- |
| `KEEP` | 该项保留原文 |
| `EMPTY` | 该项视为空；多值拼接时去掉空段，避免多余分隔符 |
| `ERROR` | 抛 `ExcelDataCheckException`（或项目既有 Excel 校验异常），信息含字段名、`dictType`（若有）、原文；任一子项失败即失败 |

### 内联 `dictText`

- 每项 `"值=标签"`，仅第一个 `=` 为分隔（标签可含 `=`）
- 非法项（无 `=`）：构建映射时跳过并 warn，不拖垮整次导入导出

### 无注解 / 非字典字段

- 无 `@ExcelDictFormat`：Converter 检测到无注解后原样读写，不改变 EasyExcel 默认行为
- 全局 Converter 注册在 `String` 类型上，必须保证「无注解直通」

## SPI 与挂载

**`DictLookup`**

```text
String getLabel(String dictType, String value);  // 无映射返回 null
String getValue(String dictType, String label);  // 无映射返回 null
```

- 只服务系统字典；内联表不进 SPI
- 实现复用 `listByType` 缓存，禁止逐格打库
- 类型不存在或列表为空：查询返回 `null`，由 `missPolicy` 接手

**`DictLookupHolder`**

- `set` / `get`；system AutoConfiguration 启动时注入 `SysDictLookup`
- Converter 需要查库时从 Holder 取实现
- Holder 为空且字段使用了 `dictType`：
  - `ERROR`：明确提示字典服务未就绪
  - `KEEP` / `EMPTY`：按策略处理原文（不假装查到了映射）

**`ExcelUtils`**

- 所有 write / read 入口与现有 `ExcelBigNumberConvert` 并列注册 `ExcelDictConvert`

## 错误与边界

| 场景 | 行为 |
| --- | --- |
| 导入/导出 `missPolicy=ERROR` | 抛 Excel 校验类异常，中断当前操作 |
| `dictType` + `dictText` 同时声明 | 以 `dictType` 为准 |
| Lookup 未注册 + `dictType` | 见挂载节 |
| 仅内联、无 Spring | 可独立工作，不依赖 Holder |

## 验证要点

- 内联：单值 / 多值双向
- 系统字典：mock `DictLookup` 双向
- `KEEP` / `EMPTY` / `ERROR` 各至少一条
- 无注解 String、空白单元格不被误改
- Lookup 未注册 + `dictType` + 三种 `missPolicy`

## 实现顺序（建议）

1. 完善注解与 `DictMissPolicy`
2. `DictLookup` + Holder + 内联解析工具 + `ExcelDictConvert`
3. `ExcelUtils` 注册 Converter
4. `SysDictLookup` + AutoConfiguration 挂载
5. 单测或最小手工验证用例
