package io.github.genkidoudou.web.system.file.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.system.file.dto.SysFileQueryBo;
import io.github.genkidoudou.web.system.file.dto.SysFileUploadVo;
import io.github.genkidoudou.web.system.file.dto.SysFileVo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 系统文件管理服务。
 */
public interface SysFileService {

    /**
     * 文件分页列表（默认仅未删除）。
     */
    PageInfo<SysFileVo> page(SysFileQueryBo query);

    /**
     * 上传文件并返回登记记录。
     */
    SysFileUploadVo upload(MultipartFile file, String classify);

    /**
     * 上传字节内容并返回登记记录（用于导入失败明细等程序生成文件）。
     */
    SysFileUploadVo uploadBytes(byte[] content, String filename, String classify);

    /**
     * 按相对路径读取文件流（须为已登记且未删除的文件），供浏览器 inline 预览。
     *
     * @param relativePath 存储相对路径
     */
    DownloadPayload viewStream(String relativePath);

    /**
     * 获取下载资源与文件名。
     */
    DownloadPayload download(Long fileId);

    /**
     * 批量删除：逻辑删 + 同步删除存储对象。
     */
    void removeBatch(List<Long> fileIds);

    /**
     * 下载载荷（资源 + 文件名 + contentType）。
     */
    record DownloadPayload(Resource resource, String originalName, String contentType) {
    }
}

