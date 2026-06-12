package io.github.genkidoudou.web.ai.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.ai.config.AiProperties;
import io.github.genkidoudou.web.ai.constants.AiApiKeyType;
import io.github.genkidoudou.web.ai.constants.AiConstants;
import io.github.genkidoudou.web.ai.constants.AiDefaultSlot;
import io.github.genkidoudou.web.ai.constants.AiModelType;
import io.github.genkidoudou.web.ai.constants.AiProvider;
import io.github.genkidoudou.web.ai.constants.AiTestStatus;
import io.github.genkidoudou.web.ai.domain.AiModel;
import io.github.genkidoudou.web.ai.dto.AiModelBo;
import io.github.genkidoudou.web.ai.dto.AiModelOptionVo;
import io.github.genkidoudou.web.ai.dto.AiModelQueryBo;
import io.github.genkidoudou.web.ai.dto.AiModelVo;
import io.github.genkidoudou.web.ai.dto.AiSetDefaultBo;
import io.github.genkidoudou.web.ai.dto.AiTestResultVo;
import io.github.genkidoudou.web.ai.mapper.AiModelMapper;
import io.github.genkidoudou.web.ai.registry.AiModelConnectionTester;
import io.github.genkidoudou.web.ai.registry.AiModelRegistry;
import io.github.genkidoudou.web.ai.service.AiModelService;
import io.github.genkidoudou.web.ai.support.AiDimensionValidator;
import io.github.genkidoudou.web.ai.support.AiSecretSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * AI 大模型配置管理服务实现。
 */
@Service
public class AiModelServiceImpl implements AiModelService {

    private static final Set<String> VALID_SLOTS = Set.of(
        AiDefaultSlot.CHAT, AiDefaultSlot.EMBEDDING, AiDefaultSlot.WORKFLOW_CHAT
    );

    private final AiModelMapper modelMapper;
    private final PasswordCodec passwordCodec;
    private final AiProperties aiProperties;
    private final AiModelRegistry modelRegistry;
    private final AiModelConnectionTester connectionTester;

    public AiModelServiceImpl(AiModelMapper modelMapper,
                              PasswordCodec passwordCodec,
                              AiProperties aiProperties,
                              AiModelRegistry modelRegistry,
                              AiModelConnectionTester connectionTester) {
        this.modelMapper = modelMapper;
        this.passwordCodec = passwordCodec;
        this.aiProperties = aiProperties;
        this.modelRegistry = modelRegistry;
        this.connectionTester = connectionTester;
    }

    @Override
    public PageInfo<AiModelVo> page(AiModelQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        LambdaQueryWrapper<AiModel> wrapper = Wrappers.<AiModel>lambdaQuery()
            .eq(AiModel::getDeleted, AiConstants.NOT_DELETED)
            .like(StrUtil.isNotBlank(query.getName()), AiModel::getName, query.getName())
            .like(StrUtil.isNotBlank(query.getCode()), AiModel::getCode, query.getCode())
            .eq(StrUtil.isNotBlank(query.getModelType()), AiModel::getModelType, query.getModelType())
            .eq(StrUtil.isNotBlank(query.getProvider()), AiModel::getProvider, query.getProvider())
            .eq(query.getStatus() != null, AiModel::getStatus, query.getStatus())
            .eq(StrUtil.isNotBlank(query.getDefaultSlot()), AiModel::getDefaultSlot, query.getDefaultSlot())
            .orderByDesc(AiModel::getUpdateTime);

        Page<AiModel> mp = modelMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<AiModelVo> rows = new ArrayList<>(mp.getRecords().size());
        for (AiModel row : mp.getRecords()) {
            rows.add(toVo(row, false));
        }
        Page<AiModelVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public AiModelVo getInfo(Long modelId, boolean revealSecrets) {
        AiModel row = getById(modelId);
        if (row == null) {
            return null;
        }
        return toVo(row, revealSecrets);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(AiModelBo req) {
        validateBusinessFields(req, null);
        ensureCodeUnique(req.getCode(), null);

        AiModel entity = buildEntity(req, null);
        entity.setLastTestStatus(AiTestStatus.UNTESTED);
        entity.setDeleted(AiConstants.NOT_DELETED);
        modelMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AiModelBo req) {
        AiModel old = getById(req.getModelId());
        if (old == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "模型不存在或已删除");
        }
        validateBusinessFields(req, old);
        ensureCodeUnique(req.getCode(), req.getModelId());

        AiModel entity = buildEntity(req, old);
        entity.setModelId(req.getModelId());
        if (req.getStatus() == null) {
            entity.setStatus(old.getStatus());
        }
        entity.setDefaultSlot(old.getDefaultSlot());
        modelMapper.updateById(entity);
        modelRegistry.evict(req.getModelId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> modelIds) {
        if (modelIds == null || modelIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "删除模型 ID 不能为空");
        }
        for (Long modelId : modelIds) {
            AiModel row = getById(modelId);
            if (row == null) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "存在无效的模型 ID: " + modelId);
            }
            validateRemovable(row);
        }
        for (Long modelId : modelIds) {
            modelRegistry.evict(modelId);
            AiModel upd = new AiModel();
            upd.setModelId(modelId);
            upd.setDeleted(AiConstants.DELETED);
            modelMapper.updateById(upd);
        }
    }

    @Override
    public AiTestResultVo test(Long modelId) {
        if (getById(modelId) == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "模型不存在或已删除");
        }
        return connectionTester.test(modelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(AiSetDefaultBo req) {
        String slot = normalizeSlot(req.getDefaultSlot());
        AiModel row = getById(req.getModelId());
        if (row == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "模型不存在或已删除");
        }
        validateSlotModelType(slot, row.getModelType());
        if (AiDefaultSlot.EMBEDDING.equals(slot)) {
            AiDimensionValidator.validateRequired(row.getDimensions(), aiProperties.getVectorDimensions());
        }
        modelRegistry.clearDefaultSlot(slot);
        AiModel upd = new AiModel();
        upd.setModelId(req.getModelId());
        upd.setDefaultSlot(slot);
        modelMapper.updateById(upd);
        modelRegistry.evict(req.getModelId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearDefault(String defaultSlot) {
        String slot = normalizeSlot(defaultSlot);
        modelRegistry.clearDefaultSlot(slot);
        modelRegistry.evictAll();
    }

    @Override
    public String export(List<Long> modelIds, String format, boolean includeSecrets) {
        if (!includeSecrets && !aiProperties.getExport().isIncludeSecrets()) {
            includeSecrets = false;
        }
        List<AiModel> models = loadExportModels(modelIds);
        String fmt = format == null ? "yaml" : format.trim().toLowerCase(Locale.ROOT);
        if ("env".equals(fmt)) {
            return exportEnv(models, includeSecrets);
        }
        return exportYaml(models, includeSecrets);
    }

    @Override
    public List<AiModelOptionVo> options(String modelType) {
        LambdaQueryWrapper<AiModel> wrapper = Wrappers.<AiModel>lambdaQuery()
            .eq(AiModel::getDeleted, AiConstants.NOT_DELETED)
            .eq(AiModel::getStatus, AiConstants.STATUS_NORMAL);
        if (StrUtil.isNotBlank(modelType)) {
            if (AiModelType.isLanguageFilter(modelType)) {
                wrapper.in(AiModel::getModelType, AiModelType.LANGUAGE, AiModelType.CHAT);
            } else if (AiModelType.isVectorFilter(modelType)) {
                wrapper.in(AiModel::getModelType, AiModelType.VECTOR, AiModelType.EMBEDDING);
            } else {
                wrapper.eq(AiModel::getModelType, modelType.trim().toUpperCase(Locale.ROOT));
            }
        }
        wrapper.orderByAsc(AiModel::getName);
        List<AiModel> rows = modelMapper.selectList(wrapper);
        List<AiModelOptionVo> options = new ArrayList<>(rows.size());
        for (AiModel row : rows) {
            AiModelOptionVo vo = new AiModelOptionVo();
            vo.setModelId(row.getModelId());
            vo.setName(row.getName());
            vo.setCode(row.getCode());
            vo.setModelType(row.getModelType());
            vo.setProvider(row.getProvider());
            vo.setDefaultSlot(row.getDefaultSlot());
            options.add(vo);
        }
        return options;
    }

    @Override
    public List<AiModelVo> importFromYaml() {
        // 可选能力：从 spring.ai 自动配置生成 ENV_REF 草稿，本期仅占位返回空列表
        return List.of();
    }

    private AiModel getById(Long modelId) {
        if (modelId == null) {
            return null;
        }
        AiModel row = modelMapper.selectById(modelId);
        if (row == null || AiConstants.DELETED == row.getDeleted()) {
            return null;
        }
        return row;
    }

    private AiModelVo toVo(AiModel row, boolean revealSecrets) {
        AiModelVo vo = BeanUtil.copyProperties(row, AiModelVo.class);
        vo.setApiKey(AiSecretSupport.maskForDisplay(row.getApiKeyType(), row.getApiKey(), revealSecrets));
        return vo;
    }

    private AiModel buildEntity(AiModelBo req, AiModel old) {
        AiModel entity = new AiModel();
        entity.setName(req.getName());
        entity.setCode(req.getCode().trim());
        entity.setDescription(StrUtil.nullToEmpty(req.getDescription()));
        entity.setModelType(req.getModelType().trim().toUpperCase(Locale.ROOT));
        entity.setProvider(req.getProvider().trim().toUpperCase(Locale.ROOT));
        entity.setBaseUrl(req.getBaseUrl().trim());
        entity.setApiKeyType(normalizeApiKeyType(req.getApiKeyType()));
        entity.setApiKey(resolveApiKeyForSave(req, old));
        entity.setModelName(req.getModelName().trim());
        entity.setCompletionsPath(StrUtil.trimToNull(req.getCompletionsPath()));
        entity.setEmbeddingsPath(StrUtil.trimToNull(req.getEmbeddingsPath()));
        entity.setDimensions(req.getDimensions());
        entity.setTemperature(req.getTemperature());
        entity.setMaxTokens(req.getMaxTokens());
        entity.setRequestTimeoutMs(req.getRequestTimeoutMs() == null ? 60_000 : req.getRequestTimeoutMs());
        entity.setStatus(req.getStatus() == null ? AiConstants.STATUS_NORMAL : req.getStatus());
        return entity;
    }

    private String resolveApiKeyForSave(AiModelBo req, AiModel old) {
        String valueType = normalizeApiKeyType(req.getApiKeyType());
        if (old != null && AiSecretSupport.isKeepExistingSecret(valueType, req.getApiKey())) {
            return old.getApiKey();
        }
        if (AiApiKeyType.SECRET.equals(valueType)) {
            return AiSecretSupport.encodeForStorage(passwordCodec, req.getApiKey());
        }
        return StrUtil.nullToEmpty(req.getApiKey());
    }

    private void validateBusinessFields(AiModelBo req, AiModel old) {
        String modelType = req.getModelType().trim().toUpperCase(Locale.ROOT);
        String provider = req.getProvider().trim().toUpperCase(Locale.ROOT);
        if (!AiModelType.isSupported(modelType)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "不支持的模型类型: " + modelType);
        }
        if (!AiProvider.isSupported(provider)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "不支持的 Provider: " + provider);
        }
        if (AiModelType.isVector(modelType)) {
            AiDimensionValidator.validateRequired(req.getDimensions(), aiProperties.getVectorDimensions());
        }
        if (AiProvider.isOpenAiCompatible(provider)
            && !AiProvider.isOllama(provider)
            && AiApiKeyType.PLAIN.equals(normalizeApiKeyType(req.getApiKeyType()))
            && StrUtil.isBlank(req.getApiKey())
            && (old == null || StrUtil.isBlank(old.getApiKey()))) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "该厂商模型需配置 API Key");
        }
    }

    private void validateRemovable(AiModel row) {
        if (StrUtil.isNotBlank(row.getDefaultSlot())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "模型「" + row.getName() + "」为全局默认（" + row.getDefaultSlot() + "），请先清除默认再删除");
        }
        int kbRefs = modelMapper.countKbRefByModelId(row.getModelId());
        if (kbRefs > 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "模型「" + row.getName() + "」仍被 " + kbRefs + " 个知识库引用，无法删除");
        }
        int wfRefs = modelMapper.countWfRefByModelId(row.getModelId());
        if (wfRefs > 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "模型「" + row.getName() + "」仍被 " + wfRefs + " 个工作流引用，无法删除");
        }
    }

    private void validateSlotModelType(String slot, String modelType) {
        if (AiDefaultSlot.CHAT.equals(slot) || AiDefaultSlot.WORKFLOW_CHAT.equals(slot)) {
            if (!AiModelType.isLanguage(modelType)) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, slot + " 默认须为语言模型");
            }
        } else if (AiDefaultSlot.EMBEDDING.equals(slot)) {
            if (!AiModelType.isVector(modelType)) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "EMBEDDING 默认须为向量模型");
            }
        }
    }

    private void ensureCodeUnique(String code, Long excludeModelId) {
        if (StrUtil.isBlank(code)) {
            return;
        }
        AiModel existing = modelMapper.selectOne(
            Wrappers.<AiModel>lambdaQuery()
                .eq(AiModel::getCode, code.trim())
                .eq(AiModel::getDeleted, AiConstants.NOT_DELETED)
                .ne(excludeModelId != null, AiModel::getModelId, excludeModelId)
                .last("LIMIT 1")
        );
        if (existing != null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "模型编码已存在: " + code);
        }
    }

    private String normalizeApiKeyType(String valueType) {
        if (StrUtil.isBlank(valueType)) {
            return AiApiKeyType.PLAIN;
        }
        return valueType.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeSlot(String defaultSlot) {
        if (StrUtil.isBlank(defaultSlot)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "defaultSlot 不能为空");
        }
        String slot = defaultSlot.trim().toUpperCase(Locale.ROOT);
        if (!VALID_SLOTS.contains(slot)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "无效的 defaultSlot: " + defaultSlot);
        }
        return slot;
    }

    private List<AiModel> loadExportModels(List<Long> modelIds) {
        if (modelIds == null || modelIds.isEmpty()) {
            return modelMapper.selectList(
                Wrappers.<AiModel>lambdaQuery()
                    .eq(AiModel::getDeleted, AiConstants.NOT_DELETED)
                    .eq(AiModel::getStatus, AiConstants.STATUS_NORMAL)
                    .orderByAsc(AiModel::getCode)
            );
        }
        List<AiModel> models = new ArrayList<>();
        for (Long modelId : modelIds) {
            AiModel row = getById(modelId);
            if (row != null) {
                models.add(row);
            }
        }
        return models;
    }

    private String exportYaml(List<AiModel> models, boolean includeSecrets) {
        StringBuilder sb = new StringBuilder("spring:\n  ai:\n");
        Map<String, Object> openAiRoot = new LinkedHashMap<>();
        for (AiModel model : models) {
            if (AiProvider.isOllama(model.getProvider())) {
                appendOllamaYaml(sb, model, includeSecrets);
            } else if (AiProvider.isOpenAiCompatible(model.getProvider())) {
                mergeOpenAiYaml(openAiRoot, model, includeSecrets);
            }
        }
        if (!openAiRoot.isEmpty()) {
            sb.append("    openai:\n");
            appendYamlMap(sb, openAiRoot, 6);
        }
        return sb.toString();
    }

    private void mergeOpenAiYaml(Map<String, Object> root, AiModel model, boolean includeSecrets) {
        root.put("base-url", model.getBaseUrl());
        root.put("api-key", formatExportKey(model, includeSecrets));
        if (AiModelType.isLanguage(model.getModelType())) {
            Map<String, Object> chat = mapChild(root, "chat");
            chat.put("base-url", model.getBaseUrl());
            chat.put("api-key", formatExportKey(model, includeSecrets));
            if (StrUtil.isNotBlank(model.getCompletionsPath())) {
                chat.put("completions-path", model.getCompletionsPath());
            }
            Map<String, Object> options = mapChild(chat, "options");
            options.put("model", model.getModelName());
            if (model.getTemperature() != null) {
                options.put("temperature", model.getTemperature());
            }
            if (model.getMaxTokens() != null) {
                options.put("max-tokens", model.getMaxTokens());
            }
        } else {
            Map<String, Object> embedding = mapChild(root, "embedding");
            embedding.put("base-url", model.getBaseUrl());
            embedding.put("api-key", formatExportKey(model, includeSecrets));
            Map<String, Object> options = mapChild(embedding, "options");
            options.put("model", model.getModelName());
            if (model.getDimensions() != null) {
                options.put("dimensions", model.getDimensions());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapChild(Map<String, Object> parent, String key) {
        Object existing = parent.get(key);
        if (existing instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        Map<String, Object> child = new LinkedHashMap<>();
        parent.put(key, child);
        return child;
    }

    private void appendOllamaYaml(StringBuilder sb, AiModel model, boolean includeSecrets) {
        sb.append("    ollama:\n");
        sb.append("      base-url: ").append(model.getBaseUrl()).append('\n');
        if (StrUtil.isNotBlank(model.getApiKey())) {
            sb.append("      api-key: ").append(formatExportKey(model, includeSecrets)).append('\n');
        }
        sb.append("      ").append(AiModelType.isLanguage(model.getModelType()) ? "chat" : "embedding").append(":\n");
        sb.append("        options:\n");
        sb.append("          model: ").append(model.getModelName()).append('\n');
        if (model.getDimensions() != null) {
            sb.append("          dimensions: ").append(model.getDimensions()).append('\n');
        }
    }

    @SuppressWarnings("unchecked")
    private void appendYamlMap(StringBuilder sb, Map<String, Object> map, int indent) {
        String pad = " ".repeat(indent);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> child) {
                sb.append(pad).append(entry.getKey()).append(":\n");
                appendYamlMap(sb, (Map<String, Object>) child, indent + 2);
            } else {
                sb.append(pad).append(entry.getKey()).append(": ").append(value).append('\n');
            }
        }
    }

    private String exportEnv(List<AiModel> models, boolean includeSecrets) {
        StringBuilder sb = new StringBuilder();
        for (AiModel model : models) {
            String key = model.getCode().toUpperCase(Locale.ROOT).replace('-', '_') + "_API_KEY";
            sb.append(key).append('=').append(formatExportKey(model, includeSecrets)).append('\n');
        }
        return sb.toString();
    }

    private String formatExportKey(AiModel model, boolean includeSecrets) {
        if (includeSecrets) {
            String plain = AiSecretSupport.resolvePlainValue(passwordCodec, model.getApiKeyType(), model.getApiKey());
            return plain == null ? "" : plain;
        }
        if (AiApiKeyType.ENV_REF.equals(model.getApiKeyType())) {
            return "${" + model.getApiKey() + "}";
        }
        if (AiApiKeyType.SECRET.equals(model.getApiKeyType())) {
            return "${" + model.getCode().toUpperCase(Locale.ROOT).replace('-', '_') + "_API_KEY}";
        }
        return model.getApiKey();
    }
}
