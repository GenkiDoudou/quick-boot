import {download} from '@/utils/request'

export default {
  install(Vue) {
    Vue.config.globalProperties.$download = download
  }
}
