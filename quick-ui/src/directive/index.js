/**
 * 全局自定义指令注册：挂载 v-hasRole、v-hasPermi 权限指令。
 *
 * @param {import('vue').App} app Vue 应用实例
 */
import hasRole from './permission/hasRole'
import hasPermi from './permission/hasPermi'

export default function directive(app) {
  app.directive('hasRole', hasRole)
  app.directive('hasPermi', hasPermi)
}
