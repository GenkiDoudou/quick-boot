import { onBeforeUnmount, ref } from 'vue'

import { ElMessage } from 'element-plus'

import { runAsync, runDebug, subscribeRunStream } from '@/api/workflow'
import { formatRunDisplayOutputs } from '../utils/runOutputUtils'

/**
 * 从运行详情中提取最终展示文本（输出节点结果，支持多输出变量）。
 * @param {object} detail 运行详情
 * @returns {string}
 */
export function extractRunOutputText(detail) {
  if (!detail) return ''

  const steps = detail.steps || []
  const endSteps = steps.filter((step) => step.nodeType === 'end' && step.status === 'SUCCESS')
  const answerSteps = steps.filter((step) => step.nodeType === 'answer' && step.status === 'SUCCESS')
  const endStep = endSteps.length ? endSteps[endSteps.length - 1] : null
  const answerStep = answerSteps.length ? answerSteps[answerSteps.length - 1] : null
  const outputs = endStep?.outputs || answerStep?.outputs || detail.outputs || {}
  return formatRunDisplayOutputs(outputs)
}



/**

 * 工作流运行调试 composable（Debug / 异步 SSE、节点 runStatus 高亮、Trace）。

 * @param {object} options

 * @param {import('vue').Ref<string>} options.workflowId 工作流 ID

 * @param {import('vue').Ref<Array>} options.nodes 画布节点

 * @param {() => object} [options.getGraph] 获取 graph（预留扩展）

 * @param {(nodeId: string) => void} [options.focusNode] 定位节点回调

 * @returns {object}

 */

export function useWorkflowRun({ workflowId, nodes, focusNode, getGraph }) {

  const running = ref(false)

  const runPanelVisible = ref(false)

  const traceSteps = ref([])

  const streamText = ref('')

  const lastRunStep = ref(null)

  const streamEnabled = ref(false)



  let unsubscribeStream = null



  function closeStream() {

    if (unsubscribeStream) {

      unsubscribeStream()

      unsubscribeStream = null

    }

  }



  /**

   * 更新节点 runStatus。

   * @param {string} nodeId

   * @param {'RUNNING'|'SUCCESS'|'FAILED'|null} status

   */

  function setNodeRunStatus(nodeId, status) {

    nodes.value = nodes.value.map((n) => {

      if (n.id !== nodeId) return n

      return {

        ...n,

        data: { ...n.data, runStatus: status }

      }

    })

  }



  function clearAllRunStatus() {

    nodes.value = nodes.value.map((n) => ({

      ...n,

      data: { ...n.data, runStatus: null }

    }))

  }



  function buildPayload(inputs, stream) {

    const payload = {

      workflowId: workflowId.value,

      useDraft: true,

      stream,

      inputs: { ...inputs }

    }

    const { kbId, ...rest } = inputs

    payload.inputs = rest

    if (kbId) {

      payload.kbId = kbId

    }

    if (getGraph) {

      payload.graph = getGraph()

    }

    return payload

  }



  function applyRunOutput(detail) {

    streamText.value = extractRunOutputText(detail)

  }



  function handleStreamEvents(runId) {

    closeStream()

    streamText.value = ''

    unsubscribeStream = subscribeRunStream(runId, {

      onEvent: (event, data) => {

        if (event === 'step_start') {

          setNodeRunStatus(data.nodeId, 'RUNNING')

          const step = {

            nodeId: data.nodeId,

            nodeType: data.nodeType,

            status: 'RUNNING',

            orderNo: data.orderNo,

            inputs: data.inputs,

            outputs: null

          }

          traceSteps.value.push(step)

          lastRunStep.value = step

        } else if (event === 'llm_delta') {

          streamText.value = data.accumulated || streamText.value + (data.delta || '')

        } else if (event === 'step_end') {

          const status = data.status === 'FAILED' ? 'FAILED' : 'SUCCESS'

          setNodeRunStatus(data.nodeId, status)

          const idx = traceSteps.value.findIndex(

            (s) => s.nodeId === data.nodeId && s.status === 'RUNNING'

          )

          const step = {

            nodeId: data.nodeId,

            nodeType: data.nodeType,

            status,

            durationMs: data.durationMs,

            inputs: data.inputs,

            outputs: data.outputs

          }

          if (idx >= 0) {

            traceSteps.value[idx] = { ...traceSteps.value[idx], ...step }

          } else {

            traceSteps.value.push(step)

          }

          lastRunStep.value = step

          if ((data.nodeType === 'answer' || data.nodeType === 'end') && data.outputs) {
            streamText.value = extractRunOutputText({ outputs: data.outputs, steps: traceSteps.value })
          }

        } else if (event === 'loop_iteration') {
          if (data.phase === 'end' && data.roundResult != null && String(data.roundResult) !== '') {
            const line = String(data.roundResult)
            streamText.value = streamText.value ? `${streamText.value}\n${line}` : line
          }
        } else if (event === 'done') {

          if (data.outputs) {

            streamText.value = extractRunOutputText({ outputs: data.outputs, steps: traceSteps.value })

          }

          ElMessage.success('运行完成')

          running.value = false

        } else if (event === 'error') {

          ElMessage.error(data.message || '运行失败')

          running.value = false

        }

      },

      onError: (err) => {

        ElMessage.error(err.message || 'SSE 连接异常')

        running.value = false

      }

    })

  }



  /**

   * 执行测试运行。

   * @param {Record<string, any>} inputs Start 入参

   * @param {boolean} [useStream] 是否流式（async+SSE）

   */

  async function runTest(inputs, useStream) {

    runPanelVisible.value = true

    running.value = true

    traceSteps.value = []

    streamText.value = ''

    lastRunStep.value = null

    clearAllRunStatus()

    closeStream()



    const stream = useStream ?? streamEnabled.value



    try {

      if (stream) {

        const res = await runAsync({ ...buildPayload(inputs, true), usePublished: false })

        const runId = res.data?.runId

        if (runId) {

          handleStreamEvents(runId)

          ElMessage.success('异步运行已启动')

        } else {

          running.value = false

        }

      } else {

        const res = await runDebug(buildPayload(inputs, false))

        const detail = res.data || {}

        traceSteps.value = (detail.steps || []).map((s) => ({

          ...s,

          inputs: s.inputs,

          outputs: s.outputs

        }))

        if (detail.steps?.length) {

          lastRunStep.value = traceSteps.value[traceSteps.value.length - 1]

          detail.steps.forEach((s) => {

            const st = s.status === 'FAILED' ? 'FAILED' : 'SUCCESS'

            setNodeRunStatus(s.nodeId, st)

          })

        }

        applyRunOutput(detail)

        ElMessage.success('测试运行完成')

        running.value = false

      }

    } catch (err) {
      const msg = err?.message || err?.msg || '运行失败'
      if (msg && msg !== 'error') {
        ElMessage.error(msg)
      }
      running.value = false
    }

  }



  /**

   * 点击 Trace 步骤定位节点并更新 lastRunStep。

   * @param {object} step

   */

  function focusTraceStep(step) {

    if (!step?.nodeId) return

    lastRunStep.value = step

    focusNode?.(step.nodeId)

  }



  onBeforeUnmount(() => {

    closeStream()

  })



  return {

    running,

    runPanelVisible,

    traceSteps,

    streamText,

    lastRunStep,

    streamEnabled,

    runTest,

    focusTraceStep,

    closeStream,

    clearAllRunStatus

  }

}

