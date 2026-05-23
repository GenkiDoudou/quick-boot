<template>
  <template v-if="sortedColumns.length > 0">
    <el-table-column
        v-for="(item, index) in sortedColumns"
        :key="index"
        :type="item.type"
        :index="item.index"
        :label="item.label"
        :column-key="item.columnKey"
        :prop="item.prop"
        :width="item.width"
        :min-width="item.minWidth"
        :fixed="item.fixed"
        :render-header="item.renderHeader"
        :sortable="item.sortable"
        :sort-method="item.sortMethod"
        :sort-by="item.sortBy"
        :sort-orders="item.sortOrders"
        :resizable="item.resizable"
        :formatter="item.formatter"
        :show-overflow-tooltip="item.showOverflowTooltip"
        :align="item.align"
        :header-align="item.headerAlign"
        :class-name="item.className"
        :label-class-name="item.labelClassName"
        :selectable="item.selectable"
        :reserve-selection="item.reserveSelection"
        :filters="item.filters"
        :filter-placement="item.filterPlacement"
        :filter-class-name="item.filterClassName"
        :filter-multiple="item.filterMultiple"
        :filter-method="item.filterMethod"
        :filtered-value="item.filteredValue"
        :tooltip-formatter="item.tooltipFormatter"
    >
      <!-- 当columnType为TAG时使用c7DictTag组件 -->
      <template v-if="item.columnType === 'tag'" #default="{ row }">
        <c7-dict-tag :options="item.dictList" :modelValue="row[item.prop]" />
      </template>

      <!-- 当columnType为IMAGE时使用c7Preview组件 -->
      <template v-else-if="item.columnType === ColumnEnumType.IMAGE" #default="{ row }">
        <c7-preview :urls="row[item.prop]" width="100px" height="100px" cover-type="file" />
      </template>

      <!-- 当columnType为SLOT时透传插槽 -->
      <template v-else-if="item.columnType === ColumnEnumType.SLOT" #default="{ row, $index }">
        <slot 
          :name="item.slotName || ('slot_' + item.prop)"
          :row="row" 
          :index="$index"
        />
      </template>
    </el-table-column>
  </template>
</template>

<script setup lang="ts">
import { computed, defineOptions } from 'vue'
import { ColumnEnumType, TableColumnProps } from '../../types/table'
import C7DictTag from '../c7-dict-tag/index.vue'
import C7Preview from '../c7-preview/index.vue'

defineOptions({
  name: 'C7JsonTableColumn'
})

/**
 * 组件属性接口
 */
interface Props {
  columns?: TableColumnProps[]        // 列配置数组
  modelValue?: Record<string, any>    // 绑定值（预留）
}

const props = withDefaults(defineProps<Props>(), {
  columns: () => [],
  modelValue: () => ({})
})

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, any>]
}>()

/**
 * 计算属性：排序后的列配置
 * 处理逻辑：
 * 1. 过滤掉 visible 为 false 的列
 * 2. 为没有 order 的列分配默认顺序（使用数组索引）
 * 3. 按 order 升序排序
 */
const sortedColumns = computed(() => {
  return props.columns
      .filter(column => column.visible !== false)
      .map((item, index) => ({
        ...item,
        // 如果没有指定 order，使用数组索引作为默认值
        order: item.order ?? index
      }))
      .sort((a, b) => a.order - b.order)
})
</script>

