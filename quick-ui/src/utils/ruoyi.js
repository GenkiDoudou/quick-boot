// 转换字符串，undefined,null等转化为""
export function parseStrEmpty(str) {
  if (!str || str === 'undefined' || str === 'null') {
    return ''
  }
  return str
}

// 返回项目路径
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

export function blobValidate(data) {
  return data.type !== 'application/json'
}

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

export function resetForm(refName) {
  if (this.$refs[refName]) {
    this.$refs[refName].resetFields();
  }
}

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

export function dfs(node, childrenMap, config) {
  if (childrenMap[node[config.id]] != null) {
    node[config.children] = childrenMap[node[config.id]];
    for (let child of node[config.children]) {
      dfs(child, childrenMap, config);
    }
  }
}

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
