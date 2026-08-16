/**
 * 文件下载插件（$download）：将 utils/request 的 download 挂载为全局方法。
 */
import {download} from '@/utils/request'

export default {
  install(Vue) {
    Vue.config.globalProperties.$download = download
  }
}
