package io.github.genkidoudou.monitor.internal.loghub.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.monitor.internal.loghub.dto.LogHubListVo;
import io.github.genkidoudou.monitor.internal.loghub.dto.LogHubQueryBo;
import io.github.genkidoudou.monitor.internal.loghub.service.LogHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志中心 HTTP 接口：合并多来源日志的统一查询入口。
 */
@Tag(name = "日志中心")
@Validated
@RestController
@RequestMapping("/monitor/logHub")
@RequiredArgsConstructor
public class LogHubController {

  private final LogHubService logHubService;

  /**
   * 按条件合并查询页面、接口、慢 SQL、操作、登录等日志。
   *
   * @param query 时间范围、来源、关键字等筛选条件
   * @return 近似分页的合并日志列表
   */
  @Operation(summary = "合并日志列表（近似分页）")
  @SaCheckPermission("monitor:logHub:query")
  @GetMapping("/list")
  public R<LogHubListVo> list(LogHubQueryBo query) {
    return R.ok(logHubService.list(query));
  }
}
