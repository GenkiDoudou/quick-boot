<template>
  <div class="c7-title-e2e-wrap">
    <h1 data-testid="c7-title-page-h1">C7Title Dev</h1>

    <section data-testid="tc-default">
      <h2>默认（labelSize=h4、底边、主色变量）</h2>
      <c7-title label="区块标题" data-testid="c7-title-default"/>
    </section>

    <section data-testid="tc-labelsize-sync">
      <h2>labelSize=h2 且未传 tag → 语义 h2</h2>
      <c7-title label="应为 h2 语义" label-size="h2" data-testid="c7-title-h2-sync"/>
    </section>

    <section data-testid="tc-explicit-h4">
      <h2>labelSize=h2 + tag=h4 显式 → 语义 h4、字号 h2 档</h2>
      <c7-title label="显式 h4" label-size="h2" tag="h4" data-testid="c7-title-explicit-h4"/>
    </section>

    <section data-testid="tc-custom-size">
      <h2>自定义 labelSize</h2>
      <c7-title label="22px 字号" label-size="22px" data-testid="c7-title-px"/>
    </section>

    <section data-testid="tc-no-border">
      <h2>showBorder=false</h2>
      <c7-title label="无底边" :show-border="false" data-testid="c7-title-no-border"/>
    </section>

    <section data-testid="tc-icon-string">
      <h2>icon 字符串（Setting）</h2>
      <c7-title label="带图标" icon="Setting" data-testid="c7-title-icon"/>
    </section>

    <section data-testid="tc-slots-actions">
      <h2>默认插槽 actions</h2>
      <c7-title label="右侧按钮" data-testid="c7-title-actions">
        <el-button size="small" type="primary">操作</el-button>
      </c7-title>
    </section>

    <section data-testid="tc-decoration">
      <h2>decorationColor</h2>
      <c7-title
          label="自定义装饰色"
          decoration-color="#67c23a"
          data-testid="c7-title-deco"
      />
    </section>
  </div>
</template>

<script setup>
</script>

<style scoped>
.c7-title-e2e-wrap {
  padding: 24px;
  max-width: 720px;
}

section {
  margin-bottom: 32px;
}

section h2 {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin: 0 0 8px;
}
</style>
