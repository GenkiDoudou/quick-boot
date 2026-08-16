<template>
  <div class="c7-watermark-e2e">
    <h1 data-testid="c7-watermark-title">C7Watermark Dev</h1>

    <section data-testid="tc-container">
      <h2>容器模式 + 文本</h2>
      <c7-watermark
          :text="['C7Watermark', '容器模式']"
          :rotate="-18"
          :gap-x="90"
          :gap-y="70"
          style="min-height: 140px; border: 1px dashed var(--el-border-color)"
      >
        <div class="c7-watermark-e2e__box">
          <p>下方按钮应可点（水印层 pointer-events: none）。</p>
          <el-button type="primary" data-testid="c7-watermark-slot-btn" @click="slotClicks++">
            点我（{{ slotClicks }}）
          </el-button>
        </div>
      </c7-watermark>
    </section>

    <section data-testid="tc-fullscreen-viewport">
      <h2>全屏 viewport</h2>
      <p>
        `fullscreen` + `fullscreen-scope=&quot;viewport&quot;` 为固定视口铺满（见组件实现）。
        本页下方另有 document 长页示例；验收 viewport 时可临时注释 document 区块或单独开路由仅渲染 viewport 用例。
      </p>
    </section>

    <section data-testid="tc-fullscreen-document">
      <h2>全屏 document + 长页</h2>
      <c7-watermark
          text="DOCUMENT-SCOPE"
          :fullscreen="true"
          fullscreen-scope="document"
          :z-index="5002"
          :gap-x="120"
          :gap-y="100"
      >
        <div class="c7-watermark-e2e__long">
          <p>首屏说明：滚动到底部仍应有水印（document 高度）。</p>
          <el-button type="success" @click="docClicks++">长页内按钮 {{ docClicks }}</el-button>
          <div class="c7-watermark-e2e__spacer"/>
          <p>底部区域</p>
        </div>
      </c7-watermark>
    </section>

    <section data-testid="tc-image-fallback">
      <h2>图片失败回落文本</h2>
      <c7-watermark
          image="https://invalid.example.com/not-a-real-image.png"
          text="回落文本 OK"
          style="min-height: 100px; border: 1px dashed var(--el-border-color)"
      >
        <p>应看到「回落文本 OK」重复水印。</p>
      </c7-watermark>
    </section>

    <section data-testid="tc-svg-image">
      <h2>同源 SVG 图片</h2>
      <c7-watermark
          :image="userSvgUrl"
          text="SVG 失败则见此行"
          cross-origin="anonymous"
          :width="48"
          :height="48"
          :gap-x="80"
          :gap-y="80"
          style="min-height: 120px; border: 1px dashed var(--el-border-color)"
      >
        <p>小图标平铺。</p>
      </c7-watermark>
    </section>

    <section data-testid="tc-tamper-on">
      <h2>防删开启（editable=false）</h2>
      <p>打开 DevTools 删除 `.c7-watermark__layer` 节点，应在约 40ms 内恢复。</p>
      <c7-watermark
          :editable="false"
          text="TAMPER-ON"
          style="min-height: 80px; border: 1px dashed var(--el-border-color)"
      >
        <span>内容</span>
      </c7-watermark>
    </section>

    <section data-testid="tc-tamper-off">
      <h2>防删关闭（tamperResistant=false + editable=false）</h2>
      <p>删除水印层后不应自动恢复。</p>
      <c7-watermark
          :tamper-resistant="false"
          :editable="false"
          text="TAMPER-OFF"
          style="min-height: 80px; border: 1px dashed var(--el-border-color)"
      >
        <span>内容</span>
      </c7-watermark>
    </section>

    <section data-testid="tc-disabled">
      <h2>disabled</h2>
      <c7-watermark disabled text="不应出现" style="min-height: 60px; border: 1px dashed var(--el-border-color)">
        <span>无水印层</span>
      </c7-watermark>
    </section>
  </div>
</template>

<script setup>
/** C7Watermark 组件 E2E 联调页：覆盖全屏/容器、图片回落与防篡改。 */
import { ref } from 'vue'
import userSvg from '@/assets/icons/svg/user.svg'

const slotClicks = ref(0)
const docClicks = ref(0)
/** @type {string} */
const userSvgUrl = userSvg
</script>

<style scoped>
.c7-watermark-e2e {
  padding: 16px;
  max-width: 960px;
}

section {
  margin-bottom: 32px;
}

.c7-watermark-e2e__box {
  padding: 12px;
}

.c7-watermark-e2e__long {
  padding: 12px;
}

.c7-watermark-e2e__spacer {
  height: 1800px;
}
</style>
