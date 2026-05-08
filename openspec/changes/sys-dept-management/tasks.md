## 1. 数据库与领域模型

- [ ] 1.1 在 `quickboot-web/src/main/resources/db/migration` 新增 **`V2__sys_dept.sql`**（版本号若已占用则顺延）：创建 `sys_dept` 表，含 `dept_id`、`parent_id`（顶级 **-1**）、`dept_name`、`order_num`、`leader`、`phone`、`email`、`status`、`remark`、`del_flag`、审计字段；`parent_id`+`del_flag` 索引；H2/MySQL 可执行
- [ ] 1.2 新增 `SysDept` 实体与 MyBatis-Plus `Mapper`（包路径与 `quickboot-web` 现有分层一致），字段与表映射齐备 **JavaDoc**

## 2. 后端接口（对齐 `dept.js`）

- [ ] 2.1 实现 `DeptService`：加载全表（未删）→ 内存建树；**`list`** 支持无参全树与 **`deptName`/`leader`/`status` 剪枝**（语义见 `specs/sys-dept/spec.md`）；**`treeselect`** 全量树映射为 `id`/`label`/`children`；**`children` 空为 `[]`**
- [ ] 2.2 实现 `GET /system/dept/list`、`GET /system/dept/treeselect`、`GET /system/dept/{id}`、`POST /system/dept`、`PUT /system/dept`、`DELETE /system/dept/{id}`，返回 **`R`** 且成功体 **`data`** 形状与设计一致
- [ ] 2.3 **写操作校验**：父存在、非自指、**防成环**；删除前 **子部门计数**；失败返回明确 **`msg`**
- [ ] 2.4（可选）在 Service 预留 **用户占用删除校验** 扩展点（接口或 TODO + JavaDoc 指向 `spec.md` 延后条），**不**调用数据库用户表

## 3. 后端验证

- [ ] 3.1 `mvn -pl quickboot-web test`（或至少编译）通过；补充 **Service 或 Web 层单测** 覆盖：全树、`deptName` 剪枝、删有子失败、改父成环失败

## 4. 前端部门管理页

- [ ] 4.1 新增 `quick-ui/src/views/system/dept/index.vue`（及必要子组件如 `add-or-update.vue`）：筛选 **`deptName`/`leader`/`status`**（`useDict('sys_normal_disable')`）；**`el-table` 树** 绑定 `listDept` 的 **`res.data`**（注意 axios 封装：以实际返回结构为准，确保为树根数组）；`row-key="deptId"`，`tree-props` 使用 **`children: 'children'`**
- [ ] 4.2 新增/编辑表单：上级部门使用 **`el-tree-select` 或等价**，数据来自 **`listTreeDept`**；字段与后端 **camelCase** 对齐
- [ ] 4.3 操作列：**查看**（只读）、**新增**（可带当前行 `deptId` 为默认 `parentId`）、**修改**、**删除**（`ElMessageBox` 二次确认 + 展示后端错误）
- [ ] 4.4 权限字符串与 **`v-hasPermi`** 对齐（建议）：**`system:dept:list`**、**`system:dept:query`**、**`system:dept:add`**、**`system:dept:edit`**、**`system:dept:remove`**（若与团队字典不一致则在 PR 说明中列出最终值）
- [ ] 4.5 在 **`quick-ui/src/router/index.js`**（或项目实际动态路由来源）注册部门管理路由，**meta.title** 如「部门管理」，路径不与现有冲突

## 5. 联调与构建

- [ ] 5.1 本地启动 **quickboot-web** 与 **quick-ui**，完成增删改查与筛选、**`treeselect`** 联调截图或文字记录
- [ ] 5.2 `cd quick-ui && pnpm build:prod` 通过

## 6. 收尾

- [ ] 6.1 对照 `openspec/changes/sys-dept-management/specs/sys-dept/spec.md` 全条走查，更新本 `tasks.md` 勾选状态
- [ ] 6.2（可选）`docs` 或变更说明中增加「**`list` 为树形 `data`**」一句，避免调用方误用
