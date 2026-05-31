<template>

  <el-button

      v-bind="filteredAttrs"

      :type="mergedType"

      :plain="mergedPlain"

      :size="size"

      :disabled="disabled"

      :loading="internalLoading"

      :icon="hasDefaultSlot ? undefined : mergedIcon"

      @click="debouncedRun"

  >

    <slot>{{ mergedLabel }}</slot>

  </el-button>

</template>



<script setup>

defineOptions({inheritAttrs: false})



/**

 * C7 业务按钮：在 ElButton 上封装「校验 → 确认 → 异步执行 → 成功判定 → 提示 → 回调」固定流水线。

 *

 * 约定（与 axios 封装一致）：{@code clickFunction} 内若请求失败应走 reject；resolve 表示 HTTP/封装层已成功，

 * 可选再用 checkSuccess 区分业务成功与否。执行中 busy 会阻止整条流水线重入；internalLoading

 * 仅在 {@code clickFunction} 执行期为 true。

 *

 * @emits before-click 流水线开始（校验前）

 * @emits success 业务成功，载荷为 {@code clickFunction} resolve 值

 * @emits error 校验失败、用户取消确认、请求 reject、或 {@code checkSuccess} 为 false，载荷为 Error 或其它 reason

 * @emits after-click 流水线结束，载荷为是否整体成功（确认取消 / 校验失败 / veto / 异常 / 业务失败均为 false）

 */

import {computed, ref, unref, useAttrs, useSlots, watch, onUnmounted} from 'vue'

import debounce from 'lodash/debounce'

import {ElMessage, ElMessageBox, ElNotification} from 'element-plus'

import { endOperation } from '@/monitor/operationContext'

import {

  Plus,

  Edit,

  Delete,

  Search,

  Refresh,

  Upload,

  Download,

  Check,

  Close

} from '@element-plus/icons-vue'



/** @type {Record<string, { label: string, type: string, plain: boolean, icon: object }>} */

const PRESETS = {

  add: {label: '新增', type: 'primary', plain: false, icon: Plus},

  edit: {label: '修改', type: 'primary', plain: false, icon: Edit},

  delete: {label: '删除', type: 'danger', plain: false, icon: Delete},

  query: {label: '查询', type: 'primary', plain: false, icon: Search},

  refresh: {label: '重置', type: 'default', plain: false, icon: Refresh},

  upload: {label: '上传', type: 'primary', plain: false, icon: Upload},

  download: {label: '下载', type: 'primary', plain: false, icon: Download},

  submit: {label: '提交', type: 'primary', plain: false, icon: Check},

  cancel: {label: '取消', type: 'default', plain: false, icon: Close}

}



const props = defineProps({

  /** 预设类型：决定默认文案、图标、type、plain（可被显式 props 覆盖） */

  btnType: {

    type: String,

    default: ''

  },

  /** 业务异步函数；失败请 reject，成功 resolve（与项目 request 拦截器一致） */

  clickFunction: {

    type: Function,

    required: true

  },

  /**

   * 防抖窗口（ms）：lodash debounce {@code leading: true}、{@code trailing: false}，

   * 首击立即响应，窗口内后续点击忽略；与 {@code busy} 互补。

   */

  debounceDelay: {

    type: Number,

    default: 300

  },

  /** 是否在执行前弹出确认框（或 {@link confirmFn}） */

  confirm: {

    type: Boolean,

    default: false

  },

  /** 默认确认文案（未传 {@link confirmFn} 时使用） */

  confirmMessage: {

    type: String,

    default: '确定执行该操作？'

  },

  /**

   * 自定义确认逻辑，返回 true 继续、false 中止（不发 error 提示）。

   * 与 {@link confirm} 同时为 true 时优先本函数。

   */

  confirmFn: {

    type: Function,

    default: null

  },

  /** 是否先执行表单校验 */

  validate: {

    type: Boolean,

    default: false

  },

  /**

   * Element Plus Form 实例（或 ref），需具备 {@code validate(): Promise<void>}；

   * 传父组件中 {@code const formRef = ref()} 绑定到 {@code el-form} 后的 ref 即可。

   */

  validateRef: {

    type: Object,

    default: null

  },

  /**

   * 返回 false 则中止流水线（不发成功/失败提示），并触发 {@code after-click(false)}。

   * 支持同步或异步。

   */

  beforeClick: {

    type: Function,

    default: null

  },

  /**

   * 在 {@code emit('before-click')} 之前执行；供 {@code C7ButtonGroup} 等包裹组件注入组级逻辑（如 {@code before-command}）。

   * 支持同步或返回 Promise；抛错或 reject 将中止流水线并触发 {@code after-click(false)}（失败提示规则与同流水线其它异常一致）。

   */

  beforePipeline: {

    type: Function,

    default: null

  },

  /**

   * 在 {@code clickFunction} resolve 后调用；返回 false 视为业务失败（走 error 提示与 {@code after-click(false)}）。

   */

  checkSuccess: {

    type: Function,

    default: () => true

  },

  /** 成功后的文案；有值则提示（除非 {@link successNotify} 为 true 时用通知） */

  successMessage: {

    type: String,

    default: ''

  },

  /** 为 true 时成功提示用 ElNotification，内容为 {@link successMessage} 或默认「操作成功」 */

  successNotify: {

    type: Boolean,

    default: false

  },

  /** 是否在失败时弹出错误提示（确认取消、校验失败、beforeClick 否决不提示） */

  showErrorToast: {

    type: Boolean,

    default: true

  },

  /** 失败提示兜底文案（catch 时优先于 Error.message） */

  errorMessage: {

    type: String,

    default: ''

  },

  /** 覆盖预设的按钮文案 */

  label: {

    type: String,

    default: ''

  },

  /** 覆盖预设的 Element 按钮 type */

  type: {

    type: String,

    default: ''

  },

  /** 覆盖预设 plain */

  plain: {

    type: Boolean,

    default: undefined

  },

  /** 尺寸，同 ElButton */

  size: {

    type: String,

    default: ''

  },

  /** 禁用 */

  disabled: {

    type: Boolean,

    default: false

  }

})



const emit = defineEmits(['before-click', 'after-click', 'success', 'error'])



const attrs = useAttrs()

const slots = useSlots()



const hasDefaultSlot = computed(() => !!slots.default)



/** 透传除 Vue 事件外的属性（避免与内部点击流水线重复触发）；勿在外层写 @click，请用 emit / clickFunction */

const filteredAttrs = computed(() => {

  const rest = {...attrs}

  Object.keys(rest).forEach((k) => {

    if (k.startsWith('on') && k.length > 2 && /[A-Z]/.test(k[2])) {

      delete rest[k]

    }

  })

  return rest

})



const preset = computed(() => {

  const key = props.btnType

  return key && PRESETS[key] ? PRESETS[key] : null

})



const mergedLabel = computed(() => props.label || preset.value?.label || '')

const mergedType = computed(() => props.type || preset.value?.type || 'default')

const mergedPlain = computed(() =>

    props.plain !== undefined ? props.plain : (preset.value?.plain ?? false)

)

const mergedIcon = computed(() => preset.value?.icon ?? null)



const busy = ref(false)

const internalLoading = ref(false)



let debouncedRun = debounce(runPipeline, props.debounceDelay, {leading: true, trailing: false})



watch(

    () => props.debounceDelay,

    (ms) => {

      debouncedRun.cancel()

      debouncedRun = debounce(runPipeline, ms, {leading: true, trailing: false})

    }

)



onUnmounted(() => {

  debouncedRun.cancel()

})



async function runPipeline() {

  if (busy.value) {

    return

  }

  busy.value = true

  try {

    if (typeof props.beforePipeline === 'function') {

      try {

        await props.beforePipeline()

      } catch (err) {

        emit('error', err)

        if (props.showErrorToast) {

          const text =

              props.errorMessage ||

              (err && err.message) ||

              '操作失败'

          ElMessage.error(text)

        }

        emit('after-click', false)

        return

      }

    }

    emit('before-click')



    if (typeof props.beforeClick === 'function') {

      const ok = await props.beforeClick()

      if (ok === false) {

        emit('after-click', false)

        return

      }

    }



    if (props.validate) {

      const form = unref(props.validateRef)

      if (!form || typeof form.validate !== 'function') {

        // 配置错误：与「表单校验失败」区分，不弹 toast（对齐 spec：校验失败无错误提示）

        emit('error', new Error('validateRef 无效'))

        emit('after-click', false)

        return

      }

      try {

        await form.validate()

      } catch {

        emit('after-click', false)

        return

      }

    }



    if (props.confirm) {

      try {

        if (typeof props.confirmFn === 'function') {

          const ok = await props.confirmFn()

          if (!ok) {

            emit('after-click', false)

            return

          }

        } else {

          await ElMessageBox.confirm(props.confirmMessage, '系统提示', {

            confirmButtonText: '确定',

            cancelButtonText: '取消',

            type: 'warning'

          })

        }

      } catch {

        emit('after-click', false)

        return

      }

    }



    internalLoading.value = true

    try {

      const result = await props.clickFunction()

      if (!props.checkSuccess(result)) {

        const biz = new Error('业务处理未通过')

        emit('error', biz)

        if (props.showErrorToast) {

          ElMessage.error(props.errorMessage || biz.message)

        }

        emit('after-click', false)

        return

      }

      emit('success', result)

      const msg = props.successMessage || (props.successNotify ? '操作成功' : '')

      if (msg) {

        if (props.successNotify) {

          ElNotification.success({title: '提示', message: msg})

        } else {

          ElMessage.success(msg)

        }

      }

      emit('after-click', true)

    } catch (err) {

      emit('error', err)

      if (props.showErrorToast) {

        const text =

            props.errorMessage ||

            (err && err.message) ||

            '操作失败'

        ElMessage.error(text)

      }

      emit('after-click', false)

    } finally {

      internalLoading.value = false

    }

  } finally {

    busy.value = false

    // 与全局 click 监听的 beginOperation 成对，避免删除等操作完成后批次永不 flush
    endOperation()

  }

}

</script>

