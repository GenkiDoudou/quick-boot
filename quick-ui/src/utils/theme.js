/**
 * Element Plus 主题色注入：将主色写入 CSS 变量 `--el-color-primary`。
 *
 * @param {string} theme 十六进制颜色值
 */
export function handleThemeStyle(theme) {
  const el = document.documentElement
  el.style.setProperty('--el-color-primary', theme)
}
