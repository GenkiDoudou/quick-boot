package io.github.genkidoudou.web.knowledge.mcp.runtime;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import org.springframework.ai.tool.ToolCallback;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 部分模型在思考模式或 Tool Calls 兼容异常时，会在正文输出 {@code <function_calls>/<invoke>} 伪 XML，
 * 而非 OpenAI 格式的 {@code tool_calls}。本类解析该文本并手动触发 {@link ToolCallback}。
 */
public final class McpTextualToolCallSupport {

    private static final Pattern INVOKE_BLOCK = Pattern.compile(
        "<invoke\\s+name=\"([^\"]+)\">\\s*([\\s\\S]*?)\\s*</invoke>",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern PARAM_BLOCK = Pattern.compile(
        "<parameter\\s+name=\"([^\"]+)\">\\s*([\\s\\S]*?)\\s*</parameter>",
        Pattern.CASE_INSENSITIVE);

    /**
     * 解析出的文本式工具调用。
     *
     * @param toolName   工具名（可能与 MCP 前缀名一致，如 {@code JavaSDKMCPClient_bing_search}）
     * @param arguments  参数键值
     */
    public record ParsedCall(String toolName, Map<String, Object> arguments) {
    }

    private McpTextualToolCallSupport() {
    }

    /**
     * 尝试从模型正文中解析 XML 式工具调用。
     *
     * @param text 模型输出
     * @return 解析结果；无法识别时为空
     */
    public static Optional<ParsedCall> tryParse(String text) {
        if (StrUtil.isBlank(text) || !text.contains("<invoke")) {
            return Optional.empty();
        }
        Matcher invokeMatcher = INVOKE_BLOCK.matcher(text);
        if (!invokeMatcher.find()) {
            return Optional.empty();
        }
        String toolName = invokeMatcher.group(1).trim();
        String body = invokeMatcher.group(2);
        Map<String, Object> args = new LinkedHashMap<>();
        Matcher paramMatcher = PARAM_BLOCK.matcher(body);
        while (paramMatcher.find()) {
            args.put(paramMatcher.group(1).trim(), paramMatcher.group(2).trim());
        }
        if (StrUtil.isBlank(toolName)) {
            return Optional.empty();
        }
        return Optional.of(new ParsedCall(toolName, args));
    }

    /**
     * 按名称匹配并执行工具（支持 MCP 前缀名与短名互匹配）。
     *
     * @param callbacks 已包装追踪的回调数组
     * @param call      解析出的调用
     * @return 工具返回字符串
     */
    public static String invoke(ToolCallback[] callbacks, ParsedCall call) {
        ToolCallback callback = findCallback(callbacks, call.toolName());
        if (callback == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "未找到 MCP 工具: " + call.toolName());
        }
        String input = call.arguments().isEmpty() ? "{}" : JSONUtil.toJsonStr(call.arguments());
        return callback.call(input);
    }

    private static ToolCallback findCallback(ToolCallback[] callbacks, String requestedName) {
        if (callbacks == null || callbacks.length == 0 || StrUtil.isBlank(requestedName)) {
            return null;
        }
        String normalized = requestedName.trim();
        ToolCallback exact = null;
        ToolCallback suffix = null;
        for (ToolCallback callback : callbacks) {
            if (callback == null || callback.getToolDefinition() == null) {
                continue;
            }
            String registered = callback.getToolDefinition().name();
            if (StrUtil.isBlank(registered)) {
                continue;
            }
            if (registered.equals(normalized)) {
                exact = callback;
                break;
            }
            if (registered.endsWith("_" + normalized) || normalized.endsWith("_" + registered)) {
                suffix = callback;
            }
        }
        return exact != null ? exact : suffix;
    }
}
