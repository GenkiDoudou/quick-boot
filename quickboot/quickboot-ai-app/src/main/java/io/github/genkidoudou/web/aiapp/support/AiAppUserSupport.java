package io.github.genkidoudou.web.aiapp.support;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * AI 应用当前用户标识解析。
 */
@Component
public class AiAppUserSupport {

    /**
     * 获取当前登录用户 ID 字符串；未登录时返回 anonymous。
     *
     * @return userKey
     */
    public String currentUserKey() {
        if (StpUtil.isLogin()) {
            return String.valueOf(StpUtil.getLoginId());
        }
        return "anonymous";
    }
}
