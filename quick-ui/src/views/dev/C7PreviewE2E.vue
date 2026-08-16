<template>
  <div class="c7-preview-e2e-wrap">
    <h1 data-testid="c7-preview-title">C7Preview Dev</h1>

    <section data-testid="tc-none-multi-img">
      <h2>none · 多图</h2>
      <c7-preview :urls="multiImgUrls" cover-type="none" :width="100" :height="100"/>
    </section>

    <section data-testid="tc-none-mixed">
      <h2>none · 图+视频+文件</h2>
      <c7-preview :urls="mixedUrls" cover-type="none" :width="120" :height="120"/>
    </section>

    <section data-testid="tc-button-mixed">
      <h2>button · 混合（应优先进图片预览）</h2>
      <c7-preview :urls="mixedUrls" cover-type="button"/>
    </section>

    <section data-testid="tc-button-multi-video">
      <h2>button · 仅多视频</h2>
      <c7-preview :urls="multiVideoUrls" cover-type="button"/>
    </section>

    <section data-testid="tc-file">
      <h2>file · 表格</h2>
      <c7-preview :urls="mixedUrls" cover-type="file"/>
    </section>

    <section data-testid="tc-guard">
      <h2>onPreview 阻止（点图应无预览）</h2>
      <c7-preview :urls="multiImgUrls" cover-type="none" :on-preview="blockAll" :width="80" :height="80"/>
    </section>

    <section data-testid="tc-empty">
      <h2>空 urls · button 禁用</h2>
      <c7-preview urls="" cover-type="button"/>
    </section>

    <p data-testid="c7-preview-log">last: {{ log }}</p>
  </div>
</template>

<script setup>
/** C7Preview 组件 E2E 联调页：覆盖多图/视频/文件预览与 onPreview 拦截。 */
import {ref} from 'vue'

const log = ref('')

const multiImgUrls =
    'https://picsum.photos/seed/c7p-a/120/120.webp,https://picsum.photos/seed/c7p-b/120/120.webp'

const mixedUrls = [
  'https://picsum.photos/seed/c7p-1/100/100.jpg',
  'https://www.w3schools.com/html/mov_bbb.mp4',
  'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf',
].join(',')

const multiVideoUrls = [
  'https://www.w3schools.com/html/mov_bbb.mp4',
  'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
].join(',')

function blockAll() {
  log.value = 'blocked'
  return false
}
</script>

<style scoped>
.c7-preview-e2e-wrap {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
</style>
