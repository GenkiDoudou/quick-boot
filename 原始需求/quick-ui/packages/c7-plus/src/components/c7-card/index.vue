<template>
  <el-card class="box-card">
    <template #header>
      <div class="clearfix">
        <!-- 色块部分 -->
        <span class="color-block" v-if="props.isShowColorBlock"
              :style="{backgroundColor: colorBlockStyle.backgroundColor}"></span>

        <!-- 文字部分 -->
        <span class="text" :style="textStyle">{{ props.label }}</span>
        <el-button style="float: right; padding: 3px 0" text @click="toggleContent">
          {{ isExpanded ? '收起' : '展开' }}
        </el-button>
      </div>
    </template>
    <transition name="fade">
      <div v-if="isExpanded">
        <slot></slot>
      </div>
    </transition>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed, defineOptions } from 'vue'

defineOptions({
  name: 'C7Card'
})

/**
 * 文本大小类型
 */
type TextSize = 'h1' | 'h2' | 'h3' | 'h4' | 'h5'

/**
 * 组件属性接口
 */
interface Props {
  label?: string              // 卡片标题
  isShowColorBlock?: boolean  // 是否显示色块装饰
  colorBlockColor?: string    // 色块颜色
  textSize?: TextSize         // 标题文字大小
  isBold?: boolean            // 标题是否加粗
}

const props = withDefaults(defineProps<Props>(), {
  label: '',
  isShowColorBlock: false,
  colorBlockColor: '#409eff',
  textSize: 'h2',
  isBold: true
})

/**
 * 展开/收起状态
 * 默认为展开状态
 */
const isExpanded = ref(true)

/**
 * 切换内容显示状态
 */
const toggleContent = () => {
  isExpanded.value = !isExpanded.value
}

/**
 * 文字大小映射表
 * 将 h1-h5 映射到对应的 em 值
 */
const sizeClasses: Record<TextSize, string> = {
  h1: '2em',      // 32px
  h2: '1.5em',    // 24px
  h3: '1.17em',   // 18.72px
  h4: '1em',      // 16px
  h5: '0.83em'    // 13.28px
}

/**
 * 计算属性：色块样式
 */
const colorBlockStyle = computed(() => ({
  backgroundColor: props.colorBlockColor
}))

/**
 * 计算属性：标题文本样式
 */
const textStyle = computed(() => ({
  fontWeight: props.isBold ? 'bold' : 'normal',
  fontSize: sizeClasses[props.textSize],
  flex: 1  // 占据剩余空间
}))
</script>

<style scoped>
.clearfix::after {
  content: "";
  clear: both;
  display: table;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.color-block {
  display: inline-block;
  width: 10px;
  height: 23px;
  margin-right: 8px;
  vertical-align: middle;
}

.text {
  flex: 1;
  font-weight: bold;
}
</style>

