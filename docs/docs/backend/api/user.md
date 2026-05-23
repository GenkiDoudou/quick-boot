# 用户接口

基础路径：`/system/user`（均需登录 + Client 签名，除非全局关闭）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 分页列表 |
| GET | `/{userId}` | 详情 |
| POST | `/create` | 新增，Body: `SysUserBo` |
| POST | `/update` | 修改 |
| POST | `/remove` | 删除，Body: id 数组 |
| POST | `/changeStatus` | 修改状态 |
| POST | `/resetPwd` | 重置密码 |
| GET | `/authRole/{userId}` | 查询角色 |
| POST | `/authRole` | 分配角色 |
| POST | `/importData` | `multipart/form-data` 导入 |
| GET | `/importTemplate` | 模板下载 |
| GET | `/importError` | 失败明细 |
| POST | `/export` | 导出 Excel |

认证相关（`AuthController`，无 `/system` 前缀）：

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/login/captcha-config` | 验证码配置 |
| POST | `/login` | 登录 |
| GET | `/getInfo` | 当前用户 |
| GET | `/getRouters` | 动态菜单 |
| POST | `/logout` | 登出 |

详见 [用户管理](../modules/user-management)。
