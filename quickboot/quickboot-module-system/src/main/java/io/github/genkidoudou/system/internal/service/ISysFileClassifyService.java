package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.system.internal.vo.SysFileClassifyVo;

import java.util.Collection;
import java.util.List;

/**
 * 文件分类管理（公开 API 仅 Vo）。
 */
public interface ISysFileClassifyService {

  /**
   * 分页查询。
   *
   * @param pageRequest 分页与条件
   * @return 分页结果
   */
  PageInfo<SysFileClassifyVo> page(PageRequest<SysFileClassifyVo> pageRequest);

  /**
   * 详情。
   *
   * @param classifyId 主键
   * @return Vo
   */
  SysFileClassifyVo getDetail(Long classifyId);

  /**
   * 按分类键查询（含停用；软删不返回）。
   *
   * @param classify 分类键
   * @return Vo；不存在则 {@code null}
   */
  SysFileClassifyVo getByClassifyKey(String classify);

  /**
   * 列出启用中的分类（未删且 status=0）。
   *
   * @return Vo 列表
   */
  List<SysFileClassifyVo> listEnabled();

  /**
   * 新增。
   *
   * @param vo 可写字段
   * @return 主键
   */
  Long add(SysFileClassifyVo vo);

  /**
   * 修改；不可变更 classify 键。
   *
   * @param vo 含主键
   * @return 是否成功
   */
  boolean update(SysFileClassifyVo vo);

  /**
   * 批量软删；若仍有未删 {@code sys_file} 引用对应 classify 则拒绝。
   *
   * @param ids 主键集合
   */
  void remove(Collection<Long> ids);
}
