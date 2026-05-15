package io.github.genkidoudou.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.core.R;
import io.github.genkidoudou.common.i18n.I18nUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

import java.io.IOException;

@UtilityClass
public class ServletUtils {

    /**
     * 写响应
     *
     * @param response 响应
     * @param code     错误编码
     * @param args     参数
     * @return
     * @since 2026/3/7
     */
    public static void writeResponse(HttpServletResponse response, Integer code, Object... args) {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        String message = I18nUtil.getMessage(code + "", args);
        R<Object> error = R.error(code, message);
        try {
            response.getWriter().write(objectMapper.writeValueAsString(error));
            response.getWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
