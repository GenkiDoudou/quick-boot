package io.github.genkidoudou.config;

import cn.dev33.satoken.exception.NotLoginException;
import io.github.genkidoudou.common.core.GlobalMsgCode;
import io.github.genkidoudou.common.core.R;
import io.github.genkidoudou.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(NotLoginException.class)
    public R<Void> handlerException(NotLoginException e) {
        log.error("未登录", e);
        return R.error(401, "未登录");
    }

    @ExceptionHandler(BaseException.class)
    public R<Void> handleBaseException(BaseException e) {
        int code = e.getCode() != null ? e.getCode() : GlobalMsgCode.BAD_REQUEST;
        String msg = e.getMsg() != null ? e.getMsg() : e.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = "请求处理失败";
        }
        log.warn("业务异常: {}", msg);
        return R.error(code, msg);
    }

    // 全局异常拦截
    @ExceptionHandler
    public R<Void> handlerException(Exception e) {
        log.error("系统异常,请联系管理员", e);
        return R.error(500, "系统异常,请联系管理员");
    }
}