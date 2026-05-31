-- 非顶级目录菜单 component 须为 ParentView，避免嵌套 Layout 导致双左侧菜单与主区域留白。
-- 顶级目录（parent_id = -1 或 0）保持 Layout。

UPDATE sys_menu
SET component = 'ParentView'
WHERE menu_type = 'M'
  AND parent_id IS NOT NULL
  AND parent_id NOT IN (-1, 0)
  AND (component IS NULL OR component = '' OR component = 'Layout');
