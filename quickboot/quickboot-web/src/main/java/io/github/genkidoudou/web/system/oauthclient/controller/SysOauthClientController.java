package io.github.genkidoudou.web.system.oauthclient.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthClient;
import io.github.genkidoudou.web.system.oauthclient.dto.SysOauthClientBo;
import io.github.genkidoudou.web.system.oauthclient.dto.SysOauthClientCredentialsVo;
import io.github.genkidoudou.web.system.oauthclient.dto.SysOauthClientRevealBo;
import io.github.genkidoudou.web.system.oauthclient.dto.SysOauthClientVo;
import jakarta.validation.Valid;
import io.github.genkidoudou.web.system.oauthclient.service.SysOauthClientService;
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
 * OAuth2 授权服务器客户端管理。
 */
@Tag(name = "OAuth客户端管理")
@Validated
@RestController
@RequestMapping("/system/oauthClient")
@RequiredArgsConstructor
public class SysOauthClientController {

    private final SysOauthClientService service;

    @Operation(summary = "客户端列表")
    @SaCheckPermission("system:oauthClient:list")
    @GetMapping("/list")
    public R<List<SysOauthClientVo>> list(@RequestParam(required = false) String clientName) {
        List<SysOauthClientVo> result = new ArrayList<>();
        for (SysOauthClient row : service.list(clientName)) {
            result.add(BeanUtil.copyProperties(row, SysOauthClientVo.class));
        }
        return R.ok(result);
    }

    @Operation(summary = "客户端详情（不含 client_secret）")
    @SaCheckPermission("system:oauthClient:query")
    @GetMapping("/{clientId}")
    public R<SysOauthClientVo> get(@Parameter(description = "客户端ID") @PathVariable @NotBlank String clientId) {
        return R.ok(service.getDetailVo(clientId));
    }

    @Operation(summary = "查看客户端密钥（需校验当前用户密码）")
    @SaCheckPermission("system:oauthClient:query")
    @PostMapping("/{clientId}/revealSecret")
    public R<SysOauthClientCredentialsVo> revealSecret(
            @Parameter(description = "客户端ID") @PathVariable @NotBlank String clientId,
            @Valid @RequestBody SysOauthClientRevealBo body) {
        return R.ok(service.revealCredentials(clientId, body.getPassword()));
    }

    @Operation(summary = "新增客户端")
    @SaCheckPermission("system:oauthClient:add")
    @PostMapping("/create")
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysOauthClientBo req) {
        service.add(req);
        return R.ok();
    }

    @Operation(summary = "修改客户端")
    @SaCheckPermission("system:oauthClient:edit")
    @PostMapping("/update")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody SysOauthClientBo req) {
        service.update(req);
        return R.ok();
    }

    @Operation(summary = "删除客户端")
    @SaCheckPermission("system:oauthClient:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody Map<String, List<String>> body) {
        service.remove(body.get("ids"));
        return R.ok();
    }
}
