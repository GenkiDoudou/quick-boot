# 日期时间

- **Jackson**：`yyyy-MM-dd HH:mm:ss`，时区 `GMT+8`（`application.yml`）
- **MVC**：`spring.mvc.format.date` / `date-time`
- **前端展示**：`parseTime`（`quick-ui/src/utils/ruoyi.js`）

业务层推荐使用 `LocalDateTime` 与统一格式化，避免多套格式并存。
