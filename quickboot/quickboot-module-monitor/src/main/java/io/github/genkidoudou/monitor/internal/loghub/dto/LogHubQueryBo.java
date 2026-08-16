package io.github.genkidoudou.monitor.internal.loghub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 日志中心查询条件。
 */
@Data
@Schema(description = "日志中心查询")
public class LogHubQueryBo {
  /** 开始时间 yyyy-MM-dd HH:mm:ss */
  private String beginTime;
  /** 结束时间 yyyy-MM-dd HH:mm:ss */
  private String endTime;
  /** page,api,sql,oper,login 逗号或多值；空默认 page+api+sql */
  private List<String> sources;
  /** 操作人/用户标识 */
  private String actor;
  /** 通用关键字 */
  private String keyword;
  /** 页面路径模糊 */
  private String pagePath;
  /** 会话精确或模糊 */
  private String sessionId;
  /** 接口 URL 模糊 */
  private String apiUrl;
  /** 1 成功 / 0 失败；空全部 */
  private String okFlag;
  /** 链路标识，精确匹配 */
  private String traceId;
  /** 客户端 ID，精确匹配（RUM appId / 慢SQL·操作·登录 clientId） */
  private String clientId;
  /** 返回条数上限，默认 50，最大 100 */
  private Integer pageSize = 50;
}
