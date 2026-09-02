/**
 * CRUD 列表 / 表单 composable：Tier A schema 驱动页共用状态与标准交互。
 */
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 列表页基础能力：tableRef、详情弹窗、刷新。
 *
 * @returns {{ tableRef: import('vue').Ref, detailVisible: import('vue').Ref, detail: import('vue').Ref, refreshTable: Function, openDetailFromRow: Function, openDetailFromApi: Function }}
 */
export function useCrudListPage() {
  const tableRef = ref(null)
  const detailVisible = ref(false)
  const detail = ref(null)

  /** 触发表格重新拉取当前页数据 */
  function refreshTable() {
    tableRef.value?.refreshData?.()
  }

  /** 详情直接使用行快照（只读日志类页面） */
  function openDetailFromRow(row) {
    detail.value = row ? { ...row } : null
    detailVisible.value = true
  }

  /**
   * 详情走后端 get 接口。
   *
   * @param {object} row 当前行
   * @param {(id: string|number) => Promise} getFn API get
   * @param {string} idField 主键字段
   */
  async function openDetailFromApi(row, getFn, idField) {
    const id = row?.[idField]
    if (id == null || id === '') {
      ElMessage.warning('记录主键无效')
      return
    }
    const res = await getFn(id)
    detail.value = res?.data ?? res
    detailVisible.value = true
  }

  return {
    tableRef,
    detailVisible,
    detail,
    refreshTable,
    openDetailFromRow,
    openDetailFromApi
  }
}

/**
 * CRUD 表单弹窗：新增 / 编辑 / 单行删除确认。
 *
 * @param {object} options
 * @param {object} options.api 至少含 add/update/remove（remove 接收 id 数组）
 * @param {() => object} options.formInitial 表单初始值工厂
 * @param {object} [options.formRules] el-form rules
 * @param {string} options.idField 主键字段名
 * @param {string} [options.labelField] 删除确认展示字段
 * @param {(row: object) => boolean|void} [options.beforeRemove] 返回 false 阻止删除
 * @param {(row: object) => Promise<object>|object} [options.loadDetail] 编辑前拉详情；默认用行数据
 * @param {() => void} [options.onSaved] 保存成功后回调（通常 refreshTable）
 * @param {() => void} [options.onRemoved] 删除成功后回调
 */
export function useCrudForm(options) {
  const {
    api,
    formInitial,
    formRules = {},
    idField,
    labelField = idField,
    beforeRemove,
    loadDetail,
    onSaved,
    onRemoved
  } = options

  const formRef = ref(null)
  const formVisible = ref(false)
  const isAdd = ref(true)
  const form = reactive(formInitial())

  function resetForm() {
    Object.assign(form, formInitial())
  }

  function openAdd() {
    isAdd.value = true
    resetForm()
    formVisible.value = true
  }

  /**
   * 打开编辑弹窗；若提供 loadDetail 则异步填充表单。
   *
   * @param {object} row 列表行
   */
  async function openEdit(row) {
    isAdd.value = false
    if (loadDetail) {
      const data = await loadDetail(row)
      Object.assign(form, data)
    } else {
      Object.assign(form, row)
    }
    formVisible.value = true
  }

  /** 校验并提交新增/修改 */
  async function submitForm() {
    await formRef.value?.validate()
    if (isAdd.value) {
      const payload = { ...form }
      delete payload[idField]
      await api.add(payload)
    } else {
      await api.update({ ...form })
    }
    ElMessage.success('保存成功')
    formVisible.value = false
    onSaved?.()
  }

  /**
   * 单行删除（含确认框）；beforeRemove 可拦截内置参数等场景。
   *
   * @param {object} row
   */
  function removeRow(row) {
    if (beforeRemove?.(row) === false) {
      return
    }
    const label = row?.[labelField] ?? row?.[idField] ?? ''
    ElMessageBox.confirm(`确认删除「${label}」？`, '提示', { type: 'warning' })
      .then(() => api.remove([row[idField]]))
      .then(() => {
        ElMessage.success('删除成功')
        onRemoved?.()
      })
      .catch(() => {})
  }

  return {
    formRef,
    formVisible,
    isAdd,
    form,
    formRules,
    openAdd,
    openEdit,
    submitForm,
    removeRow,
    resetForm
  }
}

/**
 * 列表 + 表单一体 CRUD 页（Tier A 标准增删改）。
 *
 * @param {object} options useCrudForm 选项（api/formInitial/idField 等）
 */
export function useCrudPage(options) {
  const list = useCrudListPage()
  const form = useCrudForm({
    ...options,
    onSaved: () => {
      list.refreshTable()
      options.onSaved?.()
    },
    onRemoved: () => {
      list.refreshTable()
      options.onRemoved?.()
    }
  })
  return { ...list, ...form }
}

/**
 * 清空列表数据确认操作（日志类页面 toolbar）。
 *
 * @param {string} message 确认文案
 * @param {() => Promise} cleanFn 清空 API
 * @param {() => void} [afterClean] 成功后回调
 */
export function confirmCleanList(message, cleanFn, afterClean) {
  ElMessageBox.confirm(message, '提示', { type: 'warning' })
    .then(() => cleanFn())
    .then(() => {
      ElMessage.success('已清空')
      afterClean?.()
    })
    .catch(() => {})
}
