package io.github.genkidoudou.web.workflow.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工作流运行步骤 Trace 实体，对应表 {@code wf_run_step}。
 */
@Data
@TableName("wf_run_step")
public class WfRunStep implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "step_id", type = IdType.ASSIGN_ID)
    private Long stepId;

    /** 所属运行实例 ID。 */
    private Long runId;

    /** 画布节点 ID。 */
    private String nodeId;

    /** 节点类型。 */
    private String nodeType;

    /** 步骤状态：RUNNING / SUCCESS / FAILED / SKIPPED。 */
    private String status;

    /** 节点入参 JSON（脱敏后落库）。 */
    private String inputsJson;

    /** 节点出参 JSON（脱敏后落库）。 */
    private String outputsJson;

    /** 失败原因。 */
    private String errorMsg;

    /** 步骤耗时（毫秒）。 */
    private Long durationMs;

    /** 执行顺序号，从 1 递增。 */
    private Integer orderNo;

    private LocalDateTime createTime;
}
