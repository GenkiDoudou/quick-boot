package io.github.genkidoudou.web.knowledge.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.web.knowledge.dto.KnowledgeChatBo;
import io.github.genkidoudou.web.knowledge.dto.KnowledgeChatVo;
import io.github.genkidoudou.web.knowledge.rag.KnowledgeChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库 RAG 问答接口。
 */
@Tag(name = "知识库RAG问答")
@Validated
@RestController
@RequestMapping("/knowledge/chat")
@RequiredArgsConstructor
public class KnowledgeChatController {

    private final KnowledgeChatService chatService;

    @Operation(summary = "RAG 问答")
    @SaCheckPermission("knowledge:chat")
    @PostMapping
    public R<KnowledgeChatVo> chat(@Validated @RequestBody KnowledgeChatBo req) {
        return R.ok(chatService.chat(req));
    }
}
