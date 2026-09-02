package ${packageName}.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import ${packageName}.internal.service.I${className}Service;
import ${packageName}.internal.vo.${className}Vo;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ${tableComment!} 管理（Vo-only Controller，委托 {@link I${className}Service}）。
 */
@Tag(name = "${functionName!tableComment}")
@RequiredArgsConstructor
@RestController
@RequestMapping("/${moduleName}/${businessName}")
public class ${className}Controller {

  private final I${className}Service service;

  @Operation(summary = "分页查询")
  @SaCheckPermission("${permissionPrefix}:list")
  @PostMapping("/page")
  public R<PageInfo<${className}Vo>> page(@RequestBody PageRequest<${className}Vo> pageRequest) {
    return R.ok(service.page(pageRequest));
  }

  @Operation(summary = "详情")
  @SaCheckPermission(value = {"${permissionPrefix}:query", "${permissionPrefix}:list"}, mode = SaMode.OR)
  @GetMapping("/{id}")
  public R<${className}Vo> get(@Parameter(description = "主键") @PathVariable <#if pkColumn??>${pkColumn.javaType}<#else>Long</#if> id) {
    return R.ok(service.getDetail(id));
  }

  @Operation(summary = "新增")
  @SaCheckPermission("${permissionPrefix}:add")
  @PostMapping("/add")
  public R<String> add(@RequestBody @Validated(AddGroup.class) ${className}Vo vo) {
    Long id = service.add(vo);
    return R.ok(id == null ? null : String.valueOf(id));
  }

  @Operation(summary = "修改")
  @SaCheckPermission("${permissionPrefix}:edit")
  @PostMapping("/update")
  public R<Boolean> update(@RequestBody @Validated(UpdateGroup.class) ${className}Vo vo) {
    return R.ok(service.update(vo));
  }

  @Operation(summary = "批量删除")
  @SaCheckPermission("${permissionPrefix}:remove")
  @PostMapping("/remove")
  public R<Void> remove(@RequestBody List<<#if pkColumn??>${pkColumn.javaType}<#else>Long</#if>> ids) {
    service.remove(ids);
    return R.ok();
  }

  @Operation(summary = "导出")
  @IgnoreLogger(type = IgnoreLogger.Type.RESULT)
  @SaCheckPermission("${permissionPrefix}:export")
  @PostMapping("/export")
  public void export(@RequestBody(required = false) ${className}Vo request, HttpServletResponse response) {
    List<${className}Vo> rows = service.export(request);
    ExcelUtils.exportExcel(rows, "${functionName!tableComment!}", ${className}Vo.class, response);
  }
}
