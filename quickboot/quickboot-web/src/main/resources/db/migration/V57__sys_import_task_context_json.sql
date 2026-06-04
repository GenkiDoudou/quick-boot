-- 导入任务扩展上下文（如字典数据导入需绑定 dictType）

ALTER TABLE sys_import_task
    ADD COLUMN context_json TEXT NULL COMMENT '业务上下文 JSON，如 {"dictType":"sys_user_sex"}' AFTER duplicate_strategy;
