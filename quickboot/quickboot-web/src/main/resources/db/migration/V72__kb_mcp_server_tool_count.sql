-- MCP 连接测试成功后持久化工具数量，供卡片列表展示

ALTER TABLE kb_mcp_server
    ADD COLUMN tool_count INT NULL COMMENT '最近成功测试发现的工具数量' AFTER last_test_time;
