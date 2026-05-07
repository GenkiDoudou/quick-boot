<template>
  <div class="c7-datepicker-e2e-wrap">
    <h1 data-testid="c7-datepicker-title">C7DatePicker Dev</h1>

    <section data-testid="tc-range-array">
      <h2>daterange · rangeMerge=false · 外部合并串回显</h2>
      <p>初始 <code>"2024-06-01,2024-06-30"</code>，应能回显；输出为数组。</p>
      <c7-date-picker
          v-model="rangeArrayVal"
          type="daterange"
          data-testid="c7-dp-range-array"
      />
      <pre data-testid="c7-dp-range-array-model">{{ displayJson(rangeArrayVal) }}</pre>
    </section>

    <section data-testid="tc-range-merge">
      <h2>daterange · rangeMerge=true · mergeDelimiter="|"</h2>
      <p>选择后对外应为单串，如 <code>2024-01-01|2024-12-31</code>。</p>
      <c7-date-picker
          v-model="rangeMergeVal"
          type="daterange"
          range-merge
          merge-delimiter="|"
          data-testid="c7-dp-range-merge"
      />
      <pre data-testid="c7-dp-range-merge-model">{{ displayJson(rangeMergeVal) }}</pre>
    </section>

    <section data-testid="tc-format-override">
      <h2>显式 format / value-format 覆盖映射</h2>
      <p>单日，强制 <code>YYYY/MM/DD</code>。</p>
      <c7-date-picker
          v-model="overrideVal"
          type="date"
          format="YYYY/MM/DD"
          value-format="YYYY/MM/DD"
          data-testid="c7-dp-override"
      />
      <pre data-testid="c7-dp-override-model">{{ displayJson(overrideVal) }}</pre>
    </section>

    <section data-testid="tc-unmapped-type">
      <h2>未映射 type（dates）— 不注入默认 format</h2>
      <p>多选日期；若控制台无本组件强行覆盖，行为由 EP 决定。</p>
      <c7-date-picker
          v-model="datesVal"
          type="dates"
          data-testid="c7-dp-dates"
      />
      <pre data-testid="c7-dp-dates-model">{{ displayJson(datesVal) }}</pre>
    </section>

    <section data-testid="tc-invalid-merge">
      <h2>非法合并串（应 warn 后清空）</h2>
      <button type="button" data-testid="c7-dp-set-invalid" @click="badRangeVal = 'not-a-date,'">
        写入非法串
      </button>
      <c7-date-picker
          v-model="badRangeVal"
          type="daterange"
          data-testid="c7-dp-bad"
      />
      <pre data-testid="c7-dp-bad-model">{{ displayJson(badRangeVal) }}</pre>
    </section>
  </div>
</template>

<script setup>
import {ref} from 'vue'

const rangeArrayVal = ref('2024-06-01,2024-06-30')
const rangeMergeVal = ref(null)
const overrideVal = ref(null)
const datesVal = ref([])
const badRangeVal = ref(null)

function displayJson(v) {
  try {
    return JSON.stringify(v)
  } catch {
    return String(v)
  }
}
</script>

<style scoped>
.c7-datepicker-e2e-wrap {
  padding: 16px;
  max-width: 960px;
}

section {
  margin-bottom: 28px;
}

pre {
  margin-top: 8px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
