# 代码模板（生成时按需裁剪）

## Entity（雪花主键）

```java
package io.github.genkidoudou.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.genkidoudou.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_demo")
public class SysDemo extends BaseEntity implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  @TableId(value = "demo_id", type = IdType.ASSIGN_ID)
  private Long demoId;

  private String demoName;
  private String status;
}
```

## Mapper

```java
package io.github.genkidoudou.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.system.entity.SysDemo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysDemoMapper extends BaseMapper<SysDemo> {
}
```

## Service

```java
package io.github.genkidoudou.system.service;

import io.github.genkidoudou.system.entity.SysDemo;

public interface ISysDemoService {
  SysDemo findById(Long demoId);
}
```

```java
package io.github.genkidoudou.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.genkidoudou.system.entity.SysDemo;
import io.github.genkidoudou.system.mapper.SysDemoMapper;
import io.github.genkidoudou.system.service.ISysDemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysDemoServiceImpl extends ServiceImpl<SysDemoMapper, SysDemo>
    implements ISysDemoService {

  @Override
  public SysDemo findById(Long demoId) {
    return demoId == null ? null : this.getById(demoId);
  }
}
```

## Controller（风格 B 动作路径）

```java
package io.github.genkidoudou.system.controller;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.system.service.ISysDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/system/demo")
@RequiredArgsConstructor
public class SysDemoController {

  private final ISysDemoService sysDemoService;

  @GetMapping("/list")
  public R<PageInfo<?>> list(/* QueryBo */) {
    return R.ok(/* page */);
  }

  @GetMapping("/{demoId}")
  public R<?> get(@PathVariable Long demoId) {
    return R.ok(sysDemoService.findById(demoId));
  }
}
```

## Controller（风格 A REST）

```java
@RequestMapping("/system/demos")
public class SysDemoController {
  @GetMapping
  public R<?> list() { return R.ok(); }

  @GetMapping("/{id}")
  public R<?> get(@PathVariable Long id) { return R.ok(); }

  @PostMapping
  public R<Void> create(@RequestBody @Validated Object body) { return R.ok(); }

  @PutMapping("/{id}")
  public R<Void> update(@PathVariable Long id, @RequestBody Object body) { return R.ok(); }

  @DeleteMapping("/{id}")
  public R<Void> remove(@PathVariable Long id) { return R.ok(); }
}
```

## DDL 片段（H2 MySQL 模式友好）

```sql
CREATE TABLE IF NOT EXISTS sys_demo (
  demo_id     BIGINT       NOT NULL,
  demo_name   VARCHAR(128) NOT NULL,
  status      CHAR(1)      NOT NULL DEFAULT '0',
  del_flag    CHAR(1)      NOT NULL DEFAULT '0',
  remark      VARCHAR(500),
  create_by   VARCHAR(64),
  create_time TIMESTAMP    NULL,
  update_by   VARCHAR(64),
  update_time TIMESTAMP    NULL,
  PRIMARY KEY (demo_id),
  CONSTRAINT uk_sys_demo_name UNIQUE (demo_name)
);
```
