package io.github.genkidoudou.web.ai.prompt.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提示词版本快照实体，对应 {@code ai_prompt_version}。
 */
@Data
@TableName("ai_prompt_version")
public class AiPromptVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "version_id", type = IdType.ASSIGN_ID)
    private Long versionId;

    /** 所属提示词 ID。 */
    private Long promptId;

    /** 递增版本号，从 1 开始。 */
    private Integer versionNo;

    /** 变更摘要。 */
    private String changeSummary;

    /** 完整快照 JSON：sections + variables。 */
    private String snapshotJson;

    /**
     * 版本来源：EDIT / OPTIMIZE / AB_ADOPT / ROLLBACK / PUBLISH。
     *
     * @see io.github.genkidoudou.web.ai.prompt.constants.AiPromptVersionSource
     */
    private String source;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
