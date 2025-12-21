let api = [];
const apiDocListSize = 1
api.push({
    name: 'default',
    order: '1',
    list: []
})
api[0].list.push({
    alias: 'DemoCurdController',
    order: '1',
    link: 'crud',
    desc: 'crud',
    list: []
})
api[0].list[0].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/demo/crud/list',
    methodId: 'c8ced5eae035895851271ccc6554d383',
    desc: '页面',
});
api[0].list[0].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:12000/demo/crud/remoteSelect',
    methodId: 'cd9b3f0653994cd422c01238ff4cbc16',
    desc: '远程选择',
});
api[0].list[0].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:12000/demo/crud/{id}',
    methodId: '522a44419df567e2b328cad1887f32b8',
    desc: '按id获取',
});
api[0].list[0].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://localhost:12000/demo/crud/tree',
    methodId: '6d0f961edabac506e270816a53c37259',
    desc: '树形下拉框',
});
api[0].list[0].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://localhost:12000/demo/crud/tree/parent/{id}',
    methodId: '59923e8b99ab90c0309fc912aa7b8d63',
    desc: '查询所有的父级',
});
api[0].list[0].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://localhost:12000/demo/crud',
    methodId: '56a0ebbc34172b6d17e7cb7b7f445ea1',
    desc: '新增',
});
api[0].list[0].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://localhost:12000/demo/crud',
    methodId: 'ff7f707fa00ef011fc88af65a9bd7400',
    desc: '修改',
});
api[0].list[0].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://localhost:12000/demo/crud',
    methodId: '3556054f30edbfd66d2e1b73fa16a792',
    desc: '批量删除',
});
api[0].list.push({
    alias: 'GenTableColumnController',
    order: '2',
    link: '代码生成业务表字段',
    desc: '代码生成业务表字段',
    list: []
})
api[0].list[1].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/generator/gentablecolumn/list',
    methodId: 'fc16fe0c2257708484402fefcc83a463',
    desc: '分页查询',
});
api[0].list[1].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:12000/generator/gentablecolumn/updateBatch',
    methodId: 'a795dc04ce8044d9dff99de83b6c3799',
    desc: '批量修改',
});
api[0].list.push({
    alias: 'GenTableController',
    order: '3',
    link: '代码生成业务表',
    desc: '代码生成业务表',
    list: []
})
api[0].list[2].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/generator/gentable/list',
    methodId: '2d4658e419aa0f663bd1da670fee8514',
    desc: '分页查询',
});
api[0].list[2].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:12000/generator/gentable/save',
    methodId: '4a90a0d42a74394c4e239301a37a56e7',
    desc: '保存',
});
api[0].list[2].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:12000/generator/gentable',
    methodId: 'd65ce949ce5d3b94bf23e942af71bb02',
    desc: '根据id修改',
});
api[0].list[2].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://localhost:12000/generator/gentable/{id}',
    methodId: 'c27d525fa7463cf06977f527e6c59c71',
    desc: '根据id查询',
});
api[0].list[2].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://localhost:12000/generator/gentable',
    methodId: 'bb2421c2669bd8dde8f38f3718757128',
    desc: '根据ids 删除',
});
api[0].list[2].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://localhost:12000/generator/gentable/createTableSave',
    methodId: 'd843eeadff3b435cb0b5fe2792a24fe0',
    desc: '根据sql创建表',
});
api[0].list[2].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://localhost:12000/generator/gentable/dbTables',
    methodId: '538d7b98153113c838ca6097c0cda255',
    desc: '从数据库导入表',
});
api[0].list[2].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://localhost:12000/generator/gentable/importTable',
    methodId: '4e6c34b85eda520e2c5642f58d30af82',
    desc: '导入表',
});
api[0].list[2].list.push({
    order: '9',
    deprecated: 'false',
    url: 'http://localhost:12000/generator/gentable/tableSyn/{tableId}',
    methodId: '97687d53ad7872380cc0cbfc49cf1577',
    desc: '表信息同步',
});
api[0].list[2].list.push({
    order: '10',
    deprecated: 'false',
    url: 'http://localhost:12000/generator/gentable/generator',
    methodId: '0c34b4b18965c18603d0e5a2e9d7d974',
    desc: '代码生成',
});
api[0].list[2].list.push({
    order: '11',
    deprecated: 'false',
    url: 'http://localhost:12000/generator/gentable/generator/download',
    methodId: '22c583377c988d90886d8546cb534386',
    desc: '代码生成(下载)',
});
api[0].list[2].list.push({
    order: '12',
    deprecated: 'false',
    url: 'http://localhost:12000/generator/gentable/preview/{tableId}',
    methodId: 'c956981af1cca0ed5e126ebfaf82053f',
    desc: '预览',
});
api[0].list.push({
    alias: 'SysJobController',
    order: '4',
    link: '定时任务调度表',
    desc: '定时任务调度表',
    list: []
})
api[0].list[3].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/quartz/sysjob/list',
    methodId: 'f5ac82616d81db034af7f10be0381aa7',
    desc: '分页查询',
});
api[0].list[3].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:12000/quartz/sysjob',
    methodId: '80bdeb8ce3f066330a6463ac70fab359',
    desc: '保存',
});
api[0].list[3].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:12000/quartz/sysjob',
    methodId: '7c724c2e9a65409e5360618074c6d727',
    desc: '根据id修改',
});
api[0].list[3].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://localhost:12000/quartz/sysjob/{id}',
    methodId: 'bc47fb73b7237343317662216c889fc0',
    desc: '根据id查询',
});
api[0].list[3].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://localhost:12000/quartz/sysjob',
    methodId: '96454f2f2a3e6e8e650ecbcfe3317198',
    desc: '根据ids 删除',
});
api[0].list[3].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://localhost:12000/quartz/sysjob/changeStatus/{id}/{status}',
    methodId: '0083bce113d2059a96bfe723f8de7ac7',
    desc: '修改状态',
});
api[0].list[3].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://localhost:12000/quartz/sysjob/run/{id}',
    methodId: '751c394814a723a2aa128866f061e082',
    desc: '立即执行',
});
api[0].list.push({
    alias: 'SysJobLogController',
    order: '5',
    link: '定时任务调度日志表',
    desc: '定时任务调度日志表',
    list: []
})
api[0].list[4].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/quartz/sysjoblog/list',
    methodId: '2d990e1341fcfa719a8aacf746c92e36',
    desc: '分页查询',
});
api[0].list[4].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:12000/quartz/sysjoblog/{id}',
    methodId: '4f1eac2ad6e5527e1fcd719aa9e362a6',
    desc: '根据id查询',
});
api[0].list[4].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:12000/quartz/sysjoblog/clean',
    methodId: '7b47dc18917e091b23588337f66e2cb8',
    desc: '清空日志',
});
api[0].list.push({
    alias: 'LoginUserController',
    order: '6',
    link: '登录用户',
    desc: '登录用户',
    list: []
})
api[0].list[5].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/getInfo',
    methodId: '9a6e683fdfc536783d99640f318e9d96',
    desc: '获取当前登陆人的信息',
});
api[0].list[5].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:12000/getRouters',
    methodId: '9a4cce1507fd9c9d3adde893bddf9af1',
    desc: '获取当前登录人的路由信息',
});
api[0].list.push({
    alias: 'SysDictControllr',
    order: '7',
    link: '字典项',
    desc: '字典项',
    list: []
})
api[0].list[6].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/system/dict/type/{dictType}',
    methodId: 'ffcb900be3aed7015180ed76674189cb',
    desc: '根据字段类型查询字典项的信息',
});
api[0].list[6].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:12000/system/dict/type/page',
    methodId: 'de3a7b1f703c224a253e9584423ed10f',
    desc: '字典类型分页接口',
});
api[0].list[6].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:12000/system/dict/type/info/{id}',
    methodId: '2fbebe85f9ce771c02395cda81f62125',
    desc: '根据id查询',
});
api[0].list[6].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://localhost:12000/system/dict/type',
    methodId: '276a79d903336550a506249e772838bf',
    desc: '保存字典类型',
});
api[0].list[6].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://localhost:12000/system/dict/type',
    methodId: '6585b9ce96179b9af2342c6e18096fe6',
    desc: '修改字典类型',
});
api[0].list[6].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://localhost:12000/system/dict/type',
    methodId: 'e888e7d997a004e3858acf4d91fbd3dd',
    desc: '删除字典类型',
});
api[0].list[6].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://localhost:12000/system/dict/data/page',
    methodId: 'ffaae710e8722bab2b429d545b5d4b4b',
    desc: '字典数据分页',
});
api[0].list[6].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://localhost:12000/system/dict/data',
    methodId: 'f33df906fa257c950d18e9dae7087047',
    desc: '字典项保存',
});
api[0].list[6].list.push({
    order: '9',
    deprecated: 'false',
    url: 'http://localhost:12000/system/dict/data',
    methodId: 'ed0251c95a4976c3c6b06ab93b0c3b27',
    desc: '字典项修改',
});
api[0].list[6].list.push({
    order: '10',
    deprecated: 'false',
    url: 'http://localhost:12000/system/dict/data',
    methodId: '4bae0569343ec1e42ab8308d034a8592',
    desc: '字典项删除',
});
api[0].list[6].list.push({
    order: '11',
    deprecated: 'false',
    url: 'http://localhost:12000/system/dict/data/{id}',
    methodId: '4e0bcb62938832e3a0f773046591c75a',
    desc: '根据id查询字典项',
});
api[0].list[6].list.push({
    order: '12',
    deprecated: 'false',
    url: 'http://localhost:12000/system/dict/types',
    methodId: 'a00fcfbe91263b97cd8aa3435adabd88',
    desc: '查询所有的字典项',
});
api[0].list[6].list.push({
    order: '13',
    deprecated: 'false',
    url: 'http://localhost:12000/system/dict/export',
    methodId: '99292bca8e7a3fb475482772f627cef3',
    desc: 'excel导出',
});
api[0].list.push({
    alias: 'SysLogininforController',
    order: '8',
    link: '系统访问记录',
    desc: '系统访问记录',
    list: []
})
api[0].list[7].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/system/syslogininfor/list',
    methodId: 'c1930190dba653eb282733f6630bd70b',
    desc: '分页查询',
});
api[0].list[7].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:12000/system/syslogininfor/{id}',
    methodId: '96d7b4895ded0f2a3a7a33b0cdc3fcc7',
    desc: '根据id查询',
});
api[0].list.push({
    alias: 'SysMenuController',
    order: '9',
    link: '系统菜单',
    desc: '系统菜单',
    list: []
})
api[0].list[8].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/system/menu/treeselect',
    methodId: '92838e173380ff066ce1c7a4410b553a',
    desc: '获取菜单下拉树列表',
});
api[0].list[8].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:12000/system/menu/roleMenuTreeselect/{roleId}',
    methodId: '94a30855b08423ed79c9241cfae653fe',
    desc: '根据角色id查询对应的菜单树列表',
});
api[0].list[8].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:12000/system/menu/list',
    methodId: '159c4dad2fa117552ac415bda0a0f07d',
    desc: '查询菜单列表',
});
api[0].list[8].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://localhost:12000/system/menu/{id}',
    methodId: '8a4ad99079c3d814172065c5225122ab',
    desc: '根据id查询',
});
api[0].list[8].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://localhost:12000/system/menu',
    methodId: '333a643ab62970a4ac162cea3eb6bfaa',
    desc: '保存菜单',
});
api[0].list[8].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://localhost:12000/system/menu',
    methodId: '4b0eb5480bf7a1226ca6b95df3e0678a',
    desc: '根据id修改',
});
api[0].list[8].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://localhost:12000/system/menu/{id}',
    methodId: '76502a7a254b5697e8c667b257d082f0',
    desc: '根据id删除',
});
api[0].list[8].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://localhost:12000/system/menu/tree',
    methodId: '95c9c86cf83dc1b81a19af1bd804acda',
    desc: '树形下拉框',
});
api[0].list[8].list.push({
    order: '9',
    deprecated: 'false',
    url: 'http://localhost:12000/system/menu/tree/parent/{id}',
    methodId: 'e219c0672d3707cee221f45eabc9cf6a',
    desc: '查询所有的父级',
});
api[0].list.push({
    alias: 'SysOperLogController',
    order: '10',
    link: '操作日志记录',
    desc: '操作日志记录',
    list: []
})
api[0].list[9].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/system/sysoperlog/list',
    methodId: '4bd1b343562ce2eeeae7523bfa4dc55b',
    desc: '分页查询',
});
api[0].list[9].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:12000/system/sysoperlog/{id}',
    methodId: '16057f928e9eda4fda37293e48249d78',
    desc: '根据id查询',
});
api[0].list.push({
    alias: 'SysRoleController',
    order: '11',
    link: '角色',
    desc: '角色',
    list: []
})
api[0].list[10].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/system/role/list',
    methodId: '7ebc4b63aede6019b34d6dc5b0209950',
    desc: '分页查询',
});
api[0].list[10].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:12000/system/role/export',
    methodId: '34ca4b0079589d868dc2e906c74b12fc',
    desc: '导出',
});
api[0].list[10].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:12000/system/role',
    methodId: 'cf1c348c71158d22f6a0acdfac2207ac',
    desc: '新增角色',
});
api[0].list[10].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://localhost:12000/system/role',
    methodId: '7a7432450582a8ffd620d55925e1d365',
    desc: '修改角色信息',
});
api[0].list[10].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://localhost:12000/system/role/{id}',
    methodId: 'ee3da943515804ec05c2305d40380b27',
    desc: '根据id查询',
});
api[0].list[10].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://localhost:12000/system/role',
    methodId: '707257f885608ce2317b29ea266392b8',
    desc: '根据id集合删除',
});
api[0].list[10].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://localhost:12000/system/role/checkedKeys/{roleId}',
    methodId: 'cc57bb7ce45989fe7d91eefda68760a2',
    desc: '该角色选中的菜单id',
});
api[0].list[10].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://localhost:12000/system/role/lists',
    methodId: '634df3406a2ad33d8ac08ffe659fac09',
    desc: '所有的角色',
});
api[0].list.push({
    alias: 'SysUserController',
    order: '12',
    link: '系统用户',
    desc: '系统用户',
    list: []
})
api[0].list[11].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/sys/user/page',
    methodId: '01ef6f658a1dfd7e164601adf5df6c05',
    desc: '用户分页',
});
api[0].list[11].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:12000/sys/user',
    methodId: 'dd8669bfc34d799a3f9624baf6a8cb57',
    desc: '用户信息保存',
});
api[0].list[11].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:12000/sys/user',
    methodId: '6e998bdc61ab1cbbd0f615600908f218',
    desc: '根据id修改',
});
api[0].list[11].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://localhost:12000/sys/user/{id}',
    methodId: '0e70f641bb24d50c69c805134323f24a',
    desc: '根据id查询',
});
api[0].list[11].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://localhost:12000/sys/user/delete',
    methodId: '12b78821c364a56c84b91f5b49527e8b',
    desc: '根据ids 删除',
});
api[0].list[11].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://localhost:12000/sys/user/resetPwd/{userId}',
    methodId: 'b339f509619d9c2d5a1c5fbb12db58ec',
    desc: '重置密码',
});
api[0].list[11].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://localhost:12000/sys/user/updateStatus/{userId}/{status}',
    methodId: '31014cf6b2c810907c3a5aa7ea05be3b',
    desc: '修改状态',
});
api[0].list[11].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://localhost:12000/sys/user/exportExcel',
    methodId: 'b69506102d5961f1dbed95059228f4a9',
    desc: '导出excel',
});
api[0].list[11].list.push({
    order: '9',
    deprecated: 'false',
    url: 'http://localhost:12000/sys/user/importExcel',
    methodId: '03ab1d566ae6f04756727f6c969e804c',
    desc: '导入excel',
});
api[0].list.push({
    alias: 'FileController',
    order: '13',
    link: '文件控制层',
    desc: '文件控制层',
    list: []
})
api[0].list[12].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/file/upload/{classify}',
    methodId: 'fad4b43c8d76ed454560274a7e437f60',
    desc: '文件上传',
});
api[0].list[12].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:12000/file/view/{classify}/**',
    methodId: 'bac1106cd61fb0a9b5de1ec68ee8e7e9',
    desc: '图片查看',
});
api[0].list.push({
    alias: 'CaptchaController',
    order: '14',
    link: '验证码控制层',
    desc: '验证码控制层',
    list: []
})
api[0].list[13].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/captchaImage',
    methodId: 'e25d6b801d164b712fcdd28527d524e4',
    desc: '创建验证码',
});
api[0].list.push({
    alias: 'SaTokenLoginController',
    order: '15',
    link: 'sa-token_登录控制器',
    desc: 'Sa-Token 登录控制器',
    list: []
})
api[0].list[14].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://localhost:12000/login',
    methodId: 'cdace50f54552b8b774d9b6d9dab3fc3',
    desc: '用户登录',
});
api[0].list[14].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://localhost:12000/json',
    methodId: 'f112579c09f4ec2e34e485c07db33a59',
    desc: '用户登录（JSON格式）',
});
api[0].list[14].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://localhost:12000/auth',
    methodId: '40da1742a9cc0a44acb167476a6bf015',
    desc: '用户登录（兼容原有接口）',
});
api[0].list[14].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://localhost:12000/logout',
    methodId: '6c0d8d171919c0b1465bef676e8e72b5',
    desc: '用户登出',
});
api[0].list[14].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://localhost:12000/check',
    methodId: 'ba5b9822d16128f0b3f81ffb0e17c967',
    desc: '检查登录状态',
});
api[0].list[14].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://localhost:12000/user',
    methodId: 'c5278def05ea0011041d2877fdc011c6',
    desc: '获取当前登录用户信息',
});
api[0].list[14].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://localhost:12000/kickout/{loginId}',
    methodId: '167bf84cd39114a1d0b3c862416371ee',
    desc: '踢人下线',
});
api[0].list[14].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://localhost:12000/kickout/{loginId}/{device}',
    methodId: '715667422df664eface06df80b5b0991',
    desc: '踢人下线（指定设备）',
});
document.onkeydown = keyDownSearch;
function keyDownSearch(e) {
    const theEvent = e;
    const code = theEvent.keyCode || theEvent.which || theEvent.charCode;
    if (code === 13) {
        const search = document.getElementById('search');
        const searchValue = search.value.toLocaleLowerCase();

        let searchGroup = [];
        for (let i = 0; i < api.length; i++) {

            let apiGroup = api[i];

            let searchArr = [];
            for (let i = 0; i < apiGroup.list.length; i++) {
                let apiData = apiGroup.list[i];
                const desc = apiData.desc;
                if (desc.toLocaleLowerCase().indexOf(searchValue) > -1) {
                    searchArr.push({
                        order: apiData.order,
                        desc: apiData.desc,
                        link: apiData.link,
                        alias: apiData.alias,
                        list: apiData.list
                    });
                } else {
                    let methodList = apiData.list || [];
                    let methodListTemp = [];
                    for (let j = 0; j < methodList.length; j++) {
                        const methodData = methodList[j];
                        const methodDesc = methodData.desc;
                        if (methodDesc.toLocaleLowerCase().indexOf(searchValue) > -1) {
                            methodListTemp.push(methodData);
                            break;
                        }
                    }
                    if (methodListTemp.length > 0) {
                        const data = {
                            order: apiData.order,
                            desc: apiData.desc,
                            link: apiData.link,
                            alias: apiData.alias,
                            list: methodListTemp
                        };
                        searchArr.push(data);
                    }
                }
            }
            if (apiGroup.name.toLocaleLowerCase().indexOf(searchValue) > -1) {
                searchGroup.push({
                    name: apiGroup.name,
                    order: apiGroup.order,
                    list: searchArr
                });
                continue;
            }
            if (searchArr.length === 0) {
                continue;
            }
            searchGroup.push({
                name: apiGroup.name,
                order: apiGroup.order,
                list: searchArr
            });
        }
        let html;
        if (searchValue === '') {
            const liClass = "";
            const display = "display: none";
            html = buildAccordion(api,liClass,display);
            document.getElementById('accordion').innerHTML = html;
        } else {
            const liClass = "open";
            const display = "display: block";
            html = buildAccordion(searchGroup,liClass,display);
            document.getElementById('accordion').innerHTML = html;
        }
        const Accordion = function (el, multiple) {
            this.el = el || {};
            this.multiple = multiple || false;
            const links = this.el.find('.dd');
            links.on('click', {el: this.el, multiple: this.multiple}, this.dropdown);
        };
        Accordion.prototype.dropdown = function (e) {
            const $el = e.data.el;
            let $this = $(this), $next = $this.next();
            $next.slideToggle();
            $this.parent().toggleClass('open');
            if (!e.data.multiple) {
                $el.find('.submenu').not($next).slideUp("20").parent().removeClass('open');
            }
        };
        new Accordion($('#accordion'), false);
    }
}

function buildAccordion(apiGroups, liClass, display) {
    let html = "";
    if (apiGroups.length > 0) {
        if (apiDocListSize === 1) {
            let apiData = apiGroups[0].list;
            let order = apiGroups[0].order;
            for (let j = 0; j < apiData.length; j++) {
                html += '<li class="'+liClass+'">';
                html += '<a class="dd" href="#' + apiData[j].alias + '">' + apiData[j].order + '.&nbsp;' + apiData[j].desc + '</a>';
                html += '<ul class="sectlevel2" style="'+display+'">';
                let doc = apiData[j].list;
                for (let m = 0; m < doc.length; m++) {
                    let spanString;
                    if (doc[m].deprecated === 'true') {
                        spanString='<span class="line-through">';
                    } else {
                        spanString='<span>';
                    }
                    html += '<li><a href="#' + doc[m].methodId + '">' + apiData[j].order + '.' + doc[m].order + '.&nbsp;' + spanString + doc[m].desc + '<span></a> </li>';
                }
                html += '</ul>';
                html += '</li>';
            }
        } else {
            for (let i = 0; i < apiGroups.length; i++) {
                let apiGroup = apiGroups[i];
                html += '<li class="'+liClass+'">';
                html += '<a class="dd" href="#_'+apiGroup.order+'_' + apiGroup.name + '">' + apiGroup.order + '.&nbsp;' + apiGroup.name + '</a>';
                html += '<ul class="sectlevel1">';

                let apiData = apiGroup.list;
                for (let j = 0; j < apiData.length; j++) {
                    html += '<li class="'+liClass+'">';
                    html += '<a class="dd" href="#' + apiData[j].alias + '">' +apiGroup.order+'.'+ apiData[j].order + '.&nbsp;' + apiData[j].desc + '</a>';
                    html += '<ul class="sectlevel2" style="'+display+'">';
                    let doc = apiData[j].list;
                    for (let m = 0; m < doc.length; m++) {
                       let spanString;
                       if (doc[m].deprecated === 'true') {
                           spanString='<span class="line-through">';
                       } else {
                           spanString='<span>';
                       }
                       html += '<li><a href="#' + doc[m].methodId + '">'+apiGroup.order+'.' + apiData[j].order + '.' + doc[m].order + '.&nbsp;' + spanString + doc[m].desc + '<span></a> </li>';
                   }
                    html += '</ul>';
                    html += '</li>';
                }

                html += '</ul>';
                html += '</li>';
            }
        }
    }
    return html;
}