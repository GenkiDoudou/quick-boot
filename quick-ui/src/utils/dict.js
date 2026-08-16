/**
 * 字典 composable 与展示辅助工具。
 *
 * useDict：按 dictType 拉取选项并写入 Pinia 缓存，返回响应式 ref 字典项。
 * 样式辅助：LIST_CLASS_OPTIONS / isPlainDictStyle / resolveListClassTagType。
 */
import { ref, toRefs } from 'vue'
import useDictStore from '@/store/modules/dict'
import { getDicts } from '@/api/system/dict/data'

/** 字典数据「回显样式」可选项（与 RuoYi-Vue3 一致） */
export const LIST_CLASS_OPTIONS = [
  { value: 'default', label: '默认' },
  { value: 'primary', label: '主要' },
  { value: 'success', label: '成功' },
  { value: 'info', label: '信息' },
  { value: 'warning', label: '警告' },
  { value: 'danger', label: '危险' }
]

/**
 * 是否以纯文本展示（listClass 为 default/空 且 cssClass 为空）。
 * @param {string|null|undefined} listClass
 * @param {string|null|undefined} cssClass
 */
export function isPlainDictStyle(listClass, cssClass) {
  const lc = listClass ?? ''
  const cc = cssClass ?? ''
  return (lc === '' || lc === 'default') && (cc === '' || cc == null)
}

/**
 * 将 listClass 转为 Element Plus Tag type。
 * @param {string|null|undefined} listClass
 * @returns {'primary'|'success'|'info'|'warning'|'danger'|undefined}
 */
export function resolveListClassTagType(listClass) {
  const raw = String(listClass ?? '').trim().toLowerCase()
  if (!raw || raw === 'default') {
    return undefined
  }
  const allowed = new Set(['primary', 'success', 'info', 'warning', 'danger'])
  return allowed.has(raw) ? /** @type {'primary'|'success'|'info'|'warning'|'danger'} */ (raw) : undefined
}

/**
 * 按字典类型批量加载选项（带 Pinia 缓存）。
 *
 * 首次请求 API 并 setDict；命中缓存则直接复用。返回 toRefs 对象，键为 dictType。
 * @param {...string} args 字典类型编码列表，如 useDict('sys_normal_disable', 'sys_user_sex')
 * @returns {Record<string, import('vue').Ref<Array<{ label: string, value: string, elTagType?: string, elTagClass?: string }>>>}
 */
export function useDict(...args) {
  const res = ref({})
  return (() => {
    args.forEach((dictType) => {
      res.value[dictType] = []
      const dicts = useDictStore().getDict(dictType)
      if (dicts) {
        res.value[dictType] = dicts
      } else {
        getDicts(dictType).then(resp => {
          res.value[dictType] = resp.data.map(p => ({
            label: p.dictLabel,
            value: p.dictValue,
            elTagType: p.listClass,
            elTagClass: p.cssClass
          }))
          useDictStore().setDict(dictType, res.value[dictType])
        })
      }
    })
    return toRefs(res.value)
  })()
}
