package io.github.genkidoudou.report.internal.security;

/**
 * 积木分享/预览 URI 判定（与社区集成实践一致）。
 */
public final class JimuShareUriMatcher {

    private JimuShareUriMatcher() {
    }

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

    public static boolean hasShareTokenParam(String queryString) {
        return queryString != null && queryString.contains("shareToken");
    }
}
