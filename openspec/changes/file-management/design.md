## Context

- See `proposal.md` for motivation; detailed product decisions are in `docs/superpowers/specs/2026-08-12-file-management-design.md`.
- `bak` already has `FileTemplate`, YAML classifies, global register hook, and file admin page — current monorepo lacks the backend module; `quick-ui` already has partial `C7Upload`/`C7Preview`/API stubs.
- Current system module patterns: `quickboot-module-system` + `BaseEntity` (`del_flag` CHAR(1)) + Flyway under `quickboot-app`.

## Goals / Non-Goals

**Goals:**

- Port local-only storage into `quickboot-common`, replace YAML classifies with DB-backed rules.
- Explicit management-page registration only (no global upload hook).
- Ship classify admin + file admin pages and keep C7 components working.

**Non-Goals:**

- MinIO backend, real compression, chunked upload, Office preview conversion, recycle-bin restore.

## Decisions

1. **Migrate bak `FileTemplate` subset; local only**  
   - Port local backend + template APIs; leave MinIO types/config out or stubbed-disabled.  
   - Alternative: rewrite minimal upload-only service — rejected (loses proven path/preview helpers).

2. **Classify source of truth = `sys_file_classify`**  
   - `FileTemplate`/access layer resolves rules via a classify provider (system module) with short cache + invalidate on CRUD.  
   - Alternative: keep YAML + sync UI — rejected (dual source).

3. **No global `FileUploadHook` registration**  
   - Only `SysFileService.upload` inserts `sys_file` after successful store; on insert failure, delete object and fail the request.  
   - Alternative: bak global hook — rejected (conflicts with confirmed 5B).

4. **`sys_file` aligns `BaseEntity.del_flag`**  
   - Do not copy bak `deleted` Integer; use CHAR(1) soft delete + audit fields.  
   - Keep explicit `uploader_*` / `upload_time` for list display.

5. **API path split**  
   - `/file/**` for business upload/preview/classify read.  
   - `/system/fileClassify/**` and `/system/file/**` for admin.  
   - Keep `/system/file/view/{*relativePath}` to match existing `buildFileViewUrl` in quick-ui.

6. **Frontend**  
   - Calibrate existing packages; add `views/system/fileClassify` and `views/system/file` with `C7JsonTable`/`C7Dialog`.  
   - Size input: MB in form → `limit_size_bytes` on submit.

7. **Packages / Modulith**  
   - Storage abstractions in `common`; entities/services/controllers in `module-system`; migrations + `qc.file` in `app`.  
   - Classify provider API must avoid illegal module cycles (common depends only on an SPI/interface if needed, or system supplies rules through a bridge bean registered into common auto-config).

## Risks / Trade-offs

- [Compress field unused] → 已实现：`compress_enabled=1` 时服务端压缩 jpg/png/bmp；参数见 `qc.file.compress`。  
- [Business uploads invisible in admin list] → Accepted by product choice 5B; document clearly.  
- [common ↔ system classify lookup cycle] → Prefer SPI in common + implementation in system, or read via injected `FileClassifyResolver`.  
- [Orphan files if register fails without rollback] → Mandatory delete-on-register-failure.  
- [Deleting classify with history] → Hard refuse while `sys_file` references exist.  

## Migration Plan

1. Flyway: create `sys_file_classify` + `sys_file`, seed `default`, insert menus/perms.  
2. Deploy backend with `qc.file.type=local` and local path.  
3. Deploy quick-ui pages; smoke upload via management and via `C7Upload`.  
4. Rollback: disable menus; drop tables only if unused (destructive). Objects under local path may need manual cleanup.

## Open Questions

- Exact next Flyway version number: use current max + 1 at implementation time.  
- Whether `/file/preview` anonymous path list needs an `application.yml` entry in this change (align with existing security anonymous-paths pattern when wiring).
