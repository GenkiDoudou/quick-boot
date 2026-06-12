package io.github.genkidoudou.web.knowledge.dto;

import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识库文档修改入参（上传由 multipart 接口处理）。
 */
@Data
@Schema(description = "知识库文档业务入参")
public class KbDocumentBo {

    @NotNull(message = "文档ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "文档ID")
    private Long docId;

    @NotNull(message = "知识库ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "所属知识库ID")
    private Long kbId;

    @NotBlank(message = "文档标题不能为空", groups = UpdateGroup.class)
    @Size(max = 200, message = "文档标题长度不能超过200", groups = UpdateGroup.class)
    @Schema(description = "展示标题")
    private String title;
}
