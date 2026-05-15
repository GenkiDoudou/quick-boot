<template>
  <el-descriptions
    :title="title"
    :column="column"
    :direction="direction"
    :size="size"
    :border="border"
    :label-class-name="labelClassName"
    :content-class-name="contentClassName"
    class="c7-descriptions"
  >
    <template v-if="$slots.title" #title>
      <slot name="title" />
    </template>

    <template v-if="$slots.extra" #extra>
      <slot name="extra" />
    </template>

    <el-descriptions-item
      v-for="item in items"
      :key="item.prop || item.label"
      :label="item.label"
      :span="item.span"
      :label-class-name="item.labelClassName"
      :content-class-name="item.contentClassName"
    >
      <!-- 优先使用自定义 slot -->
      <template v-if="item.slotName">
        <slot :name="item.slotName" :value="getValue(data, item)" :item="item" :data="data" />
      </template>

      <!-- columnType: tag -->
      <template v-else-if="item.columnType === 'tag' && item.dictList">
        <el-tag
          v-if="!isEmpty(getValue(data, item))"
          :type="getTagType(item.dictList, getValue(data, item))"
          size="small"
        >
          {{ getDictLabel(item.dictList, getValue(data, item)) }}
        </el-tag>
        <span v-else class="c7-descriptions__empty">{{ item.emptyText ?? defaultEmptyText }}</span>
      </template>

      <!-- columnType: image -->
      <template v-else-if="item.columnType === 'image'">
        <el-image
          v-if="!isEmpty(getValue(data, item))"
          :src="getValue(data, item)"
          :style="{ width: item.imageWidth || '60px', height: item.imageHeight || '60px' }"
          fit="cover"
          :preview-src-list="[getValue(data, item)]"
          preview-teleported
        />
        <span v-else class="c7-descriptions__empty">{{ item.emptyText ?? defaultEmptyText }}</span>
      </template>

      <!-- columnType: link -->
      <template v-else-if="item.columnType === 'link'">
        <a
          v-if="!isEmpty(getValue(data, item))"
          :href="resolveLinkHref(item, getValue(data, item))"
          :target="item.linkTarget || '_blank'"
          class="c7-descriptions__link"
        >
          {{ resolveLinkText(item, getValue(data, item)) }}
        </a>
        <span v-else class="c7-descriptions__empty">{{ item.emptyText ?? defaultEmptyText }}</span>
      </template>

      <!-- columnType: copy 或 copyable -->
      <template v-else-if="item.columnType === 'copy' || item.copyable">
        <template v-if="!isEmpty(getValue(data, item))">
          <span class="c7-descriptions__copy-content">
            <span>{{ formatValue(getValue(data, item), item) }}</span>
            <C7Copy
              :text="item.copyText ? item.copyText(getValue(data, item)) : String(getValue(data, item))"
              mode="icon"
              :icon-size="13"
            />
          </span>
        </template>
        <span v-else class="c7-descriptions__empty">{{ item.emptyText ?? defaultEmptyText }}</span>
      </template>

      <!-- 默认文本 -->
      <template v-else>
        <span v-if="!isEmpty(formatValue(getValue(data, item), item))">
          {{ formatValue(getValue(data, item), item) }}
        </span>
        <span v-else class="c7-descriptions__empty">{{ item.emptyText ?? defaultEmptyText }}</span>
      </template>
    </el-descriptions-item>

    <!-- 透传额外的具名 slot（非内置 slot 名） -->
    <slot />
  </el-descriptions>
</template>

<script setup>
import C7Copy from '../C7Copy/index.vue'

defineOptions({ name: 'C7Descriptions' })

const props = defineProps({
  /** 绑定的数据对象 */
  data: {
    type: Object,
    default: () => ({})
  },
  /** 描述项配置列表 */
  items: {
    type: Array,
    default: () => []
  },
  /** 标题 */
  title: {
    type: String,
    default: ''
  },
  /** 每行显示的列数，仅支持数字 */
  column: {
    type: Number,
    default: 3
  },
  /** 排列方向 */
  direction: {
    type: String,
    default: 'horizontal',
    validator: (v) => ['horizontal', 'vertical'].includes(v)
  },
  /** 尺寸 */
  size: {
    type: String,
    default: 'default',
    validator: (v) => ['large', 'default', 'small'].includes(v)
  },
  /** 是否带边框 */
  border: {
    type: Boolean,
    default: false
  },
  /** 全局标签 className */
  labelClassName: {
    type: String,
    default: ''
  },
  /** 全局内容 className */
  contentClassName: {
    type: String,
    default: ''
  },
  /** 全局空值兜底文本，单个 item.emptyText 优先级更高 */
  defaultEmptyText: {
    type: String,
    default: '暂无'
  }
})

/**
 * 从 data 中按 item.prop 取值，支持点号嵌套路径
 * 例如：prop='user.name' → data.user.name
 * 修正：使用 hasOwnProperty 避免原型链污染
 */
function getValue(data, item) {
  if (!item.prop) return undefined
  const keys = item.prop.split('.')
  let value = data
  for (const key of keys) {
    if (value !== null && typeof value === 'object' &&
        Object.prototype.hasOwnProperty.call(value, key)) {
      value = value[key]
    } else {
      return undefined
    }
  }
  return value
}

/**
 * 判断值是否为空（null / undefined / 空字符串）
 */
function isEmpty(val) {
  return val === null || val === undefined || val === ''
}

/**
 * 格式化值：优先 formatter，其次直接返回字符串
 */
function formatValue(value, item) {
  if (typeof item.formatter === 'function') {
    return item.formatter(value, item)
  }
  if (isEmpty(value)) return ''
  return String(value)
}

/**
 * 从字典列表中查找标签文本
 */
function getDictLabel(dictList, value) {
  if (!dictList || !dictList.length) return value
  const found = dictList.find((d) => String(d.value) === String(value))
  return found ? found.label : value
}

/**
 * 从字典列表中查找 tag 类型
 */
function getTagType(dictList, value) {
  if (!dictList || !dictList.length) return ''
  const found = dictList.find((d) => String(d.value) === String(value))
  return found?.elTagType || ''
}

/**
 * 解析 link 的 href
 */
function resolveLinkHref(item, value) {
  if (typeof item.linkHref === 'function') return item.linkHref(value, props.data)
  if (item.linkHref) return item.linkHref
  return value
}

/**
 * 解析 link 的显示文字
 */
function resolveLinkText(item, value) {
  if (typeof item.linkText === 'function') return item.linkText(value, props.data)
  if (item.linkText) return item.linkText
  return value
}
</script>

<style scoped>
.c7-descriptions__empty {
  color: #c0c4cc;
  font-size: 13px;
}

.c7-descriptions__link {
  color: #409eff;
  text-decoration: none;
  transition: color 0.2s;
}

.c7-descriptions__link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.c7-descriptions__copy-content {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
</style>
