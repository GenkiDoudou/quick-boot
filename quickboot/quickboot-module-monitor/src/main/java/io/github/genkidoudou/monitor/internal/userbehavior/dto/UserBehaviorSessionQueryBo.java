package io.github.genkidoudou.monitor.internal.userbehavior.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户行为会话查询条件。
 */
@Data
@Schema(description = "用户行为会话查询")
public class UserBehaviorSessionQueryBo {
  /** 用户标识（登录名或 userId） */
  private String uin;
  /** 用户名，与 uin 二选一 */
  private String userName;
  /** 会话 ID，精确匹配 */
  private String sessionId;
  /** 开始时间 yyyy-MM-dd HH:mm:ss */
  private String beginTime;
  /** 结束时间 yyyy-MM-dd HH:mm:ss */
  private String endTime;
  /** 返回会话条数上限，默认 50，最大 200 */
  private Integer limit = 50;
}
