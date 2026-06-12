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
 * 工作流定义实体，对应表 {@code wf_workflow}。
 */
@Data
@TableName("wf_workflow")
public class WfWorkflow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "workflow_id", type = IdType.ASSIGN_ID)
    private Long workflowId;

    /** 工作流名称。 */
    private String name;

    /** 描述说明。 */
    private String description;

    /**
     * 状态：DRAFT / PUBLISHED / DISABLED。
     *
     * @see io.github.genkidoudou.web.workflow.constants.WfWorkflowStatus
     */
    private String status;

    /** 当前发布版本 ID，草稿编辑时不影响该字段。 */
    private Long publishedVersionId;

    /** 可选 Chat 模型 ai_model.model_id。 */
    private Long chatModelId;

    /** 预留：是否允许 Bot 绑定。 */
    private Integer botEnabled;

    /** 预留：是否允许对外 API Key 调用。 */
    private Integer externalApiEnabled;

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
