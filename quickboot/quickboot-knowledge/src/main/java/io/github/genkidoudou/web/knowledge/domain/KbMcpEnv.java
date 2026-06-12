package io.github.genkidoudou.web.knowledge.domain;

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
 * MCP 环境变量实体，对应表 {@code kb_mcp_env}。
 */
@Data
@TableName("kb_mcp_env")
public class KbMcpEnv implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "env_id", type = IdType.ASSIGN_ID)
    private Long envId;

    /** 关联 {@code kb_mcp_server.mcp_id}。 */
    private Long mcpId;

    /** 环境变量名。 */
    private String envKey;

    /**
     * 值类型：PLAIN / SECRET / ENV_REF。
     *
     * @see io.github.genkidoudou.web.knowledge.constants.McpEnvValueType
     */
    private String valueType;

    /** 明文、SM4 密文或环境变量引用名。 */
    private String envValue;

    /** 排序序号。 */
    private Integer sortOrder;

    /** 逻辑删除：0 否 / 1 是。 */
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
