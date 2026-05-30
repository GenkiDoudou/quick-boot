package io.github.genkidoudou.web.monitor.clienttrack.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 前端用户行为监控批次，与表 {@code sys_client_track} 对应。
 * <p>
 * 一次 flush 对应一行；{@code eventsJson} 为事件数组 JSON，供排障时还原点击/路由/API 链路。
 */
@Data
@TableName("sys_client_track")
public class SysClientTrack implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "batch_id", type = IdType.ASSIGN_ID)
    private Long batchId;

    /** 过渡：批次内首个 API 的 serverTraceId；新数据以 operationId 为主关联键。 */
    private String traceId;

    /** 前端一次用户操作 ID，主关联键。 */
    private String operationId;

    private Long userId;

    private String userName;

    /** normal / error / leave / timer。 */
    private String reason;

    /** 批次内末次 page 路径摘要（列 page_path，避免 MySQL 保留字 page）。 */
    private String pagePath;

    private String ua;

    /** JSON 数组字符串。 */
    private String eventsJson;

    private String clientIp;

    private LocalDateTime createTime;
}
