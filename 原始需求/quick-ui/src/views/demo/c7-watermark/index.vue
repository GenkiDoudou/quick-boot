<template>
  <div class="demo-page">
    <div class="demo-header">
      <h2 class="demo-title">C7Watermark 水印</h2>
      <p class="demo-desc">基于 Canvas 绘制水印并平铺在容器上，支持文本/图片水印、防删除（MutationObserver）、全屏模式、旋转/透明度/间距等配置。</p>
    </div>

    <demo-section title="基础文本水印">
      <C7Watermark text="内部资料" :opacity="0.5">
        <div class="watermark-content">页面内容区域（文本水印）</div>
      </C7Watermark>
      <demo-code :code="code1" />
    </demo-section>

    <demo-section title="多行文本水印">
      <C7Watermark :text="['QuickBoot', '2024-01-01']" :opacity="0.5">
        <div class="watermark-content">页面内容区域（多行文本水印）</div>
      </C7Watermark>
      <demo-code :code="code2" />
    </demo-section>

    <demo-section title="自定义颜色与旋转">
      <C7Watermark text="机密文件" font-color="rgba(255,0,0,0.2)" :rotate="-45" :opacity="1">
        <div class="watermark-content">页面内容区域（红色 -45° 水印）</div>
      </C7Watermark>
      <demo-code :code="code3" />
    </demo-section>

    <demo-section title="自定义字号与间距">
      <C7Watermark text="DRAFT" :font-size="24" :gap-x="60" :gap-y="60" :opacity="0.5">
        <div class="watermark-content">页面内容区域（大字号，小间距）</div>
      </C7Watermark>
      <demo-code :code="code4" />
    </demo-section>

    <demo-section title="条件显示（:disabled）">
      <div style="margin-bottom:12px;">
        <el-switch v-model="disabled" active-text="禁用水印" inactive-text="启用水印" />
      </div>
      <C7Watermark text="条件水印" :disabled="disabled" :opacity="0.5">
        <div class="watermark-content">切换上方开关控制水印显示/隐藏</div>
      </C7Watermark>
      <demo-code :code="code5" />
    </demo-section>

    <demo-section title="自定义 zIndex">
      <C7Watermark text="低层级" :z-index="1" :opacity="0.5">
        <div class="watermark-content" style="position:relative;z-index:2;background:rgba(64,158,255,0.08);border-radius:6px;">
          此内容 z-index=2，高于水印层（z-index=1），水印在内容下方
        </div>
      </C7Watermark>
      <demo-code :code="code6" />
    </demo-section>

    <demo-section title="全屏水印（fullscreen）">
      <el-button type="primary" @click="showFullscreen = true">开启全屏水印</el-button>
      <el-button v-if="showFullscreen" @click="showFullscreen = false">关闭全屏水印</el-button>
      <C7Watermark v-if="showFullscreen" text="全屏水印演示" fullscreen :editable="false" :opacity="0.3" />
      <demo-code :code="code7" />
    </demo-section>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineOptions({ name: 'C7WatermarkDemo' })

const disabled = ref(false)
const showFullscreen = ref(false)

const code1 = `<C7Watermark text="内部资料" :opacity="0.15">
  <div>页面内容</div>
</C7Watermark>`

const code2 = `<C7Watermark :text="['公司名称', '2024-01-01']" font-color="rgba(0,0,0,0.1)">
  <div>页面内容</div>
</C7Watermark>`

const code3 = `<C7Watermark
  text="机密文件"
  font-color="rgba(255,0,0,0.15)"
  :rotate="-45"
>
  <div>页面内容</div>
</C7Watermark>`

const code4 = `<C7Watermark text="DRAFT" :font-size="24" :gap-x="60" :gap-y="60">
  <div>页面内容</div>
</C7Watermark>`

const code5 = `<!-- :disabled 动态控制水印显隐 -->
<C7Watermark text="草稿" :disabled="!isDraft">
  <div>页面内容</div>
</C7Watermark>`

const code6 = `<!-- 调整水印层级，避免遮挡弹层 -->
<C7Watermark text="水印" :z-index="1">
  <div>页面内容</div>
</C7Watermark>`

const code7 = `<!-- 全屏水印，防截图防删除 -->
<C7Watermark
  :text="currentUser.name"
  fullscreen
  :editable="false"
  :opacity="0.08"
/>`
</script>

<style scoped lang="scss">
.demo-page {
  padding: 24px;
  max-width: 960px;
  margin: 0 auto;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.demo-header {
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e4e7ed;
  .demo-title { font-size: 24px; font-weight: 600; color: #1a1a2e; margin: 0 0 8px; }
  .demo-desc { color: #606266; font-size: 14px; margin: 0; line-height: 1.6; }
}
.watermark-content {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #606266;
  background: #f5f7fa;
  border-radius: 6px;
}
</style>

<script>
import { defineComponent, ref, h } from 'vue'
export const DemoSection = defineComponent({
  name: 'DemoSection',
  props: { title: String },
  setup(props, { slots }) {
    return () => h('div', { class: 'demo-section' }, [
      h('h3', { class: 'section-title' }, props.title),
      h('div', { class: 'section-body' }, slots.default?.())
    ])
  }
})
export const DemoCode = defineComponent({
  name: 'DemoCode',
  props: { code: String },
  setup(props) {
    const open = ref(false)
    return () => h('div', { class: 'code-toggle' }, [
      h('span', { class: 'code-toggle-btn', onClick: () => { open.value = !open.value } }, open.value ? '▲ 收起代码' : '▶ 查看示例代码'),
      open.value ? h('pre', { class: 'code-block' }, h('code', {}, props.code)) : null
    ])
  }
})
</script>

<style>
.demo-section { margin-bottom: 36px; background: #fff; border: 1px solid #ebeef5; border-radius: 8px; padding: 20px 24px; box-shadow: 0 1px 4px rgba(0,0,0,.04); }
.section-title { font-size: 15px; font-weight: 600; color: #303133; margin: 0 0 12px; padding-bottom: 10px; border-bottom: 1px dashed #ebeef5; }
.section-body { padding-top: 4px; display: flex; flex-direction: column; gap: 10px; }
.code-toggle { margin-top: 8px; }
.code-toggle-btn { font-size: 12px; color: #409eff; cursor: pointer; user-select: none; }
.code-toggle-btn:hover { text-decoration: underline; }
.code-block { margin-top: 8px; background: #282c34; color: #abb2bf; border-radius: 6px; padding: 14px 16px; font-size: 12px; font-family: 'JetBrains Mono', 'Fira Code', monospace; overflow-x: auto; line-height: 1.6; white-space: pre; }
</style>
