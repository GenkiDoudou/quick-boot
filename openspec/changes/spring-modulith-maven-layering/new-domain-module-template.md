# 新业务域模块模板

权威设计：[`docs/superpowers/specs/2026-08-08-spring-modulith-maven-layering-design.md`](../../../../docs/superpowers/specs/2026-08-08-spring-modulith-maven-layering-design.md)

## Maven

1. 新建目录 `quickboot/quickboot-module-<domain>/`
2. `artifactId`：`quickboot-module-<domain>`
3. 依赖：`quickboot-core`（传递 `common`）；按需依赖他域时**仅**依赖对方公开类型（同反应器内可依赖对方模块，但代码只引用 `*.api`）
4. 在父 POM `<modules>` 中注册；`quickboot-app` 增加对该模块的依赖

## 包结构

```text
io.github.genkidoudou.<domain>/
  package-info.java          @ApplicationModule
  api/                       @NamedInterface("api") + Facade/DTO
  internal/                  controller / service / mapper / entity / config / api 实现
```

## Modulith

- 在 `quickboot-app` 测试侧 `ApplicationModuleSourceFactory#getModuleBasePackages` 追加 `io.github.genkidoudou.<domain>`（或改为 `explicitly-annotated` 统一探测）。
- 跨域只注入他域 `api` 接口；禁止引用 `internal`。

## 清单

- [ ] POM + 父反应器
- [ ] `api` / `internal` + `package-info`
- [ ] app 依赖与组件扫描覆盖新包
- [ ] `ModularityTests` / `verify()` 通过
