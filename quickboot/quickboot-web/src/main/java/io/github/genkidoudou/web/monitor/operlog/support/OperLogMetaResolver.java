package io.github.genkidoudou.web.monitor.operlog.support;



import cn.hutool.core.util.StrUtil;

import io.github.genkidoudou.common.monitor.operlog.OperLogBusinessType;

import io.github.genkidoudou.common.monitor.operlog.OperLogMeta;

import io.github.genkidoudou.common.monitor.operlog.OperLogPublishingAspect;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;



import java.lang.reflect.Method;



/**

 * 从 {@link OperLogMeta}、OpenAPI 注解或请求语义解析操作日志标题与类型。

 */

public final class OperLogMetaResolver {



    private OperLogMetaResolver() {

    }



    /**

     * @param method        当前执行方法

     * @param declaringType 声明该方法的类型（用于读取类上 {@link Tag}）

     * @return 非 {@code null}

     */

    public static ResolvedOperLogMeta resolve(Method method, Class<?> declaringType) {

        String requestMethod = OperLogPublishingAspect.currentRequestMethod();

        String requestUri = OperLogPublishingAspect.currentRequestUri();



        OperLogMeta meta = method.getAnnotation(OperLogMeta.class);

        if (meta != null) {

            String title = StrUtil.blankToDefault(meta.title(), defaultTitle(method, declaringType));

            int businessType = meta.businessType() != OperLogBusinessType.OTHER

                ? meta.businessType()

                : inferBusinessType(method, requestMethod, requestUri);

            return new ResolvedOperLogMeta(title, businessType, meta.operatorType());

        }

        Tag tag = declaringType.getAnnotation(Tag.class);

        Operation op = method.getAnnotation(Operation.class);

        StringBuilder sb = new StringBuilder();

        if (tag != null && StrUtil.isNotBlank(tag.name())) {

            sb.append(tag.name().trim());

        }

        if (op != null && StrUtil.isNotBlank(op.summary())) {

            if (!sb.isEmpty()) {

                sb.append('-');

            }

            sb.append(op.summary().trim());

        }

        String title = sb.isEmpty() ? defaultTitle(method, declaringType) : sb.toString();

        int businessType = inferBusinessType(method, requestMethod, requestUri);

        return new ResolvedOperLogMeta(title, businessType, 1);

    }



    /**

     * 根据 HTTP 方法与路径/方法名推断业务类型（与若依 {@code BusinessType} 对齐）。

     */

    static int inferBusinessType(Method method, String httpMethod, String requestUri) {

        if (!"POST".equalsIgnoreCase(httpMethod)) {

            return OperLogBusinessType.OTHER;

        }

        String uri = requestUri == null ? "" : requestUri.toLowerCase();

        String name = method.getName().toLowerCase();

        if (matchesMutation(name, uri, "export")) {

            return OperLogBusinessType.EXPORT;

        }

        if (matchesMutation(name, uri, "import")) {

            return OperLogBusinessType.IMPORT;

        }

        if (matchesMutation(name, uri, "remove", "delete", "clean")) {

            return OperLogBusinessType.DELETE;

        }

        if (matchesMutation(name, uri, "create", "add")) {

            return OperLogBusinessType.INSERT;

        }

        if (matchesMutation(name, uri, "update", "edit", "change", "reset", "grant", "auth", "save", "run", "sync")) {

            return OperLogBusinessType.UPDATE;

        }

        return OperLogBusinessType.OTHER;

    }



    private static boolean matchesMutation(String methodName, String uri, String... keywords) {

        for (String keyword : keywords) {

            if (methodName.contains(keyword) || uri.contains("/" + keyword)) {

                return true;

            }

        }

        return false;

    }



    private static String defaultTitle(Method method, Class<?> declaringType) {

        return declaringType.getSimpleName() + "." + method.getName();

    }



    /**

     * 解析结果：标题、业务类型、操作者类别。

     */

    public record ResolvedOperLogMeta(String title, int businessType, int operatorType) {

    }

}


