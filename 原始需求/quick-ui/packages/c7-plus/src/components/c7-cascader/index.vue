<template>
  <el-cascader
      v-bind="$attrs"
      :model-value="selectedValue"
      @update:modelValue="onChange"
      :options="options"
      :props="cascaderProps"
      clearable
      collapse-tags
      collapse-tags-tooltip
  />
</template>

<script setup lang="ts">
import { ref, watch, computed, defineOptions } from 'vue'
import { useFetchOptions } from '../../hooks/useFetchOptions'
import { jsonGet } from '../../utils/utils'
import { logger } from '../../utils/logger'
import { handleError } from '../../utils/errorHandler'

defineOptions({ name: 'C7Cascader' })

/**
 * 组件属性接口
 */
interface Props {
  modelValue?: string | any[] | number  // 绑定值
  multiple?: boolean                     // 是否多选
  checkStrictly?: boolean                // 是否严格模式（可选任意级）
  fetchData?: Function                   // 异步获取数据的函数
  dataFormatter?: Function               // 数据格式化函数
  labelKey?: string                      // 标签字段名
  valueKey?: string                      // 值字段名
  resultKey?: string                     // 异步返回结果中的数据路径
  dataList?: any[]                       // 静态数据列表
  fetchParams?: Record<string, any>      // 异步请求参数
  resultType?: number                    // 返回结果类型：1-数组，2-逗号字符串
  lazy?: boolean                         // 是否懒加载
  rootParentId?: string | number         // 懒加载根节点父ID
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: undefined,
  multiple: false,
  checkStrictly: false,
  fetchData: undefined,
  dataFormatter: undefined,
  labelKey: 'label',
  valueKey: 'value',
  resultKey: 'data',
  dataList: () => [],
  fetchParams: () => ({}),
  resultType: 1,
  lazy: false,
  rootParentId: -1
})

const emit = defineEmits<{
  'update:modelValue': [value: any]
}>()

/**
 * el-cascader 的 props 配置
 * 核心配置：
 * - checkStrictly: true - 强制严格模式，可选任意级
 * - emitPath: false - 只返回选中节点的值，不返回完整路径
 */
const cascaderProps = computed(() => ({
  multiple: props.multiple,
  checkStrictly: true,      // ⭐ 强制严格模式
  emitPath: false,          // ⭐ 只返回值，不返回路径
  lazy: props.lazy,
  label: props.labelKey,
  value: props.valueKey,
  children: 'children',
  lazyLoad: props.lazy ? lazyLoad : undefined
}))

/**
 * 使用 hook 获取选项数据
 */
const { options } = useFetchOptions({
  fetchData: props.fetchData,
  fetchParams: props.fetchParams,
  resultKey: props.resultKey,
  dataFormatter: props.dataFormatter,
  dataList: props.dataList,
  lazy: props.lazy
})

/**
 * el-cascader 内部值
 * 严格模式 + emitPath=false 时：
 * - 单选：[1]
 * - 多选：[1, 2, 3]
 */
const selectedValue = ref<any[]>([])

/**
 * 监听外部值变化，转换为内部格式
 * 支持三种输入格式：
 * 1. 数组：[1, 2, 3]
 * 2. 字符串：'1,2,3'
 * 3. 单值：1
 */
watch(
    () => props.modelValue,
    val => {
      if (Array.isArray(val)) {
        // 数组格式：转换为数字数组
        selectedValue.value = val.map(Number)
      } else if (typeof val === 'string') {
        // 字符串格式：拆分并转换为数字数组
        selectedValue.value = val
            .split(',')
            .filter(Boolean)
            .map(Number)
      } else if (val != null) {
        // 单值格式：包装为数组
        selectedValue.value = [Number(val)]
      } else {
        // 空值
        selectedValue.value = []
      }
    },
    { immediate: true }
)

/**
 * 内部值变化处理
 * 根据 multiple 和 resultType 决定输出格式
 * 
 * @param val 内部值数组
 */
function onChange(val: any[]) {
  if (!props.multiple) {
    // 单选模式
    const v = val?.[0]
    emit(
        'update:modelValue',
        props.resultType === 2 ? String(v ?? '') : v
    )
    return
  }

  // 多选模式
  emit(
      'update:modelValue',
      props.resultType === 2 ? val.join(',') : val
  )
}

/**
 * 懒加载函数
 * 当节点展开时动态加载子节点
 * 
 * @param node 当前节点
 * @param resolve 回调函数，用于返回子节点数据
 */
function lazyLoad(node: any, resolve: Function) {
  if (props.fetchData) {
    // 使用节点的 value 或根节点 ID 作为参数
    const parentId = node.value ?? props.rootParentId
    
    props.fetchData(parentId).then((res: any) => {
      // 从响应中提取数据
      const data = jsonGet(res, props.resultKey, [])
      resolve(data)
    }).catch((error: any) => {
      handleError(error, {
        showToast: false,
        defaultMessage: '懒加载失败',
        logError: true
      })
      logger.error('懒加载失败:', error)
      resolve([])
    })
  } else {
    resolve([])
  }
}
</script>

