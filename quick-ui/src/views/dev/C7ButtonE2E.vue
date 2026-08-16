<template>
  <div class="c7-e2e-wrap">
    <h1 data-testid="e2e-title">C7Button E2E</h1>

    <section data-testid="tc002">
      <h2>TC_002 全局组件</h2>
      <c7-button
          data-testid="c7-tc002-basic"
          btn-type="add"
          :click-function="fnResolve"
      />
    </section>

    <section data-testid="tc003">
      <h2>TC_003 预设 btnType</h2>
      <div class="row">
        <c7-button
            v-for="t in presetTypes"
            :key="t"
            :data-testid="'c7-preset-' + t"
            :btn-type="t"
            :click-function="fnResolve"
        />
      </div>
      <c7-button
          data-testid="c7-no-preset"
          label="仅文案"
          type="info"
          :click-function="fnResolve"
      />
    </section>

    <section data-testid="tc004">
      <h2>TC_004 覆盖预设</h2>
      <c7-button
          data-testid="c7-override"
          btn-type="add"
          label="自定义"
          type="warning"
          :plain="true"
          size="small"
          :click-function="fnResolve"
      />
    </section>

    <section data-testid="tc005">
      <h2>TC_005 beforeClick 否决</h2>
      <c7-button
          data-testid="c7-before-veto"
          type="primary"
          label="否决"
          :before-click="beforeClickVeto"
          :click-function="countTc005"
      />
      <span data-testid="tc005-count">{{ tc005Count }}</span>
    </section>

    <section data-testid="tc006">
      <h2>TC_006 校验失败</h2>
      <el-form ref="formRef" :model="formModel" :rules="formRules" data-testid="tc006-form">
        <el-form-item prop="name">
          <el-input v-model="formModel.name" placeholder="必填留空以失败" data-testid="tc006-name"/>
        </el-form-item>
      </el-form>
      <c7-button
          data-testid="c7-validate-fail"
          label="校验"
          :validate="true"
          :confirm="true"
          confirm-message="TC006 不应出现"
          :validate-ref="formRef"
          :click-function="countTc006"
      />
      <span data-testid="tc006-count">{{ tc006Count }}</span>
    </section>

    <section data-testid="tc007">
      <h2>TC_007 确认取消</h2>
      <c7-button
          data-testid="c7-confirm-cancel"
          label="弹出确认"
          :confirm="true"
          confirm-message="TC007 是否继续？"
          :click-function="countTc007"
      />
      <span data-testid="tc007-count">{{ tc007Count }}</span>
    </section>

    <section data-testid="tc008">
      <h2>TC_008 delete + 确认</h2>
      <c7-button
          data-testid="c7-delete-confirm"
          btn-type="delete"
          :confirm="true"
          confirm-message="TC008 删除确认"
          :click-function="countTc008"
      />
      <span data-testid="tc008-count">{{ tc008Count }}</span>
    </section>

    <section data-testid="tc009">
      <h2>TC_009 loading / busy</h2>
      <c7-button
          data-testid="c7-slow"
          label="慢请求2s"
          :click-function="fnSlowCounted"
      />
      <span data-testid="tc009-count">{{ tc009Count }}</span>
    </section>

    <section data-testid="tc010">
      <h2>TC_010 防抖计数</h2>
      <c7-button
          data-testid="c7-debounce"
          label="快resolve"
          :click-function="debounceCounted"
      />
      <span data-testid="tc010-count">{{ debounceCount }}</span>
    </section>

    <section data-testid="tc011">
      <h2>TC_011 checkSuccess false</h2>
      <c7-button
          data-testid="c7-check-fail"
          label="业务失败"
          :click-function="fnResolve"
          :check-success="checkAlwaysFalse"
      />
    </section>

    <section data-testid="tc012">
      <h2>TC_012 reject</h2>
      <c7-button
          data-testid="c7-reject"
          label="reject"
          :click-function="fnReject"
          error-message="TC012 失败"
      />
    </section>

    <section data-testid="tc013">
      <h2>TC_013 成功提示</h2>
      <c7-button
          data-testid="c7-success-msg"
          label="成功Message"
          :click-function="fnResolve"
          success-message="TC013 操作成功"
      />
      <c7-button
          data-testid="c7-success-notify"
          class="tc013-notify"
          label="成功Notification"
          :click-function="fnResolve"
          success-message="TC013 Notify成功"
          :success-notify="true"
      />
    </section>

    <section data-testid="tc014">
      <h2>TC_014 confirmFn false</h2>
      <c7-button
          data-testid="c7-confirmfn-no"
          label="confirmFn否"
          :confirm="true"
          :confirm-fn="confirmFnFalse"
          :click-function="countTc014"
      />
      <span data-testid="tc014-count">{{ tc014Count }}</span>
    </section>

    <section data-testid="tc015">
      <h2>TC_015 showErrorToast false</h2>
      <c7-button
          data-testid="c7-no-err-toast"
          label="静默reject"
          :click-function="fnReject"
          :show-error-toast="false"
      />
      <c7-button
          data-testid="c7-no-err-toast-checkfail"
          label="静默业务失败"
          :click-function="fnResolve"
          :check-success="checkAlwaysFalse"
          :show-error-toast="false"
      />
    </section>

    <section data-testid="tc-grp-data-auto">
      <h2>C7ButtonGroup 数据 mode=auto maxVisible=2</h2>
      <p class="hint">第三个按钮应在「更多」内；点击仍走 C7Button 确认链。</p>
      <c7-button-group
          data-testid="c7-grp-data-auto"
          mode="auto"
          :max-visible="2"
          :buttons="grpDataButtons"
          @before-command="grpBeforeCmd++"
          @after-command="onGrpAfterCmd"
      />
      <div class="row">
        <span data-testid="grp-before-cmd-count">{{ grpBeforeCmd }}</span>
        <span data-testid="grp-after-cmd-count">{{ grpAfterCmd }}</span>
        <span data-testid="grp-click-exec-count">{{ grpClickExec }}</span>
      </div>
    </section>

    <section data-testid="tc-grp-slot-auto">
      <h2>C7ButtonGroup 插槽 mode=auto maxVisible=2</h2>
      <c7-button-group
          ref="grpSlotRef"
          data-testid="c7-grp-slot-auto"
          mode="auto"
          :max-visible="2"
          responsive
      >
        <c7-button
            data-testid="grp-slot-btn-1"
            btn-type="add"
            label="槽1"
            :click-function="countGrpSlot"
        />
        <c7-button
            data-testid="grp-slot-btn-2"
            btn-type="edit"
            label="槽2"
            :click-function="countGrpSlot"
        />
        <c7-button
            v-if="grpSlotThirdVisible"
            data-testid="grp-slot-btn-3"
            btn-type="delete"
            label="槽3"
            :click-function="countGrpSlot"
        />
      </c7-button-group>
      <div class="row">
        <span data-testid="grp-slot-count">{{ grpSlotCount }}</span>
        <el-button data-testid="grp-slot-toggle-third" size="small" @click="toggleGrpSlotThird">
          切换第三个按钮
        </el-button>
      </div>
    </section>

    <section data-testid="tc016">
      <h2>TC_016 validateRef 无效</h2>
      <c7-button
          data-testid="c7-invalid-validate-ref"
          label="无效ref"
          :validate="true"
          :validate-ref="invalidRefObj"
          :click-function="countTc016"
      />
      <span data-testid="tc016-count">{{ tc016Count }}</span>
    </section>
  </div>
</template>

<script setup>
/** C7Button 组件 E2E 联调页：覆盖预设类型、流水线、防抖与 after-click 等用例。 */
import {computed, nextTick, ref} from 'vue'

const presetTypes = ['add', 'edit', 'delete', 'query', 'refresh', 'upload', 'download', 'submit', 'cancel']

const fnResolve = () => Promise.resolve({ok: true})
const fnReject = () => Promise.reject(new Error('tc012-reject'))

const tc005Count = ref(0)
const countTc005 = async () => {
  tc005Count.value++
  return fnResolve()
}
const beforeClickVeto = async () => false

const formRef = ref()
const formModel = ref({name: ''})
const formRules = {name: [{required: true, message: '必填', trigger: 'blur'}]}
const tc006Count = ref(0)
const countTc006 = async () => {
  tc006Count.value++
  return fnResolve()
}

const tc007Count = ref(0)
const countTc007 = async () => {
  tc007Count.value++
  return fnResolve()
}

const tc008Count = ref(0)
const countTc008 = async () => {
  tc008Count.value++
  return fnResolve()
}

const tc009Count = ref(0)
const fnSlowCounted = () =>
    new Promise((resolve) => {
      tc009Count.value++
      setTimeout(() => resolve({}), 2000)
    })

const debounceCount = ref(0)
const debounceCounted = async () => {
  debounceCount.value++
  return fnResolve()
}

const checkAlwaysFalse = () => false

const tc014Count = ref(0)
const countTc014 = async () => {
  tc014Count.value++
  return fnResolve()
}
const confirmFnFalse = async () => false

const invalidRefObj = ref({notForm: true})
const tc016Count = ref(0)
const countTc016 = async () => {
  tc016Count.value++
  return fnResolve()
}

const grpBeforeCmd = ref(0)
const grpAfterCmd = ref(0)
const grpClickExec = ref(0)
const grpDataButtons = computed(() => [
  {
    key: 'g1',
    btnType: 'query',
    label: '查',
    clickFunction: async () => {
      grpClickExec.value++
      return fnResolve()
    }
  },
  {
    key: 'g2',
    btnType: 'refresh',
    label: '刷',
    clickFunction: async () => {
      grpClickExec.value++
      return fnResolve()
    }
  },
  {
    key: 'g3',
    btnType: 'delete',
    label: '删',
    confirm: true,
    confirmMessage: 'ButtonGroup 折叠项删除确认',
    clickFunction: async () => {
      grpClickExec.value++
      return fnResolve()
    }
  }
])

function onGrpAfterCmd() {
  grpAfterCmd.value++
}

const grpSlotRef = ref()
const grpSlotThirdVisible = ref(true)
const grpSlotCount = ref(0)
const countGrpSlot = async () => {
  grpSlotCount.value++
  return fnResolve()
}

async function toggleGrpSlotThird() {
  grpSlotThirdVisible.value = !grpSlotThirdVisible.value
  await nextTick()
  grpSlotRef.value?.forceUpdate?.()
}
</script>

<style scoped>
.c7-e2e-wrap {
  padding: 24px;
}

.row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tc013-notify {
  margin-left: 8px;
}

section {
  margin-bottom: 28px;
  border-bottom: 1px solid #eee;
  padding-bottom: 16px;
}

h2 {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.hint {
  font-size: 12px;
  color: #999;
  margin: 0 0 8px;
}
</style>
