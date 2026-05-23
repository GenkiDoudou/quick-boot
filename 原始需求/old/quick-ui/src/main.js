import {createApp} from 'vue'

import Cookies from 'js-cookie'
import { appConfig } from '@/config/env'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import locale from 'element-plus/es/locale/lang/zh-cn'
import '@/assets/styles/index.scss' // global css

import App from './App'
import store from './store'
import router from './router'
import directive from './directive' // directive
// 导入C7组件库（直接从 c7-plus 引用）
import C7Components from '../packages/c7-plus/src/index.ts'
// 加载组件
// 注册指令
import plugins from './plugins' // plugins
import {download} from '@/utils/request'
// svg图标
import 'virtual:svg-icons-register'
import SvgIcon from '@/components/SvgIcon'
import elementIcons from '@/components/SvgIcon/svgicon'
import './permission' // permission control

import {createPinia} from "pinia";

// 分页组件
import Pagination from '@/components/Pagination'
// 自定义表格工具组件
import RightToolbar from '@/components/RightToolbar'
// 富文本组件
import Editor from "@/components/Editor"
// 文件上传组件
import FileUpload from "@/components/FileUpload"

// 图片预览组件
import ImagePreview from "@/components/ImagePreview"
// 自定义树选择组件
import TreeSelect from '@/components/TreeSelect'
// 字典标签组件
import DictTag from '@/components/DictTag'

import formLayout from '@/components/form-layout/index.vue'

// 为了向后兼容，保留全局属性挂载（推荐使用 composables）
// 推荐使用方式：
// - useDict() 替代 this.useDict / proxy.useDict
// - useUtils() 替代 this.parseTime / proxy.parseTime 等
// - useModal() 替代 this.$modal / proxy.$modal
// - useTab() 替代 this.$tab / proxy.$tab
// - useAuth() 替代 this.$auth / proxy.$auth
import {useDict} from '@/utils/dict'
import {parseTime, resetForm, addDateRange, handleTree, selectDictLabel, selectDictLabels} from '@/utils/ruoyi'
import { checkPermission } from './directive/permission/permissionUtils'
import * as validate from '@/utils/validate'

const app = createApp(App)

// ⚠️ 注意：以下全局属性挂载仅用于向后兼容
// 新代码请使用 composables（见 src/composables/index.ts）
app.config.globalProperties.useDict = useDict
app.config.globalProperties.$validate = validate
app.config.globalProperties.download = download
app.config.globalProperties.parseTime = parseTime
app.config.globalProperties.resetForm = resetForm
app.config.globalProperties.handleTree = handleTree
app.config.globalProperties.addDateRange = addDateRange
app.config.globalProperties.selectDictLabel = selectDictLabel
app.config.globalProperties.selectDictLabels = selectDictLabels
app.config.globalProperties.checkPermission = checkPermission

// 全局组件挂载
app.component('DictTag', DictTag)
app.component('Pagination', Pagination)
app.component('TreeSelect', TreeSelect)
app.component('FileUpload', FileUpload)
app.component('ImagePreview', ImagePreview)
app.component('RightToolbar', RightToolbar)
app.component('Editor', Editor)
app.component('formLayout', formLayout)

const pinia = createPinia();
app.use(pinia); // 注册 Pinia




app.use(router)
app.use(store)
app.use(plugins)
app.use(directive)  // 注册自定义指令
app.use(elementIcons)
app.use(C7Components)
app.component('svg-icon', SvgIcon)



// 使用element-plus 并且设置全局的大小
app.use(ElementPlus, {
    locale: locale,
    // 支持 large、default、small
    size: Cookies.get('size') || 'default'
})

// 更新加载提示文字
const loadingElement = document.getElementById('loading-text')
if (loadingElement) {
  loadingElement.textContent = appConfig.loadingText
}

app.mount('#app')
