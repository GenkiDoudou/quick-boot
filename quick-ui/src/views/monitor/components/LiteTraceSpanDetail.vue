<template>
  <div v-if="!sel" class="span-detail-empty">点选瀑布中的接口 / SQL 查看详情</div>
  <div v-else class="span-detail">
    <template v-if="sel.kind === 'api'">
      <div class="kv">
        <label>URL</label>
        <div class="copy-field">
          <span class="mono">{{ sel.url || sel.name || '—' }}</span>
          <c7-copy v-if="canCopy(sel.url || sel.name)" mode="icon" size="small" class="hover-copy" :text="sel.url || sel.name" />
        </div>
      </div>
      <div class="kv">
        <label>Method</label>
        <span>{{ (sel.method || '—').toString().toUpperCase() }}</span>
      </div>
      <div class="kv">
        <label>HTTP 状态</label>
        <span>{{ sel.status ?? '—' }}</span>
      </div>
      <div class="kv">
        <label>业务码</label>
        <span>{{ sel.bizCode != null ? String(sel.bizCode) : '—' }}</span>
      </div>
      <div class="kv">
        <label>业务消息</label>
        <span>{{ sel.bizMsg || '—' }}</span>
      </div>
      <div class="kv">
        <label>耗时说明</label>
        <div class="dur-box">
          <div class="dur-row">
            <span class="dur-k">前端观测</span>
            <span class="dur-v">{{ sel.feMs != null ? `${sel.feMs} ms` : '—' }}</span>
            <span class="dur-hint">浏览器侧整段请求（含网络）</span>
          </div>
          <div class="dur-row">
            <span class="dur-k">后端处理</span>
            <span class="dur-v">{{ sel.beMs != null ? `${sel.beMs} ms` : '—' }}</span>
            <span class="dur-hint">服务端处理，瀑布条按此绘制</span>
          </div>
          <div v-if="netHint != null" class="dur-row">
            <span class="dur-k">大致差额</span>
            <span class="dur-v">{{ netHint }} ms</span>
            <span class="dur-hint">多含网络 / 排队 / 序列化</span>
          </div>
        </div>
      </div>
      <div class="kv kv-pre">
        <label>Query</label>
        <pre class="body-pre">{{ sel.query || '—' }}</pre>
        <c7-copy v-if="canCopy(sel.query)" mode="button" size="small" class="copy-btn-wrap" :text="sel.query" button-text="复制" />
      </div>
      <div class="kv kv-pre">
        <label>Params</label>
        <pre class="body-pre">{{ sel.requestParams || '—' }}</pre>
        <c7-copy
          v-if="canCopy(sel.requestParams)"
          mode="button"
          size="small"
          class="copy-btn-wrap"
          :text="sel.requestParams"
          button-text="复制"
        />
      </div>
      <div class="kv kv-pre">
        <label>Body</label>
        <pre class="body-pre">{{ sel.requestBody || '—' }}</pre>
        <c7-copy
          v-if="canCopy(sel.requestBody)"
          mode="button"
          size="small"
          class="copy-btn-wrap"
          :text="sel.requestBody"
          button-text="复制"
        />
      </div>
      <div class="kv kv-pre">
        <label>响应预览</label>
        <pre class="body-pre">{{ sel.responsePreview || '—' }}</pre>
        <c7-copy
          v-if="canCopy(sel.responsePreview)"
          mode="button"
          size="small"
          class="copy-btn-wrap"
          :text="sel.responsePreview"
          button-text="复制"
        />
      </div>
      <div v-if="trace" class="kv-group">
        <div class="kv-group-title">链路上下文</div>
        <div class="kv">
          <label>traceId</label>
          <div class="copy-field">
            <span class="mono">{{ trace.traceId || '—' }}</span>
            <c7-copy v-if="canCopy(trace.traceId)" mode="icon" size="small" class="hover-copy" :text="trace.traceId" />
          </div>
        </div>
        <div class="kv">
          <label>operationId</label>
          <div class="copy-field">
            <span class="mono">{{ trace.operationId || '—' }}</span>
            <c7-copy v-if="canCopy(trace.operationId)" mode="icon" size="small" class="hover-copy" :text="trace.operationId" />
          </div>
        </div>
        <div class="kv">
          <label>页面路径</label>
          <span class="mono">{{ trace.pagePath || '—' }}</span>
        </div>
      </div>
    </template>

    <template v-else-if="sel.kind === 'sql'">
      <div class="kv">
        <label>Mapper</label>
        <div class="copy-field">
          <span class="mono">{{ sel.mapperId || sel.name || '—' }}</span>
          <c7-copy v-if="canCopy(sel.mapperId || sel.name)" mode="icon" size="small" class="hover-copy" :text="sel.mapperId || sel.name" />
        </div>
      </div>
      <div class="kv">
        <label>耗时</label>
        <span>{{ sel.durationMs ?? 0 }} ms</span>
      </div>
      <div class="kv kv-pre">
        <label>SQL</label>
        <pre class="sql-pre">{{ sel.sql || '—' }}</pre>
        <c7-copy v-if="canCopy(sel.sql)" mode="button" size="small" class="copy-btn-wrap" :text="sel.sql" button-text="复制" />
      </div>
    </template>

    <template v-else-if="sel.kind === 'action'">
      <div class="kv">
        <label>操作</label>
        <span>{{ sel.action || sel.name || '—' }}</span>
      </div>
      <div class="kv">
        <label>operationId</label>
        <span class="mono">{{ sel.operationId || '—' }}</span>
      </div>
    </template>

    <template v-else>
      <div class="kv">
        <label>名称</label>
        <span class="mono">{{ sel.name || '—' }}</span>
      </div>
      <div class="kv">
        <label>类型</label>
        <span>{{ sel.kindLabel || sel.kind || '—' }}</span>
      </div>
      <div class="kv">
        <label>耗时</label>
        <span>{{ sel.durationMs ?? 0 }} ms</span>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import C7Copy from '@/packages/C7Copy/index.vue'

/**
 * Lite Trace 瀑布图右侧 Span 详情面板：接口/SQL/操作等类型的字段展示与复制。
 */
defineOptions({ name: 'LiteTraceSpanDetail' })

const props = defineProps({
  sel: { type: Object, default: null },
  trace: { type: Object, default: null }
})

function canCopy(v) {
  return v != null && String(v).trim() !== '' && String(v) !== '—'
}

/** 前端观测与后端处理耗时差（大致网络/排队开销） */
const netHint = computed(() => {
  const s = props.sel
  if (!s || s.feMs == null || s.beMs == null) return null
  return Math.max(0, Number(s.feMs) - Number(s.beMs))
})
</script>

<style scoped>
.span-detail-empty {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  padding: 24px 8px;
}
.span-detail {
  min-width: 0;
  font-size: 12px;
}
.kv {
  margin-bottom: 10px;
  font-size: 12px;
}
.kv label {
  display: block;
  color: var(--el-text-color-secondary);
  margin-bottom: 2px;
  font-size: 11px;
}
.kv span,
.kv .mono {
  word-break: break-all;
  color: var(--el-text-color-primary);
}
.mono {
  font-family: ui-monospace, Consolas, monospace;
}
.kv-pre {
  position: relative;
}
.kv pre {
  margin: 0;
  padding: 8px;
  background: #0f172a;
  color: #e2e8f0;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  overflow: auto;
  font-size: 11px;
}
.kv pre.sql-pre {
  max-height: 360px;
}
.kv pre.body-pre {
  max-height: 220px;
}
.copy-btn-wrap {
  margin-top: 4px;
}
.copy-field {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  max-width: 100%;
  min-width: 0;
}
.copy-field .hover-copy {
  opacity: 0;
  flex-shrink: 0;
  transition: opacity 0.12s ease;
}
.copy-field:hover .hover-copy,
.copy-field:focus-within .hover-copy {
  opacity: 1;
}
.dur-box {
  background: #f8fafc;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 8px;
}
.dur-row {
  display: grid;
  grid-template-columns: 72px 72px 1fr;
  gap: 6px;
  align-items: baseline;
  margin-bottom: 6px;
  font-size: 11px;
}
.dur-row:last-child {
  margin-bottom: 0;
}
.dur-k {
  color: var(--el-text-color-secondary);
}
.dur-v {
  color: #2563eb;
  font-family: ui-monospace, Consolas, monospace;
}
.dur-hint {
  color: #94a3b8;
  font-size: 10px;
}
.kv-group {
  margin-top: 14px;
  padding-top: 10px;
  border-top: 1px solid var(--el-border-color-lighter);
}
.kv-group-title {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}
</style>
