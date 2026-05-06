export default {
  install(Vue) {
    Vue.config.globalProperties.$cache = {
      session: {
        getItem(key) {
          return sessionStorage.getItem(key)
        },
        setItem(key, value) {
          return sessionStorage.setItem(key, value)
        },
        removeItem(key) {
          return sessionStorage.removeItem(key)
        },
        clear() {
          return sessionStorage.clear()
        }
      },
      local: {
        getItem(key) {
          return localStorage.getItem(key)
        },
        setItem(key, value) {
          return localStorage.setItem(key, value)
        },
        removeItem(key) {
          return localStorage.removeItem(key)
        },
        clear() {
          return localStorage.clear()
        }
      }
    }
  }
}
