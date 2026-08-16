package io.github.genkidoudou.monitor.internal.litetrace.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.monitor.internal.litetrace.dto.PageVisitVo;
import io.github.genkidoudou.monitor.internal.litetrace.dto.TraceIndexQueryBo;
import io.github.genkidoudou.monitor.internal.litetrace.dto.TraceIndexVo;
import io.github.genkidoudou.monitor.internal.litetrace.dto.TraceSpanVo;
import io.github.genkidoudou.monitor.internal.litetrace.service.LiteTraceQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Lite 链路查询（A′ / E）。
 */
@Tag(name = "Lite链路查询")
@Validated
@RestController
@RequestMapping("/monitor/liteTrace")
@RequiredArgsConstructor
public class LiteTraceQueryController {

    private final LiteTraceQueryService liteTraceQueryService;

    /**
     * 分页查询链路索引。
     *
     * @param query 筛选、排序与分页条件
     * @return 链路索引分页结果
     */
    @Operation(summary = "链路索引分页")
    @SaCheckPermission("monitor:liteTrace:query")
    @GetMapping("/index/list")
    public R<PageInfo<TraceIndexVo>> list(@Validated TraceIndexQueryBo query) {
        return R.ok(liteTraceQueryService.page(query));
    }

    /**
     * 按 pageVisitId 聚合的页面访问列表。
     *
     * @param query 时间范围、关键字等筛选条件
     * @return 页面访问聚合摘要列表
     */
    @Operation(summary = "页面访问聚合列表")
    @SaCheckPermission("monitor:liteTrace:query")
    @GetMapping("/pageVisit/list")
    public R<List<PageVisitVo>> pageVisits(@Validated TraceIndexQueryBo query) {
        return R.ok(liteTraceQueryService.listPageVisits(query));
    }

    /**
     * 查询某次页面访问下的全部链路。
     *
     * @param pageVisitId 页面访问标识
     * @param beginTime   可选，开始时间
     * @param endTime     可选，结束时间
     * @return 该次访问关联的链路索引列表
     */
    @Operation(summary = "某次页面访问下的链路列表")
    @SaCheckPermission("monitor:liteTrace:query")
    @GetMapping("/pageVisit/{pageVisitId}/traces")
    public R<List<TraceIndexVo>> pageVisitTraces(
        @PathVariable("pageVisitId") String pageVisitId,
        @RequestParam(required = false) String beginTime,
        @RequestParam(required = false) String endTime) {
        return R.ok(liteTraceQueryService.listByPageVisit(pageVisitId, beginTime, endTime));
    }

    /**
     * 按 traceId 查询链路索引详情。
     *
     * @param traceId 链路标识
     * @return 链路索引；不存在时抛业务异常
     */
    @Operation(summary = "链路索引详情")
    @SaCheckPermission("monitor:liteTrace:query")
    @GetMapping("/index/{traceId}")
    public R<TraceIndexVo> detail(@PathVariable("traceId") String traceId) {
        return R.ok(liteTraceQueryService.detail(traceId));
    }

    /**
     * 查询某条链路下的全部 span 片段。
     *
     * @param traceId 链路标识
     * @return 按起始偏移排序的 span 列表
     */
    @Operation(summary = "链路片段列表")
    @SaCheckPermission("monitor:liteTrace:query")
    @GetMapping("/spans/{traceId}")
    public R<List<TraceSpanVo>> spans(@PathVariable("traceId") String traceId) {
        return R.ok(liteTraceQueryService.spans(traceId));
    }

    /**
     * 写入或补全纯 API/Job 根索引，供开发验证使用。
     *
     * @param traceId    可选，为空则自动生成
     * @param rootSource 根来源：api / job 等
     * @param entry      入口描述
     * @param caller     调用方名称
     * @return 含 traceId 的映射；副作用：写入 sys_trace_index
     */
    @Operation(summary = "写入纯 API/Job 根索引（开发验证）")
    @SaCheckPermission("monitor:liteTrace:query")
    @PostMapping("/index/ensureRoot")
    public R<Map<String, String>> ensureRoot(
        @RequestParam(required = false) String traceId,
        @RequestParam(defaultValue = "api") String rootSource,
        @RequestParam(defaultValue = "POST /openapi/demo") String entry,
        @RequestParam(defaultValue = "partner") String caller) {
        String tid = (traceId == null || traceId.isBlank())
            ? UUID.randomUUID().toString().replace("-", "")
            : traceId.trim();
        liteTraceQueryService.ensureRoot(tid, rootSource, entry, caller);
        return R.ok(Map.of("traceId", tid));
    }
}
