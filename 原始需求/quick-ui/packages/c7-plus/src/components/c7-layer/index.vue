<template>
  <div class="c7-layer" :class="layerClass">
    <div class="layer-header">
      <div class="layer-title" @click="handleBack" v-if="showBack">
        <el-icon :size="22">
          <Back />
        </el-icon>
        <slot name="title">{{ title }}</slot>
      </div>
      <div class="layer-title" v-else>
        <slot name="title">{{ title }}</slot>
      </div>
      <div class="layer-close" @click="handleClose">
        <el-icon :size="22">
          <Close />
        </el-icon>
      </div>
    </div>
    <div class="layer-content">
      <slot />
    </div>
    <div class="layer-footer" v-if="$slots.footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineOptions } from 'vue'
import { Back, Close } from '@element-plus/icons-vue'

defineOptions({
  name: 'C7Layer'
})

/**
 * 主题类型
 */
type Theme = 'default' | 'primary' | 'success' | 'warning' | 'danger'

/**
 * 尺寸类型
 */
type Size = 'small' | 'medium' | 'large'

/**
 * 组件属性接口
 */
interface Props {
  title?: string       // 标题文本
  showBack?: boolean   // 是否显示返回按钮
  theme?: Theme        // 主题颜色
  size?: Size          // 尺寸大小
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  showBack: true,
  theme: 'default',
  size: 'medium'
})

const emit = defineEmits<{
  close: []
  back: []
}>()

/**
 * 计算属性：样式类
 * 根据主题和尺寸生成对应的 CSS 类名
 */
const layerClass = computed(() => ({
  [`theme-${props.theme}`]: true,
  [`size-${props.size}`]: true
}))

/**
 * 处理返回事件
 */
const handleBack = () => {
  emit('back')
}

/**
 * 处理关闭事件
 */
const handleClose = () => {
  emit('close')
}
</script>

<style lang="scss" scoped>
.c7-layer {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 99;

  .layer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-height: 55px;
    padding: 0 20px;
    box-sizing: border-box;
    border-bottom: 1px solid var(--el-border-color-light);

    .layer-title {
      display: flex;
      align-items: center;
      font-weight: 600;
      color: var(--el-text-color-primary);
      font-size: 16px;
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: var(--el-color-primary);
      }

      .el-icon {
        margin-right: 8px;
        transition: transform 0.3s;
      }

      &:hover .el-icon {
        transform: translateX(-2px);
      }
    }

    .layer-close {
      cursor: pointer;
      color: var(--el-text-color-regular);
      transition: color 0.3s;
      padding: 4px;
      border-radius: 4px;

      &:hover {
        color: var(--el-color-danger);
        background-color: var(--el-color-danger-light-9);
      }
    }
  }

  .layer-content {
    padding: 20px;
    box-sizing: border-box;
    background-color: #fff;
  }

  .layer-footer {
    padding: 16px 20px;
    box-sizing: border-box;
    text-align: right;
    background-color: var(--el-bg-color-page);
    border-top: 1px solid var(--el-border-color-light);
    border-radius: 0 0 8px 8px;
  }

  // 主题样式
  &.theme-primary .layer-header .layer-title {
    color: var(--el-color-primary);
  }

  &.theme-success .layer-header .layer-title {
    color: var(--el-color-success);
  }

  &.theme-warning .layer-header .layer-title {
    color: var(--el-color-warning);
  }

  &.theme-danger .layer-header .layer-title {
    color: var(--el-color-danger);
  }

  // 尺寸样式
  &.size-small {
    .layer-header {
      min-height: 45px;
      padding: 0 16px;

      .layer-title {
        font-size: 14px;
      }
    }

    .layer-content {
      padding: 16px;
    }

    .layer-footer {
      padding: 12px 16px;
    }
  }

  &.size-large {
    .layer-header {
      min-height: 65px;
      padding: 0 24px;

      .layer-title {
        font-size: 18px;
      }
    }

    .layer-content {
      padding: 24px;
    }

    .layer-footer {
      padding: 20px 24px;
    }
  }
}

@media (max-width: 768px) {
  .c7-layer {
    margin: 10px;

    .layer-header {
      padding: 0 16px;

      .layer-title {
        font-size: 14px;
      }
    }

    .layer-content {
      padding: 16px;
    }

    .layer-footer {
      padding: 12px 16px;
    }
  }
}
</style>

