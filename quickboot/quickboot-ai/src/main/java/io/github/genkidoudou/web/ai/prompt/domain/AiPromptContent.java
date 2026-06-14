package io.github.genkidoudou.web.ai.prompt.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 提示词内容段实体，对应 {@code ai_prompt_content}。
 * <p>
 * {@code version_id=0} 表示当前编辑草稿；非 0 关联历史版本快照。
 */
@Data
@TableName("ai_prompt_content")
public class AiPromptContent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "content_id", type = IdType.ASSIGN_ID)
    private Long contentId;

    /** 所属提示词 ID。 */
    private Long promptId;

    /** 版本 ID；0 为草稿。 */
    private Long versionId;

    /** 内容段键，如 systemPrompt。 */
    private String sectionKey;

    /** 段正文。 */
    private String content;
}
