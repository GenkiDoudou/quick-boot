package io.github.genkidoudou.web.workflow.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.workflow.dto.WfRunAsyncBo;
import io.github.genkidoudou.web.workflow.dto.WfRunDebugBo;
import io.github.genkidoudou.web.workflow.dto.WfRunDetailVo;
import io.github.genkidoudou.web.workflow.dto.WfRunQueryBo;
import io.github.genkidoudou.web.workflow.dto.WfRunVo;

import java.util.Map;

/**
 * 工作流运行服务：Debug、异步、查询。
 */
public interface WorkflowRunService {

    WfRunDetailVo debugRun(WfRunDebugBo req);

    WfRunVo asyncRun(WfRunAsyncBo req);

    WfRunDetailVo getInfo(Long runId);

    PageInfo<WfRunVo> page(WfRunQueryBo query);

    /**
     * 构建运行时上下文（供 Engine / AsyncExecutor 使用）。
     *
     * @param runId      运行 ID
     * @param startNodeId start 节点 ID
     * @param inputs     入参
     * @param kbId       知识库 ID
     * @param userId     用户 ID
     * @param stream     是否流式
     * @return 上下文
     */
    io.github.genkidoudou.web.workflow.engine.WorkflowContext buildContext(
        Long runId, Long workflowId, String startNodeId, Map<String, Object> inputs, Long kbId, String userId, boolean stream);
}
