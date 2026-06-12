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
 * 工作流运行实例实体，对应表 {@code wf_run}。
 */
@Data
@TableName("wf_run")
public class WfRun implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "run_id", type = IdType.ASSIGN_ID)
    private Long runId;

    private Long workflowId;

    private Long versionId;

    /**
     * 触发类型：DEBUG / ASYNC / API。
     *
     * @see io.github.genkidoudou.web.workflow.constants.WfTriggerType
     */
    private String triggerType;

    /**
     * 运行模式：SYNC / ASYNC。
     *
     * @see io.github.genkidoudou.web.workflow.constants.WfRunMode
     */
    private String runMode;

    /**
     * 运行状态。
     *
     * @see io.github.genkidoudou.web.workflow.constants.WfRunStatus
     */
    private String status;

    /** 运行入参 JSON。 */
    private String inputsJson;

    /** 运行出参 JSON。 */
    private String outputsJson;

    /** 失败原因摘要。 */
    private String errorMsg;

    /** 总耗时（毫秒）。 */
    private Long durationMs;

    /** 是否启用 SSE 流式：0 否 / 1 是。 */
    private Integer streamEnabled;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
