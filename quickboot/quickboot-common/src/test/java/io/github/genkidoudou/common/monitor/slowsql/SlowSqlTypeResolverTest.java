package io.github.genkidoudou.common.monitor.slowsql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlowSqlTypeResolverTest {

    @Test
    void resolveSelectAndWithCte() {
        assertEquals(SlowSqlType.SELECT, SlowSqlTypeResolver.resolve("SELECT id FROM sys_user WHERE id = 1"));
        assertEquals(SlowSqlType.SELECT, SlowSqlTypeResolver.resolve("WITH t AS (SELECT 1) SELECT * FROM t"));
    }

    @Test
    void resolveDml() {
        assertEquals(SlowSqlType.INSERT, SlowSqlTypeResolver.resolve("INSERT INTO sys_user (id) VALUES (1)"));
        assertEquals(SlowSqlType.INSERT, SlowSqlTypeResolver.resolve("REPLACE INTO sys_user VALUES (1)"));
        assertEquals(SlowSqlType.UPDATE, SlowSqlTypeResolver.resolve("UPDATE sys_user SET name = 'a' WHERE id = 1"));
        assertEquals(SlowSqlType.DELETE, SlowSqlTypeResolver.resolve("DELETE FROM sys_user WHERE id = 1"));
    }

    @Test
    void resolveCommentsAndOther() {
        assertEquals(SlowSqlType.SELECT, SlowSqlTypeResolver.resolve("/* slow */ SELECT 1"));
        assertEquals(SlowSqlType.OTHER, SlowSqlTypeResolver.resolve(""));
        assertEquals(SlowSqlType.TRUNCATE, SlowSqlTypeResolver.resolve("TRUNCATE TABLE sys_user"));
    }
}
