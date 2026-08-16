package io.github.genkidoudou.system.internal.entity;

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

  /** 访问日志主键。 */
  @TableId(value = "info_id", type = IdType.ASSIGN_ID)
  private Long infoId;

  /** 可空，登录失败时无用户主键。 */
  private Long userId;

  /** 登录账号。 */
  private String userName;

  /** OAuth 客户端 ID。 */
  private String clientId;

  /** 登录 IP 地址。 */
  private String ipaddr;

  /** 登录地点（解析结果）。 */
  private String loginLocation;

  /** 浏览器类型。 */
  private String browser;

  /** 操作系统。 */
  private String os;

  /** 与字典 {@code sys_login_status} 一致：0 成功，1 失败。 */
  private String status;

  /** 提示消息。 */
  private String msg;

  /** 登录时间。 */
  private LocalDateTime loginTime;
}
