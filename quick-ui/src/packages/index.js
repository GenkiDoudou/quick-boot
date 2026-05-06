/**
 * 业务增强组件统一入口：导出组件并在应用上全局注册。
 */
import C7Button from './C7Button/index.vue'
import C7ButtonGroup from './C7ButtonGroup/index.vue'

export { C7Button, C7ButtonGroup }

/**
 * 注册 packages 内全局组件（名称与组件一致，如 C7Button）。
 *
 * @param {import('vue').App} app Vue 应用实例
 */
export function installPackages(app) {
  app.component('C7Button', C7Button)
  app.component('C7ButtonGroup', C7ButtonGroup)
}
