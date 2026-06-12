package io.github.genkidoudou.web.workflow.handler;



import cn.hutool.core.util.StrUtil;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;

import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;

import io.github.genkidoudou.web.workflow.engine.NodeHandler;

import io.github.genkidoudou.web.workflow.engine.NodeResult;

import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;

import io.github.genkidoudou.web.workflow.engine.WorkflowContext;

import io.github.genkidoudou.web.workflow.stream.WorkflowStreamEmitter;

import io.github.genkidoudou.web.workflow.util.JsonDeepParseUtil;

import org.springframework.stereotype.Component;



import java.util.HashMap;

import java.util.List;

import java.util.Map;

import java.util.regex.Matcher;

import java.util.regex.Pattern;



/**

 * 输出（answer）节点：{@code outputMode} 互斥——variables 返回 JSON 结构，text 返回一段话。

 */

@Component

public class AnswerNodeHandler implements NodeHandler {



    private static final String MODE_TEXT = "text";

    private static final String MODE_VARIABLES = "variables";



    /** 整段模板仅为单个占位符时保留结构化值，避免 Object 被 stringify 后再 JSON 转义 */
    private static final Pattern SINGLE_PLACEHOLDER = Pattern.compile("^\\{\\{([^}]+)}}\\s*$");



    private final TemplateRenderer templateRenderer;

    private final WorkflowStreamEmitter streamEmitter;



    public AnswerNodeHandler(TemplateRenderer templateRenderer, WorkflowStreamEmitter streamEmitter) {

        this.templateRenderer = templateRenderer;

        this.streamEmitter = streamEmitter;

    }



    @Override

    public String type() {

        return WfNodeType.ANSWER;

    }



    @Override

    @SuppressWarnings("unchecked")

    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {

        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();

        String outputMode = resolveOutputMode(data);



        if (MODE_TEXT.equals(outputMode)) {

            return NodeResult.success(executeTextMode(node, data, context));

        }

        return NodeResult.success(executeVariablesMode(data, context));

    }



    /**

     * 返回文本：渲染模板为一段话，不包装 JSON。

     */

    private Map<String, Object> executeTextMode(GraphNodeDto node, Map<String, Object> data, WorkflowContext context) {

        Map<String, Object> outputs = new HashMap<>();

        String outputTemplate = data.get("output") == null ? "" : String.valueOf(data.get("output"));

        if (StrUtil.isNotBlank(outputTemplate)) {

            String rendered = templateRenderer.render(outputTemplate, context);

            outputs.put("text", rendered);

            boolean streaming = Boolean.TRUE.equals(data.get("streaming")) && context.isStreamEnabled();

            if (streaming && StrUtil.isNotBlank(rendered)) {

                streamEmitter.emitLlmDelta(context.getRunId(), node.getId(), rendered, rendered);

            }

        }

        if (data.containsKey("citations") && data.get("citations") != null) {

            String citationsTpl = String.valueOf(data.get("citations"));

            if (StrUtil.isNotBlank(citationsTpl)) {

                outputs.put("citations", templateRenderer.render(citationsTpl, context));

            }

        }

        return outputs;

    }



    /**

     * 返回变量：映射为结构化 JSON 字段，不写入 text 镜像。

     */

    @SuppressWarnings("unchecked")

    private Map<String, Object> executeVariablesMode(Map<String, Object> data, WorkflowContext context) {

        Map<String, Object> outputs = new HashMap<>();

        Object outputVariablesObj = data.get("outputVariables");

        if (outputVariablesObj instanceof List<?> list) {

            for (Object item : list) {

                if (!(item instanceof Map<?, ?> row)) {

                    continue;

                }

                String key = row.get("key") == null ? "" : String.valueOf(row.get("key")).trim();

                String valueTpl = row.get("value") == null ? "" : String.valueOf(row.get("value"));

                if (StrUtil.isBlank(key) || StrUtil.isBlank(valueTpl)) {

                    continue;

                }

                outputs.put(key, resolveOutputValue(valueTpl, context));

            }

        }

        return outputs;

    }



    /**

     * 解析输出模式；未配置时按字段推断，默认 variables。

     */

    private String resolveOutputMode(Map<String, Object> data) {

        Object mode = data.get("outputMode");

        if (MODE_TEXT.equals(mode)) {

            return MODE_TEXT;

        }

        if (MODE_VARIABLES.equals(mode)) {

            return MODE_VARIABLES;

        }

        String outputTemplate = data.get("output") == null ? "" : String.valueOf(data.get("output"));

        if (StrUtil.isNotBlank(outputTemplate)) {

            return MODE_TEXT;

        }

        return MODE_VARIABLES;

    }



    /**

     * 解析输出变量值：纯占位符保留 Map/List 等结构；混合模板仍走字符串渲染。

     */

    private Object resolveOutputValue(String valueTpl, WorkflowContext context) {

        if (StrUtil.isBlank(valueTpl)) {

            return "";

        }

        Matcher matcher = SINGLE_PLACEHOLDER.matcher(valueTpl.trim());

        if (matcher.matches()) {

            Object resolved = templateRenderer.resolveObject(matcher.group(1).trim(), context);

            if (resolved != null) {

                return JsonDeepParseUtil.deepParse(resolved);

            }

            return "";

        }

        return templateRenderer.render(valueTpl, context);

    }

}

