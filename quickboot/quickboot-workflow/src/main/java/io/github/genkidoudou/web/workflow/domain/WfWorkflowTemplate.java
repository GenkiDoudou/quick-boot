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
 * 工作流模板实体，对应表 {@code wf_workflow_template}。
 */
@Data
@TableName("wf_workflow_template")
public class WfWorkflowTemplate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "template_id", type = IdType.ASSIGN_ID)
    private Long templateId;

    /** 唯一编码，新建工作流时作为 templateCode 传入。 */
    private String code;

    /** 展示名称。 */
    private String name;

    /** 描述说明。 */
    private String description;

    /** 图 DSL JSON 字符串。 */
    private String graphJson;

    /** 是否内置：0 否 / 1 是（内置模板不可删除）。 */
    private Integer builtin;

    /**
     * 状态：ENABLED / DISABLED。
     *
     * @see io.github.genkidoudou.web.workflow.constants.WfTemplateStatus
     */
    private String status;

    /** 下拉排序，升序。 */
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
