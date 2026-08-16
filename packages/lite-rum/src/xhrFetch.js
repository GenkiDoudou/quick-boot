/**
 * 全局 XHR / fetch 接口测速（CDN / 无 axios 场景）。
 * 与业务 axios 观测同时开启会导致双报，PC 管理端应关闭此项。
 *
 * @param {{
 *   trackApi: (api: Record<string, unknown>) => void,
 *   shouldSkipUrl: (url: string) => boolean,
 *   getPage: () => string
 * }} opts
 * @returns {() => void}
 */
export function bindXhrFetchHook(opts) {
  if (typeof window === 'undefined') {
    return () => {}
  }

  const { trackApi, shouldSkipUrl, getPage } = opts
  const cleanups = []

  // 通过原型链挂载 __liteRum 元数据，在 loadend 时计算耗时并上报
  if (typeof XMLHttpRequest !== 'undefined') {
    const XHR = XMLHttpRequest
    const open = XHR.prototype.open
    const send = XHR.prototype.send
    XHR.prototype.open = function (method, url, ...rest) {
      this.__liteRum = {
        method: String(method || 'GET').toLowerCase(),
        url: String(url || ''),
        start: 0
      }
      return open.call(this, method, url, ...rest)
    }
    XHR.prototype.send = function (...args) {
      const meta = this.__liteRum
      if (meta) {
        meta.start = Date.now()
        this.addEventListener('loadend', () => {
          if (shouldSkipUrl(meta.url)) return
          trackApi({
            method: meta.method,
            url: meta.url.split('?')[0],
            query: meta.url.includes('?') ? meta.url.slice(meta.url.indexOf('?') + 1).slice(0, 500) : '',
            status: this.status,
            ok: this.status >= 200 && this.status < 400,
            durationMs: Date.now() - meta.start,
            page: getPage()
          })
        })
      }
      return send.apply(this, args)
    }
    cleanups.push(() => {
      XHR.prototype.open = open
      XHR.prototype.send = send
    })
  }

  if (typeof window.fetch === 'function') {
    const rawFetch = window.fetch.bind(window)
    window.fetch = async function liteRumFetch(input, init) {
      const url = typeof input === 'string' ? input : (input && input.url) || ''
      const method = String((init && init.method) || (input && input.method) || 'GET').toLowerCase()
      const start = Date.now()
      try {
        const res = await rawFetch(input, init)
        if (!shouldSkipUrl(url)) {
          trackApi({
            method,
            url: String(url).split('?')[0],
            query: String(url).includes('?') ? String(url).slice(String(url).indexOf('?') + 1).slice(0, 500) : '',
            status: res.status,
            ok: res.ok,
            durationMs: Date.now() - start,
            page: getPage()
          })
        }
        return res
      } catch (e) {
        if (!shouldSkipUrl(url)) {
          trackApi({
            method,
            url: String(url).split('?')[0],
            status: 0,
            ok: false,
            durationMs: Date.now() - start,
            page: getPage()
          })
        }
        throw e
      }
    }
    cleanups.push(() => {
      window.fetch = rawFetch
    })
  }

  return () => {
    cleanups.forEach((fn) => fn())
  }
}
