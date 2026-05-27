package io.github.genkidoudou.web.tool.gen.support;

import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link GenDbIntrospector} 集成测试。
 */
@SpringBootTest
@ActiveProfiles("dev")
class GenDbIntrospectorTest {

    @Autowired
    private GenDbIntrospector introspector;

    @Test
    void shouldLoadSysNoticeTableWithPk() {
        TableInfo info = introspector.getTableInfo("sys_notice");
        assertNotNull(info, "sys_notice 应存在");
        assertFalse(info.getFields().isEmpty());
        assertTrue(
            info.getFields().stream().anyMatch(f -> f.isKeyFlag()),
            "应包含主键列"
        );
    }
}
