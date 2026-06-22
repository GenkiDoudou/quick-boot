/**
 * 业务增强组件统一入口：导出组件并在应用上全局注册。
 */
import C7Button from './C7Button/index.vue'
import C7ButtonGroup from './C7ButtonGroup/index.vue'
import C7Select from './C7Select/index.vue'
import C7Pagination from './C7Pagination/index.vue'
import C7Card from './C7Card/index.vue'
import C7Copy from './C7Copy/index.vue'
import C7Checkbox from './C7Checkbox/index.vue'
import C7Radio from './C7Radio/index.vue'
import C7Switch from './C7Switch/index.vue'
import C7DictTag from './C7DictTag/index.vue'
import C7Dialog from './C7Dialog/index.vue'
import C7Descriptions from './C7Descriptions/index.vue'
import C7DatePicker from './C7DatePicker/index.vue'
import C7TimePicker from './C7TimePicker/index.vue'
import C7Title from './C7Title/index.vue'
import C7TreeSelect from './C7TreeSelect/index.vue'
import C7Cascader from './C7Cascader/index.vue'
import C7Watermark from './C7Watermark/index.vue'
import C7Preview from './C7Preview/index.vue'
import C7JsonTableColumn from './C7JsonTableColumn/index.vue'
import C7JsonTable from './C7JsonTable/index.vue'
import C7CardGrid from './C7CardGrid/index.vue'
import C7ExcelDownload from './C7ExcelDownload/index.vue'
import C7ExcelUpload from './C7ExcelUpload/index.vue'
import C7Upload from './C7Upload/index.vue'
import {
  setMessageBoxDefaults,
  c7Confirm,
  c7Alert,
  c7Prompt,
  c7DangerConfirm,
  c7Loading,
  mergeMessageBoxOptions,
  mapMessageBoxResolve,
  mapMessageBoxReject,
  splitTitleAndOptions,
} from './C7MessageBox/index.js'

export {
  C7Button,
  C7ButtonGroup,
  C7Select,
  C7Pagination,
  C7Card,
  C7Copy,
  C7Checkbox,
  C7Radio,
  C7Switch,
  C7DictTag,
  C7Dialog,
  C7Descriptions,
  C7DatePicker,
  C7TimePicker,
  C7Title,
  C7TreeSelect,
  C7Cascader,
  C7Watermark,
  C7Preview,
  C7JsonTableColumn,
  C7JsonTable,
  C7CardGrid,
  C7ExcelDownload,
  C7ExcelUpload,
  C7Upload,
  setMessageBoxDefaults,
  c7Confirm,
  c7Alert,
  c7Prompt,
  c7DangerConfirm,
  c7Loading,
  mergeMessageBoxOptions,
  mapMessageBoxResolve,
  mapMessageBoxReject,
  splitTitleAndOptions,
}

/**
 * 注册 packages 内全局组件（名称与组件一致，如 C7Button）。
 *
 * @param {import('vue').App} app Vue 应用实例
 */
export function installPackages(app) {
  app.component('C7Button', C7Button)
  app.component('C7ButtonGroup', C7ButtonGroup)
  app.component('C7Select', C7Select)
  app.component('C7Pagination', C7Pagination)
  app.component('C7Card', C7Card)
  app.component('C7Copy', C7Copy)
  app.component('C7Checkbox', C7Checkbox)
  app.component('C7Radio', C7Radio)
  app.component('C7Switch', C7Switch)
  app.component('C7DictTag', C7DictTag)
  app.component('C7Dialog', C7Dialog)
  app.component('C7Descriptions', C7Descriptions)
  app.component('C7DatePicker', C7DatePicker)
  app.component('C7TimePicker', C7TimePicker)
  app.component('C7Title', C7Title)
  app.component('C7TreeSelect', C7TreeSelect)
  app.component('C7Cascader', C7Cascader)
  app.component('C7Watermark', C7Watermark)
  app.component('C7Preview', C7Preview)
  app.component('C7JsonTableColumn', C7JsonTableColumn)
  app.component('C7JsonTable', C7JsonTable)
  app.component('C7CardGrid', C7CardGrid)
  app.component('C7ExcelDownload', C7ExcelDownload)
  app.component('C7ExcelUpload', C7ExcelUpload)
  app.component('C7Upload', C7Upload)
}
