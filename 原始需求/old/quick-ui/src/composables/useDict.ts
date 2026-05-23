/**
 * 字典数据 Composable
 * 替代全局 useDict 方法
 */
import { ref, toRefs } from 'vue'
import useDictStore from '@/store/modules/dict'
import { getDicts } from '@/api/system/dict/data'

/**
 * 获取字典数据
 * @param args 字典类型列表
 * @returns 字典数据的响应式引用
 */
export function useDict(...args: string[]) {
  const res = ref<Record<string, any[]>>({})
  
  args.forEach((dictType) => {
    res.value[dictType] = []
    const dicts = useDictStore().getDict(dictType)
    if (dicts) {
      res.value[dictType] = dicts
    } else {
      getDicts(dictType).then(resp => {
        res.value[dictType] = resp.data.map((p: any) => ({
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
}

