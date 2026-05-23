# 样式管理

## 全局入口

`src/assets/styles/index.scss` 在 `main.js` 引入，包含：

- Element Plus 变量覆盖
- 布局（sidebar、navbar、tags-view）
- 与 `DESIGN.md` 对齐的 design token（颜色、圆角、间距）

## 组织方式

| 文件/目录 | 说明 |
|-----------|------|
| `_variables.module.scss` | SCSS 变量 |
| `element-ui.scss` | EP 组件微调 |
| `sidebar.scss`、`tags-view.scss` | 布局局部 |

页面级样式使用 `<style scoped lang="scss">`，避免污染全局。

## 主题

`src/utils/theme.js` + `settingsStore`：切换主题色，写入 CSS 变量。

## 规范要点（DESIGN.md）

- 主色、成功/警告/危险色使用 token，勿硬编码散落色值
- 列表页工具栏、查询区、表格区间距与 `config/index.vue` 一致
- 暗色模式跟随 Element Plus `dark/css-vars.css`

## 相关

- [开发规范](./development-guide)
- [列表页模板](./list-page-template)
