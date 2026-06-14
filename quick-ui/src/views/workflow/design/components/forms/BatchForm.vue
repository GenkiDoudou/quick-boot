<template>

  <div class="batch-form">

    <el-form label-position="top" size="small">

      <WfVariableTableSection

        title="输入参数"

        tooltip="每个参数须引用上游数组；批处理体内以 {{batchId.index}}、{{batchId.参数名}} 引用当前轮次的索引与数组元素"

        :columns="inputColumns"

        :has-rows="inputRows.length > 0"

        empty-text="请先添加输入参数：参数名自定义，参数值选择上游数组变量"

        add-title="添加输入"

        @add="addInput"

      >

        <div v-for="(row, idx) in inputRows" :key="row._id" class="wf-vt-section__row">

          <el-input

            v-model="row.key"

            size="small"

            placeholder="参数名"

            class="wf-vt-section__col wf-vt-section__col--name"

            @change="syncInput"

          />

          <ConditionValueField

            v-model="row.source"

            :variable-tree="variableTree"

            placeholder="数组引用，如 {{start_1.items}}"

            class="wf-vt-section__col wf-vt-section__col--value"

            @update:model-value="syncInput"

          />

          <el-button

            link

            type="danger"

            class="wf-vt-section__col wf-vt-section__col--actions"

            title="删除"

            @click.stop="removeInput(idx)"

          >

            <el-icon :size="16"><Minus /></el-icon>

          </el-button>

        </div>

      </WfVariableTableSection>



      <el-form-item label="并行上限">

        <el-radio-group v-model="local.parallelMode" @change="sync">

          <el-radio value="fixed">固定值（1–10）</el-radio>

          <el-radio value="ref">引用上游数值</el-radio>

        </el-radio-group>

        <el-input-number

          v-if="local.parallelMode === 'fixed'"

          v-model="local.parallelLimit"

          :min="1"

          :max="10"

          controls-position="right"

          class="batch-form__num"

          @change="sync"

        />

        <ConditionValueField

          v-else

          v-model="local.parallelLimitSource"

          :variable-tree="variableTree"

          placeholder="如 {{start_1.parallel}}"

          class="batch-form__parallel-ref"

          @update:model-value="sync"

        />

      </el-form-item>



      <el-form-item label="最大运行次数">

        <el-input-number

          v-model="local.maxRuns"

          :min="1"

          :max="200"

          controls-position="right"

          @change="sync"

        />

        <div class="batch-form__hint">按最短输入数组长度与 maxRuns 取较小值</div>

      </el-form-item>



      <WfVariableTableSection

        title="输出参数"

        tooltip="从批处理体内节点收集字段，汇总为数组输出"

        :columns="outputColumns"

        :has-rows="outputRows.length > 0"

        empty-text="请添加输出参数：输出名、体内节点与字段"

        add-title="添加输出"

        @add="addOutput"

      >

        <div v-for="(row, idx) in outputRows" :key="row._id" class="wf-vt-section__row batch-form__out-row">

          <el-input

            v-model="row.key"

            size="small"

            placeholder="输出名"

            class="wf-vt-section__col wf-vt-section__col--name"

            @change="syncOutput"

          />

          <el-select

            v-model="row.nodeId"

            placeholder="体内节点"

            clearable

            filterable

            size="small"

            class="wf-vt-section__col wf-vt-section__col--type"

            @change="syncOutput"

          >

            <el-option

              v-for="n in bodyOutputNodes"

              :key="n.id"

              :label="`${n.label} · ${n.id}`"

              :value="n.id"

            />

          </el-select>

          <el-input

            v-model="row.field"

            size="small"

            placeholder="字段"

            class="wf-vt-section__col wf-vt-section__col--flex"

            @change="syncOutput"

          />

          <el-button

            link

            type="danger"

            class="wf-vt-section__col wf-vt-section__col--actions"

            title="删除"

            @click.stop="removeOutput(idx)"

          >

            <el-icon :size="16"><Minus /></el-icon>

          </el-button>

        </div>

      </WfVariableTableSection>

    </el-form>

  </div>

</template>



<script setup>

import { computed, reactive, ref, watch } from 'vue'

import { Minus } from '@element-plus/icons-vue'

import ConditionValueField from './ConditionValueField.vue'

import WfVariableTableSection from './shared/WfVariableTableSection.vue'

import { getNodeLabel } from '../../nodeMeta'

import { collectBatchBodyChildIds } from '../../utils/batchUtils'



defineOptions({ name: 'BatchForm' })



const props = defineProps({

  modelValue: { type: Object, required: true },

  nodeId: { type: String, default: '' },

  variableTree: { type: Array, default: () => [] },

  canvasNodes: { type: Array, default: () => [] },

  errors: { type: Object, default: () => ({}) }

})



const emit = defineEmits(['update:modelValue'])



const inputColumns = [

  { key: 'name', label: '参数名', class: 'wf-vt-section__col--name' },

  { key: 'value', label: '数组引用', class: 'wf-vt-section__col--flex' }

]



const outputColumns = [

  { key: 'name', label: '输出名', class: 'wf-vt-section__col--name' },

  { key: 'node', label: '体内节点', class: 'wf-vt-section__col--type' },

  { key: 'field', label: '字段', class: 'wf-vt-section__col--flex' }

]



const local = reactive({

  parallelMode: 'fixed',

  parallelLimit: 10,

  parallelLimitSource: '',

  maxRuns: 100

})



const inputRows = ref([])

const outputRows = ref([])

let syncing = false

let inputRowSeq = 0

let outputRowSeq = 0



const bodyId = computed(() => props.modelValue?.bodyId || '')



const bodyOutputNodes = computed(() => {

  const ids = collectBatchBodyChildIds(bodyId.value, props.canvasNodes)

  return (props.canvasNodes || [])

    .filter((n) => ids.has(n.id))

    .map((n) => ({

      id: n.id,

      label: n.data?.label || getNodeLabel(n.data?.wfType)

    }))

})



watch(

  () => props.modelValue,

  (val) => {

    if (syncing) return

    local.parallelMode = val?.parallelLimitSource ? 'ref' : 'fixed'

    local.parallelLimit = val?.parallelLimit ?? 10

    local.parallelLimitSource = val?.parallelLimitSource || ''

    local.maxRuns = val?.maxRuns ?? 100



    const inputs = Array.isArray(val?.inputParameters) ? val.inputParameters : []

    inputRows.value = inputs.map((item, idx) => ({

      key: item?.key || '',

      source: item?.source || '',

      _id: inputRows.value[idx]?._id || `in_${++inputRowSeq}`

    }))

    if (!inputRows.value.length) {

      inputRows.value = [{ key: '', source: '', _id: `in_${++inputRowSeq}` }]

    }



    const outputs = Array.isArray(val?.outputParameters) ? val.outputParameters : []

    outputRows.value = outputs.map((item, idx) => ({

      key: item?.key || '',

      nodeId: item?.nodeId || '',

      field: item?.field || 'result',

      _id: outputRows.value[idx]?._id || `out_${++outputRowSeq}`

    }))

    if (!outputRows.value.length) {

      outputRows.value = [{ key: '', nodeId: '', field: 'result', _id: `out_${++outputRowSeq}` }]

    }

  },

  { immediate: true, deep: true }

)



function buildPayload() {

  return {

    ...props.modelValue,

    ...local,

    parallelLimitSource: local.parallelMode === 'ref' ? local.parallelLimitSource : '',

    inputParameters: inputRows.value.map((r) => ({

      key: (r.key || '').trim(),

      source: r.source || ''

    })),

    outputParameters: outputRows.value.map((r) => ({

      key: (r.key || '').trim(),

      nodeId: (r.nodeId || '').trim(),

      field: (r.field || 'result').trim()

    }))

  }

}



function emitUpdate() {

  syncing = true

  emit('update:modelValue', buildPayload())

  queueMicrotask(() => {

    syncing = false

  })

}



function syncInput() {

  emitUpdate()

}



function syncOutput() {

  emitUpdate()

}



function sync() {

  emitUpdate()

}



function addInput() {

  inputRows.value.push({ key: '', source: '', _id: `in_${++inputRowSeq}` })

  emitUpdate()

}



function removeInput(idx) {

  inputRows.value.splice(idx, 1)

  if (!inputRows.value.length) {

    inputRows.value.push({ key: '', source: '', _id: `in_${++inputRowSeq}` })

  }

  emitUpdate()

}



function addOutput() {

  outputRows.value.push({ key: '', nodeId: '', field: 'result', _id: `out_${++outputRowSeq}` })

  emitUpdate()

}



function removeOutput(idx) {

  outputRows.value.splice(idx, 1)

  if (!outputRows.value.length) {

    outputRows.value.push({ key: '', nodeId: '', field: 'result', _id: `out_${++outputRowSeq}` })

  }

  emitUpdate()

}

</script>



<style scoped lang="scss">

.batch-form__hint {

  font-size: 12px;

  color: #909399;

  margin-top: 6px;

  line-height: 1.4;

}



.batch-form__num {

  margin-top: 8px;

}



.batch-form__parallel-ref {

  margin-top: 8px;

  width: 100%;

}



.batch-form__out-row {

  align-items: flex-start;

}

</style>


