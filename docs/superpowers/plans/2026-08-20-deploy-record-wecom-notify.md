# Deploy Record + WeCom Notify Implementation Plan

> **For agentic workers:** 按任务顺序落地；设计见 `docs/superpowers/specs/2026-08-20-deploy-record-wecom-notify-design.md`。

**Goal:** Jenkins 成功写发布记录 + 企微通知；管理端可查列表。

**Architecture:** `sys_deploy_record` + callback Token；Jenkins post 调首台 `:9993`；quick-ui 监控菜单列表。

**Tech Stack:** Spring Boot / Flyway / Sa-Token / Jenkins / Element Plus

---

### Task 1: DDL + 后端 CRUD/callback — done
### Task 2: 菜单 + quick-ui 列表 — done
### Task 3: Jenkinsfile + env example + README — done
