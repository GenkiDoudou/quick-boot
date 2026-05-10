<template>
  <div class="c7-timepicker-e2e-wrap">
    <h1 data-testid="c7-timepicker-title">C7TimePicker Dev</h1>

    <section data-testid="tc-default-format">
      <h2>单选 · 未传 format（默认 HH:mm:ss）</h2>
      <c7-time-picker v-model="singleVal" data-testid="c7-tp-single" />
      <pre data-testid="c7-tp-single-model">{{ displayJson(singleVal) }}</pre>
    </section>

    <section data-testid="tc-range-merge">
      <h2>is-range · rangeMerge · 合并串回显</h2>
      <p>初始 <code>"08:00:00,18:00:00"</code>，对外为单串。</p>
      <c7-time-picker
          v-model="rangeMergeVal"
          is-range
          range-merge
          data-testid="c7-tp-range-merge"
      />
      <pre data-testid="c7-tp-range-merge-model">{{ displayJson(rangeMergeVal) }}</pre>
    </section>

    <section data-testid="tc-merge-delimiter">
      <h2>is-range · rangeMerge · mergeDelimiter="|"</h2>
      <c7-time-picker
          v-model="pipeMergeVal"
          is-range
          range-merge
          merge-delimiter="|"
          data-testid="c7-tp-pipe"
      />
      <pre data-testid="c7-tp-pipe-model">{{ displayJson(pipeMergeVal) }}</pre>
    </section>

    <section data-testid="tc-range-array">
      <h2>is-range · rangeMerge=false · 须为数组（字符串误传应清空+warn）</h2>
      <p>初始为合法二元数组；勿传合并串到本模式。</p>
      <c7-time-picker
          v-model="rangeArrayVal"
          is-range
          data-testid="c7-tp-range-array"
      />
      <pre data-testid="c7-tp-range-array-model">{{ displayJson(rangeArrayVal) }}</pre>
    </section>

    <section data-testid="tc-single-with-merge-prop">
      <h2>非 is-range · rangeMerge=true（合并无效果）</h2>
      <c7-time-picker v-model="singleWithMergeProp" range-merge data-testid="c7-tp-single-merge" />
      <pre data-testid="c7-tp-single-merge-model">{{ displayJson(singleWithMergeProp) }}</pre>
    </section>

    <section data-testid="tc-invalid-merge">
      <h2>非法合并串（应 warn 后清空）</h2>
      <button type="button" data-testid="c7-tp-set-invalid" @click="badVal = '08:00:00'">
        写入缺分隔符的串
      </button>
      <c7-time-picker
          v-model="badVal"
          is-range
          range-merge
          data-testid="c7-tp-bad"
      />
      <pre data-testid="c7-tp-bad-model">{{ displayJson(badVal) }}</pre>
    </section>

    <section data-testid="tc-format-override">
      <h2>显式 format / value-format 覆盖默认</h2>
      <c7-time-picker
          v-model="overrideVal"
          format="HH:mm"
          value-format="HH:mm"
          data-testid="c7-tp-override"
      />
      <pre data-testid="c7-tp-override-model">{{ displayJson(overrideVal) }}</pre>
    </section>
  </div>
</template>

<script setup>
import {ref} from 'vue'

const singleVal = ref(null)
const rangeMergeVal = ref('08:00:00,18:00:00')
const pipeMergeVal = ref('09:00:00|17:00:00')
const rangeArrayVal = ref(['10:00:00', '11:00:00'])
const singleWithMergeProp = ref(null)
const badVal = ref(null)
const overrideVal = ref(null)

function displayJson(v) {
  try {
    return JSON.stringify(v)
  } catch {
    return String(v)
  }
}
</script>

<style scoped>
.c7-timepicker-e2e-wrap {
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
