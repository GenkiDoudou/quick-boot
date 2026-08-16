package io.github.genkidoudou.monitor.internal.userbehavior.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.monitor.internal.userbehavior.dto.UserBehaviorNodeVo;
import io.github.genkidoudou.monitor.internal.userbehavior.dto.UserBehaviorSessionQueryBo;
import io.github.genkidoudou.monitor.internal.userbehavior.dto.UserBehaviorSessionVo;
import io.github.genkidoudou.monitor.internal.userbehavior.service.UserBehaviorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户行为分析 HTTP 接口：会话列表与时间线查询。
 */
@Tag(name = "用户行为")
@Validated
@RestController
@RequestMapping("/monitor/userBehavior")
@RequiredArgsConstructor
public class UserBehaviorController {

  private final UserBehaviorService userBehaviorService;

  /**
   * 按用户或 sessionId 查询会话摘要列表。
   *
   * @param query 用户标识、时间范围与条数限制
   * @return 会话摘要列表
   */
  @Operation(summary = "会话列表")
  @SaCheckPermission("monitor:userBehavior:query")
  @GetMapping("/sessions")
  public R<List<UserBehaviorSessionVo>> sessions(UserBehaviorSessionQueryBo query) {
    return R.ok(userBehaviorService.listSessions(query));
  }

  /**
   * 查询指定会话内的事件时间线。
   *
   * @param sessionId  会话标识
   * @param beginTime  可选，开始时间
   * @param endTime    可选，结束时间
   * @return 按时间排序的行为节点列表
   */
  @Operation(summary = "会话时间线")
  @SaCheckPermission("monitor:userBehavior:query")
  @GetMapping("/timeline")
  public R<List<UserBehaviorNodeVo>> timeline(
    @RequestParam String sessionId,
    @RequestParam(required = false) String beginTime,
    @RequestParam(required = false) String endTime) {
    return R.ok(userBehaviorService.timeline(sessionId, beginTime, endTime));
  }
}
