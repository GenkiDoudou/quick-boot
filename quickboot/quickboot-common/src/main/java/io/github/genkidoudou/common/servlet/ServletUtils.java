package io.github.genkidoudou.common.servlet;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.i18n.I18nUtil;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Servlet 层写出统一 JSON 错误的工具，供 Filter / 防火墙等在未进入 MVC 前返回与 {@link R} 契约一致的响应体。
 * <p>
 * <b>业务码与 i18n 键（选项 A）</b>：{@code Integer code} 既作为 {@code R.code}，也经 {@code String.valueOf(code)}
 * 作为 {@link I18nUtil} / {@code MessageSource} 的词条键，请在 {@code messages*.properties} 中为该数字字符串维护文案。
 * <p>
 * <b>Locale</b>：文案使用 {@link org.springframework.context.i18n.LocaleContextHolder}；若在设置 Locale 的
 * Filter（如 {@code LocaleResolver}）<b>之前</b>调用，可能始终得到默认语言——请通过 Filter {@code @Order} 保证顺序。
 * <p>
 * <b>响应已提交</b>：若 {@link HttpServletResponse#isCommitted()} 为 {@code true}，本方法<b>静默返回</b>且不写出任何内容
 * （避免重复提交或输出流异常）；调用方应避免在已提交响应上误调。
 * <p>
 * <b>ObjectMapper</b>：优先从 Spring 容器取 {@link ObjectMapper} 以与全局 Jackson 配置对齐；取不到时使用
 * {@code new ObjectMapper()}，可能与应用主配置存在细微差异（详见实现与单测）。
 */
public final class ServletUtils {

    /** 与验收要求一致的 JSON Content-Type（含 UTF-8 声明）。 */
    public static final String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=UTF-8";

    private ServletUtils() {
    }

    /**
     * 写出 HTTP 200 + {@code R.error} JSON；HTTP 状态固定 200，错误类型由响应体 {@code code} 区分。
     *
     * @param response HTTP 响应，不可为 {@code null}
     * @param code     业务错误码；若为 {@code null} 则兜底为 {@link HttpCodes#INTERNAL_ERROR}（500），词条键同步为 {@code "500"}
     * @param args     国际化占位参数（变参）；无参时可省略。将按原样转为 {@code Object[]} 传入 {@link I18nUtil#getMessage(String, Object[])}
     * @throws IOException 序列化或写出失败时
     */
    public static void writeResponse(HttpServletResponse response, Integer code, Object... args) throws IOException {
        writeResponse(response, code, null, args);
    }

    /**
     * 写出 HTTP 200 + {@code R.error} JSON，且支持在 i18n 词条未命中时使用兜底文案。
     * <p>
     * 词条键仍为 {@code String.valueOf(code)}；当 i18n 未命中（或 MessageSource 不可用）时，若 {@code fallbackMessage}
     * 非空则使用该文案，否则回退为词条键本身。
     *
     * @param response        HTTP 响应，不可为 {@code null}
     * @param code            业务错误码；若为 {@code null} 则兜底为 {@link HttpCodes#INTERNAL_ERROR}
     * @param fallbackMessage i18n 未命中时的兜底文案（可为 {@code null}）
     * @param args            国际化占位参数（变参）
     * @throws IOException 序列化或写出失败时
     */
    public static void writeResponse(HttpServletResponse response, Integer code, String fallbackMessage, Object... args)
            throws IOException {
        if (response == null) {
            throw new IllegalArgumentException("response must not be null");
        }
        if (response.isCommitted()) {
            return;
        }

        int biz = code != null ? code : HttpCodes.INTERNAL_ERROR;
        String messageKey = String.valueOf(biz);
        Object[] argArray = (args == null || args.length == 0) ? null : args;
        String msg = I18nUtil.getMessage(messageKey, argArray, fallbackMessage);
        R<Void> body = R.error(biz, msg);

        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(CONTENT_TYPE_JSON_UTF8);

        ObjectMapper mapper = resolveObjectMapper();
        mapper.writeValue(response.getWriter(), body);
        response.getWriter().flush();
    }

    private static ObjectMapper resolveObjectMapper() {
        try {
            return SpringUtil.getBean(ObjectMapper.class);
        } catch (Throwable ignored) {
            return new ObjectMapper();
        }
    }
}
