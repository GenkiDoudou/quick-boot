package ${packageName}.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import ${packageName}.internal.vo.${className}Vo;

import java.util.Collection;
import java.util.List;

/**
 * ${tableComment!} 服务（公开 API 仅 Vo）。
 */
public interface I${className}Service {

  /**
   * 分页查询。
   *
   * @param pageRequest 分页与筛选
   * @return Vo 分页
   */
  PageInfo<${className}Vo> page(PageRequest<${className}Vo> pageRequest);

  /**
   * 详情。
   *
   * @param id 主键
   * @return Vo
   */
  ${className}Vo getDetail(<#if pkColumn??>${pkColumn.javaType}<#else>Long</#if> id);

  /**
   * 新增。
   *
   * @param vo 可写字段
   * @return 新建主键
   */
  Long add(${className}Vo vo);

  /**
   * 修改。
   *
   * @param vo 含主键
   * @return 是否成功
   */
  boolean update(${className}Vo vo);

  /**
   * 批量删除。
   *
   * @param ids 主键集合
   */
  void remove(Collection<<#if pkColumn??>${pkColumn.javaType}<#else>Long</#if>> ids);

  /**
   * 导出列表（按条件或 ids）。
   *
   * @param query 查询条件
   * @return Vo 列表
   */
  List<${className}Vo> export(${className}Vo query);
}
