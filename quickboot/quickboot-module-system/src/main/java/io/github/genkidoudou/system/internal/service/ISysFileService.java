package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.system.internal.vo.SysFileUploadVo;
import io.github.genkidoudou.system.internal.vo.SysFileVo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

/**
 * 系统文件管理（仅管理端上传登记 {@code sys_file}）。
 */
public interface ISysFileService {

  /**
   * 分页列表（默认排除软删）。
   *
   * @param pageRequest 分页与条件（originalName / uploaderUserName / classify）
   * @return 分页结果
   */
  PageInfo<SysFileVo> page(PageRequest<SysFileVo> pageRequest);

  /**
   * 管理端上传：落盘后显式插入 {@code sys_file}；登记失败则删对象并失败。
   *
   * @param file     文件
   * @param classify 分类键
   * @return 上传结果
   */
  SysFileUploadVo upload(MultipartFile file, String classify);

  /**
   * 按相对路径打开预览流（须为未删登记记录）。
   *
   * @param relativePath 存储相对路径
   * @return 流载荷
   */
  DownloadPayload viewStream(String relativePath);

  /**
   * 按文件主键打开 inline 预览流（带可展示的 Content-Type）。
   *
   * @param fileId 主键
   * @return 流载荷
   */
  DownloadPayload preview(Long fileId);

  /**
   * 按文件主键下载。
   *
   * @param fileId 主键
   * @return 流载荷
   */
  DownloadPayload download(Long fileId);

  /**
   * 批量软删并删除本地对象；对象缺失视为成功。
   *
   * @param fileIds 主键集合
   */
  void remove(Collection<Long> fileIds);

  /**
   * 下载/预览载荷。
   *
   * @param resource     文件资源
   * @param originalName 展示用文件名
   * @param contentType  MIME，可空
   */
  record DownloadPayload(Resource resource, String originalName, String contentType) {
  }
}
