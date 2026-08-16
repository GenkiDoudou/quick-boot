package io.github.genkidoudou.monitor.internal.litetrace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 链路索引查询条件。
 */
@Data
@Schema(description = "链路索引查询")
public class TraceIndexQueryBo {

    @Schema(description = "来源 browser/api/job；多值逗号")
    private String rootSource;

    @Schema(description = "列表模式：api=接口(browser+api) job=任务；空=按 rootSource")
    private String listMode;

    /** 链路标识，精确匹配 */
    private String traceId;

    /** 客户端操作 ID，精确匹配 */
    private String operationId;

    /** 用户标识，模糊匹配 */
    private String uin;

    /** 入口名称，模糊匹配 */
    private String entryName;

    /** 调用方名称，精确匹配 */
    private String callerName;

    /** 前端动作名称，精确匹配 */
    private String actionName;

    /** 页面路径，模糊匹配 */
    private String pagePath;

    /** 会话 ID，模糊匹配 */
    private String sessionId;

    /** 页面访问 ID，精确匹配 */
    private String pageVisitId;

    @Schema(description = "搜索类型：all/page/url/traceId/sessionId/pageVisitId/keyword")
    private String searchType;

    @Schema(description = "搜索关键字（配合 searchType）")
    private String keyword;

    @Schema(description = "自由查询串，如 traceId:xxx")
    private String q;

    @Schema(description = "开始时间 yyyy-MM-dd HH:mm:ss，按 started_at>=")
    private String beginTime;

    @Schema(description = "结束时间 yyyy-MM-dd HH:mm:ss，按 started_at<")
    private String endTime;

    @Schema(description = "结果：1 成功 / 0 失败；空=全部")
    private String okFlag;

    @Schema(description = "排序字段：time/duration/name")
    private String sortKey;

    @Schema(description = "asc/desc")
    private String sortDir;

    /** 页码，默认 1 */
    private Integer pageNum = 1;

    /** 每页条数，默认 20，最大 100 */
    private Integer pageSize = 20;
}
