package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档库目录树节点。
 */
@Data
@Schema(description = "文档库目录树节点")
public class KbDocLibraryFolderTreeVo {

    @Schema(description = "目录ID")
    private Long folderId;

    @Schema(description = "父目录ID")
    private Long parentId;

    @Schema(description = "目录名称")
    private String name;

    @Schema(description = "排序号")
    private Integer orderNum;

    @Schema(description = "子目录")
    private List<KbDocLibraryFolderTreeVo> children = new ArrayList<>();
}
