package io.github.genkidoudou.web.system.oauthprovider.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.system.oauthprovider.domain.SysOauthProvider;
import io.github.genkidoudou.web.system.oauthprovider.dto.SysOauthProviderBo;
import io.github.genkidoudou.web.system.oauthprovider.dto.SysOauthProviderVo;
import io.github.genkidoudou.web.system.oauthprovider.service.SysOauthProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 外部 IdP 配置管理。
 */
@Tag(name = "外部IdP管理")
@Validated
@RestController
@RequestMapping("/system/oauthProvider")
@RequiredArgsConstructor
public class SysOauthProviderController {

    private final SysOauthProviderService service;

    @Operation(summary = "IdP 列表")
    @SaCheckPermission("system:oauthProvider:list")
    @GetMapping("/list")
    public R<List<SysOauthProviderVo>> list(@RequestParam(required = false) String providerName) {
        List<SysOauthProviderVo> result = new ArrayList<>();
        for (SysOauthProvider row : service.list(providerName)) {
            result.add(BeanUtil.copyProperties(row, SysOauthProviderVo.class));
        }
        return R.ok(result);
    }

    @Operation(summary = "IdP 详情")
    @SaCheckPermission("system:oauthProvider:query")
    @GetMapping("/{providerCode}")
    public R<SysOauthProviderVo> get(@Parameter(description = "提供方编码") @PathVariable @NotBlank String providerCode) {
        SysOauthProvider row = service.getByCode(providerCode);
        if (row == null) {
            return R.ok();
        }
        return R.ok(BeanUtil.copyProperties(row, SysOauthProviderVo.class));
    }

    @Operation(summary = "新增 IdP")
    @SaCheckPermission("system:oauthProvider:add")
    @PostMapping("/create")
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysOauthProviderBo req) {
        service.add(req);
        return R.ok();
    }

    @Operation(summary = "修改 IdP")
    @SaCheckPermission("system:oauthProvider:edit")
    @PostMapping("/update")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody SysOauthProviderBo req) {
        service.update(req);
        return R.ok();
    }

    @Operation(summary = "删除 IdP")
    @SaCheckPermission("system:oauthProvider:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody Map<String, List<String>> body) {
        service.remove(body.get("ids"));
        return R.ok();
    }
}
