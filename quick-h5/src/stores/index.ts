/**
 * Pinia 根实例：挂载 persistedstate 插件，使用 uni 存储持久化。
 */
import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-plugin-persistedstate'

const pinia = createPinia()

pinia.use(
  createPersistedState({
    storage: {
      getItem: uni.getStorageSync,
      setItem: uni.setStorageSync,
    },
  }),
)

export default pinia

export { useUserStore } from './user'
export { useDictStore } from './dict'
