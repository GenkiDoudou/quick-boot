package ${packageName}.${moduleName}.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import ${packageName}.${moduleName}.dto.${className}Bo;
import ${packageName}.${moduleName}.dto.${className}QueryBo;
import ${packageName}.${moduleName}.dto.${className}Vo;
import ${packageName}.${moduleName}.service.${className}Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
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
 * ${tableComment!} 控制器。
 */
@Tag(name = "${functionName!tableComment}")
@Validated
@RestController
@RequestMapping("/${moduleName}/${businessName}")
@RequiredArgsConstructor
public class ${className}Controller {

    private final ${className}Service service;

    @Operation(summary = "${tableComment!}分页列表")
    @SaCheckPermission("${permissionPrefix}:list")
    @GetMapping("/list")
    public R<PageInfo<${className}Vo>> list(@Validated ${className}QueryBo query) {
        return R.ok(service.page(query));
    }

    @Operation(summary = "${tableComment!}详情")
    @SaCheckPermission("${permissionPrefix}:list")
    @GetMapping("/{id}")
    public R<${className}Vo> get(@Parameter(description = "主键") @PathVariable @Min(1) <#if pkColumn??>${pkColumn.javaType}<#else>Long</#if> id) {
        ${className}Vo vo = service.getById(id);
        if (vo == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "记录不存在或已删除");
        }
        return R.ok(vo);
    }

    @Operation(summary = "新增${tableComment!}")
    @SaCheckPermission("${permissionPrefix}:add")
    @PostMapping("/create")
    public R<Void> create(@Validated @RequestBody ${className}Bo req) {
        service.add(req);
        return R.ok();
    }

    @Operation(summary = "修改${tableComment!}")
    @SaCheckPermission("${permissionPrefix}:edit")
    @PostMapping("/update")
    public R<Void> update(@Validated @RequestBody ${className}Bo req) {
        service.update(req);
        return R.ok();
    }

    @Operation(summary = "删除${tableComment!}")
    @SaCheckPermission("${permissionPrefix}:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<<#if pkColumn??>${pkColumn.javaType}<#else>Long</#if>> ids) {
        service.removeBatch(ids);
        return R.ok();
    }
}
