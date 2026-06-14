package io.github.genkidoudou.web.workflow.engine;

import cn.hutool.core.util.StrUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 循环中间变量类型兼容性校验（对齐扣子「设置值类型须与中间变量一致」）。
 */
public final class LoopVariableTypeChecker {

    private LoopVariableTypeChecker() {
    }

    /**
     * @param expectedType 中间变量声明类型
     * @param value        待写入值（已渲染）
     * @return 是否兼容
     */
    public static boolean isCompatible(String expectedType, Object value) {
        if (StrUtil.isBlank(expectedType) || "any".equalsIgnoreCase(expectedType)) {
            return true;
        }
        String type = expectedType.trim().toLowerCase();
        if (value == null) {
            return true;
        }
        return switch (type) {
            case "string" -> value instanceof String || value instanceof CharSequence;
            case "number" -> value instanceof Number;
            case "boolean" -> value instanceof Boolean;
            case "array" -> value instanceof List || value instanceof Collection || value.getClass().isArray();
            case "object" -> value instanceof Map;
            default -> true;
        };
    }

    /**
     * @param expectedType 期望类型
     * @param value          实际值
     * @return 失败描述；兼容时返回 null
     */
    public static String incompatibilityMessage(String expectedType, Object value) {
        if (isCompatible(expectedType, value)) {
            return null;
        }
        return "中间变量类型为 " + expectedType + "，与设置值类型 "
            + (value == null ? "null" : value.getClass().getSimpleName()) + " 不兼容";
    }
}
