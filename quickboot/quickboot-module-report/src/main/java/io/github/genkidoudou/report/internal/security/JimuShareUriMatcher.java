package io.github.genkidoudou.report.internal.security;

/**
 * 积木分享/预览 URI 判定（与社区集成实践一致）。
 */
public final class JimuShareUriMatcher {

    private JimuShareUriMatcher() {
    }

    /**
     * 判断 URI 是否为积木分享/预览路径（无需登录态）。
     *
     * @param uri 请求路径
     * @return 命中分享/预览路径时 {@code true}
     */
    public static boolean isShareUri(String uri) {
        if (uri == null || uri.isEmpty()) {
            return false;
        }
        return uri.contains("/jmreport/view/")
                || uri.contains("/jimureport/share/view/")
                || uri.contains("/drag/share/view/")
                || uri.contains("/drag/share/")
                || uri.contains("/drag/preview/")
                || uri.contains("/drag/view")
                || uri.contains("/drag/page/view");
    }

    /**
     * 判断查询串是否携带积木分享 token 参数。
     *
     * @param queryString URL 查询串
     * @return 含 {@code shareToken} 时 {@code true}
     */
    public static boolean hasShareTokenParam(String queryString) {
        return queryString != null && queryString.contains("shareToken");
    }
}
