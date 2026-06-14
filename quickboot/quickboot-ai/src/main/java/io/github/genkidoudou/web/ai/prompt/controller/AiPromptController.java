package io.github.genkidoudou.web.ai.prompt.controller;



import cn.dev33.satoken.annotation.SaCheckPermission;

import io.github.genkidoudou.common.api.PageInfo;

import io.github.genkidoudou.common.api.R;

import io.github.genkidoudou.common.validation.group.AddGroup;

import io.github.genkidoudou.common.validation.group.UpdateGroup;

import io.github.genkidoudou.web.ai.prompt.dto.AiPromptBo;

import io.github.genkidoudou.web.ai.prompt.dto.AiPromptOptionVo;

import io.github.genkidoudou.web.ai.prompt.dto.AiPromptOptimizeBo;

import io.github.genkidoudou.web.ai.prompt.dto.AiPromptOptimizeResultVo;

import io.github.genkidoudou.web.ai.prompt.dto.AiPromptQueryBo;

import io.github.genkidoudou.web.ai.prompt.dto.AiPromptVo;

import io.github.genkidoudou.web.ai.prompt.service.AiPromptOptimizeService;

import io.github.genkidoudou.web.ai.prompt.service.AiPromptService;

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

 * AI 提示词管理接口。

 */

@Tag(name = "AI 提示词管理")

@Validated

@RestController

@RequestMapping("/ai/prompt")

@RequiredArgsConstructor

public class AiPromptController {



    private final AiPromptService promptService;

    private final AiPromptOptimizeService optimizeService;



    @Operation(summary = "提示词分页列表")

    @SaCheckPermission("ai:prompt:list")

    @GetMapping("/list")

    public R<PageInfo<AiPromptVo>> list(@Validated AiPromptQueryBo query) {

        return R.ok(promptService.page(query));

    }



    @Operation(summary = "提示词下拉选项")

    @SaCheckPermission("ai:prompt:query")

    @GetMapping("/options")

    public R<List<AiPromptOptionVo>> options() {

        return R.ok(promptService.listOptions());

    }



    @Operation(summary = "提示词详情")

    @SaCheckPermission("ai:prompt:query")

    @GetMapping("/getInfo")

    public R<AiPromptVo> getInfo(

        @Parameter(description = "提示词 ID") @RequestParam @Min(1) Long promptId) {

        AiPromptVo vo = promptService.getInfo(promptId);

        if (vo == null) {

            return R.ok();

        }

        return R.ok(vo);

    }



    @Operation(summary = "新增提示词")

    @SaCheckPermission("ai:prompt:add")

    @PostMapping("/add")

    public R<Long> add(@Validated(AddGroup.class) @RequestBody AiPromptBo req) {

        return R.ok(promptService.add(req));

    }



    @Operation(summary = "修改提示词")

    @SaCheckPermission("ai:prompt:edit")

    @PostMapping("/update")

    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody AiPromptBo req) {

        promptService.update(req);

        return R.ok();

    }



    @Operation(summary = "删除提示词")

    @SaCheckPermission("ai:prompt:remove")

    @PostMapping("/remove")

    public R<Void> remove(@RequestBody List<Long> promptIds) {

        promptService.removeBatch(promptIds);

        return R.ok();

    }



    @Operation(summary = "AI 优化提示词内容")

    @SaCheckPermission("ai:prompt:optimize")

    @PostMapping("/optimize")

    public R<AiPromptOptimizeResultVo> optimize(@Validated @RequestBody AiPromptOptimizeBo req) {

        return R.ok(optimizeService.optimize(req));

    }

}

