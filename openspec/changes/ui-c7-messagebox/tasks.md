## 1. 模块骨架与默认配置

- [ ] 1.1 新建 **`quick-ui/src/packages/C7MessageBox/`**（入口文件与实现拆分方式与现有 **`packages`** 风格一致；无 **`.vue`**）
- [ ] 1.2 实现 **`setMessageBoxDefaults(config)`** 与模块级默认值存储；**浅合并**逻辑与 JSDoc 说明
- [ ] 1.3 实现 **`mergeOptions(perCall)`**（或等价私有辅助）：**`{ ...defaults, ...perCall }`**

## 2. EP 结果归一化与基础对话框

- [ ] 2.1 实现 **`normalizeMessageBoxResult`**（命名可调整）：将 **EP** **`resolve`** 的 **`MessageBoxData`** 转为 **`{ action, value? }`**；将用户取消类的 **reject** 转为 **`resolve`** 且 **`action`** 与 **EP** 约定一致（注释写明依赖的 **EP** 版本行为）
- [ ] 2.2 实现 **`c7Alert(message, title?, options?)`**
- [ ] 2.3 实现 **`c7Confirm(message, title?, options?)`**（无 **`asyncConfirm`** 时的同步确定路径）

## 3. `asyncConfirm` 与 `c7DangerConfirm`

- [ ] 3.1 **`c7Confirm`**：支持 **`options.asyncConfirm`**（**`() => Promise<unknown>`**）；**`confirmButtonLoading`** 优先，文档化回退 **`ElLoading.service`** 的条件与注释
- [ ] 3.2 处理中文案默认 **`处理中...`** 及可覆盖字段；成功关闭 / 失败保持打开 + **`errorNotify?.(err)`**；**不**默认 **`ElMessage`**
- [ ] 3.3 实现 **`c7DangerConfirm`**（**`type: 'warning'`** + 危险确认按钮 class，与当前 **EP** 版本对齐）

## 4. `c7Prompt` 与 `c7Loading`

- [ ] 4.1 实现 **`c7Prompt`**，校验相关 **仅透传** **EP** 字段
- [ ] 4.2 实现 **`c7Loading(text?, options?)`**，返回 **`{ close() }`**

## 5. 集成与构建校验

- [ ] 5.1 在 **`quick-ui/src/packages/index.js`** 增加 **`C7MessageBox`** 相关 **命名导出**（**不**加入 **`installPackages`**）
- [ ] 5.2 执行 **`pnpm -C quick-ui build:prod`**（或仓库约定的生产构建命令）确认无报错

## 6. 文档

- [ ] 6.1 新增 VitePress 页面 **`docs/docs/frontend/components/通用组件/c7-messagebox.md`**：API 表、**`setMessageBoxDefaults`** 示例、**`asyncConfirm` + `errorNotify`** 示例、与 **`main.ts`** 初始化说明
- [ ] 6.2 更新 **`docs/.vitepress/config/sidebar.ts`**「通用组件」分组，增加 **C7MessageBox** 导航项

## 7. 验证（按需）

- [ ] 7.1 若 **`quick-ui`** 已配置单元测试框架：为 **`normalizeMessageBoxResult`** / **`mergeOptions`** 增加 **mock EP** 用例；否则在本变更目录或文档中附 **手工验收清单**（覆盖 **`spec.md`** 中各 Scenario 要点）
