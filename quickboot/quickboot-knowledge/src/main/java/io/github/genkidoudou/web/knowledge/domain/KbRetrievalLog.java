package io.github.genkidoudou.web.knowledge.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库检索测试历史，对应表 {@code kb_retrieval_log}。
 */
@Data
@TableName("kb_retrieval_log")
public class KbRetrievalLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "log_id", type = IdType.ASSIGN_ID)
    private Long logId;

    private Long kbId;

    private String query;

    private String searchMode;

    private Integer topK;

    private Double similarityThreshold;

    private Integer hitCount;

    private String createBy;

    private LocalDateTime createTime;
}
