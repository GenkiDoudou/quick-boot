# quickboot-web 拆分 system/tools Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `quickboot/quickboot-web` 拆成 `quickboot-system` 与 `quickboot-tools` 两个业务子模块，`quickboot-web` 仅保留启动入口与模块聚合依赖。

**Architecture:** 新增两个同级 Maven 模块承载原 `quickboot-web` 下的业务包；保持包名不变（仍为 `io.github.genkidoudou.web...`），通过 `quickboot-web` 依赖聚合保证 Spring Boot 默认扫描仍生效。

**Tech Stack:** Java 17、Spring Boot、Maven 多模块工程

---

## 文件结构（锁定）

**Create**
- `quickboot/quickboot-system/pom.xml`
- `quickboot/quickboot-tools/pom.xml`

**Modify**
- `quickboot/pom.xml`：新增 `<module>quickboot-system</module>`、`<module>quickboot-tools</module>`
- `quickboot/quickboot-web/pom.xml`：新增依赖 `quickboot-system`、`quickboot-tools`；（视迁移结果）下沉/移除不再需要的依赖

**Move**
- 从 `quickboot/quickboot-web/src/main/java` 迁移部分包到：
  - `quickboot/quickboot-system/src/main/java`
  - `quickboot/quickboot-tools/src/main/java`
- `quickboot/quickboot-web/src/main/java/io/github/genkidoudou/WebApplication.java` 保留不动

**Keep**
- `quickboot/quickboot-web/src/main/resources/**` 暂时不拆（作为启动模块资源汇聚点）

---

### Task 1: 新增 `quickboot-system` Maven 模块

**Files:**
- Create: `quickboot/quickboot-system/pom.xml`

- [ ] **Step 1: 创建模块目录结构**
  - 创建 `quickboot/quickboot-system/`
  - 创建 `quickboot/quickboot-system/src/main/java/`

- [ ] **Step 2: 写入 `quickboot/quickboot-system/pom.xml`（packaging=jar）**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>io.github.genkidoudou</groupId>
        <artifactId>quickboot</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <groupId>io.github.genkidoudou.web</groupId>
    <artifactId>quickboot-system</artifactId>
    <description>Web System 子模块</description>

    <dependencies>
        <dependency>
            <groupId>io.github.genkidoudou.common</groupId>
            <artifactId>quickboot-common</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>io.github.genkidoudou.core</groupId>
            <artifactId>quickboot-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>io.github.genkidoudou.report</groupId>
            <artifactId>quickboot-report</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- system/monitor 中使用到的 Web/Validation 等由启动模块统一引入；
             若迁移后编译缺依赖，再按编译报错最小补齐。 -->
    </dependencies>
</project>
```

- [ ] **Step 3: 先不做额外插件配置**

---

### Task 2: 新增 `quickboot-tools` Maven 模块

**Files:**
- Create: `quickboot/quickboot-tools/pom.xml`

- [ ] **Step 1: 创建模块目录结构**
  - 创建 `quickboot/quickboot-tools/`
  - 创建 `quickboot/quickboot-tools/src/main/java/`

- [ ] **Step 2: 写入 `quickboot/quickboot-tools/pom.xml`（packaging=jar）**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>io.github.genkidoudou</groupId>
        <artifactId>quickboot</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <groupId>io.github.genkidoudou.web</groupId>
    <artifactId>quickboot-tools</artifactId>
    <description>Web Tools 子模块</description>

    <dependencies>
        <dependency>
            <groupId>io.github.genkidoudou.common</groupId>
            <artifactId>quickboot-common</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>io.github.genkidoudou.core</groupId>
            <artifactId>quickboot-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>io.github.genkidoudou.report</groupId>
            <artifactId>quickboot-report</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</project>
```

---

### Task 3: 父工程聚合新增模块

**Files:**
- Modify: `quickboot/pom.xml`

- [ ] **Step 1: 在 `<modules>` 中加入新模块**
  - 添加 `quickboot-system`
  - 添加 `quickboot-tools`

- [ ] **Step 2: Maven 验证聚合（只验证模型，不跑测试）**

Run:
- `mvn -pl quickboot -DskipTests -q validate`

Expected:
- 退出码 0

---

### Task 4: 调整 `quickboot-web` 依赖为聚合 system/tools

**Files:**
- Modify: `quickboot/quickboot-web/pom.xml`

- [ ] **Step 1: 添加依赖**
  - `io.github.genkidoudou.web:quickboot-system:${project.version}`
  - `io.github.genkidoudou.web:quickboot-tools:${project.version}`

- [ ] **Step 2: 先保留原依赖，等迁移后再按编译结果最小收敛**

---

### Task 5: 迁移 Java 包到新模块（保持包名不变）

**Files:**
- Move: `quickboot/quickboot-web/src/main/java/io/github/genkidoudou/web/system/**` → `quickboot/quickboot-system/src/main/java/io/github/genkidoudou/web/system/**`
- Move: `quickboot/quickboot-web/src/main/java/io/github/genkidoudou/web/monitor/online/**` → `quickboot/quickboot-system/src/main/java/io/github/genkidoudou/web/monitor/online/**`
- Move: `quickboot/quickboot-web/src/main/java/io/github/genkidoudou/web/monitor/operlog/**` → `quickboot/quickboot-system/src/main/java/io/github/genkidoudou/web/monitor/operlog/**`
- Move: `quickboot/quickboot-web/src/main/java/io/github/genkidoudou/web/monitor/logininfor/**` → `quickboot/quickboot-system/src/main/java/io/github/genkidoudou/web/monitor/logininfor/**`
- Move: 其余 `quickboot-web/src/main/java/io/github/genkidoudou/web/**`（除上述 system 范围外）→ `quickboot/quickboot-tools/src/main/java/io/github/genkidoudou/web/**`
- Keep: `quickboot/quickboot-web/src/main/java/io/github/genkidoudou/WebApplication.java`

- [ ] **Step 1: 迁移 system 范围包到 `quickboot-system`**
- [ ] **Step 2: 迁移其余 web 包到 `quickboot-tools`**
- [ ] **Step 3: 确认 `quickboot-web` 下 `io/github/genkidoudou/web/**` 基本为空（只剩启动相关）**

---

### Task 6: 编译验证并按报错最小补齐依赖

**Files:**
- Modify: `quickboot/quickboot-system/pom.xml`（如缺依赖）
- Modify: `quickboot/quickboot-tools/pom.xml`（如缺依赖）
- Modify: `quickboot/quickboot-web/pom.xml`（如需要将原依赖下沉/移除）

- [ ] **Step 1: 编译聚合（跳过测试）**

Run:
- `mvn -pl quickboot-web -am -DskipTests clean package`

Expected:
- 退出码 0

- [ ] **Step 2: 若编译失败，按错误将缺失的第三方依赖补到实际使用的模块**
  - 原则：谁用谁依赖；只补编译缺失项，不做额外重构

---

### Task 7: 最终全量构建验证

- [ ] **Step 1: 全量构建（跳过测试）**

Run:
- `cd quickboot && mvn -DskipTests clean package`

Expected:
- 退出码 0

