package io.github.genkidoudou.web.knowledge.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.knowledge.constants.KnowledgeConstants;
import io.github.genkidoudou.web.knowledge.domain.KbKnowledgeBaseMcp;
import io.github.genkidoudou.web.knowledge.domain.KbMcpServer;
import io.github.genkidoudou.web.knowledge.mapper.KbKnowledgeBaseMcpMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbMcpServerMapper;
import io.github.genkidoudou.web.knowledge.service.KbKnowledgeBaseMcpService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 知识库与 MCP 绑定关系实现。
 */
@Service
public class KbKnowledgeBaseMcpServiceImpl implements KbKnowledgeBaseMcpService {

    private final KbKnowledgeBaseMcpMapper bindingMapper;
    private final KbMcpServerMapper mcpServerMapper;

    public KbKnowledgeBaseMcpServiceImpl(KbKnowledgeBaseMcpMapper bindingMapper,
                                         KbMcpServerMapper mcpServerMapper) {
        this.bindingMapper = bindingMapper;
        this.mcpServerMapper = mcpServerMapper;
    }

    @Override
    public List<Long> listEnabledMcpIdsByKbId(Long kbId) {
        List<Long> bound = listMcpIdsByKbId(kbId);
        if (bound.isEmpty()) {
            return List.of();
        }
        List<KbMcpServer> servers = mcpServerMapper.selectList(
            Wrappers.<KbMcpServer>lambdaQuery()
                .in(KbMcpServer::getMcpId, bound)
                .eq(KbMcpServer::getStatus, KnowledgeConstants.KB_STATUS_NORMAL)
                .eq(KbMcpServer::getDeleted, KnowledgeConstants.NOT_DELETED)
        );
        Set<Long> enabled = servers.stream().map(KbMcpServer::getMcpId).collect(Collectors.toSet());
        List<Long> result = new ArrayList<>();
        for (Long mcpId : bound) {
            if (enabled.contains(mcpId)) {
                result.add(mcpId);
            }
        }
        return result;
    }

    @Override
    public List<Long> listMcpIdsByKbId(Long kbId) {
        if (kbId == null) {
            return List.of();
        }
        return bindingMapper.selectList(
            Wrappers.<KbKnowledgeBaseMcp>lambdaQuery()
                .eq(KbKnowledgeBaseMcp::getKbId, kbId)
                .eq(KbKnowledgeBaseMcp::getDeleted, KnowledgeConstants.NOT_DELETED)
                .orderByAsc(KbKnowledgeBaseMcp::getOrderNum)
        ).stream().map(KbKnowledgeBaseMcp::getMcpId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBindings(Long kbId, List<Long> mcpIds) {
        if (kbId == null) {
            return;
        }
        bindingMapper.update(null, Wrappers.<KbKnowledgeBaseMcp>lambdaUpdate()
            .eq(KbKnowledgeBaseMcp::getKbId, kbId)
            .set(KbKnowledgeBaseMcp::getDeleted, KnowledgeConstants.DELETED));

        if (mcpIds == null || mcpIds.isEmpty()) {
            return;
        }
        Set<Long> unique = new HashSet<>();
        int order = 0;
        for (Long mcpId : mcpIds) {
            if (mcpId == null || !unique.add(mcpId)) {
                continue;
            }
            KbMcpServer server = mcpServerMapper.selectById(mcpId);
            if (server == null || KnowledgeConstants.DELETED == server.getDeleted()) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "MCP 不存在或已删除: " + mcpId);
            }
            KbKnowledgeBaseMcp binding = new KbKnowledgeBaseMcp();
            binding.setKbId(kbId);
            binding.setMcpId(mcpId);
            binding.setOrderNum(order++);
            binding.setDeleted(KnowledgeConstants.NOT_DELETED);
            bindingMapper.insert(binding);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByKbId(Long kbId) {
        if (kbId == null) {
            return;
        }
        bindingMapper.update(null, Wrappers.<KbKnowledgeBaseMcp>lambdaUpdate()
            .eq(KbKnowledgeBaseMcp::getKbId, kbId)
            .set(KbKnowledgeBaseMcp::getDeleted, KnowledgeConstants.DELETED));
    }
}
