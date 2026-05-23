# Spec-Kit 新功能与需求变更

## 1. 新建变更

```text
/opsx-new
或 openspec/changes/<id>/ 手工创建
```

## 2. 产出设计

`/opsx-continue` 或 `/opsx-propose` 生成 design、specs、tasks。

## 3. 评审

团队评审 `proposal.md`、`design.md`；定稿后再编码。

## 4. 实现

`/opsx-apply` 按 `tasks.md` 逐项完成；同步更新：

- 后端/前端代码
- Flyway
- `docs/docs/**` 用户文档

## 5. 验证与归档

`/opsx-verify` → `/opsx-archive`

## 需求变更

在原 change 上追加 spec delta 或新建 `change-id-v2`，禁止静默偏离已批准 spec。
