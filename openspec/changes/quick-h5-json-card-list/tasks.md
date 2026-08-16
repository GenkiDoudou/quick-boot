## 1. 组件

- [x] 1.1 新增 `QbCardColumn` 类型（可放在组件旁 `types` 或组件内 export）
- [x] 1.2 实现 `components/qb/QbJsonCardFields.vue`：按 columns 渲染 `qb-row` / `qb-col-*` / `qb-kv`
- [x] 1.3 支持 `type: text|dict|slot`、`showIfProp`、`emptyText`；span 映射到已有 col 类

## 2. 用户列表接入

- [x] 2.1 `pages/system/user/index.vue`：定义 `cardColumns`，meta 改用 `QbJsonCardFields`
- [x] 2.2 保持搜索、状态标签、actions 与权限逻辑不变

## 3. 验收

- [x] 3.1 对照现布局：部门/手机 span12+row；角色 span24+stack；邮箱 showIfProp
- [x] 3.2 更新产品设计文档状态为已实现（可选）
