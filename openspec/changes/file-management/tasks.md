## 1. Database and config

- [x] 1.1 Add Flyway migration (next version after current max) creating `sys_file_classify` and `sys_file` with CHAR(1) flags, unique `classify` / `relative_path`, indexes for list queries
- [x] 1.2 Seed `default` classify and insert system menus/permissions for fileClassify + file
- [x] 1.3 Add `qc.file` local-only settings in `application.yml` / `application-dev.yml` (enabled, type=local, path, viewUrlBase); do not define YAML classifies list

## 2. Common local file storage

- [x] 2.1 Port bak local `FileTemplate` stack into `quickboot-common` (local backend only; no MinIO runtime)
- [x] 2.2 Introduce classify rule resolver SPI/interface so upload validation reads DB-backed rules (no YAML classifies)
- [x] 2.3 Implement upload validation (classify exists/enabled, ext, size) and local store/delete/open-for-preview helpers
- [x] 2.4 Wire Spring auto-configuration / imports for file storage beans

## 3. File classify module

- [x] 3.1 Add `SysFileClassify` entity/mapper/service/controller under `quickboot-module-system` (`/system/fileClassify`)
- [x] 3.2 Implement CRUD: unique classify, immutable key on update, refuse remove when non-deleted `sys_file` references exist, invalidate classify cache
- [x] 3.3 Implement classify resolver bean used by common storage (list/get enabled rules)

## 4. Common file API

- [x] 4.1 Add `CommonFileController` at `/file`: list/get classifies, upload by classify (no `sys_file` insert), preview by relative path
- [x] 4.2 Align anonymous/security path exclusions for preview if required by existing security config patterns

## 5. File management module

- [x] 5.1 Add `SysFile` entity/mapper/service/controller at `/system/file` (list, upload/{classify}, view, download, remove)
- [x] 5.2 Management upload: store via `FileTemplate` then insert `sys_file`; on insert failure delete object and fail
- [x] 5.3 Remove: soft-delete + delete local object; missing object still success
- [x] 5.4 Confirm no global `FileUploadHook` auto-registration is registered

## 6. quick-ui components and APIs

- [x] 6.1 Calibrate `C7Upload` / `C7Preview` and `api/common/file.js` against `/file/**`
- [x] 6.2 Add/align `api/system/file.js` and new `api/system/fileClassify.js`

## 7. quick-ui admin pages

- [x] 7.1 Implement `views/system/fileClassify/index.vue` (C7JsonTable + dialog; edit disables classify key; compress tip)
- [x] 7.2 Implement `views/system/file/index.vue` (list/search/upload with C7Upload uploadFn override/preview/download/delete)
- [x] 7.3 Ensure route/menu component paths match seeded menus and `v-hasPermi` codes

## 8. Verification

- [ ] 8.1 Apply migration and verify `default` classify + menus exist
- [ ] 8.2 Verify common upload does not create `sys_file`; management upload does
- [ ] 8.3 Verify classify CRUD, refuse delete with references, upload reject on bad ext/size/disabled
- [x] 8.4 Unit-verify compress_enabled does not alter stored bytes (DefaultFileTemplateTest); preview/download/delete deferred to E2E
- [x] 8.4b Image compress when compress_enabled=1 (DefaultFileTemplateTest shrinks large jpeg; non-image unchanged)
- [ ] 8.5 Smoke quick-ui classify page and file page end-to-end
