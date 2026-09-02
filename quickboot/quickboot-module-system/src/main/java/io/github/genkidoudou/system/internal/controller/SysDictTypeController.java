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
import io.github.genkidoudou.system.internal.service.ISysDictTypeService;
import io.github.genkidoudou.system.internal.vo.SysDictTypeImportRow;
import io.github.genkidoudou.system.internal.vo.SysDictTypeVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
 * 字典类型管理。维护 dictType 元数据及缓存刷新、Excel 导入导出。
 */
@Tag(name = "字典类型")
@RequiredArgsConstructor
@RestController
@RequestMapping("sys/dict/type")
public class SysDictTypeController {

  private final ISysDictTypeService service;

  /**
   * 字典类型分页。
   *
   * @param pageRequest 分页参数与查询条件
   * @return 分页结果
   */
  @Operation(summary = "分页查询")
  @SaCheckPermission("system:dict:list")
  @PostMapping("page")
  public R<PageInfo<SysDictTypeVo>> page(@RequestBody PageRequest<SysDictTypeVo> pageRequest) {
    return R.ok(service.page(pageRequest));
  }

  /**
   * 字典类型详情。
   *
   * @param dictId 字典类型主键
   * @return Vo
   */
  @Operation(summary = "字典类型详情")
  @SaCheckPermission(value = {"system:dict:query", "system:dict:list"}, mode = SaMode.OR)
  @GetMapping("/{dictId}")
  public R<SysDictTypeVo> get(@Parameter(description = "字典类型主键") @PathVariable Long dictId) {
    return R.ok(service.getDetail(dictId));
  }

  /**
   * 新增字典类型；响应 data 为新建 dictId。
   *
   * @param vo 可写字段
   * @return 新建主键
   */
  @Operation(summary = "新增字典类型")
  @SaCheckPermission("system:dict:add")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':add:' + #body.dictType", message = "请勿重复提交")
  @PostMapping("add")
  public R<String> add(@RequestBody @Validated(AddGroup.class) SysDictTypeVo vo) {
    Long id = service.add(vo);
    return R.ok(id == null ? null : String.valueOf(id));
  }

  /**
   * 修改字典类型。
   *
   * @param vo 含 dictId 的可写字段
   * @return 是否成功
   */
  @Operation(summary = "修改字典类型")
  @SaCheckPermission("system:dict:edit")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':upd:' + #body.dictId", message = "请勿重复提交")
  @PostMapping("update")
  public R<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysDictTypeVo vo) {
    return R.ok(service.update(vo));
  }

  /**
   * 单条删除字典类型。
   *
   * @param dictId 字典类型主键
   * @return ok
   */
  @Operation(summary = "删除字典类型")
  @SaCheckPermission("system:dict:remove")
  @GetMapping("remove/{dictId}")
  public R<Void> removeGet(@Parameter(description = "字典类型主键") @PathVariable Long dictId) {
    service.remove(List.of(dictId));
    return R.ok();
  }

  /**
   * 批量删除字典类型。
   *
   * @param ids 字典类型主键集合
   * @return ok
   */
  @Operation(summary = "批量删除字典类型")
  @SaCheckPermission("system:dict:remove")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':rm:' + #ids", message = "请勿重复提交")
  @PostMapping("remove")
  public R<Void> remove(@RequestBody List<Long> ids) {
    service.remove(ids);
    return R.ok();
  }

  /**
   * 刷新全部字典类型及其数据缓存。
   *
   * @return ok；副作用为重建 Redis 字典缓存
   */
  @Operation(summary = "刷新全部字典缓存")
  @SaCheckPermission("system:dict:refresh")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':refresh:all'", message = "请勿重复提交")
  @PostMapping("refresh")
  public R<Void> refreshAll() {
    service.refreshAll();
    return R.ok();
  }

  /**
   * 刷新指定 dictType 的字典数据缓存。
   *
   * @param dictType 字典类型编码
   * @return ok；副作用为重建该类型缓存
   */
  @Operation(summary = "刷新指定字典缓存")
  @SaCheckPermission("system:dict:refresh")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':refresh:' + #dictType", message = "请勿重复提交")
  @PostMapping("refresh/{dictType}")
  public R<Void> refresh(@Parameter(description = "字典类型编码") @PathVariable String dictType) {
    service.refresh(dictType);
    return R.ok();
  }

  /**
   * 同步导出字典类型 xlsx。
   *
   * @param query    导出筛选条件
   * @param response 文件流
   */
  @Operation(summary = "导出字典类型")
  @IgnoreLogger(type = IgnoreLogger.Type.RESULT)
  @SaCheckPermission("system:dict:export")
  @PostMapping("export")
  public void export(@RequestBody(required = false) SysDictTypeVo query, HttpServletResponse response) throws Exception {
    ExcelUtils.exportExcel(service.export(query), "字典类型", SysDictTypeVo.class, response);
  }

  /**
   * 下载字典类型导入 Excel 模板。
   *
   * @param response 文件流
   */
  @Operation(summary = "导入模板")
  @IgnoreLogger(type = IgnoreLogger.Type.RESULT)
  @SaCheckPermission("system:dict:import")
  @GetMapping("import/template")
  public void template(HttpServletResponse response) {
    ExcelUtils.exportExcel(Collections.emptyList(), "dict-type-import-template", SysDictTypeImportRow.class, false, true, response);
  }

  /**
   * 同步导入字典类型；可选更新已存在 dictType。
   *
   * @param file          Excel 文件
   * @param updateSupport 是否更新已存在数据（true/1）
   * @return 导入统计与失败行
   */
  @Operation(summary = "导入字典类型")
  @SaCheckPermission("system:dict:import")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':import:dictType'", message = "请勿重复提交")
  @PostMapping("import")
  public R<ExcelResult<SysDictTypeImportRow>> importExcel(
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "updateSupport", defaultValue = "false") String updateSupport) throws IOException {
    boolean update = "true".equalsIgnoreCase(updateSupport) || "1".equals(updateSupport);
    return R.ok(service.importExcel(file, update));
  }
}
