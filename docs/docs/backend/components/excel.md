# excel

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.excel`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/excel/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| CellMerge | excel/annotation/CellMerge.java | /** * 标记导出合并列。 */ |
| ExcelDictFormat | excel/annotation/ExcelDictFormat.java | /** * Excel 字典翻译注解：导出 value→label，导入 label→value。 * * &lt;p&gt;优先级：非空 &#123;@link #dictType()} 走系统字典；否则用 &#123;@link #dictText()} 内联映射。  |
| ExcelBigNumberConvert | excel/conver/ExcelBigNumberConvert.java | /** * Long 型大数 Excel 转换器：超过 15 位以字符串写入，避免 Excel 精度丢失。 */ |
| ExcelDictConvert | excel/conver/ExcelDictConvert.java | /** * EasyExcel 全局 String 字典转换：仅当字段带 &#123;@link ExcelDictFormat} 时生效，否则直通。 */ |
| CellMergeStrategy | excel/conver/merge/CellMergeStrategy.java | /** * 基于 &#123;@link CellMerge} 注解的单元格合并策略。 */ |
| DictConvertEngine | excel/dict/DictConvertEngine.java | /** * Excel 字典双向转换引擎（导出 value→label，导入 label→value）。 */ |
| DictLookup | excel/dict/DictLookup.java | /** * 系统字典查询 SPI（仅服务 &#123;@code dictType}；内联映射不走此接口）。 */ |
| DictLookupHolder | excel/dict/DictLookupHolder.java | /** * &#123;@link DictLookup} 静态挂载点，供静态 &#123;@code ExcelUtils} / Converter 取实现。 */ |
| DictMissPolicy | excel/dict/DictMissPolicy.java | /** * Excel 字典未匹配策略。 */ |
| DictTextParser | excel/dict/DictTextParser.java | /** * 解析 &#123;@code @ExcelDictFormat#dictText()} 内联项（首个 &#123;@code =} 为分隔）。 */ |
| ExcelUtils | excel/ExcelUtils.java | /** * Excel 导入导出工具：封装 EasyExcel 读写、字典/大数转换、模板填充与 HTTP 下载响应头。 */ |
| ExcelDataCheckException | excel/exception/ExcelDataCheckException.java | /** * Excel 数据校验异常。 */ |
| ExcelException | excel/exception/ExcelException.java | /** * Excel 通用异常。 */ |
| DefaultExcelResult | excel/listener/DefaultExcelResult.java | /** * 默认 Excel 读取结果。 */ |
| ExcelListener | excel/listener/ExcelListener.java | /** * 统一 Excel 监听器接口。 */ |
| ExcelResult | excel/listener/ExcelResult.java | /** * Excel 读取结果。 */ |
| DictLabelResolver | excel/template/DictLabelResolver.java | /** * 从 &#123;@link ExcelDictFormat} 解析模板下拉用的标签列表。 */ |
| ExcelPropertyColumnScanner | excel/template/ExcelPropertyColumnScanner.java | /** * 扫描类上带 &#123;@link ExcelProperty} 的字段并解析列序。 * * &lt;p&gt;规则：显式 &#123;@code index &gt;= 0} 优先；未指定 index 的字段按声明序填入空闲列号。 */ |
| TemplateConstraintWriteHandler | excel/template/TemplateConstraintWriteHandler.java | /** * 导入模板列约束写入：按 &#123;@link ExcelDictFormat} 生成下拉，按 Validation 生成输入提示。 * * &lt;p&gt;严格度：提示为主（showPromptBox），不强制拒绝非法输入。 */ |
| ValidationPromptBuilder | excel/template/ValidationPromptBuilder.java | /** * 根据 Jakarta Validation（及 Hibernate &#123;@link Length}）组装 Excel 输入提示文案。 * * &lt;p&gt;拼接顺序：必填 → 格式/长度 → 其它。 */ |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
