package io.github.genkidoudou.web.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识库模块配置（{@code qc.knowledge.*}）。
 */
@Data
@ConfigurationProperties(prefix = "qc.knowledge")
public class KnowledgeProperties {

    /** 是否启用知识库模块；为 false 时不注册相关 Bean。 */
    private boolean enabled = false;

    /**
     * 为 true 时启动期检测 Ollama 等 AI 运行时；P0 默认 false，由接口层按需降级。
     */
    private boolean ollamaRequired = false;

    /** PGVector 向量维度，须与 {@code spring.ai.openai.embedding.options.dimensions} 一致。 */
    private int vectorDimensions = 768;

    /** PGVector 独立数据源，勿与 MyBatis 主库混用。 */
    private VectorDataSource vectorDatasource = new VectorDataSource();

    /** 异步入库相关配置。 */
    private Ingest ingest = new Ingest();

    /** RAG 检索与问答相关配置。 */
    private Rag rag = new Rag();

    /** 网页 URL 抓取配置。 */
    private WebFetch webFetch = new WebFetch();

    /** 独立文档库配置。 */
    private Library library = new Library();

    /** 外部 MCP 管理配置。 */
    private KnowledgeMcpProperties mcp = new KnowledgeMcpProperties();

    /**
     * 向量库 JDBC 连接配置。
     */
    @Data
    public static class VectorDataSource {

        private String url;

        private String username;

        private String password;

        private String driverClassName = "org.postgresql.Driver";
    }

    /**
     * 文档入库流水线配置。
     */
    @Data
    public static class Ingest {

        /** 全局同时执行的异步入库任务数上限。 */
        private int asyncMaxConcurrent = 2;

        /** 新建知识库时的默认分块 token 上限。 */
        private int defaultChunkSize = 800;

        /** 新建知识库时的默认分块重叠 token 数。 */
        private int defaultChunkOverlap = 120;
    }

    /**
     * 语义检索与 RAG 问答默认参数。
     */
    @Data
    public static class Rag {

        /** 相似度检索返回的最大片段数。 */
        private int topK = 8;

        /** 相似度阈值，低于该值的片段将被过滤（百炼 embedding 实测建议约 0.5）。 */
        private double similarityThreshold = 0.5;

        /** 默认检索模式：{@code VECTOR} 或 {@code HYBRID}。 */
        private String defaultSearchMode = "HYBRID";

        /** 混合检索时向量分权重（与 keywordWeight 之和不必为 1，按相对比例融合）。 */
        private double hybridVectorWeight = 0.7;

        /** 混合检索时关键词分权重。 */
        private double hybridKeywordWeight = 0.3;

        /** RRF 融合常数 k，越大则排名差异越平滑。 */
        private int hybridRrfK = 60;
    }

    /**
     * 网页正文抓取与安全限制。
     */
    @Data
    public static class WebFetch {

        /** 是否允许网页来源抓取。 */
        private boolean enabled = true;

        /** HTTP 连接与读取超时（毫秒）。 */
        private int timeoutMs = 15_000;

        /** 响应体最大字节数。 */
        private int maxBytes = 5_242_880;

        /** 请求 User-Agent。 */
        private String userAgent = "QuickBoot-KnowledgeBot/1.0";

        /** 最大跟随重定向次数。 */
        private int maxRedirects = 3;

        /**
         * 允许访问的主机白名单；为空时仅启用 SSRF 黑名单校验。
         */
        private List<String> allowedHosts = new ArrayList<>();
    }

    /**
     * 独立文档库上传限制。
     */
    @Data
    public static class Library {

        /** 单文件最大体积（MB）。 */
        private int maxFileSizeMb = 50;

        /** 允许扩展名（小写，不含点）。 */
        private List<String> allowedExtensions = List.of("pdf", "doc", "docx", "txt", "md", "html");
    }
}
