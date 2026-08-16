/**
 * 请求链路 pageVisit 五风格原型共享 mock（浏览器直接 script src 引用）
 */
(function (global) {
  const visits = [
    {
      pageVisitId: 'pv-role-20260815-133839',
      sessionId: 'sess-admin-7f2a91c0',
      uin: 'admin',
      pagePath: '/system/role',
      fromPage: '/system/user',
      okFlag: '1',
      startedAt: '2026-08-15 13:38:39',
      endedAt: '2026-08-15 13:38:41',
      durationMs: 210,
      traces: [
        {
          traceId: 'f7cf395feb8424176c5680734ad4d91a',
          entryName: 'POST /sys/role/page',
          statusCode: '200',
          okFlag: '1',
          durationMs: 44,
          spansRaw: [
            { id: 'p1', kind: 'page', name: '/system/role', service: 'browser', durationMs: 0, start: 0 },
            { id: 's1', kind: 'sql', name: 'SysRoleMapper.selectList', service: 'mysql', durationMs: 8, start: 4, sql: 'SELECT * FROM sys_role WHERE del_flag=0' },
            { id: 's2', kind: 'sql', name: 'SysUserMapper.selectById', service: 'mysql', durationMs: 3, start: 14, sql: 'SELECT * FROM sys_user WHERE user_id=?' },
            { id: 'fe', kind: 'fe_api', name: 'POST /sys/role/page', service: 'browser', durationMs: 44, start: 0, status: '200',
              requestParams: 'pageNum=1&pageSize=10', requestBody: '{"status":"0"}',
              responsePreview: '{"code":200,"msg":"success","data":{"total":3,"rows":[...]}}',
              bizCode: 200, bizMsg: 'success', feMs: 44 },
            { id: 'be', kind: 'service', name: 'POST /sys/role/page', service: 'quickboot-app', durationMs: 32, start: 2, status: '200', beMs: 32 }
          ]
        },
        {
          traceId: 'a11b22c33d44e55f6677889900aabbcc',
          entryName: 'GET /sys/role/1',
          statusCode: '200',
          okFlag: '1',
          durationMs: 28,
          spansRaw: [
            { id: 'p2', kind: 'page', name: '/system/role', service: 'browser', durationMs: 0, start: 0 },
            { id: 's3', kind: 'sql', name: 'SysRoleMapper.selectById', service: 'mysql', durationMs: 5, start: 3, sql: 'SELECT * FROM sys_role WHERE role_id=?' },
            { id: 'fe2', kind: 'fe_api', name: 'GET /sys/role/1', service: 'browser', durationMs: 28, start: 0, status: '200',
              requestParams: '', requestBody: '', responsePreview: '{"code":200,"data":{"roleId":1,"roleName":"管理员"}}',
              bizCode: 200, bizMsg: 'success', feMs: 28 },
            { id: 'be2', kind: 'service', name: 'GET /sys/role/1', service: 'quickboot-app', durationMs: 18, start: 1, status: '200', beMs: 18 }
          ]
        },
        {
          traceId: 'deadbeef0123456789abcdef01234567',
          entryName: 'GET /sys/menu/roleMenuTreeselect/1',
          statusCode: '200',
          okFlag: '1',
          durationMs: 61,
          spansRaw: [
            { id: 'p3', kind: 'page', name: '/system/role', service: 'browser', durationMs: 0, start: 0 },
            { id: 's4', kind: 'sql', name: 'SysMenuMapper.selectMenuList', service: 'mysql', durationMs: 12, start: 5, sql: 'SELECT * FROM sys_menu ORDER BY order_num' },
            { id: 'fe3', kind: 'fe_api', name: 'GET /sys/menu/roleMenuTreeselect/1', service: 'browser', durationMs: 61, start: 0, status: '200',
              requestParams: '', requestBody: '', responsePreview: '{"code":200,"menus":[...],"checkedKeys":[...]}',
              bizCode: 200, bizMsg: 'success', feMs: 61 },
            { id: 'be3', kind: 'service', name: 'GET /sys/menu/roleMenuTreeselect/1', service: 'quickboot-app', durationMs: 40, start: 2, status: '200', beMs: 40 }
          ]
        }
      ]
    },
    {
      pageVisitId: 'pv-user-20260815-133700',
      sessionId: 'sess-admin-7f2a91c0',
      uin: 'admin',
      pagePath: '/system/user',
      fromPage: '/index',
      okFlag: '1',
      startedAt: '2026-08-15 13:37:00',
      endedAt: '2026-08-15 13:37:02',
      durationMs: 95,
      traces: [
        {
          traceId: '11223344556677889900aabbccddeeff',
          entryName: 'POST /sys/user/page',
          statusCode: '200',
          okFlag: '1',
          durationMs: 95,
          spansRaw: [
            { id: 'pu', kind: 'page', name: '/system/user', service: 'browser', durationMs: 0, start: 0 },
            { id: 'feu', kind: 'fe_api', name: 'POST /sys/user/page', service: 'browser', durationMs: 95, start: 0, status: '200',
              requestBody: '{"pageNum":1}', responsePreview: '{"code":200,"data":{...}}', bizCode: 200, bizMsg: 'ok', feMs: 95 },
            { id: 'beu', kind: 'service', name: 'POST /sys/user/page', service: 'quickboot-app', durationMs: 70, start: 3, status: '200', beMs: 70 },
            { id: 'su', kind: 'sql', name: 'SysUserMapper.selectPage', service: 'mysql', durationMs: 20, start: 10, sql: 'SELECT ... FROM sys_user LIMIT ?' }
          ]
        }
      ]
    },
    {
      pageVisitId: 'pv-loghub-20260815-133200',
      sessionId: 'sess-admin-7f2a91c0',
      uin: 'admin',
      pagePath: '/monitor/logHub',
      fromPage: '/monitor/liteTrace',
      okFlag: '0',
      startedAt: '2026-08-15 13:32:00',
      endedAt: '2026-08-15 13:32:01',
      durationMs: 120,
      traces: [
        {
          traceId: 'eeff00112233445566778899aabbccdd',
          entryName: 'GET /monitor/logHub/list',
          statusCode: '500',
          okFlag: '0',
          durationMs: 120,
          spansRaw: [
            { id: 'pl', kind: 'page', name: '/monitor/logHub', service: 'browser', durationMs: 0, start: 0 },
            { id: 'fel', kind: 'fe_api', name: 'GET /monitor/logHub/list', service: 'browser', durationMs: 120, start: 0, status: '500',
              requestParams: 'beginTime=...', responsePreview: '{"code":500,"msg":"error"}', bizCode: 500, bizMsg: 'error', feMs: 120 },
            { id: 'bel', kind: 'service', name: 'GET /monitor/logHub/list', service: 'quickboot-app', durationMs: 90, start: 5, status: '500', beMs: 90 }
          ]
        }
      ]
    }
  ]

  /** 无页面 / 纯接口：OpenAPI、定时任务、无 RUM page 的后端链 */
  const pureApiTraces = [
    {
      traceId: 'openapi-9c1d2e3f4a5b6c7d8e9f001122334455',
      rootSource: 'api',
      entryName: 'GET /openapi/v1/users',
      callerName: 'partner-sdk',
      statusCode: '200',
      okFlag: '1',
      durationMs: 36,
      uin: '',
      sessionId: '',
      pageVisitId: '',
      pagePath: '',
      startedAt: '2026-08-15 14:01:02',
      spansRaw: [
        { id: 'be-o', kind: 'service', name: 'GET /openapi/v1/users', service: 'quickboot-app', durationMs: 36, start: 0, status: '200', beMs: 36 },
        { id: 'sql-o', kind: 'sql', name: 'SysUserMapper.selectList', service: 'mysql', durationMs: 9, start: 4, sql: 'SELECT user_id,user_name FROM sys_user LIMIT 100' }
      ]
    },
    {
      traceId: 'job-aabbccddeeff00112233445566778899',
      rootSource: 'job',
      entryName: 'Job SyncUserRole',
      callerName: 'quartz',
      statusCode: '200',
      okFlag: '1',
      durationMs: 210,
      uin: 'system',
      sessionId: '',
      pageVisitId: '',
      pagePath: '',
      startedAt: '2026-08-15 14:05:00',
      spansRaw: [
        { id: 'be-j', kind: 'service', name: 'Job SyncUserRole', service: 'quickboot-app', durationMs: 210, start: 0, status: '200', beMs: 210 },
        { id: 'sql-j1', kind: 'sql', name: 'SysRoleMapper.selectList', service: 'mysql', durationMs: 40, start: 10, sql: 'SELECT * FROM sys_role' },
        { id: 'sql-j2', kind: 'sql', name: 'SysUserRoleMapper.batchInsert', service: 'mysql', durationMs: 80, start: 60, sql: 'INSERT INTO sys_user_role ...' }
      ]
    },
    {
      traceId: 'beonly-11223344556677889900aabbccddee',
      rootSource: 'api',
      entryName: 'POST /sys/config',
      callerName: 'curl',
      statusCode: '401',
      okFlag: '0',
      durationMs: 12,
      uin: '',
      sessionId: '',
      pageVisitId: '',
      pagePath: '',
      startedAt: '2026-08-15 14:10:11',
      spansRaw: [
        { id: 'be-c', kind: 'service', name: 'POST /sys/config', service: 'quickboot-app', durationMs: 12, start: 0, status: '401', beMs: 12 }
      ]
    }
  ]

  function normalizeUrlKey(name) {
    return String(name || '').trim().toUpperCase().replace(/\s+/g, ' ')
  }

  function mergeSpans(raw, totalMs) {
    const list = Array.isArray(raw) ? [...raw] : []
    const apis = list.filter((s) => s.kind === 'fe_api' || s.kind === 'service')
    const others = list.filter((s) => s.kind !== 'fe_api' && s.kind !== 'service')
    const byKey = new Map()
    for (const s of apis) {
      const key = normalizeUrlKey(s.name)
      const cur = byKey.get(key) || {
        id: 'api-' + key,
        kind: 'api',
        kindLabel: '接口',
        name: s.name,
        service: 'merged',
        bar: 'be',
        ok: 'OK',
        status: s.status || '—',
        durationMs: 0,
        start: s.start || 0,
        requestParams: '',
        requestBody: '',
        responsePreview: '',
        bizCode: '—',
        bizMsg: '—',
        feMs: null,
        beMs: null,
        totalMs
      }
      if (s.kind === 'fe_api') {
        cur.requestParams = s.requestParams || cur.requestParams
        cur.requestBody = s.requestBody || cur.requestBody
        cur.responsePreview = s.responsePreview || cur.responsePreview
        cur.bizCode = s.bizCode != null ? s.bizCode : cur.bizCode
        cur.bizMsg = s.bizMsg || cur.bizMsg
        cur.feMs = s.durationMs
        cur.status = s.status || cur.status
      } else {
        cur.beMs = s.durationMs
        cur.durationMs = s.durationMs
        cur.start = s.start || cur.start
        cur.status = s.status || cur.status
        cur.service = s.service
      }
      byKey.set(key, cur)
    }
    for (const cur of byKey.values()) {
      if (!cur.durationMs) cur.durationMs = cur.beMs != null ? cur.beMs : (cur.feMs || 0)
      cur.totalMs = totalMs
    }
    const mappedOthers = others.map((s) => ({
      id: s.id,
      kind: s.kind === 'page' ? 'page' : s.kind === 'sql' ? 'sql' : s.kind,
      kindLabel: s.kind === 'page' ? '页面' : s.kind === 'sql' ? 'SQL' : s.kind,
      name: s.name,
      service: s.service,
      bar: s.kind === 'page' ? 'pg' : s.kind === 'sql' ? 'sql' : 'fe',
      ok: 'OK',
      status: s.status || '—',
      durationMs: s.durationMs || 0,
      start: s.start || 0,
      sql: s.sql,
      totalMs
    }))
    return [
      ...mappedOthers.filter((x) => x.kind === 'page'),
      ...byKey.values(),
      ...mappedOthers.filter((x) => x.kind !== 'page')
    ]
  }

  global.LT_PV_MOCK = { visits, pureApiTraces, mergeSpans }
})(typeof window !== 'undefined' ? window : globalThis)
