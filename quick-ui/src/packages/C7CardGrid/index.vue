<template>
  <div class="c7-card-grid" v-bind="$attrs">
    <!-- 搜索区 -->
    <el-form
      v-if="searchColumns.length"
      class="c7-card-grid__search"
      :model="searchParam"
      label-width="auto"
      @submit.prevent="handleSearchSubmit"
      @keyup.enter="handleSearchSubmit"
    >
      <el-row :gutter="12">
        <el-col
          v-for="(col, idx) in sortedSearchColumns"
          :key="col.prop || col.label || idx"
          :span="col.span ?? 6"
        >
          <el-form-item :label="col.label" :prop="col.prop">
            <template v-if="col.type === 'input' || col.type === undefined || col.type === ''">
              <el-input
                v-model="searchParam[col.prop]"
                clearable
                v-bind="col.props || {}"
              />
            </template>
            <template v-else-if="col.type === 'select'">
              <C7Select
                v-model="searchParam[col.prop]"
                :data-list="col.dataList ?? col.options ?? []"
                clearable
                style="width: 100%"
                v-bind="col.props || {}"
              />
            </template>
            <template v-else-if="col.type === 'date'">
              <C7DatePicker
                v-model="searchParam[col.prop]"
                type="date"
                clearable
                style="width: 100%"
                v-bind="col.props || {}"
              />
            </template>
            <template v-else-if="col.type === 'daterange'">
              <C7DatePicker
                v-model="searchParam[col.prop]"
                type="daterange"
                clearable
                style="width: 100%"
                v-bind="col.props || {}"
              />
            </template>
            <template v-else-if="col.type === 'slot'">
              <slot :name="col.prop" :form-data="searchParam" :column="col" />
            </template>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-button
              v-if="showSearchButton"
              type="primary"
              v-bind="searchButtonProps"
              @click="handleSearchSubmit"
            >{{ searchButtonText }}</el-button>
            <el-button
              v-if="showResetButton"
              v-bind="resetButtonProps"
              @click="handleSearchReset"
            >{{ resetButtonText }}</el-button>
          </el-form-item>
        </el-col>
      </el-row>
      <slot name="search-extra" />
    </el-form>

    <!-- 工具栏 -->
    <el-row v-if="showToolbar" class="c7-card-grid__toolbar" :gutter="8" align="middle">
      <el-col :span="12">
        <slot
          name="toolbar-left"
          :search-param="searchParam"
          :refresh-data="refreshData"
          :get-data-list="getDataList"
        />
        <el-button
          v-if="showAddButtonResolved"
          type="primary"
          plain
          v-bind="addButtonProps"
          @click="handleBuiltInAddClick"
        >{{ addButtonText }}</el-button>
      </el-col>
      <el-col :span="12" style="text-align: right">
        <slot name="toolbar-right" :refresh-data="refreshData" />
        <el-button circle title="刷新" @click="refreshData">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </el-col>
    </el-row>

    <!-- 卡片网格 -->
    <div v-loading="listLoading" class="c7-card-grid__body" :style="gridStyle">
      <div
        v-if="showAddCardResolved"
        class="c7-card-grid__add-card"
        role="button"
        tabindex="0"
        @click="handleAddCardClick"
        @keyup.enter="handleAddCardClick"
      >
        <slot name="add-card">
          <el-icon class="c7-card-grid__add-icon"><Plus /></el-icon>
          <span class="c7-card-grid__add-text">{{ addCardText }}</span>
        </slot>
      </div>

      <div
        v-for="row in cardRows"
        :key="row[rowKey]"
        class="c7-card-grid__item"
      >
        <slot
          name="card"
          :row="row"
          :refresh-data="refreshData"
          :get-data-list="getDataList"
        />
      </div>

      <div v-if="!listLoading && !cardRows.length && !showAddCardResolved" class="c7-card-grid__empty">
        <el-empty :description="emptyText" />
      </div>
    </div>

    <div class="c7-card-grid__pagination">
      <C7Pagination
        v-model:current-page="currentPage"
        v-model:page-size="currentPageSize"
        :total="total"
        :page-sizes="pageSizes"
        :disabled="listLoading"
        @change="onPaginationChange"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { cloneDeep, get } from '@/utils/object'
import C7Pagination from '../C7Pagination/index.vue'
import C7Select from '../C7Select/index.vue'
import C7DatePicker from '../C7DatePicker/index.vue'
import { checkPermission } from '@/directive/permission/permissionUtils'
import useUserStore from '@/store/modules/user'

defineOptions({ name: 'C7CardGrid', inheritAttrs: false })

/**
 * 卡片网格列表壳：搜索区 + 可选工具栏 + 卡片网格 + 分页。
 * 数据请求与分页逻辑对齐 {@link C7JsonTable}，展示层由 `#card` 插槽自定义。
 *
 * @prop {function(object): Promise<unknown>} listFunction 列表请求函数
 */
const props = defineProps({
  listFunction: { type: Function, required: true },
  searchColumns: { type: Array, default: () => [] },
  defaultSearchParam: { type: Object, default: () => ({}) },
  rowsKey: { type: String, default: 'data.records' },
  totalKey: { type: String, default: 'data.total' },
  rowKey: { type: String, default: 'id' },
  pageSizes: { type: Array, default: () => [12, 24, 48] },
  defaultPageSize: { type: Number, default: 12 },
  emptyText: { type: String, default: '暂无数据' },
  /** 网格最小列宽（px），用于 auto-fill */
  minCardWidth: { type: Number, default: 260 },
  /** 卡片间距（px） */
  gridGap: { type: Number, default: 16 },
  showToolbar: { type: Boolean, default: true },
  showSearchButton: { type: Boolean, default: true },
  showResetButton: { type: Boolean, default: true },
  searchButtonText: { type: String, default: '查询' },
  resetButtonText: { type: String, default: '重置' },
  searchButtonProps: { type: Object, default: () => ({}) },
  resetButtonProps: { type: Object, default: () => ({}) },
  onSearch: { type: Function, default: undefined },
  onReset: { type: Function, default: undefined },
  beforeFetch: { type: Function, default: undefined },
  showAddButton: { type: Boolean, default: false },
  addButtonPermi: { type: Array, default: () => [] },
  addButtonText: { type: String, default: '新增' },
  addButtonProps: { type: Object, default: () => ({}) },
  onAdd: { type: Function, default: undefined },
  /** 是否在网格首位展示「新增」占位卡 */
  showAddCard: { type: Boolean, default: false },
  addCardPermi: { type: Array, default: () => [] },
  addCardText: { type: String, default: '新增' },
  onAddCard: { type: Function, default: undefined },
})

const emit = defineEmits([
  'update:searchParam',
  'before-fetch',
  'after-fetch',
  'fetch-error',
  'add-click',
  'add-card-click',
  'search-submit',
  'search-reset',
])

const listLoading = ref(false)
const cardRows = ref([])
const total = ref(0)
const currentPage = ref(1)
const currentPageSize = ref(props.defaultPageSize)
const searchParam = reactive({})
const userStore = useUserStore()

const gridStyle = computed(() => ({
  '--c7-card-grid-min-width': `${props.minCardWidth}px`,
  '--c7-card-grid-gap': `${props.gridGap}px`,
}))

function resolvePermiVisible(showFlag, permiList) {
  if (!showFlag) return false
  if (!permiList || permiList.length === 0) return true
  return checkPermission(permiList)
}

const showAddButtonResolved = computed(() => {
  void userStore.permissions
  return resolvePermiVisible(props.showAddButton, props.addButtonPermi)
})

const showAddCardResolved = computed(() => {
  void userStore.permissions
  return resolvePermiVisible(props.showAddCard, props.addCardPermi)
})

function warnDev(msg) {
  if (import.meta.env.DEV) console.warn(`[C7CardGrid] ${msg}`)
}

function initSearchParam() {
  const base = cloneDeep(props.defaultSearchParam || {})
  Object.keys(searchParam).forEach((k) => {
    delete searchParam[k]
  })
  Object.assign(searchParam, base)
}

const sortedSearchColumns = computed(() => {
  const cols = [...(props.searchColumns || [])]
  return cols
    .filter((col) => {
      const t = col.type
      return !t || ['input', 'select', 'date', 'daterange', 'slot', '', undefined].includes(t)
    })
    .slice()
    .sort((a, b) => {
      const oa = a.order
      const ob = b.order
      if (oa == null && ob == null) return 0
      if (oa == null) return 1
      if (ob == null) return -1
      return Number(oa) - Number(ob)
    })
})

function buildListParams() {
  return {
    ...searchParam,
    pageNum: currentPage.value,
    pageSize: currentPageSize.value,
  }
}

async function fetchList() {
  const params = buildListParams()
  emit('before-fetch', params)
  if (typeof props.beforeFetch === 'function') {
    const allow = await props.beforeFetch(params)
    if (allow === false) return
  }
  listLoading.value = true
  try {
    const res = await props.listFunction(params)
    const rows = get(res, props.rowsKey)
    const tot = get(res, props.totalKey)
    if (!Array.isArray(rows)) {
      warnDev(`rowsKey="${props.rowsKey}" 未解析到数组，已置空列表`)
      cardRows.value = []
    } else {
      cardRows.value = rows
    }
    const n = Number(tot)
    total.value = Number.isFinite(n) ? n : 0
    emit('after-fetch', cardRows.value, total.value)
  } catch (err) {
    emit('fetch-error', err)
    cardRows.value = []
    total.value = 0
  } finally {
    listLoading.value = false
  }
}

function refreshData() {
  return fetchList()
}

function getDataList() {
  currentPage.value = 1
  return fetchList()
}

function handleSearchSubmit() {
  emit('search-submit', { ...searchParam })
  if (typeof props.onSearch === 'function') {
    const r = props.onSearch({ ...searchParam })
    if (r === false) return Promise.resolve()
  }
  currentPage.value = 1
  return fetchList()
}

function handleSearchReset() {
  emit('search-reset')
  if (typeof props.onReset === 'function') {
    const r = props.onReset()
    if (r === false) return Promise.resolve()
  }
  initSearchParam()
  currentPage.value = 1
  return fetchList()
}

function onPaginationChange() {
  return fetchList()
}

function handleBuiltInAddClick() {
  emit('add-click')
  if (typeof props.onAdd === 'function') props.onAdd()
}

function handleAddCardClick() {
  emit('add-card-click')
  if (typeof props.onAddCard === 'function') {
    props.onAddCard()
    return
  }
  if (typeof props.onAdd === 'function') {
    props.onAdd()
  }
}

watch(
  searchParam,
  () => {
    emit('update:searchParam', { ...searchParam })
  },
  { deep: true },
)

onMounted(() => {
  initSearchParam()
  emit('update:searchParam', { ...searchParam })
  fetchList()
})

defineExpose({
  refreshData,
  getDataList,
  searchParam,
  currentPage,
  currentPageSize,
  total,
  cardRows,
})
</script>

<style scoped>
.c7-card-grid__search {
  margin-bottom: 12px;
}

.c7-card-grid__search :deep(.el-select),
.c7-card-grid__search :deep(.el-date-editor) {
  min-width: 180px;
}

.c7-card-grid__toolbar {
  margin-bottom: 12px;
}

.c7-card-grid__toolbar .el-button + .el-button {
  margin-left: 8px;
}

.c7-card-grid__body {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(var(--c7-card-grid-min-width, 260px), 1fr));
  gap: var(--c7-card-grid-gap, 16px);
  min-height: 120px;
  align-items: stretch;
}

.c7-card-grid__item {
  min-width: 0;
}

.c7-card-grid__add-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 168px;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
  cursor: pointer;
  transition: border-color 0.2s, background-color 0.2s;
  color: var(--el-text-color-secondary);
}

.c7-card-grid__add-card:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.c7-card-grid__add-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.c7-card-grid__add-text {
  font-size: 14px;
}

.c7-card-grid__empty {
  grid-column: 1 / -1;
}

.c7-card-grid__pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
