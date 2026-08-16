<template>
  <div class="c7-card-e2e-wrap">
    <h1 data-testid="c7-card-title">C7Card Dev</h1>

    <section data-testid="tc-default">
      <h2>默认头 + collapsible + 色块</h2>
      <c7-card
          label="基础卡片"
          text-size="h4"
          collapsible
          show-color-block
          data-testid="c7-card-default"
      >
        <p>内容区：默认展开，点击「收起」后带 fade 隐藏。</p>
      </c7-card>
    </section>

    <section data-testid="tc-controlled">
      <h2>受控 v-model</h2>
      <p class="hint">外部：<code>expanded = {{ controlled }}</code>
        <el-button size="small" @click="controlled = !controlled">切换</el-button>
      </p>
      <c7-card
          v-model="controlled"
          label="受控卡片"
          collapsible
          text-size="h3"
          data-testid="c7-card-controlled"
      >
        <p>与上方开关同步。</p>
      </c7-card>
    </section>

    <section data-testid="tc-uncontrolled">
      <h2>非受控 default-expanded=false</h2>
      <c7-card
          label="初始折叠"
          :default-expanded="false"
          collapsible
          data-testid="c7-card-uncontrolled"
      >
        <p>初始为折叠，需点「展开」。</p>
      </c7-card>
    </section>

    <section data-testid="tc-extra-shadow">
      <h2>extra 插槽 + shadow=hover</h2>
      <c7-card
          label="操作区"
          text-size="h5"
          collapsible
          shadow="hover"
          data-testid="c7-card-extra"
      >
        <template #extra>
          <el-button size="small" text type="primary">辅助操作</el-button>
        </template>
        <p>头部右侧为 extra；卡片 shadow 为 hover。</p>
      </c7-card>
    </section>

    <section data-testid="tc-header-slot">
      <h2>自定义 header（不出现默认折叠按钮）</h2>
      <c7-card collapsible data-testid="c7-card-header-slot">
        <template #header>
          <div class="custom-head">完全自定义头部（本例未放折叠控件）</div>
        </template>
        <p>仅内容区使用默认布局；头部无内置色块/标题/折叠。</p>
      </c7-card>
    </section>

    <section data-testid="tc-toggle-slot">
      <h2>collapsible + #toggle 替换默认按钮</h2>
      <c7-card label="自定义触发器" collapsible data-testid="c7-card-toggle-slot">
        <template #toggle="{ expanded, toggle, contentId }">
          <el-button
              size="small"
              :aria-expanded="expanded"
              :aria-controls="contentId"
              @click="toggle"
          >
            {{ expanded ? '收拢' : '展开内容' }}
          </el-button>
        </template>
        <p>使用 slot props 绑定无障碍属性。</p>
      </c7-card>
    </section>

    <section data-testid="tc-alias">
      <h2>色块别名 is-show-color-block</h2>
      <c7-card
          label="仅 isShowColorBlock"
          is-show-color-block
          data-testid="c7-card-alias"
      >
        <p>未传 showColorBlock 时走 isShowColorBlock。</p>
      </c7-card>
    </section>

    <section data-testid="tc-ref">
      <h2>ref.expand / ref.collapse / ref.toggle</h2>
      <p>
        <el-button size="small" @click="cardRef?.expand()">expand</el-button>
        <el-button size="small" @click="cardRef?.collapse()">collapse</el-button>
        <el-button size="small" @click="cardRef?.toggle()">toggle</el-button>
      </p>
      <c7-card
          ref="cardRef"
          label="方法驱动"
          collapsible
          data-testid="c7-card-ref"
      >
        <p>用上方按钮调用暴露方法。</p>
      </c7-card>
    </section>
  </div>
</template>

<script setup>
/** C7Card 组件 E2E 联调页：覆盖折叠受控/非受控、色块与 extra 插槽。 */
import {ref} from 'vue'

const controlled = ref(true)
const cardRef = ref(null)
</script>

<style scoped lang="scss">
.c7-card-e2e-wrap {
  max-width: 720px;
  margin: 24px auto;
  padding: 0 16px 48px;
}

section {
  margin-bottom: 28px;
}

h1 {
  font-size: 22px;
  margin-bottom: 8px;
}

h2 {
  font-size: 15px;
  margin: 0 0 10px;
  color: var(--el-text-color-secondary);
}

.hint {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin: 0 0 8px;
}

.custom-head {
  font-weight: 600;
  color: var(--el-color-warning);
}
</style>
