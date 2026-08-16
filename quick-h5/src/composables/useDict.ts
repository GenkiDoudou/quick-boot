/**
 * 字典 composable：按类型加载字典并写入 Pinia 缓存。
 */
import { reactive, toRefs, type Ref } from 'vue'
import { getDicts, mapDictOptions, type DictOption } from '@/api/system/dict'
import { useDictStore } from '@/stores/dict'
import { toastErr } from '@/utils/toast'

type DictRefs = Record<string, Ref<DictOption[]>>

/**
 * 按类型拉取字典（带 Pinia 缓存）。
 * @example
 * const { sys_normal_disable } = useDict('sys_normal_disable')
 */
export function useDict(...types: string[]): DictRefs {
  const bag = reactive<Record<string, DictOption[]>>({})
  const store = useDictStore()

  types.forEach((dictType) => {
    bag[dictType] = []
    const cached = store.getDict(dictType)
    if (cached) {
      // Pinia 已有则直接用，避免重复请求
      bag[dictType] = cached
      return
    }
    getDicts(dictType)
      .then((rows) => {
        const opts = mapDictOptions(rows)
        bag[dictType] = opts
        store.setDict(dictType, opts)
      })
      .catch((e) => {
        toastErr(e, `字典 ${dictType} 加载失败`)
      })
  })

  return toRefs(bag) as DictRefs
}
