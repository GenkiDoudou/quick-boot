package io.github.genkidoudou.web.knowledge.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PGVector 向量库手动配置。
 * <p>
 * 使用 {@code qc.knowledge.vector-datasource} 独立 Hikari 连接池，且<strong>不</strong>向 Spring 容器注册
 * {@link javax.sql.DataSource} Bean，避免触发 {@code ConditionalOnMissingBean(DataSource.class)} 导致
 * MySQL 主库 / Flyway 误连 PostgreSQL。
 */
@Configuration
@ConditionalOnProperty(prefix = "qc.knowledge", name = "enabled", havingValue = "true")
public class KnowledgeVectorStoreConfiguration implements DisposableBean {

    /** 向量库专用连接池（非 Spring DataSource Bean，仅供本配置类使用）。 */
    private HikariDataSource vectorPool;

    /**
     * 手动构建 PGVector VectorStore，维度与 {@link KnowledgeProperties#getVectorDimensions()} 及 Embedding 模型对齐。
     *
     * @param props          知识库配置
     * @param embeddingModel Spring AI Embedding 模型
     * @return VectorStore Bean
     */
    @Bean
    public VectorStore knowledgeVectorStore(KnowledgeProperties props, EmbeddingModel embeddingModel) {
        vectorPool = createVectorPool(props);
        return PgVectorStore.builder(new org.springframework.jdbc.core.JdbcTemplate(vectorPool), embeddingModel)
            .dimensions(props.getVectorDimensions())
            .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
            .indexType(PgVectorStore.PgIndexType.HNSW)
            .initializeSchema(true)
            .build();
    }

    private static HikariDataSource createVectorPool(KnowledgeProperties props) {
        KnowledgeProperties.VectorDataSource cfg = props.getVectorDatasource();
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(cfg.getDriverClassName());
        ds.setJdbcUrl(cfg.getUrl());
        ds.setUsername(cfg.getUsername());
        ds.setPassword(cfg.getPassword());
        ds.setPoolName("knowledge-vector-pool");
        ds.setMaximumPoolSize(5);
        ds.setMinimumIdle(1);
        return ds;
    }

    @Override
    public void destroy() {
        if (vectorPool != null) {
            vectorPool.close();
        }
    }
}
