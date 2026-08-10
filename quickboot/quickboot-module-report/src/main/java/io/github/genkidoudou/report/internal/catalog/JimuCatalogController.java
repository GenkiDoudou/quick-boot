package io.github.genkidoudou.report.internal.catalog;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.github.genkidoudou.common.api.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 积木报表 / BI 目录，供菜单维护选择绑定项。
 */
@Tag(name = "积木目录")
@Validated
@RestController
@RequestMapping("/report/jimu/catalog")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qc.jimu", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JimuCatalogController {

    private final JimuCatalogService jimuCatalogService;

    @Operation(summary = "积木报表列表（菜单绑定用）")
    @SaCheckPermission(value = {"system:menu:list", "system:menu:query"}, mode = SaMode.OR)
    @GetMapping("/reports")
    public R<List<JimuCatalogItemVo>> listReports() {
        return R.ok(jimuCatalogService.listReports());
    }

    @Operation(summary = "BI 大屏列表（菜单绑定用）")
    @SaCheckPermission(value = {"system:menu:list", "system:menu:query"}, mode = SaMode.OR)
    @GetMapping("/bi-pages")
    public R<List<JimuCatalogItemVo>> listBiPages() {
        return R.ok(jimuCatalogService.listBiPages());
    }
}
