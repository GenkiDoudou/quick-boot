/**
 * SPA：History API + hashchange → PV（无 Vue 依赖）。
 * @param {(page: string) => void} onPv
 * @returns {() => void}
 */
export function bindSpaNavigation(onPv) {
  if (typeof window === 'undefined' || typeof history === 'undefined') {
    return () => {}
  }

  const emit = () => {
    const page =
      (typeof location !== 'undefined' && (location.pathname + location.search + location.hash)) || '/'
    onPv(page)
  }

  /** 包装 pushState/replaceState，在路由变更后补发 PV */
  const wrap = (type) => {
    const raw = history[type]
    if (typeof raw !== 'function') return
    history[type] = function patched(...args) {
      const ret = raw.apply(this, args)
      emit()
      return ret
    }
  }

  wrap('pushState')
  wrap('replaceState')
  window.addEventListener('popstate', emit)
  window.addEventListener('hashchange', emit)

  return () => {
    window.removeEventListener('popstate', emit)
    window.removeEventListener('hashchange', emit)
  }
}
