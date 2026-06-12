/**
 * 若依遗留工具函数集合。
 * 新代码优先使用具名 export 并补 JSDoc；全局挂载见 main.js（parseTime、handleTree 等）。
 */

/**
 * 将 undefined/null 字符串字面量转为空串。
 * @param {string} str
 * @returns {string}
 */
export function parseStrEmpty(str) {
  if (!str || str === 'undefined' || str === 'null') {
    return ''
  }
  return str
}

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

/**
 * 将对象序列化为 URL 查询字符串（支持一层嵌套对象）。
 * @param {Record<string, unknown>} params
 * @returns {string} 末尾带 & 的拼接结果，如 `name=a&age=1&`
 */
export function tansParams(params) {
  let result = ''
  for (const propName of Object.keys(params)) {
    const value = params[propName];
    const part = encodeURIComponent(propName) + "=";
    if (value !== null && value !== "" && typeof (value) !== "undefined") {
      if (typeof value === 'object') {
        for (const item of Object.keys(value)) {
          if (value[item] !== null && value[item] !== "" && typeof (value[item]) !== 'undefined') {
            result += part + encodeURIComponent(value[item]) + "&";
          }
        }
      } else {
        result += part + encodeURIComponent(value) + "&";
      }
    }
  }
  return result
}

/**
 * 判断 Blob 响应是否为文件（非 JSON 错误体）。
 * @param {Blob} data
 * @returns {boolean} true 表示可当作文件下载
 */
export function blobValidate(data) {
  return data.type !== 'application/json'
}

/**
 * 格式化日期时间。
 * @param {Date|string|number} time 支持 Date、时间戳（10/13 位）、日期字符串
 * @param {string} [pattern='{y}-{m}-{d} {h}:{i}:{s}'] 占位符 y/m/d/h/i/s/a（a=星期）
 * @returns {string|null} 无效输入返回 null
 * @example parseTime(1609459200000) // '2021-01-01 00:00:00'
 */
export function parseTime(time, pattern) {
  if (!time || time === '') {
    return null
  }
  const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}'
  let date;
  if (typeof time === 'object') {
    date = new Date(time)
  } else {
    if ((typeof time === 'string') && (/^[0-9]+$/.test(time))) {
      time = parseInt(time)
    }
    if ((typeof time === 'number') && (time.toString().length === 10)) {
      time = time * 1000
    }
    date = new Date(time)
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key]
    if (key === 'a') return ['一', '二', '三', '四', '五', '六', '日'][value]
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return value || 0
  })
  return time_str
}

/**
 * 重置 Element Plus 表单（需在 Options API 组件内通过 this 调用）。
 * @param {string} refName 表单 ref 名
 */
export function resetForm(refName) {
  if (this.$refs[refName]) {
    this.$refs[refName].resetFields();
  }
}

/**
 * 扁平数组转树形结构（DFS 挂载 children）。
 * @param {Array<Record<string, unknown>>} data 扁平节点列表
 * @param {string} [id='id'] 主键字段名
 * @param {string} [parentId='parentId'] 父键字段名
 * @param {string} [children='children'] 子节点字段名
 * @returns {Array} 根节点数组
 */
export function handleTree(data, id, parentId, children) {
  let config = {
    id: id || 'id',
    parentId: parentId || 'parentId',
    children: children || 'children'
  };

  var childrenMap = {};
  var nodeIds = {};
  var tree = [];

  for (let d of data) {
    let parentId = d[config.parentId];
    if (childrenMap[parentId] == null) {
      childrenMap[parentId] = [];
    }
    childrenMap[parentId].push(d);
    nodeIds[d[config.id]] = d;
  }

  for (let d of data) {
    let parentId = d[config.parentId];
    if (nodeIds[parentId] == null) {
      tree.push(d);
    }
  }

  for (let t of tree) {
    dfs(t, childrenMap, config);
  }

  return tree;
}

/** @private handleTree 内部 DFS */
export function dfs(node, childrenMap, config) {
  if (childrenMap[node[config.id]] != null) {
    node[config.children] = childrenMap[node[config.id]];
    for (let child of node[config.children]) {
      dfs(child, childrenMap, config);
    }
  }
}

/**
 * 字典值转单个标签。
 * @param {Array<{value: string, label: string}>|Record<string, {value, label}>} datas
 * @param {string|number} value
 * @returns {string}
 */
export function selectDictLabel(datas, value) {
  var actions = [];
  Object.keys(datas).some((key) => {
    if (datas[key].value == ('' + value)) {
      actions.push(datas[key].label);
      return true;
    }
  })
  return actions.join('');
}

/**
 * 逗号分隔的多字典值转标签串。
 * @param {Array|Record} datas
 * @param {string} value 逗号分隔，如 "1,2,3"
 * @returns {string}
 */
export function selectDictLabels(datas, value) {
  var actions = [];
  Object.keys(value.split(',')).forEach((key) => {
    Object.keys(datas).some((k) => {
      if (datas[k].value == ('' + value.split(',')[key])) {
        actions.push(datas[k].label);
        return true;
      }
    })
  })
  return actions.join(',');
}

/**
 * 为查询参数附加日期范围（beginXxx / endXxx）。
 * @param {Record<string, unknown>} params 原查询对象，会被写入 params.begin/end
 * @param {[string, string]|null} dateRange 起止日期
 * @param {string} [propName='Time'] 后缀名，如 Time → beginTime/endTime
 * @returns {Record<string, unknown>}
 */
export function addDateRange(params, dateRange, propName) {
  var search = params;
  search.params = {};
  if (null != dateRange && '' != dateRange) {
    search.params["begin" + propName] = dateRange[0];
    search.params["end" + propName] = dateRange[1];
  } else {
    search.params["begin" + propName] = "";
    search.params["end" + propName] = "";
  }
  return search;
}

/**
 * 树 node-key 用 ID：雪花 ID 保持 string，避免 JS Number 精度丢失导致误勾选。
 * @param {string|number|null|undefined} id
 * @returns {string|number|null|undefined}
 */
export function stringifyTreeId(id) {
  if (id == null || id === '') return id
  return String(id).trim()
}

/**
 * @param {Array<string|number|null|undefined>} ids
 * @returns {string[]}
 */
export function stringifyTreeIds(ids) {
  if (!Array.isArray(ids)) return []
  return ids
    .map((id) => stringifyTreeId(id))
    .filter((id) => id != null && id !== '' && id !== '0' && id !== '-1')
}

/**
 * 规范菜单权限树节点 id（与 el-tree node-key 一致，一律 string）。
 * @param {Array<Record<string, unknown>>} nodes
 */
export function normalizeMenuTreeNodes(nodes) {
  if (!Array.isArray(nodes)) return []
  return nodes.map((n) => ({
    ...n,
    id: stringifyTreeId(n?.id),
    children: normalizeMenuTreeNodes(n.children || []),
  }))
}

/**
 * 收集树节点 id（string），用于过滤无效 checkedKeys。
 * @param {Array<Record<string, unknown>>} nodes
 * @param {Set<string>} [out]
 */
export function collectTreeNodeIds(nodes, out = new Set()) {
  if (!Array.isArray(nodes)) return out
  for (const n of nodes) {
    const id = stringifyTreeId(n?.id)
    if (id != null && id !== '') out.add(String(id))
    collectTreeNodeIds(n.children, out)
  }
  return out
}

/**
 * 角色菜单树回显：禁止 default-checked-keys（父节点会连带勾选未授权子菜单）。
 * 与若依一致，逐节点 setChecked，deep=false 不向下级联。
 * @param {import('element-plus').TreeInstance | null | undefined} tree
 * @param {string[]} checkedKeys
 */
export function echoTreeCheckedKeysWithoutCascade(tree, checkedKeys) {
  if (!tree || !Array.isArray(checkedKeys)) return
  checkedKeys.forEach((id) => {
    tree.setChecked(id, true, false)
  })
}

/**
 * 提交给后端的 Long：安全整数用 number，雪花 ID 用 string。
 * @param {string|number|null|undefined} value
 * @returns {number|string|null}
 */
export function toApiLongId(value) {
  if (value == null || value === '') return null
  const s = String(value).trim()
  const n = Number(s)
  return Number.isSafeInteger(n) ? n : s
}

/**
 * @param {Array<string|number|null|undefined>} ids
 * @returns {Array<number|string>}
 */
export function toApiLongIds(ids) {
  if (!Array.isArray(ids)) return []
  return [...new Set(ids.map(toApiLongId).filter((id) => id != null))]
}
