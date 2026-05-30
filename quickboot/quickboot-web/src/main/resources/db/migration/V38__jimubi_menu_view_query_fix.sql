-- 积木 BI 大屏预览：DragIndexController 映射为 /drag/view?pageId=，非 /drag/page/view（后者为 REST 前缀，无页面处理器）
UPDATE sys_menu
SET query = CONCAT('/drag/view?pageId=', SUBSTRING(query, LENGTH('/drag/page/view?id=') + 1))
WHERE is_frame = '1'
  AND query LIKE '/drag/page/view?id=%';

UPDATE sys_menu
SET query = CONCAT('/drag/view?pageId=', SUBSTRING(query, LENGTH('/drag/page/view/') + 1))
WHERE is_frame = '1'
  AND query LIKE '/drag/page/view/%'
  AND query NOT LIKE '/drag/page/view?id=%';
