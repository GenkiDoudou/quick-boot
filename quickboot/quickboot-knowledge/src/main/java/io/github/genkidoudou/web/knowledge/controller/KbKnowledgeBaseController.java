package io.github.genkidoudou.web.knowledge.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.knowledge.domain.KbKnowledgeBase;
import io.github.genkidoudou.web.knowledge.dto.KbKnowledgeBaseBo;
import io.github.genkidoudou.web.knowledge.dto.KbKnowledgeBaseQueryBo;
import io.github.genkidoudou.web.knowledge.dto.KbKnowledgeBaseVo;
import io.github.genkidoudou.web.knowledge.service.KbKnowledgeBaseMcpService;
import io.github.genkidoudou.web.knowledge.service.KbKnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 知识库管理接口。
 */
@Tag(name = "知识库管理")
@Validated
@RestController
@RequestMapping("/knowledge/base")
@RequiredArgsConstructor
public class KbKnowledgeBaseController {

    private final KbKnowledgeBaseService service;
    private final KbKnowledgeBaseMcpService mcpBindingService;

    @Operation(summary = "知识库分页列表")
    @SaCheckPermission("knowledge:base:list")
    @GetMapping("/list")
    public R<PageInfo<KbKnowledgeBaseVo>> list(@Validated KbKnowledgeBaseQueryBo query) {
        return R.ok(service.page(query));
    }

    @Operation(summary = "知识库详情")
    @SaCheckPermission("knowledge:base:query")
    @GetMapping("/getInfo")
    public R<KbKnowledgeBaseVo> getInfo(
        @Parameter(description = "知识库ID") @RequestParam @Min(1) Long kbId) {
        KbKnowledgeBase row = service.getById(kbId);
        if (row == null) {
            return R.ok();
        }
        KbKnowledgeBaseVo vo = BeanUtil.copyProperties(row, KbKnowledgeBaseVo.class);
        vo.setMcpIds(mcpBindingService.listMcpIdsByKbId(kbId));
        return R.ok(vo);
    }

    @Operation(summary = "新增知识库")
    @SaCheckPermission("knowledge:base:add")
    @PostMapping("/add")
    public R<Void> add(@Validated(AddGroup.class) @RequestBody KbKnowledgeBaseBo req) {
        service.add(req);
        return R.ok();
    }

    @Operation(summary = "修改知识库")
    @SaCheckPermission("knowledge:base:edit")
    @PostMapping("/update")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody KbKnowledgeBaseBo req) {
        service.update(req);
        return R.ok();
    }

    @Operation(summary = "删除知识库")
    @SaCheckPermission("knowledge:base:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> kbIds) {
        service.removeBatch(kbIds);
        return R.ok();
    }
}
