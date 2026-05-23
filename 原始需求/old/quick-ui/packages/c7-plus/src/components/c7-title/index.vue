<template>
  <h1 class="ep-title">
    <span class="label" :style="labelStyle">{{ label }}</span>
    <span><slot></slot></span>
  </h1>
</template>

<script setup lang="ts">
import { computed, defineOptions } from 'vue'

defineOptions({
  name: 'C7Title'
})

/**
 * 组件属性接口
 */
interface Props {
  label?: string       // 标题文本
  labelSize?: string   // 标题大小：h1-h6 或自定义像素值
  labelColor?: string  // 标题底部装饰线颜色
}

const props = withDefaults(defineProps<Props>(), {
  label: '默认标题',
  labelSize: 'h1',
  labelColor: ''
})

/**
 * 计算属性：标题样式
 * 根据 labelSize 计算字体大小，并设置装饰线颜色
 */
const labelStyle = computed(() => {
  let fontSize: string

  // 根据预设尺寸或自定义像素值计算字体大小
  switch (props.labelSize) {
    case 'h1':
      fontSize = '32px'
      break
    case 'h2':
      fontSize = '28px'
      break
    case 'h3':
      fontSize = '24px'
      break
    case 'h4':
      fontSize = '20px'
      break
    case 'h5':
      fontSize = '18px'
      break
    case 'h6':
      fontSize = '16px'
      break
    default:
      // 支持自定义像素值，如 '25px'
      fontSize = /^(\d+)px$/.test(props.labelSize)
          ? props.labelSize
          : '20px'
      break
  }

  return {
    fontSize,
    // CSS 变量：控制底部装饰线颜色
    // 当为 transparent 时，装饰线不显示
    '--label-color': props.labelColor || 'transparent'
  }
})
</script>

<style lang="scss" scoped>
.ep-title {
  color: #333;
  padding: 18px 0;
  border-bottom: 3px solid #333;
  margin-bottom: 20px;
  font-family: "Microsoft Yahei";
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;

  .label {
    position: relative;
    font-weight: 700;
    display: inline-block;
    font-size: inherit;
    --label-color: transparent;
  }

  .label:before {
    content: "";
    display: inline-block;
    position: absolute;
    height: 3px;
    width: 100%;
    bottom: -18px;
    background-color: var(--label-color);
    visibility: visible;
  }

  .label[style*="--label-color: transparent"]::before {
    visibility: hidden;
  }
}
</style>

