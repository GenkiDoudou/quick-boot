-- 内置管理员补齐部门：数据权限要求用户须挂靠部门（与 openspec user-data-permission 一致）

UPDATE sys_user SET dept_id = 100 WHERE user_id = 1 AND (dept_id IS NULL OR dept_id = 0);
