package io.github.genkidoudou.web.system.notice.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.system.notice.dto.SysNoticeBo;
import io.github.genkidoudou.web.system.notice.dto.SysNoticeQueryBo;
import io.github.genkidoudou.web.system.notice.dto.SysNoticeVo;
import io.github.genkidoudou.web.system.notice.service.SysNoticeService;
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
 * 通知公告控制器。
 */
@Tag(name = "通知公告管理")
@Validated
@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
public class SysNoticeController {

    private final SysNoticeService service;

    @Operation(summary = "通知公告分页列表")
    @SaCheckPermission("system:notice:list")
    @GetMapping("/list")
    public R<PageInfo<SysNoticeVo>> list(@Validated SysNoticeQueryBo query) {
        return R.ok(service.page(query));
    }

    @Operation(summary = "通知公告详情")
    @SaCheckPermission("system:notice:list")
    @GetMapping("/{noticeId}")
    public R<SysNoticeVo> get(@Parameter(description = "公告ID") @PathVariable @Min(1) Long noticeId) {
        SysNoticeVo vo = service.getById(noticeId);
        if (vo == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "公告不存在或已删除");
        }
        return R.ok(vo);
    }

    @Operation(summary = "新增通知公告")
    @SaCheckPermission("system:notice:add")
    @PostMapping("/create")
    public R<Void> create(@Validated(AddGroup.class) @RequestBody SysNoticeBo req) {
        service.add(req);
        return R.ok();
    }

    @Operation(summary = "修改通知公告")
    @SaCheckPermission("system:notice:edit")
    @PostMapping("/update")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody SysNoticeBo req) {
        service.update(req);
        return R.ok();
    }

    @Operation(summary = "删除通知公告（支持批量）")
    @SaCheckPermission("system:notice:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> noticeIds) {
        service.removeBatch(noticeIds);
        return R.ok();
    }
}
