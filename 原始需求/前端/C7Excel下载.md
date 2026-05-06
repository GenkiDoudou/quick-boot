# C7Excel下载（C7ExcelDownload）原始需求

## 背景
- 导出接口通常返回 Blob，并可能在响应头中携带文件名；页面需要统一下载按钮与下载行为。

## 目标
- 提供一个下载按钮组件：
  - 点击后执行 `downloadFn`
  - 自动解析文件名并触发浏览器下载
  - 自动管理 downloading 状态与错误提示

## 功能需求
- `downloadFn()` 返回：
  - `Blob` 或 `{ data: Blob, headers }`
- 文件名解析优先级：
  1) `fileName` prop
  2) `Content-Disposition filename*=UTF-8''...`
  3) `Content-Disposition filename="..."`
  4) `defaultFileName`
- 下载行为：创建 objectURL + a 标签点击下载。
- 失败提示支持自定义 `notify(type,message)`。

## 事件
- `success(fileName)`
- `error(error)`

## 对外能力
- 暴露 `downloading` 状态给父组件。

## 验收标准
- 导出接口返回 Blob 时可成功下载；文件名解析正确。

