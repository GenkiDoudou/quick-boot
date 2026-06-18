package io.github.genkidoudou.config.embeddedmariadb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 在 DataSource 自动配置之前启动嵌入式 MariaDB4J（{@code qc.embedded-mariadb.enabled=true} 时生效）。
 */
public class MariaDB4jEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PREFIX = "qc.embedded-mariadb.";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.getProperty(PREFIX + "enabled", Boolean.class, Boolean.FALSE)) {
            return;
        }
        int port = environment.getProperty(PREFIX + "port", Integer.class, 3307);
        String dataDir = environment.getProperty(PREFIX + "data-dir", "./data/mariadb4j");
        String databaseName = environment.getProperty(PREFIX + "database-name", "qc2");
        EmbeddedMariaDbBootstrap.ensureStarted(
                port,
                EmbeddedMariaDbBootstrap.resolveDataDir(dataDir),
                databaseName);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
