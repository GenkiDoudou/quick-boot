# 系统接口

## 部门 `/system/dept`

`GET /list`、`/treeselect`、`/{deptId}` · `POST /update`、`/remove/{deptId}`

## 字典类型 `/system/dict/type`

`GET /list`、`/{dictId}` · `POST /update`、`/remove/{dictId}`、`/export`、`/refresh`、`/refresh/{dictType}`、`/import`、`/import/template`

## 字典数据 `/system/dict/data`

`GET /list`、`/type/{dictType}`、`/{dictCode}` · `POST /update`、`/remove/{dictCode}`、`/export`、`/import`、`/import/template`

## 参数 `/system/config`

`GET /list`、`/{configId}`、`/configKey/{configKey}` · `POST /create`、`/update`、`/remove`、`/refreshCache`、`/export`

## 公告 `/system/notice`

`GET /list`、`/{noticeId}` · `POST /create`、`/update`、`/remove`

## OAuth 客户端 `/system/oauthClient`

`GET /list`、`/{clientId}` · `POST /create`、`/update`、`/remove`、`/{clientId}/revealSecret`

## OAuth 提供方 `/system/oauthProvider`

`GET /list`、`/{providerCode}` · `POST /create`、`/update`、`/remove`

详见 [系统配置模块](../modules/system-management)、[OAuth2](../modules/oauth2)。
