package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.system.internal.service.ISysDictDataService;
import io.github.genkidoudou.system.internal.vo.SysDictDataImportRow;
import io.github.genkidoudou.system.internal.vo.SysDictDataVo;
import io.swagger.v3.oas.annotations.Operation;
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
 * 字典数据管理。按 dictType 维护枚举项及 Excel 导入导出。
 */
@Tag(name = "字典数据")
@RequiredArgsConstructor
@RestController
@RequestMapping("sys/dict/data")
public class SysDictDataController {

  private final ISysDictDataService service;

  /**
   * 字典数据分页。
   *
   * @param pageRequest 分页参数与查询条件
   * @return 分页结果
   */
  @Operation(summary = "分页查询")
  @SaCheckPermission("system:dictData:list")
  @PostMapping("page")
  public R<PageInfo<SysDictDataVo>> page(@RequestBody PageRequest<SysDictDataVo> pageRequest) {
    return R.ok(service.page(pageRequest));
  }

  /**
   * 字典数据详情。
   *
   * @param dictCode 字典数据主键
   * @return Vo
   */
  @Operation(summary = "字典数据详情")
  @SaCheckPermission(value = {"system:dictData:list", "system:dict:query", "system:dict:list"}, mode = SaMode.OR)
  @GetMapping("/{dictCode}")
  public R<SysDictDataVo> get(@PathVariable Long dictCode) {
    return R.ok(service.getDetail(dictCode));
  }

  /**
   * 按类型查询字典数据（供 useDict；登录用户可访问）。
   *
   * @param dictType 字典类型编码
   * @return 该类型下启用中的字典项列表
   */
  @Operation(summary = "按类型查询字典数据")
  @GetMapping("type/{dictType}")
  public R<List<SysDictDataVo>> listByType(@PathVariable String dictType) {
    return R.ok(service.listByType(dictType));
  }

  /**
   * 新增字典数据；响应 data 为新建 dictCode。
   *
   * @param vo 可写字段（含 dictType）
   * @return 新建主键
   */
  @Operation(summary = "新增字典数据")
  @SaCheckPermission("system:dictData:add")
  @PostMapping("add")
  public R<String> add(@RequestBody @Validated(AddGroup.class) SysDictDataVo vo) {
    Long id = service.add(vo);
    return R.ok(id == null ? null : String.valueOf(id));
  }

  /**
   * 修改字典数据。
   *
   * @param vo 含 dictCode 的可写字段
   * @return 是否成功
   */
  @Operation(summary = "修改字典数据")
  @SaCheckPermission("system:dictData:edit")
  @PostMapping("update")
  public R<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysDictDataVo vo) {
    return R.ok(service.update(vo));
  }

  /**
   * 单条删除字典数据。
   *
   * @param dictCode 字典数据主键
   * @return ok
   */
  @Operation(summary = "删除字典数据")
  @SaCheckPermission("system:dictData:remove")
  @GetMapping("remove/{dictCode}")
  public R<Void> removeGet(@PathVariable Long dictCode) {
    service.remove(List.of(dictCode));
    return R.ok();
  }

  /**
   * 批量删除字典数据。
   *
   * @param ids 字典数据主键集合
   * @return ok
   */
  @Operation(summary = "批量删除字典数据")
  @SaCheckPermission("system:dictData:remove")
  @PostMapping("remove")
  public R<Void> remove(@RequestBody List<Long> ids) {
    service.remove(ids);
    return R.ok();
  }

  /**
   * 同步导出字典数据 xlsx。
   *
   * @param query    导出筛选条件
   * @param response 文件流
   */
  @Operation(summary = "导出字典数据")
  @SaCheckPermission("system:dictData:export")
  @PostMapping("export")
  public void export(@RequestBody(required = false) SysDictDataVo query, HttpServletResponse response) throws Exception {
    ExcelUtils.exportExcel(service.export(query), "字典数据", SysDictDataVo.class, response);
  }

  /**
   * 下载字典数据导入 Excel 模板。
   *
   * @param response 文件流
   */
  @Operation(summary = "导入模板")
  @SaCheckPermission("system:dictData:import")
  @GetMapping("import/template")
  public void template(HttpServletResponse response) {
    ExcelUtils.exportExcel(Collections.emptyList(), "dict-data-import-template", SysDictDataImportRow.class, false, true, response);
  }

  /**
   * 同步导入字典数据；可选更新已存在项。
   *
   * @param file          Excel 文件
   * @param updateSupport 是否更新已存在数据（true/1）
   * @return 导入统计与失败行
   */
  @Operation(summary = "导入字典数据")
  @SaCheckPermission("system:dictData:import")
  @PostMapping("import")
  public R<ExcelResult<SysDictDataImportRow>> importExcel(
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "updateSupport", defaultValue = "false") String updateSupport) throws IOException {
    boolean update = "true".equalsIgnoreCase(updateSupport) || "1".equals(updateSupport);
    return R.ok(service.importExcel(file, update));
  }
}
