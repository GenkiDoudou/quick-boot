package io.github.genkidoudou.web.system.logininfor.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录访问日志，与表 {@code sys_logininfor} 对应。
 */
@Data
@TableName("sys_logininfor")
public class SysLogininfor implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "info_id", type = IdType.ASSIGN_ID)
    private Long infoId;

    /** 可空，登录失败时无用户主键。 */
    private Long userId;

    private String userName;

    private String ipaddr;

    private String loginLocation;

    private String browser;

    private String os;

    /** 与字典 {@code sys_login_status} 一致：0 成功，1 失败。 */
    private String status;

    private String msg;

    /** OAuth 客户端 ID，来自 {@code X-Client-Id}，可空。 */
    private String clientId;

    private LocalDateTime loginTime;
}
