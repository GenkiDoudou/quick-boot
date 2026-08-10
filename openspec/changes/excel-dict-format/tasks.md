## 1. Annotation & miss policy (common)

- [x] 1.1 完善 `@ExcelDictFormat`：`dictType`、`dictText`、`separator`、`missPolicy`
- [x] 1.2 新增 `DictMissPolicy` 枚举（KEEP / ERROR / EMPTY）

## 2. SPI & conversion core (common)

- [x] 2.1 新增 `DictLookup` 接口（`getLabel` / `getValue`）与 `DictLookupHolder`
- [x] 2.2 新增内联 `dictText` 解析工具（首个 `=` 分割；非法项 skip + warn）
- [x] 2.3 实现 `ExcelDictConvert`（`Converter<String>`）：无注解直通；导出 value→label；导入 label→value + 混填兼容；多值与 `missPolicy`
- [x] 2.4 `ExcelUtils` 全部 write/read 入口注册 `ExcelDictConvert`

## 3. System dict mounting

- [x] 3.1 实现 `SysDictLookup`（基于 `ISysDictDataService.listByType` 缓存，禁止逐格打库）
- [x] 3.2 `@Configuration` 启动时 `DictLookupHolder.set(SysDictLookup)`（system 已被 scan，与 OperLogPersist 一致，避免 imports 重复注册）

## 4. Verification

- [x] 4.1 补充单测或最小验证：内联单值/多值双向；KEEP/EMPTY/ERROR；无注解直通；Lookup 未注册 + dictType
- [x] 4.2 后端编译通过（`mvn -pl quickboot-common,quickboot-system -am compile` 或等价）
