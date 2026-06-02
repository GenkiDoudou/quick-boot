package io.github.genkidoudou.web.system.file.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.file.domain.SysFile;
import io.github.genkidoudou.web.system.file.dto.SysFileQueryBo;
import io.github.genkidoudou.web.system.file.dto.SysFileUploadVo;
import io.github.genkidoudou.web.system.file.dto.SysFileVo;
import io.github.genkidoudou.common.file.FilePathSupport;
import io.github.genkidoudou.common.file.FileAccessService;
import io.github.genkidoudou.web.system.file.mapper.SysFileMapper;
import io.github.genkidoudou.web.system.file.service.SysFileService;
import io.github.genkidoudou.common.file.FileTemplate;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统文件管理服务实现。
 */
@Service
public class SysFileServiceImpl implements SysFileService {

    private final SysFileMapper mapper;
    private final FileTemplate fileTemplate;
    private final FileAccessService fileAccessService;

    public SysFileServiceImpl(SysFileMapper mapper, FileTemplate fileTemplate, FileAccessService fileAccessService) {
        this.mapper = mapper;
        this.fileTemplate = fileTemplate;
        this.fileAccessService = fileAccessService;
    }

    @Override
    public PageInfo<SysFileVo> page(SysFileQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        Integer deleted = query.getDeleted() == null ? 0 : query.getDeleted();
        LambdaQueryWrapper<SysFile> w = Wrappers.<SysFile>lambdaQuery()
            .eq(SysFile::getDeleted, deleted)
            .eq(StrUtil.isNotBlank(query.getClassify()), SysFile::getClassify, query.getClassify())
            .like(StrUtil.isNotBlank(query.getOriginalName()), SysFile::getOriginalName, query.getOriginalName())
            .like(StrUtil.isNotBlank(query.getUploaderUserName()), SysFile::getUploaderUserName, query.getUploaderUserName())
            .ge(query.getUploadTimeFrom() != null, SysFile::getUploadTime, query.getUploadTimeFrom())
            .le(query.getUploadTimeTo() != null, SysFile::getUploadTime, query.getUploadTimeTo())
            .orderByDesc(SysFile::getUploadTime);

        Page<SysFile> mp = mapper.selectPage(new Page<>(pageNum, pageSize), w);
        List<SysFileVo> rows = new ArrayList<>(mp.getRecords().size());
        for (SysFile row : mp.getRecords()) {
            rows.add(BeanUtil.copyProperties(row, SysFileVo.class));
        }
        Page<SysFileVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public SysFileUploadVo upload(MultipartFile file, String classify) {
        var result = fileAccessService.upload(file, classify);
        String relativePath = result.getRelativePath();
        SysFile row = mapper.selectOne(
            Wrappers.<SysFile>lambdaQuery().eq(SysFile::getRelativePath, relativePath).last("LIMIT 1")
        );
        if (row == null) {
            throw new WarningException(ErrorCodes.System.INTERNAL_ERROR, "文件登记记录缺失: " + relativePath);
        }
        SysFileUploadVo vo = new SysFileUploadVo();
        vo.setFileId(row.getFileId());
        vo.setFileName(result.getFileName());
        vo.setRelativePath(result.getRelativePath());
        vo.setAbsolutePath(result.getAbsolutePath());
        return vo;
    }

    @Override
    public DownloadPayload viewStream(String relativePath) {
        SysFile row = getValidRowByRelativePath(relativePath);
        Resource res = fileTemplate.download(row.getRelativePath());
        return new DownloadPayload(res, row.getOriginalName(), row.getContentType());
    }

    @Override
    public DownloadPayload download(Long fileId) {
        SysFile row = getValidRow(fileId);
        Resource res = fileTemplate.download(row.getRelativePath());
        return new DownloadPayload(res, row.getOriginalName(), row.getContentType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "删除文件ID不能为空");
        }
        List<SysFile> rows = mapper.selectList(
            Wrappers.<SysFile>lambdaQuery().in(SysFile::getFileId, fileIds)
        );
        if (rows.size() != fileIds.size()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "存在无效的文件ID");
        }
        String operator = currentOperator();
        LocalDateTime now = LocalDateTime.now();
        for (SysFile row : rows) {
            if (row.getDeleted() != null && row.getDeleted() == 1) {
                continue;
            }
            SysFile upd = new SysFile();
            upd.setFileId(row.getFileId());
            upd.setDeleted(1);
            upd.setDeleteBy(operator);
            upd.setDeleteTime(now);
            mapper.updateById(upd);
            try {
                fileTemplate.delete(row.getRelativePath());
            } catch (RuntimeException ignore) {
                // 对象不存在或删除失败：按设计允许“对象不存在视为成功”，失败场景由日志排查
            }
        }
    }

    private SysFile getValidRow(Long fileId) {
        if (fileId == null || fileId < 1) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "文件ID不能为空");
        }
        SysFile row = mapper.selectById(fileId);
        if (row == null || (row.getDeleted() != null && row.getDeleted() == 1)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "文件不存在或已删除");
        }
        return row;
    }

    private SysFile getValidRowByRelativePath(String relativePath) {
        if (StrUtil.isBlank(relativePath)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "文件路径不能为空");
        }
        String path = relativePath.trim();
        FilePathSupport.validateRelativePath(path);
        SysFile row = mapper.selectOne(
            Wrappers.<SysFile>lambdaQuery()
                .eq(SysFile::getRelativePath, path)
                .eq(SysFile::getDeleted, 0)
                .last("LIMIT 1")
        );
        if (row == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "文件不存在或已删除");
        }
        return row;
    }

    private static String currentOperator() {
        try {
            if (StpUtil.isLogin()) {
                return String.valueOf(StpUtil.getLoginId());
            }
        } catch (Exception ignored) {
            // 非 Web 线程或未登录
        }
        return "system";
    }
}

