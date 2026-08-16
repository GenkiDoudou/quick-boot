/**
 * 字典内存缓存 Store。
 *
 * 结构：dict = [{ key: dictType, value: options[] }]
 * 与 composable useDict（utils/dict.js）配合：useDict 请求 API 后调用 setDict 写入。
 * 页面级缓存，无过期策略；切换用户后建议 removeDict 或刷新页面。
 */
import { defineStore } from 'pinia'

const useDictStore = defineStore(
  'dict',
  {
    state: () => ({
      dict: new Array()
    }),
    actions: {
      /**
       * 从内存缓存读取某 dictType 的 options 数组。
       * @param {string} _key dictType
       * @returns {Array|null}
       */
      getDict(_key) {
        if (_key == null && _key == "") {
          return null;
        }
        try {
          for (let i = 0; i < this.dict.length; i++) {
            if (this.dict[i].key == _key) {
              return this.dict[i].value;
            }
          }
        } catch (e) {
          return null;
        }
      },
      /**
       * 写入某 dictType 的 options（不覆盖已存在项，仅追加）。
       * @param {string} _key dictType
       * @param {Array} value { label, value, ... } 选项列表
       */
      setDict(_key, value) {
        if (_key !== null && _key !== "") {
          this.dict.push({
            key: _key,
            value: value
          });
        }
      },
      /**
       * 移除某 dictType 的缓存项。
       * @param {string} _key dictType
       * @returns {boolean} 是否删除成功
       */
      removeDict(_key) {
        var bln = false;
        try {
          for (let i = 0; i < this.dict.length; i++) {
            if (this.dict[i].key == _key) {
              this.dict.splice(i, 1);
              return true;
            }
          }
        } catch (e) {
          bln = false;
        }
        return bln;
      },
      /** 清空全部字典缓存（刷新后端字典后应调用） */
      cleanDict() {
        this.dict = new Array();
      },
      /** 预留：应用启动时可预加载字典（当前未实现） */
      initDict() {}
    }
  })

export default useDictStore
