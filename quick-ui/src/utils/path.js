/**
 * 路由 path 规范化（自 ruoyi 拆出）。
 */

/**
 * 规范化路由 path：去除重复斜杠、去掉末尾斜杠。
 * @param {string} p
 * @returns {string}
 */
export function getNormalPath(p) {
  if (!p || p.length === 0 || p === 'undefined') {
    return p
  }
  let res = p.replace('//', '/')
  if (res[res.length - 1] === '/') {
    return res.slice(0, res.length - 1)
  }
  return res
}
