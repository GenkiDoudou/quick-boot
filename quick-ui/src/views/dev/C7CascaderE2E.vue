<template>
  <div class="c7-cascader-e2e-wrap">
    <h1 data-testid="c7-cascader-title">C7Cascader Dev</h1>

    <section data-testid="tc-static">
      <h2>静态 dataList</h2>
      <c7-cascader
          v-model="staticVal"
          :data-list="staticTree"
          placeholder="请选择"
          clearable
          emit-path
          data-testid="c7-cas-static"
      />
      <pre data-testid="c7-cas-static-model">{{ modelText(staticVal) }}</pre>
    </section>

    <section data-testid="tc-autoload">
      <h2>整树 autoLoad</h2>
      <c7-cascader
          v-model="autoVal"
          :fetch-data="fetchTree"
          :auto-load="true"
          result-key="list"
          label-key="name"
          value-key="id"
          children-key="subs"
          placeholder="挂载后加载整树"
          clearable
          data-testid="c7-cas-autoload"
      />
      <pre data-testid="c7-cas-autoload-model">{{ modelText(autoVal) }}</pre>
    </section>

    <section data-testid="tc-lazy">
      <h2>懒加载 lazy</h2>
      <p class="hint">展开根节点后请求 <code>parentId=1</code> 的子列表。</p>
      <c7-cascader
          v-model="lazyVal"
          lazy
          :root-parent-id="0"
          :fetch-data="fetchLazyChildren"
          result-key="list"
          label-key="name"
          value-key="id"
          children-key="subs"
          placeholder="懒加载"
          clearable
          data-testid="c7-cas-lazy"
      />
      <pre data-testid="c7-cas-lazy-model">{{ modelText(lazyVal) }}</pre>
    </section>

    <section data-testid="tc-separator">
      <h2>多选 + separator（须 emit-path=false）</h2>
      <c7-cascader
          v-model="sepVal"
          multiple
          separator
          :emit-path="false"
          :data-list="flatLeavesTree"
          placeholder="叶子多选逗号串"
          clearable
          data-testid="c7-cas-sep"
      />
      <pre data-testid="c7-cas-sep-model">{{ modelText(sepVal) }}</pre>
    </section>

    <section data-testid="tc-sep-warn">
      <h2>多选 + separator + 默认 emitPath（控制台应 warn）</h2>
      <c7-cascader
          v-model="sepWarnVal"
          multiple
          separator
          :data-list="flatLeavesTree"
          placeholder="见控制台 [C7Cascader] warn"
          clearable
          data-testid="c7-cas-sep-warn"
      />
    </section>
  </div>
</template>

<script setup>
/** C7Cascader 组件 E2E 联调页：覆盖静态/懒加载、多选逗号串与 valueType。 */
import {ref} from 'vue'

const staticVal = ref([])
const autoVal = ref([])
const lazyVal = ref([])
const sepVal = ref('')
const sepWarnVal = ref([])

const staticTree = [
  {label: '一级', value: 'a', children: [{label: '二级', value: 'a-1'}]}
]

/** 仅两层叶子，便于多选逗号串演示 */
const flatLeavesTree = [
  {label: '甲', value: 'j'},
  {label: '乙', value: 'y'}
]

function modelText(v) {
  try {
    return JSON.stringify(v)
  } catch {
    return String(v)
  }
}

async function fetchTree() {
  const list = [
    {
      id: 10,
      name: '省',
      subs: [{id: 101, name: '市', subs: []}]
    }
  ]
  return {data: {list}}
}

async function fetchLazyChildren({parentId}) {
  await new Promise((r) => setTimeout(r, 80))
  if (parentId === 0) {
    return {
      data: {
        list: [
          {id: 1, name: '根项1', leaf: false, subs: []},
          {id: 2, name: '根项2', leaf: true, subs: []}
        ]
      }
    }
  }
  if (parentId === 1) {
    return {data: {list: [{id: 11, name: '子 1-1', subs: []}]}}
  }
  return {data: {list: []}}
}
</script>

<style scoped>
.c7-cascader-e2e-wrap {
  padding: 16px;
}

section {
  margin-bottom: 24px;
}

.hint {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
