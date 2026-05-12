package ${packag}.service;

import ${parentPackage}.common.core.PageInfo;
import ${parentPackage}.system.dos.SysConfigDo;
import java.util.List;
/**
 * <p>
 * ${tableComment!} 服务类
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */

public interface I${className}Service  {



      /**
      * 分页查询
      * @since ${date}
      * @param ${className?uncap_first}Do 参数
      * @return
      */
      PageInfo<${className}Do> page(${className}Do ${className?uncap_first}Do);

       /**
       * 保存
       * @since ${date}
       * @param ${className?uncap_first}Do 参数
       * @return
       */
       Boolean save(${className}Do ${className?uncap_first}Do);

       /**
       * 根据id修改
       * @since 2026/1/8
       * @param ${className?uncap_first}Do 参数
       * @return
       */
       Boolean updateById(${className}Do ${className?uncap_first}Do);

       /**
       * 根据id查询
       * @since ${date}
       * @param id id
       * @return
       */
      ${className}Do getVoById(Long id);

       /**
       * 根据id集合查询
       * @since ${date}
       * @param ids id集合
       * @return
       */
       Boolean deleteByIds(List<Long> ids);


}
