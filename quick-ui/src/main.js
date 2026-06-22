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
import { setupUserMonitor } from '@/monitor'
import { useDict } from '@/utils/dict'
import { parseTime, resetForm, addDateRange, handleTree, selectDictLabel, selectDictLabels } from '@/utils/ruoyi'
import { checkPermission } from '@/directive/permission/permissionUtils'
import * as validate from '@/utils/validate'

const app = createApp(App)

// 全局属性（向后兼容）
app.config.globalProperties.useDict = useDict
app.config.globalProperties.$validate = validate
app.config.globalProperties.parseTime = parseTime
app.config.globalProperties.resetForm = resetForm
app.config.globalProperties.handleTree = handleTree
app.config.globalProperties.addDateRange = addDateRange
app.config.globalProperties.selectDictLabel = selectDictLabel
app.config.globalProperties.selectDictLabels = selectDictLabels
app.config.globalProperties.checkPermission = checkPermission

app.use(store)
app.use(router)
const userMonitor = setupUserMonitor()
if (userMonitor) {
  app.use(userMonitor, { router })
}
app.use(plugins)
app.use(directive)

app.component('svg-icon', SvgIcon)
installPackages(app)

initMobileEnvironment()

app.mount('#app')
