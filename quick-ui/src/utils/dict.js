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
 * 获取字典数据
 * @param args 字典类型列表
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
