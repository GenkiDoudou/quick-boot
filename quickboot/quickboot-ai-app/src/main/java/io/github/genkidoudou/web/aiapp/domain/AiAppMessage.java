package io.github.genkidoudou.web.aiapp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 应用会话消息实体，对应表 {@code ai_app_message}。
 */
@Data
@TableName("ai_app_message")
public class AiAppMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属会话 {@code ai_app_session.id}。 */
    private Long sessionId;

    /**
     * 消息角色：user / assistant / tool。
     *
     * @see io.github.genkidoudou.web.aiapp.constants.AiAppMessageRole
     */
    private String role;

    /** 消息正文。 */
    private String content;

    /** 工具调用、工作流 runId、引用等 JSON。 */
    private String metadataJson;

    private LocalDateTime createTime;
}
