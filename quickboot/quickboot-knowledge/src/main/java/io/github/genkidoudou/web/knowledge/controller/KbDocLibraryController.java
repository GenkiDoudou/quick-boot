package io.github.genkidoudou.web.knowledge.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.knowledge.dto.KbDocLibraryFileQueryBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocLibraryFileVo;
import io.github.genkidoudou.web.knowledge.dto.KbDocLibraryFolderBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocLibraryFolderTreeVo;
import io.github.genkidoudou.web.knowledge.service.KbDocLibraryFileService;
import io.github.genkidoudou.web.knowledge.service.KbDocLibraryFolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识文档库管理接口。
 */
@Tag(name = "知识文档库管理")
@Validated
@RestController
@RequestMapping("/knowledge/library")
@RequiredArgsConstructor
public class KbDocLibraryController {

    private final KbDocLibraryFolderService folderService;
    private final KbDocLibraryFileService fileService;

    @Operation(summary = "文档库目录树")
    @SaCheckPermission("knowledge:library:list")
    @GetMapping("/folder/tree")
    public R<List<KbDocLibraryFolderTreeVo>> folderTree() {
        return R.ok(folderService.tree());
    }

    @Operation(summary = "新建文档库目录")
    @SaCheckPermission("knowledge:library:add")
    @PostMapping("/folder/add")
    public R<Long> folderAdd(@Validated(AddGroup.class) @RequestBody KbDocLibraryFolderBo req) {
        return R.ok(folderService.add(req));
    }

    @Operation(summary = "修改文档库目录")
    @SaCheckPermission("knowledge:library:edit")
    @PostMapping("/folder/update")
    public R<Void> folderUpdate(@Validated(UpdateGroup.class) @RequestBody KbDocLibraryFolderBo req) {
        folderService.update(req);
        return R.ok();
    }

    @Operation(summary = "删除文档库目录")
    @SaCheckPermission("knowledge:library:remove")
    @PostMapping("/folder/remove")
    public R<Void> folderRemove(
        @Parameter(description = "目录ID") @RequestParam @Min(1) Long folderId) {
        folderService.remove(folderId);
        return R.ok();
    }

    @Operation(summary = "文档库文件分页列表")
    @SaCheckPermission("knowledge:library:list")
    @GetMapping("/file/list")
    public R<PageInfo<KbDocLibraryFileVo>> fileList(@Validated KbDocLibraryFileQueryBo query) {
        return R.ok(fileService.page(query));
    }

    @Operation(summary = "上传文件到文档库")
    @SaCheckPermission("knowledge:library:upload")
    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<KbDocLibraryFileVo> fileUpload(
        @Parameter(description = "目录ID") @RequestParam @Min(0) Long folderId,
        @Parameter(description = "备注") @RequestParam(required = false) String remark,
        @Parameter(description = "文件") @RequestPart("file") MultipartFile file) {
        return R.ok(fileService.upload(folderId, file, remark));
    }

    @Operation(summary = "删除文档库文件")
    @SaCheckPermission("knowledge:library:remove")
    @PostMapping("/file/remove")
    public R<Void> fileRemove(@RequestBody List<Long> libFileIds) {
        fileService.removeBatch(libFileIds);
        return R.ok();
    }
}
