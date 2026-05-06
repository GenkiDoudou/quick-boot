package io.github.genkidoudou.common.security.firewall.xss;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 编译内置与配置的 XSS 启发式规则；命中时返回首个匹配的规则标识（供日志）。
 * <p>
 * <b>内置规则清单</b>（均带 {@link Pattern#CASE_INSENSITIVE} 与 {@link Pattern#DOTALL}）：
 * </p>
 * <ul>
 *     <li>{@code builtin:script-tag} — {@code &lt;script}</li>
 *     <li>{@code builtin:javascript-protocol} — {@code javascript:}</li>
 *     <li>{@code builtin:event-handler} — {@code on*} 事件写法（{@code on…=}）</li>
 *     <li>{@code builtin:danger-tags} — {@code iframe/object/embed/svg} 起始标签</li>
 *     <li>{@code builtin:expression} — {@code expression(}</li>
 *     <li>{@code builtin:data-html} — {@code data:text/html}</li>
 *     <li>{@code builtin:eval} — {@code eval(}</li>
 *     <li>{@code builtin:document} — {@code document.}</li>
 *     <li>{@code builtin:window} — {@code window.}</li>
 *     <li>{@code builtin:alert} — {@code alert(}</li>
 * </ul>
 */
public final class XssFirewallRuleSet {

    private final List<CompiledRule> rules;

    /**
     * @param properties 配置；{@code customPatterns} 中非法正则将抛出 {@link IllegalArgumentException}
     */
    public XssFirewallRuleSet(XssFirewallProperties properties) {
        List<CompiledRule> list = new ArrayList<>();
        int flags = Pattern.CASE_INSENSITIVE | Pattern.DOTALL;
        for (BuiltinRule br : BuiltinRule.values()) {
            list.add(new CompiledRule(br.ruleId(), Pattern.compile(br.pattern(), flags)));
        }
        List<String> customs = properties.getCustomPatterns();
        if (customs != null) {
            for (int i = 0; i < customs.size(); i++) {
                String pat = customs.get(i);
                if (pat == null || pat.isBlank()) {
                    continue;
                }
                try {
                    list.add(new CompiledRule("custom:" + i, Pattern.compile(pat, flags)));
                } catch (PatternSyntaxException e) {
                    throw new IllegalArgumentException(
                            "qc.security.firewall.xss.custom-patterns[" + i + "] 非法正则: " + e.getMessage(), e);
                }
            }
        }
        this.rules = List.copyOf(list);
    }

    /**
     * @param value 待检测字符串
     * @return 首个命中的规则 id；无命中则 empty
     */
    public Optional<XssMatch> matchFirst(String value) {
        if (value == null || value.isEmpty()) {
            return Optional.empty();
        }
        for (CompiledRule r : rules) {
            if (r.pattern.matcher(value).find()) {
                return Optional.of(new XssMatch(r.id));
            }
        }
        return Optional.empty();
    }

    private enum BuiltinRule {
        SCRIPT_TAG("builtin:script-tag", "<script"),
        JAVASCRIPT_PROTOCOL("builtin:javascript-protocol", "javascript:"),
        EVENT_HANDLER("builtin:event-handler", "\\bon[a-z]+\\s*="),
        DANGER_TAGS("builtin:danger-tags", "<\\s*(iframe|object|embed|svg)\\b"),
        EXPRESSION("builtin:expression", "expression\\s*\\("),
        DATA_HTML("builtin:data-html", "data:text/html"),
        EVAL("builtin:eval", "eval\\s*\\("),
        DOCUMENT("builtin:document", "document\\."),
        WINDOW("builtin:window", "window\\."),
        ALERT("builtin:alert", "alert\\s*\\(");

        private final String ruleId;
        private final String pattern;

        BuiltinRule(String ruleId, String pattern) {
            this.ruleId = ruleId;
            this.pattern = pattern;
        }

        String ruleId() {
            return ruleId;
        }

        String pattern() {
            return pattern;
        }
    }

    private record CompiledRule(String id, Pattern pattern) {
    }

    /**
     * @param ruleId 内置 {@code builtin:…} 或 {@code custom:n}
     */
    public record XssMatch(String ruleId) {
    }
}
