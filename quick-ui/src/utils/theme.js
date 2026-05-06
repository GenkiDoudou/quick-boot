export function handleThemeStyle(theme) {
  const el = document.documentElement
  el.style.setProperty('--el-color-primary', theme)
}
