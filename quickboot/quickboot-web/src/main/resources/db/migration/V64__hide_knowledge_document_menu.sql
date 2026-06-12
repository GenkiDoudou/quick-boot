-- 隐藏独立「文档管理」菜单：文档/命中测试/对话测试/设置已并入知识库详情（从知识库列表进入）

UPDATE sys_menu SET visible = '1' WHERE menu_id = 2286;
