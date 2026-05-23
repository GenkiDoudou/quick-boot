# 安全功能配置文件说明

## 文件位置

请将以下文件放置在 `classpath:sensitive/` 目录下：

- `white-list.txt` - 敏感词白名单（每行一个词）
- `black-list.txt` - 敏感词黑名单（每行一个词）
- `sql-keywords.txt` - SQL 注入关键词列表（每行一个关键词）

## 文件格式

每个文件都是纯文本格式，每行一个词汇，支持 UTF-8 编码。

### 示例：white-list.txt
```
测试词1
测试词2
```

### 示例：black-list.txt
```
敏感词1
敏感词2
```

### 示例：sql-keywords.txt
```
select
insert
update
delete
drop
create
alter
exec
execute
union
script
javascript
vbscript
onload
onerror
onclick
'
"
;
--
/*
*/
xp_
sp_
declare
cast
convert
```

## 配置说明

在 `application.yml` 中配置：

```yaml
security:
  # 敏感词过滤配置
  sensitive-word:
    enabled: true                    # 是否启用
    strategy: REPLACE                # 策略：REPLACE（替换）或 THROW（抛异常）
    ignore-urls:                     # 忽略的 URL 列表（支持 Ant 路径匹配）
      - /api/public/**
      - /actuator/**
    white-list-path: classpath:sensitive/white-list.txt
    black-list-path: classpath:sensitive/black-list.txt
    log-enabled: true                # 是否记录命中日志
  
  # XSS 防护配置
  xss:
    enabled: true                    # 是否启用
    ignore-urls:                     # 忽略的 URL 列表
      - /api/public/**
      - /actuator/**
  
  # SQL 注入防护配置
  sql-inject:
    enabled: true                    # 是否启用
    ignore-urls:                     # 忽略的 URL 列表
      - /api/public/**
      - /actuator/**
    keywords-path: classpath:sensitive/sql-keywords.txt
```

## 注意事项

1. 如果功能未启用（`enabled: false`），相关 Bean 不会被创建，不会加载依赖库
2. 文件路径支持 `classpath:` 前缀（从 classpath 读取）或绝对路径（从文件系统读取）
3. 文件不存在或为空时，会记录警告日志，但不会影响启动
4. 敏感词检测使用 DFA 算法，性能已优化
