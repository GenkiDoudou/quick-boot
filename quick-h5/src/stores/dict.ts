import { defineStore } from 'pinia'
import type { DictOption } from '@/api/system/dict'

type DictEntry = { key: string; value: DictOption[] }

/**
 * 字典内存缓存（不持久化）。
 * 与 useDict 配合：请求后 setDict；登出时可 cleanDict。
 */
export const useDictStore = defineStore('dict', {
  state: () => ({
    dict: [] as DictEntry[],
  }),
  actions: {
    /** 按 dictType 读取缓存，未命中返回 null */
    getDict(key: string): DictOption[] | null {
      if (!key) return null
      const hit = this.dict.find((d) => d.key === key)
      return hit ? hit.value : null
    },
    /** 写入或更新某类型的字典选项 */
    setDict(key: string, value: DictOption[]) {
      if (!key) return
      const i = this.dict.findIndex((d) => d.key === key)
      if (i >= 0) this.dict[i].value = value
      else this.dict.push({ key, value })
    },
    /** 移除单个类型的缓存 */
    removeDict(key: string) {
      const i = this.dict.findIndex((d) => d.key === key)
      if (i >= 0) this.dict.splice(i, 1)
    },
    /** 清空全部字典缓存（登出时调用） */
    cleanDict() {
      this.dict = []
    },
  },
})
