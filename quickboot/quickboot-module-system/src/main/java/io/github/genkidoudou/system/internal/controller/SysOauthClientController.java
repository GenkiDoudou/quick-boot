package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.idempotency.Idempotent;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.system.internal.service.ISysOauthClientService;
import io.github.genkidoudou.system.internal.vo.SysOauthClientImportResult;
import io.github.genkidoudou.system.internal.vo.SysOauthClientImportRow;
import io.github.genkidoudou.system.internal.vo.SysOauthClientVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * OAuth 客户端管理。管理端路径参数使用主键 {@code id}，避免 clientId 特殊字符问题。
 */
@Tag(name = "OAuth客户端")
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("sys/oauthclient")
public class SysOauthClientController {

  private final ISysOauthClientService iSysOauthClientService;

  /**
   * 分页（不含 secret）。
   *
   * @param pageRequest 分页参数
   * @return 分页结果
   */
  @Operation(summary = "分页查询")
  @SaCheckPermission("system:oauthClient:list")
  @PostMapping("page")
  public R<PageInfo<SysOauthClientVo>> page(@RequestBody PageRequest<SysOauthClientVo> pageRequest) {
    return R.ok(iSysOauthClientService.page(pageRequest));
  }

  /**
   * 详情（含 secret）。
   *
   * @param id 主键
   * @return Vo
   */
  @Operation(summary = "客户端详情")
  @SaCheckPermission(value = {"system:oauthClient:query", "system:oauthClient:secret", "system:oauthClient:list"}, mode = SaMode.OR)
  @GetMapping("/{id}")
  public R<SysOauthClientVo> get(@PathVariable Long id) {
    return R.ok(iSysOauthClientService.getDetail(id));
  }

  /**
   * 新增；响应 data 为新建主键 id（secret 请再调详情）。
   *
   * @param vo 可写字段
   * @return 主键 id
   */
  @Operation(summary = "新增客户端")
  @SaCheckPermission("system:oauthClient:add")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':add:' + #body.clientId", message = "请勿重复提交")
  @PostMapping("add")
  public R<String> add(@RequestBody @Validated(AddGroup.class) SysOauthClientVo vo) {
    Long id = iSysOauthClientService.add(vo);
    return R.ok(id == null ? null : String.valueOf(id));
  }

  /**
   * 修改；不变更 secret / clientId。
   *
   * @param vo 含 id
   * @return 是否成功
   */
  @Operation(summary = "修改客户端")
  @SaCheckPermission("system:oauthClient:edit")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':upd:' + #body.id", message = "请勿重复提交")
  @PostMapping("update")
  public R<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysOauthClientVo vo) {
    return R.ok(iSysOauthClientService.update(vo));
  }

  /**
   * 单条删除。
   *
   * @param id 主键
   * @return ok
   */
  @Operation(summary = "删除客户端")
  @SaCheckPermission("system:oauthClient:remove")
  @GetMapping("remove/{id}")
  public R<Void> removeGet(@PathVariable Long id) {
    iSysOauthClientService.remove(List.of(id));
    return R.ok();
  }

  /**
   * 批量删除。
   *
   * @param ids 主键集合
   * @return ok
   */
  @Operation(summary = "批量删除客户端")
  @SaCheckPermission("system:oauthClient:remove")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':rm:' + #ids", message = "请勿重复提交")
  @PostMapping("/remove")
  public R<Void> remove(@RequestBody List<Long> ids) {
    iSysOauthClientService.remove(ids);
    return R.ok();
  }

  /**
   * 同步导出 xlsx（不含 secret）。有 ids 则按勾选；否则按搜索条件。
   *
   * @param request  导出条件
   * @param response 文件流
   */
  @Operation(summary = "导出客户端")
  @IgnoreLogger(type = IgnoreLogger.Type.RESULT)
  @SaCheckPermission("system:oauthClient:export")
  @PostMapping("/export")
  public void export(@RequestBody(required = false) SysOauthClientVo request,
                     HttpServletResponse response) throws Exception {
    List<SysOauthClientVo> export = iSysOauthClientService.export(request);
    ExcelUtils.exportExcel(export, "客户端", SysOauthClientVo.class, response);
  }

  /**
   * 导入模板（无 secret / id / createTime）。
   */
  @Operation(summary = "导入模板")
  @IgnoreLogger(type = IgnoreLogger.Type.RESULT)
  @SaCheckPermission("system:oauthClient:import")
  @GetMapping("/import/template")
  public void importTemplate(HttpServletResponse response) {
    ExcelUtils.exportExcel(Collections.emptyList(), "oauth-client-import-template",
      SysOauthClientImportRow.class, false, true, response);
  }

  /**
   * 同步导入；可选更新已存在 clientId；响应不含 secret。
   *
   * @param file          Excel 文件
   * @param updateSupport 是否更新已存在数据
   * @return 导入统计
   */
  @Operation(summary = "导入客户端")
  @SaCheckPermission("system:oauthClient:import")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':import:oauthClient'", message = "请勿重复提交")
  @PostMapping("/import")
  public R<ExcelResult<SysOauthClientImportRow>> importExcel(@RequestParam("file") MultipartFile file,
                                                             @RequestParam(value = "updateSupport", defaultValue = "false")
                                                             String updateSupport) throws IOException {
    boolean update = "true".equalsIgnoreCase(updateSupport) || "1".equals(updateSupport);
    return R.ok(iSysOauthClientService.importExcel(file, update));
  }
}
