# MySQL 数据库设计规范（MySQL Database Design Specification）

## 命名规范

- 表名：使用小写英文复数名词，单词间用下划线分隔（snake_case），如 users、order_items。
- 字段名：使用小写英文单数名词或动词短语，snake_case，如 user_id、created_at。 
- 主键：统一命名为 id（除非是联合主键或特殊业务主键）。
- 外键：格式为 {关联表名}_id，如 user_id 引用 users.id。
- 索引：命名格式为 idx_{表名}_{字段名}（普通索引）或 uniq_{表名}_{字段名}（唯一索引）。
- 约束：命名格式为 fk_{表名}_{字段名}（外键）、ck_{表名}_{字段名}（检查约束）等。

## 数据类型选择

- 整数：优先使用 INT（4字节），大范围用 BIGINT；小范围用 SMALLINT/TINYINT。
- 字符串：
   - 固定长度且较短（≤20字符）：CHAR(n)
   - 可变长度：VARCHAR(n)，n 应合理预估（避免过长）
   - 超长文本：TEXT/LONGTEXT
- 时间：
   - 创建/更新时间：DATETIME
   - 仅日期：DATE
- 布尔值：使用 BOOLEAN 或 TINYINT(1)（0/1 表示 false/true）
- 金额/高精度数值：使用 DECIMAL(p, s)，如 DECIMAL(10, 2)

## 主键与外键
   
- 每张表必须有主键，推荐使用自增整数（AUTO_INCREMENT / SERIAL）。
- 禁止外键显式声明。
- 禁止使用复合主键。

## 索引策略
   
- 主键自动创建聚簇索引。
- 高频查询字段（WHERE、JOIN、ORDER BY）应建立索引。
- 唯一性业务字段（如手机号、邮箱）应加唯一索引。
- 避免过度索引（影响写性能）。
- 多列索引注意最左前缀原则。

## 字段约束
   
- 必填字段设为 NOT NULL。
- 默认值应明确（如 DEFAULT CURRENT_TIMESTAMP）。
- 使用 CHECK 约束限制业务规则（如 status IN ('active', 'inactive')）。
- 枚举类字段建议用 VARCHAR + CHECK 或单独字典表，避免数据库原生 ENUM 类型（可移植性差）。

## 其他最佳实践

- 所有表必须包含 created_at 和 updated_at 时间戳字段。
- 避免软删除（is_deleted）除非业务强需求；若使用，需配合全局查询作用域。
- 表注释和字段注释必须完整（使用 COMMENT）。
- 单表字段数建议 ≤ 50，超过应考虑垂直拆分。