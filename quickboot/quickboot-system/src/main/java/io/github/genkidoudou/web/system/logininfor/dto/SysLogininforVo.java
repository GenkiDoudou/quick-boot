package io.github.genkidoudou.web.system.logininfor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志列表展示模型。
 */
@Data
@Schema(description = "登录日志行")
public class SysLogininforVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long infoId;
    private Long userId;
    private String userName;
    private String ipaddr;
    private String loginLocation;
    private String browser;
    private String os;
    private String status;
    private String msg;
    private LocalDateTime loginTime;
}
