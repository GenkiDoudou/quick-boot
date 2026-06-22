package io.github.genkidoudou.web.knowledge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.knowledge.config.KnowledgeMcpProperties;
import io.github.genkidoudou.web.knowledge.constants.KnowledgeConstants;
import io.github.genkidoudou.web.knowledge.constants.McpEnvValueType;
import io.github.genkidoudou.web.knowledge.constants.McpTestStatus;
import io.github.genkidoudou.web.knowledge.constants.McpTransport;
import io.github.genkidoudou.web.knowledge.domain.KbKnowledgeBaseMcp;
import io.github.genkidoudou.web.knowledge.domain.KbMcpEnv;
import io.github.genkidoudou.web.knowledge.domain.KbMcpServer;
import io.github.genkidoudou.web.knowledge.dto.KbMcpEnvBo;
import io.github.genkidoudou.web.knowledge.dto.KbMcpEnvVo;
import io.github.genkidoudou.web.knowledge.dto.KbMcpOptionVo;
import io.github.genkidoudou.web.knowledge.dto.KbMcpServerBo;
import io.github.genkidoudou.web.knowledge.dto.KbMcpServerQueryBo;
import io.github.genkidoudou.web.knowledge.dto.KbMcpServerVo;
import io.github.genkidoudou.web.knowledge.dto.McpHeaderItemBo;
import io.github.genkidoudou.web.knowledge.dto.McpTestResultVo;
import io.github.genkidoudou.web.knowledge.dto.McpToolInvokeBo;
import io.github.genkidoudou.web.knowledge.dto.McpToolInvokeResultVo;
import io.github.genkidoudou.web.knowledge.mapper.KbKnowledgeBaseMcpMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbMcpEnvMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbMcpServerMapper;
import io.github.genkidoudou.web.knowledge.mcp.runtime.McpClientManager;
import io.github.genkidoudou.web.knowledge.mcp.runtime.McpConnectionTester;
import io.github.genkidoudou.web.knowledge.mcp.runtime.McpToolInvoker;
import io.github.genkidoudou.web.knowledge.mcp.support.McpHeaderSupport;
import io.github.genkidoudou.web.knowledge.mcp.support.McpSecretSupport;
import io.github.genkidoudou.web.knowledge.mcp.support.McpTransportUrlSupport;
import io.github.genkidoudou.web.knowledge.mcp.support.McpUrlGuard;
import io.github.genkidoudou.web.knowledge.service.KbMcpServerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 服务配置管理服务实现。
 */
@Service
@ConditionalOnProperty(prefix = "qc.knowledge.mcp", name = "enabled", havingValue = "true")
public class KbMcpServerServiceImpl implements KbMcpServerService {

    private final KbMcpServerMapper serverMapper;
    private final KbMcpEnvMapper envMapper;
    private final KbKnowledgeBaseMcpMapper bindingMapper;
    private final PasswordCodec passwordCodec;
    private final KnowledgeMcpProperties mcpProperties;
    private final McpClientManager clientManager;
    private final McpConnectionTester connectionTester;
    private final McpToolInvoker toolInvoker;
    private final McpUrlGuard urlGuard;

    public KbMcpServerServiceImpl(KbMcpServerMapper serverMapper,
                                  KbMcpEnvMapper envMapper,
                                  KbKnowledgeBaseMcpMapper bindingMapper,
                                  PasswordCodec passwordCodec,
                                  KnowledgeMcpProperties mcpProperties,
                                  McpClientManager clientManager,
                                  McpConnectionTester connectionTester,
                                  McpToolInvoker toolInvoker,
                                  McpUrlGuard urlGuard) {
        this.serverMapper = serverMapper;
        this.envMapper = envMapper;
        this.bindingMapper = bindingMapper;
        this.passwordCodec = passwordCodec;
        this.mcpProperties = mcpProperties;
        this.clientManager = clientManager;
        this.connectionTester = connectionTester;
        this.toolInvoker = toolInvoker;
        this.urlGuard = urlGuard;
    }

    @Override
    public PageInfo<KbMcpServerVo> page(KbMcpServerQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        LambdaQueryWrapper<KbMcpServer> wrapper = Wrappers.<KbMcpServer>lambdaQuery()
            .eq(KbMcpServer::getDeleted, KnowledgeConstants.NOT_DELETED)
            .like(StrUtil.isNotBlank(query.getName()), KbMcpServer::getName, query.getName())
            .like(StrUtil.isNotBlank(query.getCode()), KbMcpServer::getCode, query.getCode())
            .eq(StrUtil.isNotBlank(query.getTransport()), KbMcpServer::getTransport, query.getTransport())
            .eq(query.getStatus() != null, KbMcpServer::getStatus, query.getStatus())
            .orderByDesc(KbMcpServer::getUpdateTime);

        Page<KbMcpServer> mp = serverMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<KbMcpServerVo> rows = new ArrayList<>(mp.getRecords().size());
        for (KbMcpServer row : mp.getRecords()) {
            rows.add(toVo(row, false));
        }
        Page<KbMcpServerVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public KbMcpServerVo getInfo(Long mcpId, boolean revealSecrets) {
        KbMcpServer row = getById(mcpId);
        if (row == null) {
            return null;
        }
        return toVo(row, revealSecrets);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(KbMcpServerBo req) {
        validateTransportFields(req);
        ensureCodeUnique(req.getCode(), null);

        KbMcpServer entity = new KbMcpServer();
        entity.setName(req.getName());
        entity.setCode(req.getCode().trim());
        entity.setDescription(StrUtil.nullToEmpty(req.getDescription()));
        entity.setTransport(req.getTransport());
        entity.setCommand(req.getCommand());
        entity.setArgsJson(encodeArgs(req.getArgs()));
        entity.setUrl(req.getUrl());
        entity.setHeadersJson(McpHeaderSupport.encodeHeaders(passwordCodec, req.getHeaders()));
        entity.setRequestTimeoutMs(req.getRequestTimeoutMs() == null ? 30_000 : req.getRequestTimeoutMs());
        entity.setStatus(req.getStatus() == null ? KnowledgeConstants.KB_STATUS_NORMAL : req.getStatus());
        entity.setLastTestStatus(McpTestStatus.UNTESTED);
        entity.setDeleted(KnowledgeConstants.NOT_DELETED);
        serverMapper.insert(entity);

        saveEnvs(entity.getMcpId(), req.getEnvs(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(KbMcpServerBo req) {
        KbMcpServer old = getById(req.getMcpId());
        if (old == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "MCP 配置不存在或已删除");
        }
        validateTransportFields(req);
        ensureCodeUnique(req.getCode(), req.getMcpId());

        KbMcpServer entity = new KbMcpServer();
        entity.setMcpId(req.getMcpId());
        entity.setName(req.getName());
        entity.setCode(req.getCode().trim());
        entity.setDescription(StrUtil.nullToEmpty(req.getDescription()));
        entity.setTransport(req.getTransport());
        entity.setCommand(req.getCommand());
        entity.setArgsJson(encodeArgs(req.getArgs()));
        entity.setUrl(req.getUrl());
        entity.setHeadersJson(McpHeaderSupport.mergeHeadersForUpdate(passwordCodec, old.getHeadersJson(), req.getHeaders()));
        entity.setRequestTimeoutMs(req.getRequestTimeoutMs() == null ? old.getRequestTimeoutMs() : req.getRequestTimeoutMs());
        if (req.getStatus() != null) {
            entity.setStatus(req.getStatus());
        }
        serverMapper.updateById(entity);

        saveEnvs(req.getMcpId(), req.getEnvs(), loadEnvMap(req.getMcpId()));
        clientManager.evict(req.getMcpId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> mcpIds) {
        if (mcpIds == null || mcpIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "删除 MCP ID 不能为空");
        }
        for (Long mcpId : mcpIds) {
            if (getById(mcpId) == null) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "存在无效的 MCP ID: " + mcpId);
            }
        }
        for (Long mcpId : mcpIds) {
            clientManager.evict(mcpId);

            KbMcpServer upd = new KbMcpServer();
            upd.setMcpId(mcpId);
            upd.setDeleted(KnowledgeConstants.DELETED);
            serverMapper.updateById(upd);

            envMapper.update(null, Wrappers.<KbMcpEnv>lambdaUpdate()
                .eq(KbMcpEnv::getMcpId, mcpId)
                .set(KbMcpEnv::getDeleted, KnowledgeConstants.DELETED));

            bindingMapper.update(null, Wrappers.<KbKnowledgeBaseMcp>lambdaUpdate()
                .eq(KbKnowledgeBaseMcp::getMcpId, mcpId)
                .set(KbKnowledgeBaseMcp::getDeleted, KnowledgeConstants.DELETED));
        }
    }

    @Override
    public McpTestResultVo test(Long mcpId) {
        if (getById(mcpId) == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "MCP 配置不存在或已删除");
        }
        return connectionTester.test(mcpId);
    }

    @Override
    public McpTestResultVo listTools(Long mcpId) {
        if (getById(mcpId) == null) {
            McpTestResultVo result = new McpTestResultVo();
            result.setSuccess(false);
            result.setMessage("MCP 配置不存在或已删除");
            return result;
        }
        return connectionTester.listTools(mcpId);
    }

    @Override
    public McpToolInvokeResultVo invokeTool(McpToolInvokeBo req) {
        if (getById(req.getMcpId()) == null) {
            McpToolInvokeResultVo result = new McpToolInvokeResultVo();
            result.setSuccess(false);
            result.setMessage("MCP 配置不存在或已删除");
            return result;
        }
        return toolInvoker.invoke(req.getMcpId(), req.getToolName(), req.getArguments());
    }

    @Override
    public Map<String, Object> export(List<Long> mcpIds, boolean includeSecrets) {
        if (!includeSecrets && !mcpProperties.getExport().isIncludeSecrets()) {
            includeSecrets = false;
        }
        List<KbMcpServer> servers;
        if (mcpIds == null || mcpIds.isEmpty()) {
            servers = serverMapper.selectList(
                Wrappers.<KbMcpServer>lambdaQuery()
                    .eq(KbMcpServer::getDeleted, KnowledgeConstants.NOT_DELETED)
                    .eq(KbMcpServer::getStatus, KnowledgeConstants.KB_STATUS_NORMAL)
                    .orderByAsc(KbMcpServer::getCode)
            );
        } else {
            servers = new ArrayList<>();
            for (Long mcpId : mcpIds) {
                KbMcpServer row = getById(mcpId);
                if (row != null) {
                    servers.add(row);
                }
            }
        }
        Map<String, Object> mcpServers = new LinkedHashMap<>();
        for (KbMcpServer server : servers) {
            mcpServers.put(server.getCode(), buildExportEntry(server, includeSecrets));
        }
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("mcpServers", mcpServers);
        return root;
    }

    @Override
    public List<KbMcpOptionVo> options() {
        List<KbMcpServer> servers = serverMapper.selectList(
            Wrappers.<KbMcpServer>lambdaQuery()
                .eq(KbMcpServer::getDeleted, KnowledgeConstants.NOT_DELETED)
                .eq(KbMcpServer::getStatus, KnowledgeConstants.KB_STATUS_NORMAL)
                .orderByAsc(KbMcpServer::getName)
        );
        List<KbMcpOptionVo> options = new ArrayList<>(servers.size());
        for (KbMcpServer server : servers) {
            KbMcpOptionVo vo = new KbMcpOptionVo();
            vo.setMcpId(server.getMcpId());
            vo.setName(server.getName());
            vo.setCode(server.getCode());
            vo.setTransport(server.getTransport());
            options.add(vo);
        }
        return options;
    }

    private KbMcpServer getById(Long mcpId) {
        if (mcpId == null) {
            return null;
        }
        KbMcpServer row = serverMapper.selectById(mcpId);
        if (row == null || KnowledgeConstants.DELETED == row.getDeleted()) {
            return null;
        }
        return row;
    }

    private KbMcpServerVo toVo(KbMcpServer row, boolean revealSecrets) {
        KbMcpServerVo vo = BeanUtil.copyProperties(row, KbMcpServerVo.class);
        if (StrUtil.isNotBlank(row.getArgsJson())) {
            vo.setArgs(JSONUtil.parseArray(row.getArgsJson()).toList(String.class));
        }
        vo.setHeaders(McpHeaderSupport.decodeHeaders(row.getHeadersJson(), revealSecrets));
        vo.setEnvs(loadEnvVos(row.getMcpId(), revealSecrets));
        return vo;
    }

    private List<KbMcpEnvVo> loadEnvVos(Long mcpId, boolean revealSecrets) {
        List<KbMcpEnv> rows = envMapper.selectList(
            Wrappers.<KbMcpEnv>lambdaQuery()
                .eq(KbMcpEnv::getMcpId, mcpId)
                .eq(KbMcpEnv::getDeleted, KnowledgeConstants.NOT_DELETED)
                .orderByAsc(KbMcpEnv::getSortOrder)
        );
        List<KbMcpEnvVo> result = new ArrayList<>(rows.size());
        for (KbMcpEnv row : rows) {
            KbMcpEnvVo vo = new KbMcpEnvVo();
            vo.setEnvId(row.getEnvId());
            vo.setEnvKey(row.getEnvKey());
            vo.setValueType(row.getValueType());
            if (revealSecrets) {
                vo.setEnvValue(row.getEnvValue());
            } else {
                vo.setEnvValue(McpSecretSupport.maskForDisplay(row.getValueType(), row.getEnvValue(), false));
            }
            vo.setSortOrder(row.getSortOrder());
            result.add(vo);
        }
        return result;
    }

    private Map<String, KbMcpEnv> loadEnvMap(Long mcpId) {
        List<KbMcpEnv> rows = envMapper.selectList(
            Wrappers.<KbMcpEnv>lambdaQuery()
                .eq(KbMcpEnv::getMcpId, mcpId)
                .eq(KbMcpEnv::getDeleted, KnowledgeConstants.NOT_DELETED)
        );
        Map<String, KbMcpEnv> map = new LinkedHashMap<>();
        for (KbMcpEnv row : rows) {
            map.put(row.getEnvKey(), row);
        }
        return map;
    }

    private void saveEnvs(Long mcpId, List<KbMcpEnvBo> envs, Map<String, KbMcpEnv> oldByKey) {
        if (envs == null) {
            return;
        }
        envMapper.update(null, Wrappers.<KbMcpEnv>lambdaUpdate()
            .eq(KbMcpEnv::getMcpId, mcpId)
            .set(KbMcpEnv::getDeleted, KnowledgeConstants.DELETED));

        int order = 0;
        for (KbMcpEnvBo bo : envs) {
            if (bo == null || StrUtil.isBlank(bo.getEnvKey())) {
                continue;
            }
            KbMcpEnv entity = new KbMcpEnv();
            entity.setMcpId(mcpId);
            entity.setEnvKey(bo.getEnvKey().trim());
            entity.setValueType(normalizeValueType(bo.getValueType()));
            entity.setSortOrder(bo.getSortOrder() == null ? order++ : bo.getSortOrder());
            entity.setDeleted(KnowledgeConstants.NOT_DELETED);

            if (McpSecretSupport.isKeepExistingSecret(entity.getValueType(), bo.getEnvValue())
                && oldByKey != null && oldByKey.containsKey(entity.getEnvKey())) {
                entity.setEnvValue(oldByKey.get(entity.getEnvKey()).getEnvValue());
            } else if (McpEnvValueType.SECRET.equals(entity.getValueType())) {
                entity.setEnvValue(McpSecretSupport.encodeForStorage(passwordCodec, bo.getEnvValue()));
            } else {
                entity.setEnvValue(StrUtil.nullToEmpty(bo.getEnvValue()));
            }
            envMapper.insert(entity);
        }
    }

    private void validateTransportFields(KbMcpServerBo req) {
        String transport = req.getTransport();
        if (McpTransport.STDIO.equals(transport)) {
            if (StrUtil.isBlank(req.getCommand())) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "STDIO 传输方式下命令不能为空");
            }
        } else if (McpTransport.SSE.equals(transport) || McpTransport.STREAMABLE_HTTP.equals(transport)) {
            if (StrUtil.isBlank(req.getUrl())) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "远程传输方式下 URL 不能为空");
            }
            urlGuard.validateUrl(req.getUrl());
            String mismatch = McpTransportUrlSupport.transportMismatchHint(transport, req.getUrl());
            if (mismatch != null) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, mismatch);
            }
        } else {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "不支持的传输方式: " + transport);
        }
    }

    private void ensureCodeUnique(String code, Long excludeMcpId) {
        if (StrUtil.isBlank(code)) {
            return;
        }
        KbMcpServer existing = serverMapper.selectOne(
            Wrappers.<KbMcpServer>lambdaQuery()
                .eq(KbMcpServer::getCode, code.trim())
                .eq(KbMcpServer::getDeleted, KnowledgeConstants.NOT_DELETED)
                .ne(excludeMcpId != null, KbMcpServer::getMcpId, excludeMcpId)
                .last("LIMIT 1")
        );
        if (existing != null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "MCP 编码已存在: " + code);
        }
    }

    private String encodeArgs(List<String> args) {
        if (args == null || args.isEmpty()) {
            return null;
        }
        return JSONUtil.toJsonStr(args);
    }

    private Map<String, Object> buildExportEntry(KbMcpServer server, boolean includeSecrets) {
        Map<String, Object> entry = new LinkedHashMap<>();
        if (McpTransport.STDIO.equals(server.getTransport())) {
            entry.put("command", server.getCommand());
            if (StrUtil.isNotBlank(server.getArgsJson())) {
                entry.put("args", JSONUtil.parseArray(server.getArgsJson()).toList(String.class));
            }
            Map<String, String> env = buildExportEnv(server.getMcpId(), includeSecrets);
            if (!env.isEmpty()) {
                entry.put("env", env);
            }
        } else {
            entry.put("url", server.getUrl());
            List<McpHeaderItemBo> headers = McpHeaderSupport.decodeHeaders(server.getHeadersJson(), includeSecrets);
            if (!headers.isEmpty()) {
                Map<String, String> headerMap = new LinkedHashMap<>();
                for (McpHeaderItemBo header : headers) {
                    headerMap.put(header.getName(), formatExportValue(header.getValueType(), header.getName(),
                        header.getValue(), includeSecrets));
                }
                entry.put("headers", headerMap);
            }
        }
        return entry;
    }

    private Map<String, String> buildExportEnv(Long mcpId, boolean includeSecrets) {
        List<KbMcpEnv> rows = envMapper.selectList(
            Wrappers.<KbMcpEnv>lambdaQuery()
                .eq(KbMcpEnv::getMcpId, mcpId)
                .eq(KbMcpEnv::getDeleted, KnowledgeConstants.NOT_DELETED)
                .orderByAsc(KbMcpEnv::getSortOrder)
        );
        Map<String, String> env = new LinkedHashMap<>();
        for (KbMcpEnv row : rows) {
            if (includeSecrets) {
                String plain = McpSecretSupport.resolvePlainValue(passwordCodec, row.getValueType(), row.getEnvValue());
                env.put(row.getEnvKey(), plain == null ? "" : plain);
            } else {
                env.put(row.getEnvKey(), formatExportValue(row.getValueType(), row.getEnvKey(), row.getEnvValue(), false));
            }
        }
        return env;
    }

    private String formatExportValue(String valueType, String key, String stored, boolean includeSecrets) {
        if (includeSecrets) {
            return McpSecretSupport.resolvePlainValue(passwordCodec, valueType, stored);
        }
        if (McpEnvValueType.SECRET.equals(valueType) || McpEnvValueType.ENV_REF.equals(valueType)) {
            String refKey = McpEnvValueType.ENV_REF.equals(valueType) ? stored : key;
            return "${" + refKey + "}";
        }
        return stored;
    }

    private String normalizeValueType(String valueType) {
        if (StrUtil.isBlank(valueType)) {
            return McpEnvValueType.PLAIN;
        }
        return valueType.trim().toUpperCase();
    }
}
