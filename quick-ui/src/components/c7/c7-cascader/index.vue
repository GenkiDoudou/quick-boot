<template>
  <el-cascader
      v-bind="$attrs"
      v-model="selectedValue"
      :options="options"
      :props="props2"

  >

  </el-cascader>
</template>

<script setup>
import {ref, watch, onMounted, defineOptions, computed} from 'vue';
import {useFetchOptions} from '../../../hooks/c7Hook.ts'
import {jsonGet} from '../../../utils/c7/utils.js';

defineOptions({
  name: 'c7Cascader'
})
const props = defineProps({
  // v-model 绑定值
  modelValue: {
    type: [Array, String, Number],
    default: () => []
  },
  // 异步获取数据的函数
  fetchData: {type: Function, default: null},
  // 数据格式化回调，接收原始 list，返回格式化后的 list
  dataFormatter: {type: Function, default: null},
  // el-option 的 label 字段名
  labelKey: {type: String, default: 'label'},
  // el-option 的 value 字段名
  valueKey: {type: String, default: 'value'},
  // 异步结果中列表数据所在的路径
  resultKey: {type: String, default: 'data'},
  // 静态数据列表，当 fetchData 为 null 时使用
  dataList: {type: Array, default: () => []},
  // 异步获取数据时的额外参数
  fetchParams: {type: Object, default: () => ({})},


  /**
   * 结果类型：
   * 1：绑定值为原生数组（如：[1,2,3]）
   * 2：绑定值为逗号分隔字符串（如："1,2,3"）
   * 3：绑定值为数组的最后一个元素（如：3），反显时需根据此值查找完整路径
   */
  resultType: {
    type: Number,
    default: 1
  },
  // 懒加载：当为 true 时，开启动态加载下级节点
  lazy: {
    type: Boolean,
    default: false
  },
  /**
   * 当 lazy 为 true 且 resultType = 3 时，反显可能无法匹配上级数据，
   * 此时可传入 parent_url，根据当前节点获取所有父级节点数据
   */
  parentNodeFetchFunction: {
    type: Function, default: null
  },
  checkStrictly: {
    type: Boolean,
    default: false
  },
  rootParentId: {

    type: String,
    default: -1
  }
});

const {options, loading, fetchAndUpdate} = useFetchOptions({
  fetchData: props.fetchData,
  fetchParams: props.fetchParams,
  resultKey: props.resultKey,
  dataFormatter: props.dataFormatter,
  dataList: props.dataList,
  lazy: props.lazy,

})


const props2 = {
  lazy: props.lazy,
  value: props.valueKey,
  label: props.labelKey,
  checkStrictly: props.checkStrictly,
  lazyLoad(node, resolve) {
    const {level} = node
    console.log("node", node, node.value)
    // loadLoadOptions(null == node.value ? -1 : node.value).then(res => {
    //   let resData = res.data || [];
    //   resolve(resData)
    // });
    console.log("node", (node.value ? node.value : null))
    props.fetchData(node.value ? node.value : props.rootParentId).then(res => {
      // options.value = res.data.data;
      let resData = res.data.data || [];
      resolve(resData)
    });
  },
}
// 用于向父组件更新绑定值
const emit = defineEmits(['update:modelValue']);
// 组件挂载后自动加载数据
onMounted(async () => {
  // 先加载数据（只加载一次）
  if (fetchAndUpdate && !dataLoaded.value) {
    await fetchAndUpdate('')
    dataLoaded.value = true;
  }
  // 数据加载完成后进行反显
  loadOptions(props.modelValue);
})

// 监听 modelValue 变化，用于外部更新时的反显
watch(() => props.modelValue, (newValue, oldValue) => {
  // 只有当值真正发生变化时才进行反显
  if (newValue !== undefined && newValue !== null && newValue !== oldValue) {
    console.log("modelValue 变化，触发反显:", oldValue, "->", newValue);
    loadOptions(newValue);
  }
}, {immediate: false})

const loadOptions = async (value) => {
  // 等待数据加载完成
  if (loading.value || !dataLoaded.value) {
    console.log("数据未加载完成，等待中...", "loading:", loading.value, "dataLoaded:", dataLoaded.value)
    return;
  }

  console.log("开始反显，value:", value, "resultType:", props.resultType, "options:", options.value)

  if (props.resultType == 1) {
    console.log("resultType-1")
    // resultType = 1：绑定值为原生数组
    if (Array.isArray(value)) {
      selectedValue.value = value;
    } else if (value !== null && value !== undefined) {
      // 如果不是数组但有值，尝试转换为数组
      selectedValue.value = [value];
    } else {
      selectedValue.value = [];
    }
  } else if (props.resultType == 2) {
    console.log("resultType-2")
    // resultType = 2：绑定值为逗号分隔字符串
    if (typeof value === 'string' && value.trim()) {
      selectedValue.value = value.split(',').map(item => item.trim()).filter(item => item);
    } else {
      selectedValue.value = [];
    }
  } else if (props.resultType == 3) {
    // resultType = 3：绑定值为数组的最后一个元素
    if (value !== null && value !== undefined && value !== '') {
      if (!props.lazy) {
        const path = findPath(options.value, value,props.valueKey);
        if (path.length) {
          selectedValue.value = path;
        } else {
          console.warn("未找到路径，目标值:", value)
          selectedValue.value = [];
        }
      } else {
        // 懒加载模式：调用函数获取父级路径
        if (props.parentNodeFetchFunction) {
          try {
            const res = await props.parentNodeFetchFunction(value);
            const path = jsonGet(res, props.resultKey, []);
            selectedValue.value = path;
          } catch (error) {
            console.error("懒加载模式 - 获取父级路径失败:", error)
            selectedValue.value = [];
          }
        } else {
          console.warn("懒加载模式但未提供 parentNodeFetchFunction")
          selectedValue.value = [];
        }
      }
    } else {
      selectedValue.value = [];
    }
  }
}

//
// // 计算属性：双向绑定处理，内部维护数组/字符串之间的相互转换
// const selectedValue = computed({
//   get: () => {
//     let value = props.modelValue
//     // if (props.resultType === 2 && typeof value === 'string') {
//     //   value = value.split(',')
//     // } else if (props.resultType === 3 && typeof value === 'string') {
//     //   // 绑定为 数组的最后一个元素,分为两种情况  非懒加载模式下,需要根据绑定的值 遍历出来所有节点 获取整个父级节点数据, 懒加载模式下 需要调用函数获取
//     //   if (!props.lazy) {
//     //     const path = findPath(options.value, value);
//     //     console.log("path", path)
//     //     if (path.length) {
//     //       value = path;
//     //     }
//     //   } else {
//     //     console.log("懒加载", props.lazy)
//     //     props.parentNodeFetchFunction(value).then(res => {
//     //       value = jsonGet(res, props.resultKey, [])
//     //       console.log("value", value, res, props.resultKey)
//     //     });
//     //   }
//     //   return value;
//     // }
//     return value
//   },
//   set: (value) => {
//     console.log(value, props.resultType, props.resultType === 3)
//     let payload = value
//     if (props.resultType === 2) {
//       payload = value.join(',')
//     } else if (props.resultType === 3) {
//       payload = (value && value.length ? value[value.length - 1] : '')
//     }
//     /**
//      * 结果类型：
//      * 1：绑定值为原生数组（如：[1,2,3]）
//      * 2：绑定值为逗号分隔字符串（如："1,2,3"）
//      * 3：绑定值为数组的最后一个元素（如：3），反显时需根据此值查找完整路径
//      */
//     emit('update:modelValue', payload)
//   }
// })
const selectedValue = ref([]);
// 数据加载状态标志
const dataLoaded = ref(false);
watch(selectedValue, (value) => {
  console.log(value, props.resultType, props.resultType === 3)
  let payload = value

  if (props.resultType == 1) {
    // resultType = 1：绑定值为原生数组，直接使用
    payload = Array.isArray(value) ? value : []
  } else if (props.resultType == 2) {
    // resultType = 2：绑定值为逗号分隔字符串
    if (Array.isArray(value) && value.length > 0) {
      payload = value.join(',')
    } else {
      payload = ''
    }
  } else if (props.resultType == 3) {

    // resultType = 3：绑定值为数组的最后一个元素
    payload = (value && Array.isArray(value) && value.length ? value[value.length - 1] : '')
    console.log("payload", payload)
  }

  /**
   * 结果类型：
   * 1：绑定值为原生数组（如：[1,2,3]）
   * 2：绑定值为逗号分隔字符串（如："1,2,3"）
   * 3：绑定值为数组的最后一个元素（如：3），反显时需根据此值查找完整路径
   */
  emit('update:modelValue', payload)
})

/**
 * 递归查找指定节点的完整路径（支持通过valueKey动态配置匹配字段）
 * @param {Array} nodes - 当前级联数据
 * @param {*} target - 目标节点的匹配值（支持数字/字符串类型）
 * @param {string} [valueKey='id'] - 配置匹配字段（如'id'、'deptId'，默认值为'id'）
 * @returns {Array} 完整路径数组，若未找到则返回空数组
 */
function findPath(nodes, target, valueKey = 'id') {
  // 统一转换为字符串，避免类型不匹配问题
  const targetStr = String(target);

  for (const node of nodes) {
    // 动态获取节点的匹配字段值，做容错处理（避免节点无该字段导致报错）
    const nodeMatchValue = node[valueKey] ? String(node[valueKey]) : '';

    // 匹配目标节点
    if (nodeMatchValue === targetStr) {
      return [node[valueKey]];
    }

    // 存在子节点且子节点非空时递归查找
    if (node.children && Array.isArray(node.children) && node.children.length > 0) {
      const childPath = findPath(node.children, targetStr, valueKey);
      // 子路径存在则拼接当前节点的匹配字段值
      if (childPath.length > 0) {
        return [node[valueKey], ...childPath];
      }
    }
  }

  return [];
}
</script>