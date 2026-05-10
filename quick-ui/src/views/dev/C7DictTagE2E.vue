<template>
  <div class="c7-dict-tag-e2e-wrap">
    <h1 data-testid="c7-dict-tag-title">C7DictTag Dev</h1>

    <section data-testid="tc-basic">
      <h2>逗号串双值（max 不限制）</h2>
      <c7-dict-tag
          model-value="1,0"
          :options="statusOpts"
          dict-type="success"
          data-testid="c7-dict-tag-basic"
      />
    </section>

    <section data-testid="tc-max-popover">
      <h2>max=1 + collapse=false（+N 可点 Popover）</h2>
      <p>三枚已匹配，应显示一枚字典 tag +「+2」，点击 +2 见剩余 label。</p>
      <c7-dict-tag
          model-value="1,0,1"
          :options="statusOpts"
          :max="1"
          :collapse="false"
          dict-type="warning"
          data-testid="c7-dict-tag-popover"
      />
    </section>

    <section data-testid="tc-max-tooltip">
      <h2>max=1 + collapse=true（+N Tooltip）</h2>
      <p>悬停「+2」应出现溢出 label 列表。</p>
      <c7-dict-tag
          model-value="1,0,1"
          :options="statusOpts"
          :max="1"
          :collapse="true"
          dict-type="danger"
          data-testid="c7-dict-tag-tooltip"
      />
    </section>

    <section data-testid="tc-interleaved">
      <h2>已匹配 / 未匹配 / 已匹配 / 已匹配（max=1）</h2>
      <p>中间未匹配且 showValue=false 应为「-」；+N 仅计已匹配溢出。</p>
      <c7-dict-tag
          :model-value="['1', 'x', '0', '1']"
          :options="statusOpts"
          :max="1"
          :show-value="false"
          :collapse="false"
          data-testid="c7-dict-tag-interleaved"
      />
    </section>

    <section data-testid="tc-unmatched-show">
      <h2>未匹配 showValue=true（info tag）</h2>
      <c7-dict-tag
          model-value="unknown-code"
          :options="statusOpts"
          :show-value="true"
          data-testid="c7-dict-tag-unmatched-show"
      />
    </section>

    <section data-testid="tc-unmatched-hide">
      <h2>未匹配 showValue=false（横杠）</h2>
      <c7-dict-tag
          model-value="bad"
          :options="statusOpts"
          :show-value="false"
          data-testid="c7-dict-tag-unmatched-hide"
      />
    </section>

    <section data-testid="tc-dict-fallback">
      <h2>未知 dictType（回退 primary）</h2>
      <c7-dict-tag
          model-value="1"
          :options="statusOpts"
          dict-type="__unknown_dict_type__"
          data-testid="c7-dict-tag-fallback"
      />
    </section>

    <section data-testid="tc-empty">
      <h2>空值</h2>
      <c7-dict-tag :model-value="null" :options="statusOpts" data-testid="c7-dict-tag-empty"/>
    </section>

    <section data-testid="tc-select-shape">
      <h2>与 C7Select 形似的 options（value 为数字）</h2>
      <c7-dict-tag
          :model-value="[1, 2]"
          :options="numOpts"
          dict-type="info"
          data-testid="c7-dict-tag-num"
      />
    </section>
  </div>
</template>

<script setup>
/** Dev 联调页：覆盖双值、max、+N、tooltip、未匹配、dictType fallback。 */

const statusOpts = [
  {label: '启用', value: '1'},
  {label: '停用', value: '0'}
]

const numOpts = [
  {label: '甲', value: 1},
  {label: '乙', value: 2}
]
</script>

<style scoped>
.c7-dict-tag-e2e-wrap {
  padding: 16px 24px 48px;
  max-width: 960px;
}

section {
  margin-bottom: 28px;
}

h2 {
  font-size: 16px;
  margin: 0 0 8px;
}

p {
  margin: 0 0 8px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
