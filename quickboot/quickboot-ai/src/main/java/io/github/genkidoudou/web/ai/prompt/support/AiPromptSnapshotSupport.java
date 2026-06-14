package io.github.genkidoudou.web.ai.prompt.support;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.ai.prompt.domain.AiPromptContent;
import io.github.genkidoudou.web.ai.prompt.domain.AiPromptVariable;
import io.github.genkidoudou.web.ai.prompt.dto.AiPromptVariableBo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 提示词快照 JSON 构建与解析：sections + variables ↔ snapshot_json。
 */
public final class AiPromptSnapshotSupport {

    /** 快照 JSON 中内容段键名。 */
    public static final String KEY_SECTIONS = "sections";

    /** 快照 JSON 中变更摘要键名。 */
    public static final String KEY_CHANGE_SUMMARY = "changeSummary";

    /** 优化失败时保留原始模型输出的键名。 */
    public static final String KEY_RAW_TEXT = "rawText";

    private AiPromptSnapshotSupport() {
    }

    /**
     * 由内容段与变量列表构建快照 JSON 字符串。
     *
     * @param sections  内容段
     * @param variables 变量声明
     * @return JSON 字符串
     */
    public static String buildSnapshotJson(Map<String, String> sections, List<AiPromptVariableBo> variables) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("sections", sections == null ? Map.of() : new LinkedHashMap<>(sections));
        snapshot.put("variables", variables == null ? List.of() : variables);
        return JSONUtil.toJsonStr(snapshot);
    }

    /**
     * 由实体行构建快照 JSON。
     *
     * @param contents  内容段实体列表
     * @param variables 变量实体列表
     * @return JSON 字符串
     */
    public static String buildSnapshotJsonFromEntities(List<AiPromptContent> contents, List<AiPromptVariable> variables) {
        Map<String, String> sections = new LinkedHashMap<>();
        if (contents != null) {
            for (AiPromptContent row : contents) {
                sections.put(row.getSectionKey(), row.getContent());
            }
        }
        List<AiPromptVariableBo> varBos = new ArrayList<>();
        if (variables != null) {
            for (AiPromptVariable row : variables) {
                varBos.add(toVariableBo(row));
            }
        }
        return buildSnapshotJson(sections, varBos);
    }

    /**
     * 解析快照 JSON 为内容段 Map。
     *
     * @param snapshotJson 快照 JSON
     * @return 内容段；解析失败时返回空 Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> parseSections(String snapshotJson) {
        Map<String, String> sections = new LinkedHashMap<>();
        if (StrUtil.isBlank(snapshotJson) || !JSONUtil.isTypeJSON(snapshotJson)) {
            return sections;
        }
        Object sectionsObj = JSONUtil.parseObj(snapshotJson).get("sections");
        if (sectionsObj instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                sections.put(String.valueOf(entry.getKey()), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
            }
        }
        return sections;
    }

    /**
     * 解析快照 JSON 为变量声明列表。
     *
     * @param snapshotJson 快照 JSON
     * @return 变量列表；解析失败时返回空列表
     */
    public static List<AiPromptVariableBo> parseVariables(String snapshotJson) {
        List<AiPromptVariableBo> variables = new ArrayList<>();
        if (StrUtil.isBlank(snapshotJson) || !JSONUtil.isTypeJSON(snapshotJson)) {
            return variables;
        }
        Object variablesObj = JSONUtil.parseObj(snapshotJson).get("variables");
        if (!(variablesObj instanceof List<?> list)) {
            return variables;
        }
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                AiPromptVariableBo bo = BeanUtil.toBean(map, AiPromptVariableBo.class);
                if (StrUtil.isNotBlank(bo.getVarKey())) {
                    variables.add(bo);
                }
            }
        }
        return variables;
    }

    /**
     * 实体转变量 Bo。
     *
     * @param row 变量实体
     * @return Bo
     */
    public static AiPromptVariableBo toVariableBo(AiPromptVariable row) {
        AiPromptVariableBo bo = new AiPromptVariableBo();
        bo.setVarKey(row.getVarKey());
        bo.setVarType(row.getVarType());
        bo.setRequired(row.getRequired());
        bo.setDescription(row.getDescription());
        bo.setSort(row.getSort());
        return bo;
    }

    /**
     * Bo 转变量实体（用于草稿写入）。
     *
     * @param promptId  提示词 ID
     * @param versionId 版本 ID（0 表示草稿）
     * @param bo        变量 Bo
     * @return 变量实体
     */
    public static AiPromptVariable toVariableEntity(Long promptId, Long versionId, AiPromptVariableBo bo) {
        AiPromptVariable entity = new AiPromptVariable();
        entity.setPromptId(promptId);
        entity.setVersionId(versionId);
        entity.setVarKey(bo.getVarKey());
        entity.setVarType(StrUtil.blankToDefault(bo.getVarType(), "string"));
        entity.setRequired(bo.getRequired() != null && bo.getRequired() == 1 ? 1 : 0);
        entity.setDescription(StrUtil.nullToEmpty(bo.getDescription()));
        entity.setSort(bo.getSort() == null ? 0 : bo.getSort());
        return entity;
    }
}
