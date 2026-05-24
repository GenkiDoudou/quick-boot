package io.github.genkidoudou.common.i18n;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 业务码内置中文文案：在 {@link MessageSource} 不可用、词条未注册或解析结果仅为数字键时的兜底。
 * <p>
 * 与 {@code i18n/messages_zh_CN.properties} 保持同步；修改词条时请一并更新本类。
 */
public final class BizMessages {

    private static final String GENERIC = "操作失败，请稍后再试";

    private static final Map<String, String> ZH = buildZh();

    private BizMessages() {
    }

    /**
     * @param code 业务码字符串键
     * @return 内置中文文案，未注册时返回 {@code null}
     */
    public static String get(String code) {
        return ZH.get(code);
    }

    /**
     * 按优先级合并文案：MessageSource 有效结果 &gt; 内置词条 &gt; 调用方显式兜底 &gt; 通用兜底。
     * <p>
     * 调用方已提供非空 {@code explicitFallback} 时，请优先在 {@link I18nUtil#getMessage(String, Object[], String)} 层处理。
     *
     * @param code               业务码字符串键
     * @param args               MessageFormat 占位参数
     * @param fromMessageSource  已从 MessageSource 取到的文案（可为 {@code null}）
     * @param explicitFallback   调用方提供的兜底（可为 {@code null}）
     * @return 最终展示文案，永不为 {@code null} 或空白（末级为 {@link #GENERIC}）
     */
    public static String resolve(String code, Object[] args, String fromMessageSource, String explicitFallback) {
        if (isMeaningful(code, fromMessageSource)) {
            return fromMessageSource;
        }
        String builtin = ZH.get(code);
        if (builtin != null) {
            return format(builtin, args);
        }
        if (explicitFallback != null && !explicitFallback.isBlank()) {
            return explicitFallback;
        }
        return GENERIC;
    }

    /**
     * 判断文案是否可作为最终展示（非空且不等于纯数字业务码键）。
     */
    public static boolean isMeaningful(String code, String msg) {
        if (msg == null || msg.isBlank()) {
            return false;
        }
        return !msg.equals(code);
    }

    private static String format(String pattern, Object[] args) {
        if (args == null || args.length == 0) {
            return pattern;
        }
        try {
            return MessageFormat.format(pattern, args);
        } catch (Exception ignored) {
            return pattern;
        }
    }

    private static Map<String, String> buildZh() {
        Map<String, String> m = new HashMap<>();
        m.put("200", "操作成功");
        m.put("400", "请求参数错误");
        m.put("401", "未登录或登录已过期");
        m.put("403", "无权限访问");
        m.put("404", "访问资源不存在");
        m.put("500", "内部服务器错误");
        m.put("503", "服务暂不可用");
        m.put("10001", "参数不合法");
        m.put("10002", "请求体格式错误");
        m.put("20001", "当前状态不允许执行该操作");
        m.put("20010", "表已导入");
        m.put("20011", "生成配置或数据不存在");
        m.put("20012", "SQL 不合法");
        m.put("20013", "请选择要导入的表");
        m.put("20014", "树表模板尚未开放");
        m.put("20015", "自定义路径不支持");
        m.put("20020", "Cron 表达式不正确");
        m.put("20021", "调用目标不存在");
        m.put("20022", "调用目标须实现 ITask 接口");
        m.put("20023", "任务未在调度器中");
        m.put("30001", "请求已被限流，请稍后再试");
        m.put("30002", "Client 签名校验失败");
        m.put("30201", "请求过于频繁，请稍后重试");
        m.put("30401", "请求方式不允许");
        m.put("30402", "Host 不允许");
        m.put("30501", "内容包含敏感词：{0}");
        m.put("30601", "请求参数包含非法字符");
        m.put("30701", "请求参数包含非法脚本");
        m.put("40000", "系统繁忙，请稍后再试");
        m.put("40001", "依赖服务暂不可用");
        return Collections.unmodifiableMap(m);
    }
}
