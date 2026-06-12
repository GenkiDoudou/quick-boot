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
 * 工作流版本实体，对应表 {@code wf_workflow_version}。
 * <p>
 * 草稿与发布版本共用此表；{@code is_draft=1} 表示当前编辑中的草稿。
 */
@Data
@TableName("wf_workflow_version")
public class WfWorkflowVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "version_id", type = IdType.ASSIGN_ID)
    private Long versionId;

    /** 所属工作流 ID。 */
    private Long workflowId;

    /** 版本序号，从 1 递增。 */
    private Integer versionNo;

    /** DAG JSON DSL（nodes/edges）。 */
    private String graphJson;

    /** graph_json 的 SHA-256 校验和。 */
    private String checksum;

    /** 是否当前编辑草稿：1 是 / 0 否。 */
    private Integer isDraft;

    /** 版本备注。 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
