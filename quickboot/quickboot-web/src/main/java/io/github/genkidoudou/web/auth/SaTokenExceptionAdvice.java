package io.github.genkidoudou.web.auth;

import cn.dev33.satoken.exception.NotLoginException;
import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.api.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 将 Sa-Token 未登录异常映射为统一响应 {@link HttpCodes#UNAUTHORIZED}，供前端按业务码 401 处理。
 */
@RestControllerAdvice
public class SaTokenExceptionAdvice {

    /**
     * @param e Sa-Token 未登录或 token 无效
     * @return 业务码 401，HTTP 仍为 200（与项目统一响应约定一致）
     */
    @ExceptionHandler(NotLoginException.class)
    public R<Void> onNotLogin(NotLoginException e) {
        return R.error(HttpCodes.UNAUTHORIZED, "未登录或登录已过期");
    }
}
