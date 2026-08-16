<template>

  <c7-dialog v-model="visible" :title="form.menuId ? '修改菜单' : '新增菜单'" width="880px" :on-confirm="submit">

    <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="menu-form">

      <el-row :gutter="20">

        <el-col :span="24">

          <el-form-item prop="parentId">
            <template #label>
              <MenuFormLabel text="上级菜单" :tip="MENU_TIPS.parentId" />
            </template>

            <c7-tree-select

              v-model="form.parentId"

              :data-list="treeData"

              value-key="id"

              label-key="label"

              children-key="children"

              :check-strictly="true"

              :default-expand-all="false"

              filterable

              placeholder="请选择上级菜单"

              value-type="string"

              @change="onParentChange"

            />

          </el-form-item>

        </el-col>

      </el-row>

      <el-row :gutter="20">

        <el-col :span="24">

          <el-form-item prop="menuType">
            <template #label>
              <MenuFormLabel text="菜单类型" :tip="MENU_TIPS.menuType" />
            </template>

            <el-radio-group v-model="form.menuType" @change="onTypeChange">

              <el-radio label="M">目录</el-radio>

              <el-radio label="C">菜单</el-radio>

              <el-radio label="F">按钮</el-radio>

            </el-radio-group>

          </el-form-item>

        </el-col>

      </el-row>

      <el-row v-if="form.menuType === 'C'" :gutter="20">

        <el-col :span="24">

          <el-form-item prop="openType">
            <template #label>
              <MenuFormLabel text="打开方式" :tip="MENU_TIPS.openType" />
            </template>

            <el-radio-group v-model="form.openType" @change="onOpenTypeChange">

              <el-radio value="normal">普通菜单</el-radio>

              <el-radio value="frame">外链</el-radio>

              <el-radio value="report">积木报表</el-radio>

              <el-radio value="bi">BI 大屏</el-radio>

            </el-radio-group>

          </el-form-item>

        </el-col>

      </el-row>

      <el-row v-if="form.menuType === 'C' && form.openType === 'report'" :gutter="20">

        <el-col :span="24">

          <el-form-item prop="jimuResourceId">
            <template #label>
              <MenuFormLabel text="选择报表" :tip="MENU_TIPS.jimuReport" />
            </template>

            <el-select

              v-model="form.jimuResourceId"

              filterable

              clearable

              placeholder="请选择报表"

              style="width: 100%"

              :loading="catalogLoading"

              @change="onJimuResourceChange"

            >

              <el-option

                v-for="item in reportOptions"

                :key="item.id"

                :label="formatCatalogLabel(item)"

                :value="item.id"

              />

            </el-select>

          </el-form-item>

        </el-col>

      </el-row>

      <el-row v-if="form.menuType === 'C' && form.openType === 'bi'" :gutter="20">

        <el-col :span="24">

          <el-form-item prop="jimuResourceId">
            <template #label>
              <MenuFormLabel text="选择大屏" :tip="MENU_TIPS.jimuBi" />
            </template>

            <el-select

              v-model="form.jimuResourceId"

              filterable

              clearable

              placeholder="请选择 BI 大屏"

              style="width: 100%"

              :loading="catalogLoading"

              @change="onJimuResourceChange"

            >

              <el-option

                v-for="item in biOptions"

                :key="item.id"

                :label="formatCatalogLabel(item)"

                :value="item.id"

              />

            </el-select>

          </el-form-item>

        </el-col>

      </el-row>

      <el-row :gutter="20">

        <el-col :span="12">

          <el-form-item prop="menuName">
            <template #label>
              <MenuFormLabel
                :text="form.menuType === 'F' ? '按钮名称' : '菜单名称'"
                :tip="form.menuType === 'F' ? MENU_TIPS.buttonName : MENU_TIPS.menuName"
              />
            </template>

            <el-input v-model="form.menuName" />

          </el-form-item>

        </el-col>

        <el-col :span="12">

          <el-form-item prop="orderNum">
            <template #label>
              <MenuFormLabel text="显示排序" :tip="MENU_TIPS.orderNum" />
            </template>

            <el-input-number v-model="form.orderNum" :min="0" :max="9999" style="width: 100%" />

          </el-form-item>

        </el-col>

      </el-row>

      <el-row v-if="form.menuType !== 'F'" :gutter="20">

        <el-col :span="12">

          <el-form-item prop="icon">
            <template #label>
              <MenuFormLabel text="菜单图标" :tip="MENU_TIPS.icon" />
            </template>

            <el-popover

              v-model:visible="iconPopoverVisible"

              placement="bottom-start"

              :width="540"

              trigger="click"

            >

              <template #reference>

                <el-input

                  v-model="form.icon"

                  placeholder="点击选择图标"

                  readonly

                  @blur="showSelectIcon"

                >

                  <template #prefix>

                    <svg-icon

                      v-if="form.icon"

                      :icon-class="form.icon"

                      style="width: 16px; height: 16px"

                    />

                    <el-icon v-else style="width: 16px; height: 16px"><Search /></el-icon>

                  </template>

                </el-input>

              </template>

              <icon-select ref="iconSelectRef" :active-icon="form.icon" @selected="onIconSelected" />

            </el-popover>

          </el-form-item>

        </el-col>

        <el-col v-if="!hidePathForBlankLink" :span="12">

          <el-form-item prop="path">
            <template #label>
              <MenuFormLabel text="路由地址" :tip="MENU_TIPS.path" />
            </template>

            <el-input v-model="form.path" :placeholder="pathPlaceholder" />

          </el-form-item>

        </el-col>

      </el-row>

      <el-row v-if="showComponentRow" :gutter="20">

        <el-col :span="12">

          <el-form-item prop="component">
            <template #label>
              <MenuFormLabel text="组件路径" :tip="MENU_TIPS.component" />
            </template>

            <el-input

              v-model="form.component"

              :placeholder="form.menuType === 'M' ? '目录一般为 Layout' : '如 system/menu/index'"

            />

          </el-form-item>

        </el-col>

        <el-col :span="12">

          <el-form-item prop="routeName">
            <template #label>
              <MenuFormLabel text="路由名称" :tip="MENU_TIPS.routeName" />
            </template>
            <el-input
              v-model="form.routeName"
              :placeholder="form.openType !== 'normal' ? '建议填写；积木/外链菜单用于标签页识别' : ''"
            />
          </el-form-item>

        </el-col>

      </el-row>

      <el-row v-if="showRouteNameOnlyRow" :gutter="20">

        <el-col :span="12">

          <el-form-item prop="routeName">
            <template #label>
              <MenuFormLabel text="路由名称" :tip="MENU_TIPS.routeName" />
            </template>
            <el-input
              v-model="form.routeName"
              :placeholder="form.openType !== 'normal' ? '建议填写；积木/外链菜单用于标签页识别' : ''"
            />
          </el-form-item>

        </el-col>

      </el-row>

      <el-row v-if="form.menuType !== 'F'" :gutter="20">

        <el-col v-if="showIsExternalLinkField" :span="12">

          <el-form-item prop="isExternalLink">
            <template #label>
              <MenuFormLabel text="是否外链" :tip="MENU_TIPS.isExternalLink" />
            </template>

            <el-radio-group v-model="form.isExternalLink" @change="onIsExternalLinkChange">

              <el-radio value="0">否</el-radio>

              <el-radio value="1">是</el-radio>

            </el-radio-group>

          </el-form-item>

        </el-col>

        <el-col :span="showIsExternalLinkField ? 12 : 24">

          <el-form-item prop="visible">
            <template #label>
              <MenuFormLabel text="显示状态" :tip="MENU_TIPS.visible" />
            </template>

            <el-radio-group v-model="form.visible">

              <el-radio value="0">显示</el-radio>

              <el-radio value="1">隐藏</el-radio>

            </el-radio-group>

          </el-form-item>

        </el-col>

      </el-row>

      <el-row v-if="showLinkOpenMode" :gutter="20">

        <el-col :span="24">

          <el-form-item prop="linkOpenMode">
            <template #label>
              <MenuFormLabel text="打开目标" :tip="MENU_TIPS.linkOpenMode" />
            </template>

            <el-radio-group v-model="form.linkOpenMode" @change="onLinkOpenModeChange">

              <el-radio value="iframe">内嵌打开</el-radio>

              <el-radio value="blank">新标签页</el-radio>

            </el-radio-group>

          </el-form-item>

        </el-col>

      </el-row>

      <el-row v-if="showFrameQuery" :gutter="20">

        <el-col :span="24">

          <el-form-item prop="frameQuery">
            <template #label>
              <MenuFormLabel text="外链地址" :tip="MENU_TIPS.frameQuery" />
            </template>

            <el-input v-model="form.frameQuery" :placeholder="frameQueryPlaceholder" />

          </el-form-item>

        </el-col>

      </el-row>

      <el-row v-if="form.menuType === 'M' || form.menuType === 'C'" :gutter="20">

        <el-col :span="12">

          <el-form-item prop="isCache">
            <template #label>
              <MenuFormLabel text="是否缓存" :tip="MENU_TIPS.isCache" />
            </template>

            <el-radio-group v-model="form.isCache">

              <el-radio value="0">缓存</el-radio>

              <el-radio value="1">不缓存</el-radio>

            </el-radio-group>

          </el-form-item>

        </el-col>

      </el-row>

      <el-row :gutter="20">

        <el-col :span="24">

          <el-form-item prop="perms">
            <template #label>
              <MenuFormLabel text="权限字符" :tip="MENU_TIPS.perms" />
            </template>

            <div class="perms-dynamic">

              <div v-for="(_, index) in permsList" :key="index" class="perms-dynamic__row">

                <el-input

                  v-model="permsList[index]"

                  placeholder="如 system:menu:list"

                  clearable

                />

                <el-button type="danger" link @click="removePermsRow(index)">删除</el-button>

              </div>

              <el-button type="primary" link @click="addPermsRow">添加权限</el-button>

              <div class="perms-dynamic__hint">保存时合并为英文逗号并去重；中文逗号会转为英文逗号。</div>

            </div>

          </el-form-item>

        </el-col>

      </el-row>

      <el-row :gutter="20">

        <el-col :span="12">

          <el-form-item prop="status">
            <template #label>
              <MenuFormLabel
                :text="form.menuType === 'F' ? '按钮状态' : '菜单状态'"
                :tip="form.menuType === 'F' ? MENU_TIPS.buttonStatus : MENU_TIPS.menuStatus"
              />
            </template>

            <el-radio-group v-model="form.status">

              <el-radio v-for="d in sys_normal_disable" :key="d.value" :value="d.value">{{ d.label }}</el-radio>

            </el-radio-group>

          </el-form-item>

        </el-col>

      </el-row>

      <el-row :gutter="20">

        <el-col :span="24">

          <el-form-item prop="remark">
            <template #label>
              <MenuFormLabel text="备注" :tip="MENU_TIPS.remark" />
            </template>
            <el-input v-model="form.remark" type="textarea" :rows="2" />

          </el-form-item>

        </el-col>

      </el-row>

    </el-form>

  </c7-dialog>

</template>



<script setup>
/**
 * 菜单新增/修改弹窗：目录/菜单/按钮、外链/积木/BI 打开方式映射为后端 is_frame/path/query。
 * 通过 defineExpose({ open }) 供 index.vue 调用。
 */
import { computed, nextTick, ref } from 'vue'

import { ElMessage } from 'element-plus'

import { Search } from '@element-plus/icons-vue'

import { addMenu, getMenu, treeselectMenu, updateMenu } from '@/api/system/menu'

import { listJimuBiPages, listJimuReports } from '@/api/report/jimu'

import { useDict } from '@/utils/dict'

import IconSelect from '@/components/IconSelect/index.vue'

import MenuFormLabel from './MenuFormLabel.vue'



/** 表单项标签旁问号说明（对齐 RuoYi-Vue3 菜单表单） */
const MENU_TIPS = {
  parentId: '选择上级菜单；顶级一般选根节点。',
  menuType: '目录（M）用于侧栏分组；菜单（C）为可访问页面；按钮（F）为权限点，不生成路由。',
  openType: '普通菜单加载 Vue 组件；外链可选内嵌 iframe 或新标签页；积木/BI 固定内嵌。',
  jimuReport: '从积木报表目录选择；保存后预览地址自动写入 query（/jmreport/view/{id}）。',
  jimuBi: '从 BI 大屏目录选择；保存后预览地址自动写入 query（/drag/view?pageId=…）。',
  linkOpenMode: '内嵌：Layout 内 iframe 打开；新标签页：侧栏点击后在浏览器新窗口打开。',
  frameQuery: '内嵌时可填相对路径（拼 base-url）或 http(s) 地址；新标签页须以 http:// 或 https:// 开头。',
  menuName: '侧栏、标签页或权限树中显示的名称。',
  buttonName: '权限树中显示的按钮名称。',
  orderNum: '显示顺序，数字越小越靠前。',
  icon: '侧栏菜单图标，从图标库选择 SVG 名称。',
  path: '侧栏路由 path（英文短路径，勿填 http 地址）。内嵌外链时实际 URL 填在「外链地址」；新标签页时 URL 填在「外链地址」且可不填此项。',
  component: 'views 下组件路径，如 system/user/index；顶级目录为 Layout，子级目录为 ParentView。',
  routeName: 'Vue Router 的 name，建议英文驼峰；用于标签页与 keep-alive；积木/外链菜单建议填写。',
  isExternalLink: '选「是」表示访问外部地址；下方可选择内嵌 iframe 或浏览器新标签页打开。',
  visible: '选隐藏则不出现在侧栏，路由仍可能被直接访问。',
  isCache: '选缓存则 keep-alive 生效，需 routeName 与页面组件 name 一致；iframe 建议不缓存。',
  perms: '权限标识，如 system:menu:list；按钮类型必填；多个以英文逗号分隔入库。',
  menuStatus: '选停用则菜单不可用，侧栏不展示且路由不可访问。',
  buttonStatus: '选停用则按钮权限不可用。',
  remark: '仅管理端备注，不影响路由与权限。',
}



const ROOT = '-1'



const emit = defineEmits(['success'])

const { sys_normal_disable } = useDict('sys_normal_disable')



const visible = ref(false)

const formRef = ref(null)

const treeData = ref([])

const iconPopoverVisible = ref(false)

const iconSelectRef = ref(null)

const reportOptions = ref([])

const biOptions = ref([])

const catalogLoading = ref(false)

/** 选择报表/大屏后是否由系统自动填充名称、路由（用户手改后不再覆盖） */
const autoMeta = ref({
  menuName: false,
  path: false,
})



const defaultForm = () => ({

  menuId: null,

  parentId: ROOT,

  menuType: 'M',

  menuName: '',

  orderNum: 0,

  path: '',

  component: 'Layout',

  query: '',

  routeName: '',

  isFrame: '0',

  isCache: '0',

  visible: '0',

  status: '0',

  perms: '',

  icon: '',

  remark: '',

  /** 是否外链（UI 字段，不落库；保存时映射 is_frame / path / query） */
  isExternalLink: '0',

  openType: 'normal',

  jimuResourceId: '',

  frameQuery: '',

  /** 外链打开目标：iframe 内嵌 / blank 新标签页（不落库） */
  linkOpenMode: 'iframe',

})



const form = ref(defaultForm())

/** 权限字符多行编辑，提交前同步到 form.perms */
const permsList = ref([''])



function reqRule(message, trigger = 'blur') {

  return [{ required: true, message, trigger }]

}



const pathPlaceholder = computed(() => {
  const t = form.value.menuType
  const ot = form.value.openType
  const ext = form.value.isExternalLink === '1'
  if (t === 'C' && (ot === 'report' || ot === 'bi' || (ext && form.value.linkOpenMode === 'iframe'))) {
    return '英文短路径，如 sales-monthly（选报表后可自动生成）'
  }
  if (t === 'M' && ext && form.value.linkOpenMode === 'iframe') {
    return '侧栏路由段，如 external-link'
  }
  if (t === 'M') {
    return '顶级目录如 visual 或 /visual'
  }
  return '如 system 或 user；子菜单填相对段，勿以 / 开头'
})

const hidePathForBlankLink = computed(() => {
  return form.value.isExternalLink === '1' && form.value.linkOpenMode === 'blank'
})

/** 目录与菜单（非积木/BI）展示「是否外链」 */
const showIsExternalLinkField = computed(() => {
  const f = form.value
  if (f.menuType === 'M') return true
  if (f.menuType === 'C' && f.openType !== 'report' && f.openType !== 'bi') return true
  return false
})

/** 是否外链=是 时展示内嵌/新标签页（积木/BI 固定内嵌，不展示） */
const showLinkOpenMode = computed(() => {
  return showIsExternalLinkField.value && form.value.isExternalLink === '1'
})

const showFrameQuery = computed(() => showLinkOpenMode.value)

const showComponentRow = computed(() => {
  const f = form.value
  if (f.menuType === 'M') {
    return !(f.isExternalLink === '1' && f.linkOpenMode === 'iframe')
  }
  if (f.menuType === 'C' && f.openType === 'normal') {
    return f.isExternalLink !== '1'
  }
  return false
})

const showRouteNameOnlyRow = computed(() => {
  const f = form.value
  if (f.menuType === 'C' && f.openType === 'normal' && f.isExternalLink === '1' && f.linkOpenMode === 'iframe') {
    return true
  }
  if (f.menuType === 'C' && f.openType !== 'normal' && !hidePathForBlankLink.value) {
    return true
  }
  if (f.menuType === 'M' && f.isExternalLink === '1' && f.linkOpenMode === 'iframe') {
    return true
  }
  return false
})

const frameQueryPlaceholder = computed(() => {
  if (form.value.linkOpenMode === 'blank') {
    return '须以 http:// 或 https:// 开头，如 https://example.com/page'
  }
  return '如 /jmreport/list 或 https://example.com/page'
})



const rules = computed(() => {

  const t = form.value.menuType

  const permsFieldRules = [

    { max: 500, message: '权限字符总长度不超过 500', trigger: 'blur' },

  ]

  if (t === 'F') {

    permsFieldRules.unshift({

      validator: (_rule, value, callback) => {

        const s = normalizePermsStr(typeof value === 'string' ? value : '')

        if (!s) {

          callback(new Error('请填写权限字符'))

        } else {

          callback()

        }

      },

      trigger: 'blur',

    })

  }



  const base = {

    parentId: reqRule('请选择上级菜单', 'change'),

    menuType: reqRule('请选择类型', 'change'),

    perms: permsFieldRules,

  }



  if (t === 'M') {

    return {

      ...base,

      menuName: reqRule('请输入菜单名称'),

      orderNum: reqRule('请输入显示排序'),

      path: [{

        validator: (_rule, value, callback) => {

          const f = form.value
          if (f.isExternalLink === '1' && f.linkOpenMode === 'blank') {
            callback()
            return
          }
          const val = String(value || '').trim()
          if (f.isExternalLink === '1' && f.linkOpenMode === 'iframe' && /^https?:\/\//i.test(val)) {
            callback(new Error('内嵌打开时路由地址须为英文路径，外链 URL 请填在「外链地址」'))
            return
          }
          if (!val) {
            callback(new Error('请输入路由地址'))
          } else {
            callback()
          }

        },

        trigger: 'blur',

      }],

      isExternalLink: reqRule('请选择是否外链', 'change'),

      linkOpenMode: [{

        validator: (_rule, _value, callback) => {

          if (form.value.isExternalLink !== '1') {
            callback()
            return
          }
          if (!form.value.linkOpenMode) {
            callback(new Error('请选择打开目标'))
          } else {
            callback()
          }

        },

        trigger: 'change',

      }],

      frameQuery: [{

        validator: (_rule, _value, callback) => {

          if (form.value.isExternalLink !== '1') {
            callback()
            return
          }
          const url = String(form.value.frameQuery || '').trim()
          if (!url) {
            callback(new Error('请填写外链地址'))
            return
          }
          if (form.value.linkOpenMode === 'blank' && !/^https?:\/\//i.test(url)) {
            callback(new Error('新标签页打开须以 http:// 或 https:// 开头'))
            return
          }
          callback()

        },

        trigger: 'blur',

      }],

      visible: reqRule('请选择显示状态', 'change'),

      isCache: reqRule('请选择是否缓存', 'change'),

      status: reqRule('请选择菜单状态', 'change'),

    }

  }



  if (t === 'C') {

    const openTypeRules = {

      openType: reqRule('请选择打开方式', 'change'),

      jimuResourceId: [{

        validator: (_rule, _value, callback) => {

          const ot = form.value.openType

          if ((ot === 'report' || ot === 'bi') && !form.value.jimuResourceId) {

            callback(new Error(ot === 'report' ? '请选择报表' : '请选择 BI 大屏'))

          } else {

            callback()

          }

        },

        trigger: 'change',

      }],

      frameQuery: [{

        validator: (_rule, _value, callback) => {

          const f = form.value
          const needFrameQuery = f.isExternalLink === '1'
          if (!needFrameQuery) {
            callback()
            return
          }
          const url = String(f.frameQuery || '').trim()
          if (!url) {
            callback(new Error('请填写外链地址'))
            return
          }
          if (f.linkOpenMode === 'blank' && !/^https?:\/\//i.test(url)) {
            callback(new Error('新标签页打开须以 http:// 或 https:// 开头'))
            return
          }
          callback()

        },

        trigger: 'blur',

      }],

      linkOpenMode: [{

        validator: (_rule, _value, callback) => {

          const f = form.value
          if (f.openType === 'report' || f.openType === 'bi' || f.isExternalLink !== '1') {
            callback()
            return
          }
          if (!f.linkOpenMode) {
            callback(new Error('请选择打开目标'))
          } else {
            callback()
          }

        },

        trigger: 'change',

      }],

      isExternalLink: [{

        validator: (_rule, _value, callback) => {

          const f = form.value
          if (f.openType === 'report' || f.openType === 'bi') {
            callback()
            return
          }
          if (f.isExternalLink !== '0' && f.isExternalLink !== '1') {
            callback(new Error('请选择是否外链'))
          } else {
            callback()
          }

        },

        trigger: 'change',

      }],

    }

    return {

      ...base,

      menuName: reqRule('请输入菜单名称'),

      orderNum: reqRule('请输入显示排序'),

      path: [{

        validator: (_rule, value, callback) => {

          const f = form.value
          if (hidePathForBlankLink.value) {
            callback()
            return
          }
          if (f.openType === 'frame' && f.linkOpenMode === 'blank') {
            callback()
            return
          }
          const val = String(value || '').trim()
          if (f.isExternalLink === '1' && f.linkOpenMode === 'iframe' && /^https?:\/\//i.test(val)) {
            callback(new Error('内嵌打开时路由地址须为英文路径，外链 URL 请填在「外链地址」'))
            return
          }
          if (!val) {
            callback(new Error('请输入路由地址'))
          } else {
            callback()
          }

        },

        trigger: 'blur',

      }],

      component: [{

        validator: (_rule, value, callback) => {

          if (form.value.openType === 'normal' && form.value.isExternalLink !== '1' && !String(value || '').trim()) {

            callback(new Error('请输入组件路径'))

          } else {

            callback()

          }

        },

        trigger: 'blur',

      }],

      visible: reqRule('请选择显示状态', 'change'),

      isCache: reqRule('请选择是否缓存', 'change'),

      status: reqRule('请选择菜单状态', 'change'),

      ...openTypeRules,

    }

  }



  if (t === 'F') {

    return {

      ...base,

      menuName: reqRule('请输入按钮名称'),

      orderNum: reqRule('请输入显示排序'),

      status: reqRule('请选择按钮状态', 'change'),

    }

  }



  return base

})



function onIconSelected(name) {

  form.value.icon = name

  iconPopoverVisible.value = false

}



function showSelectIcon() {

  iconSelectRef.value?.reset?.()

}

/** 是否顶级上级菜单（与后端 ROOT -1 对齐） */
function isRootParentId(parentId) {
  const s = String(parentId ?? ROOT).trim()
  return s === ROOT || s === '-1' || s === '0'
}

/** 目录类型：按层级自动选择 Layout / ParentView，避免三级菜单嵌套 Layout。 */
function syncDirComponentForForm() {
  if (form.value.menuType !== 'M') {
    return
  }
  form.value.component = isRootParentId(form.value.parentId) ? 'Layout' : 'ParentView'
}

function onParentChange() {
  syncDirComponentForForm()
}



function onTypeChange() {

  if (form.value.menuType === 'F') {

    iconPopoverVisible.value = false

    form.value.icon = ''

  }

  if (form.value.menuType === 'M') {

    syncDirComponentForForm()

  }

  if (form.value.menuType === 'C') {

    form.value.openType = form.value.openType || 'normal'

    onOpenTypeChange()

  }

  if (form.value.menuType === 'F') {

    form.value.path = ''

    form.value.component = ''

    form.value.visible = '0'

  }

  nextTick(() => formRef.value?.clearValidate?.())

}



/** 下拉项展示：名称 + 编码/ID */
function formatCatalogLabel(item) {
  if (!item) return ''
  const name = item.name || item.id
  const extra = item.code || item.id
  return extra && extra !== name ? `${name}（${extra}）` : name
}



/** 从外链 URL 生成侧栏用英文 path（内嵌时 path 不能是 http 地址） */
function suggestPathFromUrl(url) {
  const raw = String(url || '').trim()
  if (!raw) return 'external-link'
  try {
    const u = new URL(raw)
    const slug = u.hostname.replace(/\./g, '-').replace(/[^a-zA-Z0-9-]/g, '')
    return slug || 'external-link'
  } catch {
    return 'external-link'
  }
}



function parseOpenTypeFromMenu(row) {
  const path = String(row?.path || '').trim()
  const q = String(row?.query || '').trim()
  const externalBase = {
    isExternalLink: '0',
    linkOpenMode: 'iframe',
    frameQuery: '',
  }

  if (row?.menuType === 'M') {
    if (row?.isFrame === '1') {
      const frameQuery = q || (/^https?:\/\//i.test(path) ? path : '')
      return {
        ...externalBase,
        openType: 'normal',
        jimuResourceId: '',
        isExternalLink: '1',
        linkOpenMode: 'iframe',
        frameQuery,
      }
    }
    if (/^https?:\/\//i.test(path)) {
      return {
        ...externalBase,
        openType: 'normal',
        jimuResourceId: '',
        isExternalLink: '1',
        linkOpenMode: 'blank',
        frameQuery: path,
      }
    }
    return { openType: 'normal', jimuResourceId: '', ...externalBase }
  }

  if (row?.menuType !== 'C') {
    return { openType: 'normal', jimuResourceId: '', ...externalBase }
  }
  /** 新标签页外链：path 为完整 URL，is_frame=0 */
  if (row?.isFrame !== '1' && /^https?:\/\//i.test(path)) {
    return {
      openType: 'frame',
      isExternalLink: '1',
      linkOpenMode: 'blank',
      jimuResourceId: '',
      frameQuery: path,
    }
  }
  if (row?.isFrame !== '1') {
    return { openType: 'normal', jimuResourceId: '', ...externalBase }
  }
  if (q.startsWith('/jmreport/view/')) {
    const id = q.replace(/^\/jmreport\/view\//, '').split('?')[0]
    return { openType: 'report', jimuResourceId: id, isExternalLink: '1', frameQuery: '', linkOpenMode: 'iframe' }
  }
  if (q.includes('/drag/view') || q.includes('/drag/page/view')) {
    const pathMatch = q.match(/\/drag\/page\/view\/([^/?]+)/)
    const pageIdMatch = q.match(/[?&]pageId=([^&]+)/)
    const legacyIdMatch = q.match(/[?&]id=([^&]+)/)
    const id = pathMatch?.[1] || pageIdMatch?.[1] || legacyIdMatch?.[1] || ''
    return {
      openType: 'bi',
      jimuResourceId: id ? decodeURIComponent(id) : '',
      isExternalLink: '1',
      frameQuery: '',
      linkOpenMode: 'iframe',
    }
  }
  const frameQuery = q || (/^https?:\/\//i.test(path) ? path : '')
  return { openType: 'frame', jimuResourceId: '', isExternalLink: '1', frameQuery, linkOpenMode: 'iframe' }
}



function applyExternalLinkToForm(target) {
  const isReportBi = target.menuType === 'C' && (target.openType === 'report' || target.openType === 'bi')
  if (isReportBi) {
    target.isFrame = '1'
    target.component = 'InnerLink'
    target.query = target.openType === 'report'
      ? `/jmreport/view/${target.jimuResourceId}`
      : `/drag/view?pageId=${target.jimuResourceId}`
    target.isCache = target.isCache || '1'
    return
  }

  const isExternal = target.isExternalLink === '1'
  if (!isExternal) {
    if (target.menuType === 'C' && target.openType === 'normal') {
      target.isFrame = '0'
    }
    if (target.menuType === 'M') {
      target.isFrame = '0'
    }
    return
  }

  if (target.linkOpenMode === 'blank') {
    const url = String(target.frameQuery || '').trim()
    target.isFrame = '0'
    target.component = target.menuType === 'M' ? (target.component === 'InnerLink' ? 'Layout' : (target.component || 'Layout')) : ''
    target.query = ''
    target.path = url
    target.isCache = '1'
  } else {
    target.isFrame = '1'
    target.component = 'InnerLink'
    let frameUrl = String(target.frameQuery || target.query || '').trim()
    let routePath = String(target.path || '').trim()
    if (/^https?:\/\//i.test(routePath)) {
      if (!frameUrl) {
        frameUrl = routePath
      }
      routePath = suggestPathFromUrl(frameUrl)
    }
    if (!routePath) {
      routePath = suggestPathFromUrl(frameUrl) || 'external-link'
    }
    target.path = routePath
    target.query = frameUrl
    target.isCache = target.isCache || '1'
  }
}



function loadJimuCatalog(type) {
  if (type === 'report' && reportOptions.value.length > 0) {
    return Promise.resolve()
  }
  if (type === 'bi' && biOptions.value.length > 0) {
    return Promise.resolve()
  }
  catalogLoading.value = true
  const req = type === 'bi' ? listJimuBiPages() : listJimuReports()
  return req
    .then((res) => {
      const rows = res.data || []
      if (type === 'bi') {
        biOptions.value = rows
      } else {
        reportOptions.value = rows
      }
    })
    .finally(() => {
      catalogLoading.value = false
    })
}



function suggestRoutePath(item, openType) {
  if (!item) return ''
  const code = String(item.code || '').trim()
  if (/^[a-zA-Z][a-zA-Z0-9_-]*$/.test(code)) {
    return code.toLowerCase().replace(/_/g, '-')
  }
  const prefix = openType === 'bi' ? 'bi' : 'report'
  const shortId = String(item.id || '').replace(/-/g, '').slice(0, 8)
  return `${prefix}-${shortId || 'view'}`
}



function resetAutoMeta() {
  autoMeta.value = {
    menuName: false,
    path: false,
  }
}



/** 选择报表/大屏后自动填充菜单名称、路由地址 */
function onJimuResourceChange(id) {
  if (!id) return
  const ot = form.value.openType
  const list = ot === 'bi' ? biOptions.value : reportOptions.value
  const item = list.find((row) => row.id === id)
  if (!item) return

  if (!form.value.menuName || autoMeta.value.menuName) {
    form.value.menuName = item.name || ''
    autoMeta.value.menuName = true
  }

  const path = suggestRoutePath(item, ot)
  if (!form.value.path || autoMeta.value.path) {
    form.value.path = path
    autoMeta.value.path = true
  }
}



function onOpenTypeChange() {
  const ot = form.value.openType
  if (ot === 'report' || ot === 'bi') {
    form.value.isExternalLink = '1'
    form.value.isFrame = '1'
    form.value.component = 'InnerLink'
    form.value.isCache = form.value.isCache || '1'
    loadJimuCatalog(ot)
  } else if (ot === 'frame') {
    form.value.isExternalLink = '1'
    form.value.linkOpenMode = form.value.linkOpenMode || 'iframe'
    form.value.jimuResourceId = ''
    onLinkOpenModeChange()
  } else {
    form.value.jimuResourceId = ''
    if (form.value.isExternalLink !== '1') {
      form.value.isFrame = '0'
      form.value.frameQuery = ''
      form.value.linkOpenMode = 'iframe'
      if (!form.value.component || form.value.component === 'InnerLink') {
        form.value.component = ''
      }
    } else {
      onLinkOpenModeChange()
    }
  }
  nextTick(() => formRef.value?.clearValidate?.())
}



/** 是否外链切换 */
function onIsExternalLinkChange() {
  if (form.value.isExternalLink !== '1') {
    form.value.linkOpenMode = 'iframe'
    form.value.frameQuery = ''
    if (form.value.menuType === 'C' && form.value.openType === 'frame') {
      form.value.openType = 'normal'
    }
    if (form.value.menuType === 'M') {
      form.value.component = form.value.component === 'InnerLink' ? 'Layout' : (form.value.component || 'Layout')
    } else if (!form.value.component || form.value.component === 'InnerLink') {
      form.value.component = ''
    }
    form.value.isFrame = '0'
  } else {
    onLinkOpenModeChange()
  }
  nextTick(() => formRef.value?.clearValidate?.())
}



/** 外链打开目标切换：内嵌需侧栏 path；新标签页 URL 写入 path */
function onLinkOpenModeChange() {
  if (form.value.isExternalLink !== '1') {
    return
  }
  if (form.value.linkOpenMode === 'blank') {
    form.value.isFrame = '0'
    if (form.value.menuType === 'M') {
      form.value.component = form.value.component === 'InnerLink' ? 'Layout' : (form.value.component || 'Layout')
    } else {
      form.value.component = ''
    }
    form.value.isCache = '1'
  } else {
    const path = String(form.value.path || '').trim()
    if (/^https?:\/\//i.test(path) && !String(form.value.frameQuery || '').trim()) {
      form.value.frameQuery = path
      form.value.path = suggestPathFromUrl(path)
    }
    form.value.isFrame = '1'
    form.value.component = 'InnerLink'
    form.value.isCache = form.value.isCache || '1'
  }
  nextTick(() => formRef.value?.clearValidate?.())
}



function normalizePermsStr(raw) {

  if (raw == null || typeof raw !== 'string') return ''

  return raw

    .replace(/，/g, ',')

    .split(',')

    .map((s) => s.trim())

    .filter(Boolean)

    .filter((v, i, a) => a.indexOf(v) === i)

    .join(',')

}



/** 从已保存的 perms 拆成输入行（至少一行便于编辑） */
function permsStrToList(raw) {

  const normalized = normalizePermsStr(typeof raw === 'string' ? raw : '')

  if (!normalized) return ['']

  return normalized.split(',')

}



function addPermsRow() {

  permsList.value.push('')

}



function removePermsRow(index) {

  permsList.value.splice(index, 1)

  if (permsList.value.length === 0) {

    permsList.value.push('')

  }

}



/** 将多行输入写回 form.perms，供校验与提交 */
function syncPermsFromList() {

  form.value.perms = normalizePermsStr(permsList.value.join(','))

}



function normalizeParentId(v) {
  if (v == null || v === '') return ROOT
  const s = String(v).trim()
  if (s === ROOT || s === '-1' || s === '0') return ROOT
  return s
}

/**
 * 提交给后端的 Long 字段：安全整数用 number，雪花 ID 用 string（避免 JS Number 精度丢失）。
 * @param {string|number|null|undefined} value
 * @returns {number|string|null}
 */
function toApiLongId(value) {
  if (value == null || value === '') return null
  const s = String(value).trim()
  const n = Number(s)
  return Number.isSafeInteger(n) ? n : s
}

/** 上级菜单：顶级为 -1，其余走 {@link toApiLongId} */
function toApiParentId(value) {
  const normalized = normalizeParentId(value)
  if (normalized === ROOT) return -1
  return toApiLongId(normalized)
}



/** 内嵌外链：纠正 path 误填为 http URL 的情况（编辑回显） */
function normalizeIframeFormPaths() {
  const f = form.value
  if (f.isExternalLink !== '1' || f.linkOpenMode !== 'iframe') {
    return
  }
  const path = String(f.path || '').trim()
  if (!/^https?:\/\//i.test(path)) {
    return
  }
  if (!String(f.frameQuery || '').trim()) {
    f.frameQuery = path
  }
  f.path = suggestPathFromUrl(f.frameQuery || path)
}



function open(payload = {}) {

  visible.value = true

  treeData.value = [{ id: ROOT, label: '主类目', children: [] }]

  form.value = defaultForm()

  permsList.value = ['']

  resetAutoMeta()

  reportOptions.value = []

  biOptions.value = []

  form.value.parentId = normalizeParentId(payload.parentId ?? ROOT)

  syncDirComponentForForm()



  treeselectMenu().then((res) => {

    treeData.value = [{ id: ROOT, label: '主类目', children: res.data || [] }]

  })



  if (payload.menuId) {

    getMenu(payload.menuId).then((res) => {

      const row = res.data || {}

      const parsed = parseOpenTypeFromMenu(row)

      form.value = {

        ...defaultForm(),

        ...row,

        parentId: normalizeParentId(row.parentId),

        ...parsed,

      }

      normalizeIframeFormPaths()

      syncDirComponentForForm()

      permsList.value = permsStrToList(form.value.perms)

      if (form.value.openType === 'report' || form.value.openType === 'bi') {

        loadJimuCatalog(form.value.openType).then(() => {

          if (form.value.jimuResourceId) {

            onJimuResourceChange(form.value.jimuResourceId)

          }

        })

      }

    })

  }

}



function toPayload() {

  syncPermsFromList()

  const f = { ...form.value }

  applyExternalLinkToForm(f)

  delete f.openType

  delete f.jimuResourceId

  delete f.frameQuery

  delete f.linkOpenMode

  delete f.isExternalLink

  f.parentId = toApiParentId(f.parentId)
  if (f.menuId != null && f.menuId !== '') {
    f.menuId = toApiLongId(f.menuId)
  }

  if (f.menuType === 'M' && !f.component) {

    f.component = 'Layout'

  }

  f.perms = normalizePermsStr(f.perms)

  return f

}



function submit() {

  syncPermsFromList()

  return new Promise((resolve, reject) => {

    formRef.value.validate((valid) => {

      if (!valid) {

        reject(new Error('校验未通过'))

        return

      }

      const body = toPayload()

      const req = body.menuId ? updateMenu(body) : addMenu(body)

      req

        .then(() => {

          ElMessage.success(body.menuId ? '修改成功' : '新增成功')

          emit('success')

          resolve()

        })

        .catch(reject)

    })

  })

}



defineExpose({ open })

</script>



<style scoped>

.menu-form :deep(.el-row) {

  margin-bottom: 4px;

}



.perms-dynamic {

  width: 100%;

}



.perms-dynamic__row {

  display: flex;

  align-items: center;

  gap: 8px;

  margin-bottom: 8px;

}



.perms-dynamic__row .el-input {

  flex: 1;

  min-width: 0;

}



.perms-dynamic__hint {

  margin-top: 4px;

  font-size: 12px;

  color: var(--el-text-color-secondary);

  line-height: 1.4;

}



</style>

