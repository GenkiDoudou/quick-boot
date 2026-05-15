package io.github.genkidoudou.web.system.user.datascope;

/**
 * 表名与 {@link DataPermission#tables()} 模式匹配（旧 quick-boot 同款语义）。
 */
public final class TableMatchUtil {

    private TableMatchUtil() {
    }

    /**
     * @param actual   SQL 中的表名
     * @param patterns 注解声明的模式
     * @return 是否命中
     */
    public static boolean match(String actual, String[] patterns) {
        if (actual == null || patterns == null) {
            return false;
        }
        for (String p : patterns) {
            if (p == null || p.isBlank()) {
                continue;
            }
            if (p.contains("*")) {
                if (actual.matches(p.replace("*", ".*"))) {
                    return true;
                }
            } else if (actual.equalsIgnoreCase(p)) {
                return true;
            }
        }
        return false;
    }
}
