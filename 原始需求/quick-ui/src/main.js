import { createApp } from 'vue'
import Cookies from 'js-cookie'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import locale from 'element-plus/es/locale/lang/zh-cn'
import '@/assets/styles/mobile.scss'
import '@/assets/styles/index.scss'

import App from './App'
import store from './store'
import router from './router'
import directive from './directive'
import plugins from './plugins'
import 'virtual:svg-icons-register'
import SvgIcon from '@/components/SvgIcon'
import './permission'
import { initMobileEnvironment } from '@/utils/mobile'
import installPackages from '@/packages'

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
app.use(plugins)
app.use(directive)
app.use(installPackages)
app.component('svg-icon', SvgIcon)

// Element Plus 配置
app.use(ElementPlus, {
  locale: locale,
  size: Cookies.get('size') || 'default',
  zIndex: 2000
})

// 初始化移动端环境
initMobileEnvironment()

app.mount('#app')
