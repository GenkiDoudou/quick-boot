package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.idempotency.Idempotent;
import io.github.genkidoudou.common.web.DeprecatedApiSupport;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.system.internal.service.ISysDeptService;
import io.github.genkidoudou.system.internal.vo.SysDeptImportRow;
import io.github.genkidoudou.system.internal.vo.SysDeptVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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
 * 部门管理。
 */
@Tag(name = "部门管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("sys/dept")
public class SysDeptController {

  private final ISysDeptService service;

  /**
   * 部门树形列表（POST；records 为根节点树，非物理分页）。
   *
   * @param pageRequest 筛选条件放在 param（deptName/status）
   * @return 树形列表包装为 PageInfo
   */
  @Operation(summary = "部门树形列表")
  @SaCheckPermission("system:dept:list")
  @PostMapping("page")
  public R<PageInfo<SysDeptVo>> page(@RequestBody(required = false) PageRequest<SysDeptVo> pageRequest) {
    SysDeptVo param = pageRequest != null ? pageRequest.getParam() : null;
    String deptName = param != null ? param.getDeptName() : null;
    String status = param != null ? param.getStatus() : null;
    List<SysDeptVo> tree = service.list(deptName, status);
    PageInfo<SysDeptVo> info = new PageInfo<>();
    info.setCurrent(1);
    info.setSize(tree.size());
    info.setRecords(tree);
    info.setTotal(tree.size());
    info.setPages(1);
    return R.ok(info);
  }

  /**
   * 部门列表（GET 兼容，请改用 POST {@code /page}）。
   *
   * @param deptName 名称模糊
   * @param status   状态
   * @return 树形列表
   * @deprecated 请改用 POST {@code /sys/dept/page}，data 取 records
   */
  @Deprecated
  @Operation(summary = "部门列表（兼容）", deprecated = true)
  @SaCheckPermission("system:dept:list")
  @GetMapping("list")
  public R<List<SysDeptVo>> list(HttpServletResponse response,
                                 @RequestParam(required = false) String deptName,
                                 @RequestParam(required = false) String status) {
    DeprecatedApiSupport.markDeprecated(response);
    return R.ok(service.list(deptName, status));
  }

  /**
   * 部门下拉树。
   *
   * @return 树
   */
  @Operation(summary = "部门下拉树")
  @SaCheckPermission("system:dept:list")
  @GetMapping("treeselect")
  public R<List<SysDeptVo>> tree() {
    return R.ok(service.treeSelect());
  }

  /**
   * 部门详情。
   *
   * @param id 主键
   * @return Vo
   */
  @Operation(summary = "部门详情")
  @SaCheckPermission(value = {"system:dept:query", "system:dept:list"}, mode = SaMode.OR)
  @GetMapping("/{id}")
  public R<SysDeptVo> get(@PathVariable Long id) {
    return R.ok(service.getDetail(id));
  }

  /**
   * 新增部门。
   *
   * @param v 可写字段
   * @return 主键
   */
  @Operation(summary = "新增部门")
  @SaCheckPermission("system:dept:add")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':add:' + #body.deptName + ':' + #body.parentId", message = "请勿重复提交")
  @PostMapping("add")
  public R<String> add(@RequestBody @Validated(AddGroup.class) SysDeptVo v) {
    return R.ok(String.valueOf(service.add(v)));
  }

  /**
   * 修改部门。
   *
   * @param v 含主键
   * @return 是否成功
   */
  @Operation(summary = "修改部门")
  @SaCheckPermission("system:dept:edit")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':upd:' + #body.deptId", message = "请勿重复提交")
  @PostMapping("update")
  public R<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysDeptVo v) {
    return R.ok(service.update(v));
  }

  /**
   * 单条删除。
   *
   * @param id 主键
   * @return ok
   */
  @Operation(summary = "删除部门")
  @SaCheckPermission("system:dept:remove")
  @GetMapping("remove/{id}")
  public R<Void> remove(@PathVariable Long id) {
    service.remove(List.of(id));
    return R.ok();
  }

  /**
   * 批量删除。
   *
   * @param ids 主键集合
   * @return ok
   */
  @Operation(summary = "批量删除部门")
  @SaCheckPermission("system:dept:remove")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':rm:' + #ids", message = "请勿重复提交")
  @PostMapping("remove")
  public R<Void> remove(@RequestBody List<Long> ids) {
    service.remove(ids);
    return R.ok();
  }

  /**
   * 导出部门。
   *
   * @param q        导出条件
   * @param r        文件流
   */
  @Operation(summary = "导出部门")
  @SaCheckPermission("system:dept:export")
  @PostMapping("export")
  public void export(@RequestBody(required = false) SysDeptVo q, HttpServletResponse r) throws Exception {
    ExcelUtils.exportExcel(service.export(q), "部门", SysDeptVo.class, r);
  }

  /**
   * 导入模板。
   *
   * @param r 文件流
   */
  @Operation(summary = "导入模板")
  @SaCheckPermission("system:dept:import")
  @GetMapping("import/template")
  public void template(HttpServletResponse r) {
    ExcelUtils.exportExcel(Collections.emptyList(), "dept-import-template", SysDeptImportRow.class, false, true, r);
  }

  /**
   * 导入部门。
   *
   * @param f             Excel 文件
   * @param updateSupport 是否更新已存在数据
   * @return 导入结果
   */
  @Operation(summary = "导入部门")
  @SaCheckPermission("system:dept:import")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':import:dept'", message = "请勿重复提交")
  @PostMapping("import")
  public R<ExcelResult<SysDeptImportRow>> importExcel(
    @RequestParam("file") MultipartFile f,
    @RequestParam(defaultValue = "false") String updateSupport) throws IOException {
    return R.ok(service.importExcel(f, "true".equalsIgnoreCase(updateSupport) || "1".equals(updateSupport)));
  }
}
