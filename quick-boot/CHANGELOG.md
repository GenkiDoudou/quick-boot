# Changelog
Changelog of My Project.

## Unreleased
### No issue

**refactor(docs): 重构文档网站并整合 C7 组件**

 * - 导入 C7 组件库到 docs 项目
 * - 更新按钮、标题、卡片等组件的示例
 * - 移除重复的组件导入语句
 * - 优化部分组件的使用方式

[366a6a06637a35f](https://gitee.com/rainsoil/quickboot/commit/366a6a06637a35f) luyanan *2025-09-14 08:46:50*

**chore(docs): 清理 .vitepress/cache 目录- 删除了 @element-plus_icons-vue.js、@vue_shared.js、_metadata.json 等多个缓存文件**

 * - 移除了与项目无关的导入和导出语句
 * - 清理了无用的代码片段和映射文件

[6d8a1d15d132766](https://gitee.com/rainsoil/quickboot/commit/6d8a1d15d132766) luyanan *2025-09-13 09:29:22*

**chore(docs): 清理 .vitepress/cache 目录- 删除了 @element-plus_icons-vue.js、@vue_shared.js、_metadata.json 等多个缓存文件**

 * - 移除了与项目无关的导入和导出语句
 * - 清理了无用的代码片段和映射文件

[760bcc812406cac](https://gitee.com/rainsoil/quickboot/commit/760bcc812406cac) luyanan *2025-09-13 09:28:12*

**chore(docs): 清理 .vitepress/cache 目录- 删除了 @element-plus_icons-vue.js、@vue_shared.js、_metadata.json 等多个缓存文件**

 * - 移除了与项目无关的导入和导出语句
 * - 清理了无用的代码片段和映射文件

[24344613779122c](https://gitee.com/rainsoil/quickboot/commit/24344613779122c) luyanan *2025-08-08 14:54:14*

**docs(c7): 补充 json-form、json-table 组件文档- 完善 c7-json-form 组件的属性、插槽、事件说明**

 * - 添加 c7-json-form 组件的字段配置示例
 * - 完善 c7-json-table 组件的属性、插槽、事件说明
 * - 添加 c7-json-table 组件的列配置示例

[5151e0581886f9e](https://gitee.com/rainsoil/quickboot/commit/5151e0581886f9e) luyanan *2025-07-27 13:45:25*

**docs:优化文档配置和页面布局**

 * - 修复侧边栏中 c7-json-table 链接地址
 * - 移除部分赞助商信息
 * - 删除 Pinia 相关代码
 * -简化分页组件布局
 * - 更新文档推送脚本

[df0f89ed530440c](https://gitee.com/rainsoil/quickboot/commit/df0f89ed530440c) luyanan *2025-07-27 13:05:57*

**chore:**

 * - 移除所有组件和工具类文件
 * - 删除 package.json 和 tsconfig.json
 * -移除 vite 配置文件

[437ccc0e7d08a22](https://gitee.com/rainsoil/quickboot/commit/437ccc0e7d08a22) luyanan *2025-07-27 12:08:18*

**feat(bak): 新增 c7-json-table 组件并重构相关功能**

 * - 新增 c7-json-table 组件，用于展示 JSON 数据的表格
 * - 重构 c7-button 组件，增加更多属性和事件支持
 * - 新增 utils 目录，包含常用的工具函数
 * - 新增 hooks 目录，包含自定义 Hook 函数
 * - 更新 docs 目录，添加新的组件文档和示例
 * - 优化项目结构，统一命名规范

[9cd32a42d79e10a](https://gitee.com/rainsoil/quickboot/commit/9cd32a42d79e10a) luyanan *2025-07-26 14:29:43*

**feat(c7-plus): 新增 c7JsonTableColumn 组件**

 * - 添加 c7JsonTableColumn 组件，实现基于 JSON 配置的动态表格列
 * - 支持文本、标签、图片、插槽等多种列类型
 * - 集成 c7DictTag 和 c7Preview 组件
 * - 更新文档和示例

[27de82ad9a923d7](https://gitee.com/rainsoil/quickboot/commit/27de82ad9a923d7) luyanan *2025-07-24 14:21:04*

**feat(docs): 新增 JSON Form组件并优化 CRUD 示例**

 * - 新增 JSON Form 组件，支持动态表单生成和数据绑定
 * - 更新 CRUD 示例，使用 c7-dict-tag 组件替换原有的性别字段显示
 * - 在主题配置中添加 Element Plus 的中文语言包
 * - 调整表单验证规则和提示信息，提升用户体验

[3d925a44f77b14d](https://gitee.com/rainsoil/quickboot/commit/3d925a44f77b14d) luyanan *2025-07-23 15:09:36*

**feat(components): 新增 crud 和 dict-tag 组件**

 * - 在 c7-plus 组件库中添加了 c7-crud 和 c7-dict-tag 两个新组件
 * - 更新了文档侧边栏，增加了新组件的链接
 * - 创建了新组件的文档页面和示例演示
 * - 在项目中集成了新组件，并更新了相关类型定义

[4346ba184776935](https://gitee.com/rainsoil/quickboot/commit/4346ba184776935) luyanan *2025-07-20 14:10:39*

**feat(c7-cascader):重构级联组件并添加新功能**

 * - 重新设计了级联组件的属性和API，增加了更多自定义选项
 * - 添加了懒加载和非懒加载两种数据加载方式
 * - 实现了多种结果类型的返回值处理
 * - 优化了父级节点数据的获取逻辑
 * - 重构了组件内部的数据处理和绑定逻辑

[46ccecab16ee476](https://gitee.com/rainsoil/quickboot/commit/46ccecab16ee476) luyanan *2025-07-12 15:46:14*

**feat(c7Checkbox): 重构复选框组件并添加新功能- 重新设计了 c7Checkbox 组件的 API 和内部逻辑- 添加了多种复选框样式和使用场景的示例**

 * - 新增了全选/全不选、按钮样式、change 事件等功能
 * - 优化了数据加载和处理逻辑，支持异步数据获取
 * - 更新了文档，增加了更多示例和详细的属性说明

[d318d6c6b3c4ed4](https://gitee.com/rainsoil/quickboot/commit/d318d6c6b3c4ed4) luyanan *2025-07-02 14:33:14*

**docs(c7): 重构 DatePicker组件文档**

 * - 更新文档结构，增加基础用法、结果合并、默认值和数组结果等示例
 * - 添加组件 Attributes、Events 和 Slots 的详细说明
 * - 重新组织代码示例，使其更加清晰和模块化
 * - 优化组件实现，提高代码可读性和维护性

[78fc447f109a4af](https://gitee.com/rainsoil/quickboot/commit/78fc447f109a4af) luyanan *2025-07-01 15:51:01*

**feat(c7-button): 重构按钮组件并添加新功能**

 * -重构了 c7-button 组件的内部逻辑和结构
 * - 添加了多种按钮类型和样式
 * -增加了按钮点击事件处理和二次确认功能
 * -集成了表单验证功能
 * - 优化了文档和示例代码

[64b60ac7c2bf002](https://gitee.com/rainsoil/quickboot/commit/64b60ac7c2bf002) luyanan *2025-06-30 15:02:08*

**refactor(c7-select): 重构 c7Select 组件并优化数据获取方式**

 * - 新增 api.js 文件，统一处理数据请求
 * - 修改 demo2.vue，使用新的数据获取方法
 * - 重构 c7-select 组件，使用 utils 中的 jsonGet 函数
 * - 新增 utils/utils.js 文件，添加通用的 jsonGet 函数

[51f07205772d0a3](https://gitee.com/rainsoil/quickboot/commit/51f07205772d0a3) luyanan *2025-06-28 13:41:49*

**docs(c7-select): 更新文档并添加示例**

 * - 重构了 c7-select 组件的代码，优化了功能和性能
 * - 更新了文档，增加了更多示例和使用说明
 * - 添加了多个新的示例文件，展示了不同功能的使用方法
 * - 调整了组件属性和方法，提高了灵活性和可定制性

[0f69b610873b3cb](https://gitee.com/rainsoil/quickboot/commit/0f69b610873b3cb) luyanan *2025-06-28 09:21:18*

**feat(dict): 重构字典模块并添加新功能**

 * - 重新设计了字典存储和查询逻辑，提高了效率和灵活性
 * - 新增 C7Select 组件，用于异步加载字典数据
 * - 更新了相关文档和示例- 优化了代码结构，提高了可维护性

[7106cf20c57c367](https://gitee.com/rainsoil/quickboot/commit/7106cf20c57c367) luyanan *2025-06-25 14:31:56*

**style(system): 优化字典列表页面样式**

 * - 在字典列表页面，将字典类型链接样式改为蓝色可点击样式- 优化代码格式，调整导入语句和空格

[5f9e8cc45522590](https://gitee.com/rainsoil/quickboot/commit/5f9e8cc45522590) luyanan *2025-06-21 15:03:08*

**fix(c7-ui): 修改el-table表格重置的时候参数不重置的bug**

 * 修改el-table表格重置的时候参数不重置的bug

[00a362cb55266fc](https://gitee.com/rainsoil/quickboot/commit/00a362cb55266fc) luyanan *2025-06-21 14:49:49*

**fix(docs): 修改文档**


[27b4972691bdd6e](https://gitee.com/rainsoil/quickboot/commit/27b4972691bdd6e) luyanan *2025-06-02 12:57:31*

**fix(docs): 修改文档**


[7e2533168be8263](https://gitee.com/rainsoil/quickboot/commit/7e2533168be8263) luyanan *2025-05-29 14:32:32*

**fix(system): 表格分页**


[88d911f1d2389ef](https://gitee.com/rainsoil/quickboot/commit/88d911f1d2389ef) luyanan *2025-05-09 13:51:47*

**fix(manage): 修改管理系统的代码**


[51e1c0cdb30b3ae](https://gitee.com/rainsoil/quickboot/commit/51e1c0cdb30b3ae) luyanan *2025-05-08 15:08:08*

**fix(system): 定时任务模块修改**


[bceeb2af96df363](https://gitee.com/rainsoil/quickboot/commit/bceeb2af96df363) luyanan *2025-04-28 14:43:07*

**feat(system): 管理系统组件修改**


[f0ddf7d01a72600](https://gitee.com/rainsoil/quickboot/commit/f0ddf7d01a72600) luyanan *2025-04-27 14:42:55*

**fix(docs): 修改字典值**


[3c0e6677fd1f4c0](https://gitee.com/rainsoil/quickboot/commit/3c0e6677fd1f4c0) luyanan *2025-04-23 15:09:56*

**fix(docs): 修改字典值**


[ae13cca1e54643d](https://gitee.com/rainsoil/quickboot/commit/ae13cca1e54643d) luyanan *2025-04-20 14:47:08*

**fix(docs): table组件的demo**


[b01cac4a38f9d44](https://gitee.com/rainsoil/quickboot/commit/b01cac4a38f9d44) luyanan *2025-04-17 15:03:05*

**fix(table组件):**


[7a03f2585378c41](https://gitee.com/rainsoil/quickboot/commit/7a03f2585378c41) luyanan *2025-04-13 15:52:20*

**fix(docs): 表格组件**


[52ac3993142df82](https://gitee.com/rainsoil/quickboot/commit/52ac3993142df82) luyanan *2025-04-03 15:10:03*

**fix(docs): 修改**


[230a12b4b67aceb](https://gitee.com/rainsoil/quickboot/commit/230a12b4b67aceb) luyanan *2025-04-03 13:58:09*

**fix(docs): 组件迁移**


[ce705c1627261a2](https://gitee.com/rainsoil/quickboot/commit/ce705c1627261a2) luyanan *2025-03-27 15:34:19*

**fix(docs): 字典值封装**


[2699ff6b78e1dfd](https://gitee.com/rainsoil/quickboot/commit/2699ff6b78e1dfd) luyanan *2025-03-25 15:36:00*

**fix(docs): tableHook**


[6d75fb170f63a26](https://gitee.com/rainsoil/quickboot/commit/6d75fb170f63a26) luyanan *2025-03-19 15:03:33*

**feat(ui): 增加title组件**


[866c00967de9ae3](https://gitee.com/rainsoil/quickboot/commit/866c00967de9ae3) luyanan *2025-03-18 07:07:57*

**fix(docs): 增加搜索组件**


[9df8e67ba2a93ac](https://gitee.com/rainsoil/quickboot/commit/9df8e67ba2a93ac) luyanan *2025-03-15 14:16:13*

**fix(docs): 增加文件上传和文件预览组件**


[090496a34c8231a](https://gitee.com/rainsoil/quickboot/commit/090496a34c8231a) luyanan *2025-03-11 14:51:31*

**文档**


[ebeac02da584544](https://gitee.com/rainsoil/quickboot/commit/ebeac02da584544) luyanan *2025-03-09 16:07:00*

**fix(docs): 对级联的组件的封装**


[787aa22058d29a4](https://gitee.com/rainsoil/quickboot/commit/787aa22058d29a4) luyanan *2025-03-04 14:17:10*

**feat(docs): dictHook**

 * 1. 抽离字典Hook
 * 2. 修改select组件使用dictHook

[5a4c6cb26d1dce8](https://gitee.com/rainsoil/quickboot/commit/5a4c6cb26d1dce8) luyanan *2025-03-01 16:54:19*

**feat(ui): 增加title组件**


[140fba5aa55baf3](https://gitee.com/rainsoil/quickboot/commit/140fba5aa55baf3) luyanan *2025-02-28 10:26:06*

**feat(docs): 增加title和card组件的文档**


[18c851d206fa260](https://gitee.com/rainsoil/quickboot/commit/18c851d206fa260) luyanan *2025-02-25 15:41:02*

**fix(docs): 增加对ui组件封装的需求**


[d4f9c85ee1c393e](https://gitee.com/rainsoil/quickboot/commit/d4f9c85ee1c393e) luyanan *2025-02-23 12:52:52*

**feat(doc): 破解全局注入的迷咒**


[572f864546e408e](https://gitee.com/rainsoil/quickboot/commit/572f864546e408e) luyanan *2025-02-23 02:28:38*

**feat(doc): 破解全局注入的迷咒**


[b4064c978779f86](https://gitee.com/rainsoil/quickboot/commit/b4064c978779f86) luyanan *2025-02-22 03:19:24*

**组件移动到packages**


[ada1e045ab49b88](https://gitee.com/rainsoil/quickboot/commit/ada1e045ab49b88) luyanan *2025-02-21 07:59:22*

**解决文件路径注入的问题**


[56f54638e421b25](https://gitee.com/rainsoil/quickboot/commit/56f54638e421b25) luyanan *2025-02-21 06:26:02*

**文件注入**


[4596975a82ecf8b](https://gitee.com/rainsoil/quickboot/commit/4596975a82ecf8b) luyanan *2025-02-20 10:34:36*

**feat(doc): 文档处理**


[32a3407a8f02741](https://gitee.com/rainsoil/quickboot/commit/32a3407a8f02741) luyanan *2025-02-19 15:37:41*

**feat(字典值处理):**


[ac21b682c5c979c](https://gitee.com/rainsoil/quickboot/commit/ac21b682c5c979c) luyanan *2025-02-19 14:52:31*

**feat(vitepress):**


[af9f5c8aefefb9f](https://gitee.com/rainsoil/quickboot/commit/af9f5c8aefefb9f) luyanan *2025-02-17 15:45:27*

**feat(docs): 增加docs文档**

 * 增加docs文档

[567792a36f3202e](https://gitee.com/rainsoil/quickboot/commit/567792a36f3202e) luyanan *2025-02-13 15:35:29*

**feat(字典值处理):**


[0b2969fc49c75fa](https://gitee.com/rainsoil/quickboot/commit/0b2969fc49c75fa) luyanan *2025-02-12 14:22:55*

**fix(ui): 字典值加载的bug**


[8ecac1903739dad](https://gitee.com/rainsoil/quickboot/commit/8ecac1903739dad) luyanan *2025-02-11 15:43:44*

**fix(ui): 组件代码**

 * 修改字典值多次加载的问题

[2d9061f553bd072](https://gitee.com/rainsoil/quickboot/commit/2d9061f553bd072) luyanan *2025-02-10 15:31:27*

**组件移动到packages**


[911eadcb10f64ee](https://gitee.com/rainsoil/quickboot/commit/911eadcb10f64ee) luyanan *2025-02-08 10:08:27*

**fix(ui): 修改组件导入部分的代码**


[67c38f13948ce10](https://gitee.com/rainsoil/quickboot/commit/67c38f13948ce10) luyanan *2025-02-06 09:31:19*

**feat(ui): 增加组件**


[d55241207db03db](https://gitee.com/rainsoil/quickboot/commit/d55241207db03db) luyanan *2025-02-04 15:22:55*

**feat(ui): 增加组件**


[0045c575699e7e7](https://gitee.com/rainsoil/quickboot/commit/0045c575699e7e7) luyanan *2025-01-24 09:29:30*

**文档**


[6ab43e7086d827f](https://gitee.com/rainsoil/quickboot/commit/6ab43e7086d827f) luyanan *2025-01-23 14:21:21*

**feat(init): 基础功能开发**


[2ce901cec7ce711](https://gitee.com/rainsoil/quickboot/commit/2ce901cec7ce711) luyanan *2025-01-09 14:26:28*

**feat(init): 基础功能开发**


[435473e96153b07](https://gitee.com/rainsoil/quickboot/commit/435473e96153b07) luyanan *2025-01-09 13:42:37*


