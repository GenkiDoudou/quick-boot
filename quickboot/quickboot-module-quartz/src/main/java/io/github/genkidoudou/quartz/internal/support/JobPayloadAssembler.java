package io.github.genkidoudou.quartz.internal.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.quartz.internal.dto.JobHeaderBo;
import io.github.genkidoudou.quartz.internal.dto.JobHttpConfigBo;
import io.github.genkidoudou.quartz.internal.dto.JobScriptConfigBo;
import io.github.genkidoudou.quartz.internal.dto.SysJobSaveBo;
import io.github.genkidoudou.quartz.internal.dto.SysJobVo;
import io.github.genkidoudou.quartz.internal.entity.SysJob;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定时任务结构化配置与表字段 {@code invoke_target}/{@code params} 互转。
 * <p>
 * Bean：invoke_target=Bean 名，params=纯文本参数；
 * HTTP：invoke_target=URL，params=JSON（method/headers/body 等）；
 * Script：invoke_target=脚本路径，params=JSON（args/workDir/timeoutSec）。
 */
public final class JobPayloadAssembler {

    private JobPayloadAssembler() {
    }

    /**
     * 保存前：根据 jobType 与结构化字段填充 invokeTarget、params、jobType。
     */
    public static void applyToEntity(SysJobSaveBo bo, SysJob entity) {
        JobType type = JobType.fromCode(bo.getJobType());
        if (type == null) {
            type = JobType.BEAN;
        }
        entity.setJobType(type.getCode());
        switch (type) {
            case HTTP -> applyHttp(bo.getHttpConfig(), entity);
            case SCRIPT -> applyScript(bo.getScriptConfig(), entity);
            default -> {
                entity.setInvokeTarget(StrUtil.trim(bo.getInvokeTarget()));
                entity.setParams(StrUtil.nullToEmpty(bo.getParams()));
            }
        }
    }

    /**
     * 详情/列表：从实体反解结构化配置到 Vo。
     */
    public static void enrichVo(SysJob row, SysJobVo vo) {
        JobType type = JobType.fromCode(row.getJobType());
        if (type == null) {
            vo.setJobType(JobType.BEAN.getCode());
            return;
        }
        vo.setJobType(type.getCode());
        switch (type) {
            case HTTP -> vo.setHttpConfig(toHttpConfig(row));
            case SCRIPT -> vo.setScriptConfig(toScriptConfig(row));
            default -> {
                // Bean 沿用 invokeTarget / params 字段
            }
        }
    }

    private static void applyHttp(JobHttpConfigBo config, SysJob entity) {
        if (config == null) {
            entity.setInvokeTarget("");
            entity.setParams("");
            return;
        }
        entity.setInvokeTarget(StrUtil.trim(config.getUrl()));
        JobHttpParamsPayload payload = new JobHttpParamsPayload();
        payload.setMethod(normalizeMethod(config.getMethod()));
        payload.setHeaders(toHeaderEntries(config.getHeaders()));
        payload.setBody(config.getBody());
        payload.setTimeoutMs(config.getTimeoutMs() != null ? config.getTimeoutMs() : 30_000);
        payload.setExpectStatus(StrUtil.blankToDefault(config.getExpectStatus(), "200"));
        entity.setParams(JSONUtil.toJsonStr(payload));
    }

    private static void applyScript(JobScriptConfigBo config, SysJob entity) {
        if (config == null) {
            entity.setInvokeTarget("");
            entity.setParams("");
            return;
        }
        entity.setInvokeTarget(StrUtil.trim(config.getScriptPath()));
        JobScriptParamsPayload payload = new JobScriptParamsPayload();
        payload.setArgs(config.getArgs() != null ? config.getArgs() : new ArrayList<>());
        payload.setWorkDir(config.getWorkDir());
        payload.setTimeoutSec(config.getTimeoutSec() != null ? config.getTimeoutSec() : 60);
        entity.setParams(JSONUtil.toJsonStr(payload));
    }

    private static JobHttpConfigBo toHttpConfig(SysJob row) {
        JobHttpConfigBo bo = new JobHttpConfigBo();
        bo.setUrl(row.getInvokeTarget());
        if (StrUtil.isBlank(row.getParams()) || !JSONUtil.isTypeJSON(row.getParams())) {
            bo.setMethod("GET");
            return bo;
        }
        JobHttpParamsPayload payload = JSONUtil.toBean(row.getParams(), JobHttpParamsPayload.class);
        bo.setMethod(normalizeMethod(payload.getMethod()));
        bo.setHeaders(fromHeaderEntries(payload.getHeaders()));
        bo.setBody(payload.getBody());
        bo.setTimeoutMs(payload.getTimeoutMs());
        bo.setExpectStatus(payload.getExpectStatus());
        return bo;
    }

    private static JobScriptConfigBo toScriptConfig(SysJob row) {
        JobScriptConfigBo bo = new JobScriptConfigBo();
        bo.setScriptPath(row.getInvokeTarget());
        if (StrUtil.isBlank(row.getParams()) || !JSONUtil.isTypeJSON(row.getParams())) {
            return bo;
        }
        JobScriptParamsPayload payload = JSONUtil.toBean(row.getParams(), JobScriptParamsPayload.class);
        bo.setArgs(payload.getArgs() != null ? payload.getArgs() : new ArrayList<>());
        bo.setWorkDir(payload.getWorkDir());
        bo.setTimeoutSec(payload.getTimeoutSec());
        return bo;
    }

    private static String normalizeMethod(String method) {
        if (StrUtil.isBlank(method)) {
            return "GET";
        }
        return method.trim().toUpperCase();
    }

    private static List<JobHttpParamsPayload.JobHeaderEntry> toHeaderEntries(List<JobHeaderBo> headers) {
        if (headers == null || headers.isEmpty()) {
            return new ArrayList<>();
        }
        return headers.stream()
            .filter(h -> h != null && StrUtil.isNotBlank(h.getKey()))
            .map(h -> {
                JobHttpParamsPayload.JobHeaderEntry e = new JobHttpParamsPayload.JobHeaderEntry();
                e.setKey(StrUtil.trim(h.getKey()));
                e.setValue(h.getValue() != null ? h.getValue() : "");
                return e;
            })
            .collect(Collectors.toList());
    }

    private static List<JobHeaderBo> fromHeaderEntries(List<JobHttpParamsPayload.JobHeaderEntry> headers) {
        List<JobHeaderBo> list = new ArrayList<>();
        if (headers == null) {
            return list;
        }
        for (JobHttpParamsPayload.JobHeaderEntry e : headers) {
            if (e == null || StrUtil.isBlank(e.getKey())) {
                continue;
            }
            JobHeaderBo bo = new JobHeaderBo();
            bo.setKey(e.getKey());
            bo.setValue(e.getValue());
            list.add(bo);
        }
        return list;
    }
}
