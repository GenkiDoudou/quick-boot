package io.github.genkidoudou.system.internal.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.file.FileAccessService;
import io.github.genkidoudou.common.file.FilePathSupport;
import io.github.genkidoudou.common.file.FileTemplate;
import io.github.genkidoudou.common.file.FileUploadResult;
import io.github.genkidoudou.common.mybatisplus.BaseServiceImpl;
import io.github.genkidoudou.common.security.utils.LoginUserUtils;
import io.github.genkidoudou.common.security.vo.LoginUser;
import io.github.genkidoudou.system.internal.entity.SysFile;
import io.github.genkidoudou.system.internal.mapper.SysFileMapper;
import io.github.genkidoudou.system.internal.service.ISysFileService;
import io.github.genkidoudou.system.internal.vo.SysFileUploadVo;
import io.github.genkidoudou.system.internal.vo.SysFileVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 系统文件管理实现：仅本服务上传路径写入 {@code sys_file}。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysFileServiceImpl extends BaseServiceImpl<SysFileMapper, SysFile>
  implements ISysFileService {

  private final FileAccessService fileAccessService;
  private final FileTemplate fileTemplate;

  @Override
  public PageInfo<SysFileVo> page(PageRequest<SysFileVo> pageRequest) {
    SysFileVo param = pageRequest != null ? pageRequest.getParam() : null;
    return this.page(pageRequest, q -> {
      if (param == null) {
        q.orderByDesc(SysFile::getUploadTime);
        return;
      }
      if (StrUtil.isNotBlank(param.getOriginalName())) {
        q.like(SysFile::getOriginalName, param.getOriginalName().trim());
      }
      if (StrUtil.isNotBlank(param.getUploaderUserName())) {
        q.like(SysFile::getUploaderUserName, param.getUploaderUserName().trim());
      }
      if (StrUtil.isNotBlank(param.getClassify())) {
        q.eq(SysFile::getClassify, param.getClassify().trim());
      }
      q.orderByDesc(SysFile::getUploadTime);
    }, SysFileVo.class);
  }

  /**
   * 落盘成功后登记 sys_file；登记失败则回滚删除已上传对象。
   */
  @Override
  public SysFileUploadVo upload(MultipartFile file, String classify) {
    FileUploadResult result = fileAccessService.upload(file, classify);
    String relativePath = result.getRelativePath();
    try {
      SysFile row = new SysFile();
      row.setOriginalName(StrUtil.blankToDefault(file.getOriginalFilename(), result.getFileName()));
      row.setExt(FilePathSupport.normalizeExtension(row.getOriginalName()));
      row.setSizeBytes(file.getSize());
      row.setContentType(FileAccessService.resolveContentType(file.getContentType(), row.getOriginalName()));
      row.setClassify(result.getClassify());
      row.setRelativePath(relativePath);
      LoginUser loginUser = LoginUserUtils.getLoginUser();
      if (loginUser != null && loginUser.getUserId() != null) {
        row.setUploaderUserId(loginUser.getUserId());
        row.setUploaderUserName(StrUtil.blankToDefault(loginUser.getUsername(), ""));
      } else {
        row.setUploaderUserId(0L);
        row.setUploaderUserName("");
      }
      row.setUploadTime(LocalDateTime.now());
      boolean saved = this.save(row);
      if (!saved || row.getFileId() == null) {
        throw WarningException.literal(ErrorCodes.System.INTERNAL_ERROR, "文件登记失败");
      }
      SysFileUploadVo vo = new SysFileUploadVo();
      vo.setFileId(row.getFileId());
      vo.setFileName(row.getOriginalName());
      vo.setRelativePath(relativePath);
      vo.setAbsolutePath(result.getAbsolutePath());
      vo.setClassify(result.getClassify());
      return vo;
    } catch (RuntimeException ex) {
      try {
        fileTemplate.delete(relativePath);
      } catch (RuntimeException deleteEx) {
        log.warn("登记失败后回滚对象失败: {}", relativePath, deleteEx);
      }
      if (ex instanceof WarningException) {
        throw ex;
      }
      throw WarningException.literal(ErrorCodes.System.INTERNAL_ERROR,
        "文件登记失败: " + ex.getMessage());
    }
  }

  @Override
  public DownloadPayload viewStream(String relativePath) {
    SysFile row = getValidRowByRelativePath(relativePath);
    return toPayload(row);
  }

  @Override
  public DownloadPayload preview(Long fileId) {
    return toPayload(getValidRow(fileId));
  }

  @Override
  public DownloadPayload download(Long fileId) {
    return toPayload(getValidRow(fileId));
  }

  private DownloadPayload toPayload(SysFile row) {
    Resource res = fileTemplate.download(row.getRelativePath());
    String contentType = FileAccessService.resolveContentType(row.getContentType(), row.getOriginalName());
    return new DownloadPayload(res, row.getOriginalName(), contentType);
  }

  /**
   * 软删文件记录并尝试删除本地对象；对象缺失仍视为成功。
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void remove(Collection<Long> fileIds) {
    if (CollectionUtil.isEmpty(fileIds)) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "删除文件 ID 不能为空");
    }
    List<Long> idList = fileIds.stream()
      .filter(Objects::nonNull)
      .distinct()
      .collect(Collectors.toList());
    if (idList.isEmpty()) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "删除文件 ID 不能为空");
    }
    List<SysFile> rows = this.listByIds(idList);
    if (rows.size() != idList.size()) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "存在无效的文件 ID");
    }
    for (SysFile row : rows) {
      this.removeById(row.getFileId());
      try {
        fileTemplate.delete(row.getRelativePath());
      } catch (RuntimeException ignore) {
        // 对象不存在或删除失败：按设计仍视为成功
        log.debug("删除本地对象忽略异常: {}", row.getRelativePath(), ignore);
      }
    }
  }

  private SysFile getValidRow(Long fileId) {
    if (fileId == null || fileId < 1) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "文件 ID 不能为空");
    }
    SysFile row = this.getById(fileId);
    if (row == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "文件不存在或已删除");
    }
    return row;
  }

  private SysFile getValidRowByRelativePath(String relativePath) {
    if (StrUtil.isBlank(relativePath)) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "文件路径不能为空");
    }
    String path = relativePath.trim();
    FilePathSupport.validateRelativePath(path);
    SysFile row = this.getOne(new LambdaQueryWrapper<SysFile>()
      .eq(SysFile::getRelativePath, path)
      .last("LIMIT 1"));
    if (row == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "文件不存在或已删除");
    }
    return row;
  }
}
