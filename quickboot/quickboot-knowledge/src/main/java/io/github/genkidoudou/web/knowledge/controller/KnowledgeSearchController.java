package io.github.genkidoudou.web.knowledge.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.web.knowledge.dto.ChunkHitVo;
import io.github.genkidoudou.web.knowledge.dto.KbRetrievalLogQueryBo;
import io.github.genkidoudou.web.knowledge.dto.KbRetrievalLogVo;
import io.github.genkidoudou.web.knowledge.dto.KnowledgeSearchBo;
import io.github.genkidoudou.web.knowledge.rag.KnowledgeSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 知识库语义检索接口。
 */
@Tag(name = "知识库语义检索")
@Validated
@RestController
@RequestMapping("/knowledge/search")
@RequiredArgsConstructor
public class KnowledgeSearchController {

    private final KnowledgeSearchService searchService;

    @Operation(summary = "语义检索")
    @SaCheckPermission("knowledge:search")
    @PostMapping
    public R<List<ChunkHitVo>> search(@Validated @RequestBody KnowledgeSearchBo req) {
        return R.ok(searchService.search(req));
    }

    @Operation(summary = "检索测试历史")
    @SaCheckPermission("knowledge:search")
    @GetMapping("/history")
    public R<PageInfo<KbRetrievalLogVo>> history(@Validated KbRetrievalLogQueryBo query) {
        return R.ok(searchService.pageRetrievalHistory(query));
    }
}
