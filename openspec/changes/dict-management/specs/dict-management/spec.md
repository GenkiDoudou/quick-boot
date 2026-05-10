## ADDED Requirements

### Requirement: 字典类型主从管理边界
系统 MUST 提供字典类型管理能力，并以字典类型作为字典项管理的主键入口。字典项 MUST 强绑定 `dictType`，不得脱离字典类型独立存在。

#### Scenario: 从字典类型进入字典项列表
- **WHEN** 用户在字典类型列表点击“字典项列表”操作
- **THEN** 前端跳转至字典项页面并携带对应 `dictType`，后续查询仅针对该类型

#### Scenario: 拒绝无类型字典项写入
- **WHEN** 提交新增或修改字典项请求且缺少 `dictType`
- **THEN** 系统返回业务失败并给出可读错误信息

### Requirement: 字典类型接口能力
系统 MUST 提供字典类型的新增、修改、删除、查看、导出、刷新缓存能力。写接口 SHALL 统一使用 `POST` 语义，不使用 `PUT/DELETE`。

#### Scenario: 创建字典类型成功
- **WHEN** 调用 `POST /system/dict/type` 且参数合法且 `dictType` 未冲突
- **THEN** 系统创建字典类型并返回成功响应

#### Scenario: 修改与删除采用 POST 动作路径
- **WHEN** 调用修改与删除字典类型接口
- **THEN** 路径分别使用 `POST /system/dict/type/update` 与 `POST /system/dict/type/remove/{dictId}`

### Requirement: 字典项接口能力
系统 MUST 提供字典项的新增、修改、删除、查看与导出能力，并按 `dictType` 查询列表。写接口 SHALL 统一使用 `POST` 语义。

#### Scenario: 按字典类型查询字典项
- **WHEN** 调用字典项列表接口并传入 `dictType`
- **THEN** 返回的数据仅包含该 `dictType` 下的字典项

#### Scenario: 导出当前类型字典项
- **WHEN** 在字典项页面触发导出
- **THEN** 导出结果仅包含当前 `dictType` 的字典项数据

### Requirement: 删除字典类型前置校验
系统 MUST 在删除字典类型前校验是否存在未删除字典项。若存在字典项，系统 MUST 拒绝删除并返回明确业务失败信息。

#### Scenario: 存在字典项时删除失败
- **WHEN** 删除目标字典类型且该类型下仍有未删除字典项
- **THEN** 系统拒绝删除并返回可读错误 `msg`

#### Scenario: 无字典项时删除成功
- **WHEN** 删除目标字典类型且该类型下无未删除字典项
- **THEN** 系统执行删除并返回成功响应

### Requirement: 缓存刷新双模式
系统 MUST 同时提供“刷新单个字典类型缓存”与“刷新全部字典缓存”两种能力。

#### Scenario: 刷新单个类型缓存
- **WHEN** 调用单类型刷新接口并传入 `dictType`
- **THEN** 系统仅刷新该类型缓存并返回成功

#### Scenario: 刷新全量缓存
- **WHEN** 调用全量刷新接口
- **THEN** 系统刷新所有字典缓存并返回成功

### Requirement: 前端主从页面与组件约束
系统 MUST 提供字典类型页与字典项页两级路由，并优先使用 `@/packages` 组件完成核心交互。

#### Scenario: 字典类型页功能完整
- **WHEN** 用户访问字典类型页
- **THEN** 页面提供新增、修改、删除、查看、导出、刷新缓存、进入字典项列表操作

#### Scenario: 组件体系一致
- **WHEN** 渲染主从页面的核心交互组件
- **THEN** 优先使用 `C7Button`、`C7Dialog`、`C7JsonTable`、`C7DictTag` 等 `@/packages` 组件
