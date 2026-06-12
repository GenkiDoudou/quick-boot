package io.github.genkidoudou.web.knowledge.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档异步入库任务，对应表 {@code kb_ingest_task}。
 */
@Data
@TableName("kb_ingest_task")
public class KbIngestTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "task_id", type = IdType.ASSIGN_ID)
    private Long taskId;

    /** 目标文档 ID。 */
    private Long docId;

    /**
     * 任务状态：QUEUED / RUNNING / SUCCESS / FAILED。
     *
     * @see io.github.genkidoudou.web.knowledge.constants.KbTaskStatus
     */
    private String status;

    /** 进度 0–100。 */
    private Integer progress;

    /** 已重试次数。 */
    private Integer retryCount;

    /** 失败原因摘要。 */
    private String errorMsg;

    /** 开始执行时间。 */
    private LocalDateTime startTime;

    /** 结束时间。 */
    private LocalDateTime endTime;
}
