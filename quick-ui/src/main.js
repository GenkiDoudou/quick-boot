import { createApp } from 'vue'
import '@/assets/styles/mobile.scss'
import '@/assets/styles/index.scss'
// 命令式组件样式（Message / MessageBox / Loading 等）
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'
import 'element-plus/es/components/loading/style/css'
import 'element-plus/es/components/notification/style/css'

import App from './App'
import store from './store'
import router from './router'
import directive from './directive'
import plugins from './plugins'
import 'virtual:svg-icons-register'
import SvgIcon from '@/components/SvgIcon'
import { installPackages } from '@/packages'
import './permission'
import { initMobileEnvironment } from '@/utils/mobile'
import { setupLiteRum } from '@/monitor'
import { useDict } from '@/utils/dict'
import { checkPermission } from '@/directive/permission/permissionUtils'
import * as validate from '@/utils/validate'

const app = createApp(App)

// 全局属性（向后兼容；时间/树/字典工具请直接 import 对应 utils）
app.config.globalProperties.useDict = useDict
app.config.globalProperties.$validate = validate
app.config.globalProperties.checkPermission = checkPermission

app.use(store)
app.use(router)
const liteRum = setupLiteRum()
if (liteRum) {
  liteRum.bindRouter(router)
}
app.use(plugins)
app.use(directive)

app.component('svg-icon', SvgIcon)
installPackages(app)

initMobileEnvironment()

app.mount('#app')
