package io.github.genkidoudou.web.workflow.domain;

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
 * 工作流对外 API Key 实体，对应表 {@code wf_api_key}（P0 建表预留，不启用运行时鉴权）。
 */
@Data
@TableName("wf_api_key")
public class WfApiKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "key_id", type = IdType.ASSIGN_ID)
    private Long keyId;

    /** 绑定的工作流 ID。 */
    private Long workflowId;

    /** API Key 哈希值（明文不落库）。 */
    private String apiKeyHash;

    /** 状态：0 正常 / 1 停用。 */
    private Integer status;

    /** 过期时间，为空表示永不过期。 */
    private LocalDateTime expireTime;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
