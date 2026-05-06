# C7Excel导入（C7ExcelUpload）原始需求

## 背景
- Excel 导入需要统一的文件选择、类型/大小校验、导入策略选择（覆盖/忽略），并在导入完成后展示结果与错误明细下载。

## 目标
- 提供一个导入组件：
  - 选择文件（仅 xls/xlsx）
  - 选择重复数据策略
  - 点击导入调用 `uploadFn(file,strategy)`
  - 展示导入统计与错误文件下载链接

## 功能需求
- 文件选择：
  - accept `.xls,.xlsx`
  - 校验扩展名与大小（`fileSize` MB）
  - 允许重复选择同一文件（选择后重置 input value）
- 策略：
  - `duplicateStrategy`：`overwrite|ignore`
  - 可配置文案 `overwriteLabel/ignoreLabel`
- 导入结果展示：
  - `total/successCount/failCount`
  - `failCount>0` 且 `errorFileUrl` 存在时展示错误文件下载入口
- 通知支持 `notify(type,message)` 替换默认 `ElMessage`。

## 事件
- `success(result)`
- `error(error)`

## 对外能力
- 暴露 `uploading` 与 `reset()`。

## 验收标准
- 上传成功后展示统计；失败记录存在时可下载错误明细文件。
- 非法格式/超出大小会被阻止并提示。

