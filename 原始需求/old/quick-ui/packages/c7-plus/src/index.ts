import type { App } from 'vue'
import C7Button from './components/c7-button/index.vue'
import C7ButtonGroup from './components/c7-button-group/index.vue'
import C7Card from './components/c7-card/index.vue'
import C7Cascader from './components/c7-cascader/index.vue'
import C7Checkbox from './components/c7-checkbox/index.vue'
import C7DatePicker from './components/c7-date-picker/index.vue'
import C7Dialog from './components/c7-dialog/index.vue'
import C7DictTag from './components/c7-dict-tag/index.vue'
import C7JsonForm from './components/c7-json-form/index.vue'
import C7JsonTable from './components/c7-json-table/index.vue'
import C7JsonTableColumn from './components/c7-json-table-column/index.vue'
import C7Layer from './components/c7-layer/index.vue'
import C7Preview from './components/c7-preview/index.vue'
import C7Radio from './components/c7-radio/index.vue'
import C7Select from './components/c7-select/index.vue'
import C7SwitchForm from './components/c7-switch-form/index.vue'
import C7Title from './components/c7-title/index.vue'
import C7TreeSelect from './components/c7-tree-select/index.vue'
import C7Upload from './components/c7-upload/index.vue'
import C7Descriptions from './components/c7-descriptions/index.vue'
import C7Switch from './components/c7-switch/index.vue'
import C7TimePicker from './components/c7-time-picker/index.vue'
import C7Pagination from './components/c7-pagination/index.vue'
import C7Copy from './components/c7-copy/index.vue'
import C7Watermark from './components/c7-watermark/index.vue'

// 导出所有组件
export {
  C7Button,
  C7ButtonGroup,
  C7Card,
  C7Cascader,
  C7Checkbox,
  C7DatePicker,
  C7Dialog,
  C7DictTag,
  C7JsonForm,
  C7JsonTable,
  C7JsonTableColumn,
  C7Layer,
  C7Preview,
  C7Radio,
  C7Select,
  C7SwitchForm,
  C7Title,
  C7TreeSelect,
  C7Upload,
  C7Descriptions,
  C7Switch,
  C7TimePicker,
  C7Pagination,
  C7Copy,
  C7Watermark
}

// 导出类型
export * from './types/table'
export * from './types/form'

// 导出工具函数
export * from './utils/utils'
export * from './utils/errorHandler'
export * from './utils/logger'
export * from './hooks/useFetchOptions'

// 导出 Composables
export * from './composables/useDebounce'

// 导出常量
export * from './constants'

// 导出配置
export * from './config'

// 导出 C7MessageBox
export * from './components/c7-message-box/index'

// 组件列表
const components = [
  C7Button,
  C7ButtonGroup,
  C7Card,
  C7Cascader,
  C7Checkbox,
  C7DatePicker,
  C7Dialog,
  C7DictTag,
  C7JsonForm,
  C7JsonTable,
  C7JsonTableColumn,
  C7Layer,
  C7Preview,
  C7Radio,
  C7Select,
  C7SwitchForm,
  C7Title,
  C7TreeSelect,
  C7Upload,
  C7Descriptions,
  C7Switch,
  C7TimePicker,
  C7Pagination,
  C7Copy,
  C7Watermark
]

// 定义 install 方法
const install = (app: App) => {
  components.forEach(component => {
    app.component(component.name || component.__name, component)
  })
}

// 默认导出
export default {
  install
}

