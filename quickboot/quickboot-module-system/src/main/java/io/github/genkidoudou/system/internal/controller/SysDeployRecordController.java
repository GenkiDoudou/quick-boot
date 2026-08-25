package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.system.internal.dto.DeployRecordCallbackBo;
import io.github.genkidoudou.system.internal.service.ISysDeployRecordService;
import io.github.genkidoudou.system.internal.vo.SysDeployRecordVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发布记录：Jenkins 回调入库与管理端查询。
 */
@Tag(name = "发布记录")
@IgnoreLogger(type = IgnoreLogger.Type.ALL)
@RequiredArgsConstructor
@RestController
@RequestMapping("monitor/deployRecord")
public class SysDeployRecordController {

  private final ISysDeployRecordService service;

  /** 与目标机 .env.properties 中 DEPLOY_CALLBACK_TOKEN 一致；空则拒绝回调。 */
  @Value("${DEPLOY_CALLBACK_TOKEN:}")
  private String deployCallbackToken;

  /**
   * Jenkins 部署成功回调（免登录，靠 Header Token）。
   *
   * @param token 请求头 X-Deploy-Token
   * @param bo    回调体
   * @return 空
   */
  @SaIgnore
  @Operation(summary = "Jenkins 发布回调")
  @PostMapping("/callback")
  public R<Void> callback(
    @RequestHeader(value = "X-Deploy-Token", required = false) String token,
    @Valid @RequestBody DeployRecordCallbackBo bo
  ) {
    assertCallbackToken(token);
    service.saveCallback(bo);
    return R.ok();
  }

  /**
   * 分页查询。
   *
   * @param pageRequest 分页参数
   * @return 分页结果
   */
  @Operation(summary = "发布记录分页")
  @SaCheckPermission("monitor:deployRecord:list")
  @PostMapping("/page")
  public R<PageInfo<SysDeployRecordVo>> page(@RequestBody PageRequest<SysDeployRecordVo> pageRequest) {
    return R.ok(service.page(pageRequest));
  }

  /**
   * 详情。
   *
   * @param recordId 主键
   * @return 详情
   */
  @Operation(summary = "发布记录详情")
  @SaCheckPermission(value = {"monitor:deployRecord:query", "monitor:deployRecord:list"}, mode = SaMode.OR)
  @GetMapping("/{recordId}")
  public R<SysDeployRecordVo> get(@Parameter(description = "记录主键") @PathVariable String recordId) {
    return R.ok(service.getDetail(Long.parseLong(recordId)));
  }

  /**
   * 校验回调 Token；未配置或与 Header 不一致时拒绝。
   */
  private void assertCallbackToken(String token) {
    if (StrUtil.isBlank(deployCallbackToken)) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "服务端未配置 DEPLOY_CALLBACK_TOKEN，拒绝回调");
    }
    if (!deployCallbackToken.equals(token)) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "X-Deploy-Token 无效");
    }
  }
}
