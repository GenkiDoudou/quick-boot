<template>
  <!-- 遍历排序后的可见列，逐一渲染 el-table-column -->
  <template v-for="item in sortedColumns" :key="item.prop">
    <el-table-column
      :prop="item.prop"
      :label="item.label"
      :width="item.width"
      :min-width="item.minWidth"
      :fixed="item.fixed"
      :align="item.align ?? 'left'"
      :header-align="item.headerAlign ?? item.align ?? 'left'"
      :sortable="item.sortable"
      :show-overflow-tooltip="item.showOverflowTooltip"
      v-bind="item.props"
    >
      <!-- 自定义列标题 slot：#header-[prop] -->
      <template #header>
        <slot :name="'header-' + item.prop" :column="item">
          {{ item.label }}
        </slot>
      </template>

      <!-- 列内容 slot -->
      <template #default="{ row, $index }">
        <!-- tag 类型：字典标签渲染 -->
        <template v-if="item.columnType === 'tag'">
          <C7DictTag
            :model-value="row[item.prop]"
            :options="item.dictList ?? []"
            :size="item.tagSize ?? 'default'"
            :effect="item.tagEffect ?? 'light'"
          />
        </template>

        <!-- image 类型：图片预览渲染 -->
        <template v-else-if="item.columnType === 'image'">
          <C7Preview
            :urls="String(row[item.prop] || '')"
            display-type="image"
            :width="item.imageWidth ?? 60"
            :height="item.imageHeight ?? 60"
          />
        </template>

        <!-- link 类型：超链接渲染 -->
        <template v-else-if="item.columnType === 'link'">
          <a
            :href="resolveLinkHref(item, row)"
            :target="item.linkTarget ?? '_blank'"
            class="c7-table-column__link"
            @click.stop
          >
            {{ resolveLinkText(item, row) }}
          </a>
        </template>

        <!-- slot 类型：自定义插槽 -->
        <template v-else-if="item.columnType === 'slot'">
          <slot
            :name="item.slotName ?? item.prop"
            :row="row"
            :column="item"
            :index="$index"
          />
        </template>

        <!-- text 类型（默认）：纯文本，支持 formatter 和 emptyText -->
        <template v-else>
          <span>{{ formatCellValue(item, row) }}</span>
        </template>
      </template>
    </el-table-column>
  </template>
</template>

<script setup>
import { computed } from 'vue'
import C7DictTag from '../C7DictTag/index.vue'
import C7Preview from '../C7Preview/index.vue'

defineOptions({ name: 'C7JsonTableColumn', inheritAttrs: false })

const props = defineProps({
  /**
   * 列配置数组
   * 每项支持字段：
   *   prop、label、columnType、width、minWidth、fixed、align、headerAlign、
   *   sortable、showOverflowTooltip、visible、order、props（透传给 el-table-column）
   *
   * columnType 可选值：
   *   'text'  —— 纯文本（默认），支持 formatter
   *   'tag'   —— 字典标签，需配合 dictList
   *   'image' —— 图片预览，使用 C7Preview
   *   'link'  —— 超链接，支持 linkText / linkHref / linkTarget
   *   'slot'  —— 自定义插槽，使用 slotName 指定插槽名
   */
  columns: {
    type: Array,
    default: () => []
  }
})

/**
 * 过滤 visible: false 的列，按 order 升序排序
 * order 未设置的列排在最后
 */
const sortedColumns = computed(() => {
  return props.columns
    .filter(col => col.visible !== false)
    .sort((a, b) => {
      const oa = a.order ?? 999
      const ob = b.order ?? 999
      return oa - ob
    })
})

/**
 * 格式化 text 类型单元格的值
 * 优先使用 formatter 函数，值为空时使用 emptyText，否则直接显示原始值
 */
function formatCellValue(column, row) {
  const rawValue = row[column.prop]
  // 值为空（null / undefined / ''）时显示 emptyText
  if (rawValue === null || rawValue === undefined || rawValue === '') {
    return column.emptyText ?? ''
  }
  // 存在 formatter 时调用
  if (typeof column.formatter === 'function') {
    return column.formatter(row, column, rawValue)
  }
  return rawValue
}

/**
 * 解析 link 类型的链接文字
 * linkText 支持字符串或函数 (row) => string
 */
function resolveLinkText(column, row) {
  if (typeof column.linkText === 'function') {
    return column.linkText(row)
  }
  return column.linkText ?? row[column.prop] ?? ''
}

/**
 * 解析 link 类型的链接地址
 * linkHref 支持字符串或函数 (row) => string
 */
function resolveLinkHref(column, row) {
  if (typeof column.linkHref === 'function') {
    return column.linkHref(row)
  }
  return column.linkHref ?? row[column.prop] ?? '#'
}
</script>

<style scoped>
/* 超链接默认样式 */
.c7-table-column__link {
  color: var(--el-color-primary);
  text-decoration: none;
  transition: opacity 0.15s;
}
.c7-table-column__link:hover {
  opacity: 0.75;
  text-decoration: underline;
}
</style>
