package io.github.genkidoudou.web.aiapp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 应用会话实体，对应表 {@code ai_app_session}。
 */
@Data
@TableName("ai_app_session")
public class AiAppSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联应用 {@code ai_app.id}。 */
    private Long appId;

    /** 登录 userId 或 embed 访客标识。 */
    private String userKey;

    /** 会话标题（首条消息摘要）。 */
    private String title;

    /** 智能体变量记忆快照 JSON。 */
    private String variablesJson;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
