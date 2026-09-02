package io.github.genkidoudou.web.support;

import io.github.genkidoudou.web.WebApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * quickboot 集成测试基类：加载完整 Spring 上下文（dev 嵌入式 MariaDB/Redis）。
 * <p>
 * 子类按需叠加 {@code @Transactional}、{@code @AutoConfigureMockMvc} 等。
 */
@SpringBootTest(classes = WebApplication.class)
@ActiveProfiles("dev")
@ContextConfiguration(initializers = QuickbootDevInfrastructureInitializer.class)
public abstract class QuickbootIntegrationTestBase {
}
