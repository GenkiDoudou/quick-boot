package io.github.genkidoudou.web.knowledge.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文档分块记录，对应表 {@code kb_document_chunk}。
 * <p>
 * 仅存摘要与向量映射，完整 embedding 在 PGVector 中。
 */
@Data
@TableName("kb_document_chunk")
public class KbDocumentChunk implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "chunk_id", type = IdType.ASSIGN_ID)
    private Long chunkId;

    /** 所属文档 ID。 */
    private Long docId;

    /** 文档内分块序号，从 0 起。 */
    private Integer chunkIndex;

    /** 片段摘要，用于列表与引用展示（建议 ≤500 字符）。 */
    private String contentPreview;

    /** 完整分块正文，供检索、编辑与重嵌入。 */
    private String contentFull;

    /** PGVector 中 Spring AI Document 的 id。 */
    private String vectorId;

    /** 分块 token 数，可选统计字段。 */
    private Integer tokenCount;

    /** PDF 页码等，与向量 metadata 同步。 */
    private Integer pageNumber;

    /** 是否参与检索：0 禁用，1 启用。 */
    private Integer enabled;
}
