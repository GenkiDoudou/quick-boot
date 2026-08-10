## 1. 数据与权限

- [x] 1.1 Flyway：创建 `sys_dept`、`sys_dict_type`、`sys_dict_data`；种子根部门 + `sys_normal_disable` 字典
- [x] 1.2 Flyway：如需统一 `sys_user.user_id` / `sys_user_role.user_id` 为 BIGINT，一并迁移（本期保持 VARCHAR + Java Long 映射，避免破坏现有种子）
- [x] 1.3 Flyway：菜单与按钮权限（dept / dict / dictData / user，含 export、import、refresh、resetPwd）并挂超管

## 2. 部门（后端 + 前端）

- [x] 2.1 Entity/Mapper/VO/ImportRow；`ISysDeptService` + Impl（list 树、treeselect、CRUD 显式赋值、删前校验子节点与用户绑定）
- [x] 2.2 `SysDeptController`：`list`/`treeselect`/`page?`/`add`/`update`/`remove`/`export`/`import`
- [x] 2.3 前端 `api/system/dept.js` + `views/system/dept/index.vue`（树表 + 同步导入导出）

## 3. 字典类型 / 字典项

- [x] 3.1 DictType：Entity/Service/Controller（CRUD、refresh、导入导出；删前无数据）
- [x] 3.2 DictData：Entity/Service/Controller（CRUD、`GET type/{dictType}`、导入导出；`(dictType,dictValue)` 唯一）
- [x] 3.3 前端 dict type/data 页（自 bak 改造）+ 重写 `api/system/dict/*`；`getDicts` 对齐 `/sys/dict/data/type/{dictType}`

## 4. 用户管理

- [x] 4.1 扩展 `ISysUserService`/`SysUserServiceImpl`：page/CRUD/导入导出/changeStatus/resetPwd/authRole；响应去 password；保护 admin
- [x] 4.2 `SysUserController` 暴露上述 API（路径 `sys/user`）
- [x] 4.3 前端 user 页 + add-or-update + auth-role（自 bak）；部门树选；同步导入导出；无 profile

## 5. 验证

- [x] 5.1 `mvn -pl quickboot-system -am compile` 通过
- [ ] 5.2 手工冒烟：Dept/Dict/User CRUD、导入失败明细、有用户删部门拒绝、字典 refresh、`useDict`、分配角色与重置密码
