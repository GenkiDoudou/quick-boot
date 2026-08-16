<template>
  <div class="c7-json-table-column-e2e">
    <h1 data-testid="e2e-title">C7JsonTableColumn Dev</h1>

    <section data-testid="sec-main">
      <h2>综合表（order / 各 columnType / header 插槽）</h2>
      <p>列顺序约定：B（order=1）→ A（无 order）→ C（无 order），输入顺序 A、B、C。</p>
      <el-table :data="tableRows" border empty-text="表级空">
        <C7JsonTableColumn :columns="mainColumns" empty-text="表默认空">
          <template #header-username="{ column }">
            <span data-testid="header-username-custom">自定义：{{ column.label }}</span>
          </template>
          <template #action="{ row }">
            <el-button size="small" data-testid="slot-action-btn">{{ row.name }}</el-button>
          </template>
        </C7JsonTableColumn>
      </el-table>
    </section>

    <section data-testid="sec-slot-missing">
      <h2>slot 列未提供父插槽（应显示「-」）</h2>
      <el-table :data="tableRows" border>
        <C7JsonTableColumn :columns="slotMissingColumns"/>
      </el-table>
    </section>

    <section data-testid="sec-formatter-empty">
      <h2>formatter 返回空串（不得被组件改成「-」或 emptyText）</h2>
      <el-table :data="tableRows" border>
        <C7JsonTableColumn :columns="formatterColumns"/>
      </el-table>
    </section>

    <section data-testid="sec-dict-priority">
      <h2>tag：options 优先于 dictList（同 value 应显示 options 的 label）</h2>
      <el-table :data="dictPriorityRows" border>
        <C7JsonTableColumn :columns="dictPriorityColumns"/>
      </el-table>
    </section>
  </div>
</template>

<script setup>
/** C7JsonTableColumn 组件 E2E 联调页：覆盖列类型渲染与 order 排序。 */
import {computed, ref} from 'vue'

const tableRows = ref([
  {
    name: '张三',
    username: '',
    status: '1',
    avatar: 'https://picsum.photos/seed/c7jtc1/40/40',
    docLink: 'https://example.com',
    emptyLink: '',
    unknownCol: '未知类型列展示',
  },
])

/** 与规范示例一致：A 无 order、B order=1、C 无 order → 渲染 B、A、C */
const mainColumns = computed(() => [
  {prop: 'username', label: 'A-用户名', order: undefined, columnType: 'text'},
  {prop: 'name', label: 'B-姓名', order: 1, columnType: 'text'},
  {
    prop: 'status',
    label: '状态(tag)',
    order: 2,
    columnType: 'tag',
    options: [
      {label: '启用', value: '1'},
      {label: '停用', value: '0'},
    ],
  },
  {
    prop: 'avatar',
    label: '头像',
    order: 3,
    columnType: 'image',
    width: 96,
    height: 40,
  },
  {
    prop: 'docLink',
    label: '文档',
    order: 4,
    columnType: 'link',
    linkHref: (row) => row.docLink,
    linkText: '打开',
    linkTarget: '_blank',
  },
  {
    prop: 'emptyLink',
    label: '空链接',
    order: 5,
    columnType: 'link',
    linkHref: (row) => row.emptyLink,
    linkText: '不应出现',
  },
  {prop: 'action', label: '操作', order: 6, columnType: 'slot'},
  /** `columnType` 非法时降级为 text（开发环境 console.warn） */
  {prop: 'unknownCol', label: '未知类型', order: 7, columnType: 'bogus'},
])

const slotMissingColumns = computed(() => [
  {prop: 'orphanSlot', label: '缺插槽', columnType: 'slot', slotName: 'not-provided'},
])

const formatterColumns = computed(() => [
  {
    prop: 'name',
    label: 'formatter 空串',
    columnType: 'text',
    formatter: () => '',
  },
])

const dictPriorityRows = ref([{code: '1'}])

const dictPriorityColumns = computed(() => [
  {
    prop: 'code',
    label: '字典',
    columnType: 'tag',
    options: [{label: '来自 options', value: '1'}],
    dictList: [{label: '来自 dictList', value: '1'}],
  },
])
</script>

<style scoped>
.c7-json-table-column-e2e {
  padding: 16px;
}

section {
  margin-bottom: 32px;
}

h2 {
  margin: 8px 0;
}
</style>
