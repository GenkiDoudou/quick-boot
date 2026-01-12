package ${packag}.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import ${parentPackage}.common.core.PageInfo;
import ${packag}.entity.${className}Entity;
import ${packag}.dos.${className}Do;
import ${packag}.mapper.${className}Mapper;
import ${packag}.service.I${className}Service;
import  com.su60.quickboot.data.mybatisplus.BaseVoServiceImpl;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import ${parentPackage}.data.mybatisplus.PageVoHandler;

import java.util.List;

/**
 * <p>
 * ${tableComment!} 服务实现类
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
@RequiredArgsConstructor
@Service
public class ${className}ServiceImpl extends BaseVoServiceImpl<${className}Mapper, ${className}Entity, ${className}Do> implements I${className}Service {


   @Override
   public PageInfo<${className}Do> page(${className}Do ${className?uncap_first}Do) {
    return super.page(sysConfigDo, new PageVoHandler<${className}Entity, ${className}Do>() {
    @Override
    public void queryWrapperHandler(${className}Do vo, ${className}Entity ${className?uncap_first}Entity, LambdaQueryWrapper<${className}Entity> queryWrapper) {
     <#list searchFields as searchField >
      <#if  searchField.queryType == 'LIKE' >
       queryWrapper.like(StrUtil.isNotBlank(vo.get${searchField.javaField?cap_first}()),${className}Entity::get${searchField.javaField?cap_first}, vo.get${searchField.javaField?cap_first}());
       ${className?uncap_first}Entity.set${searchField.javaField?cap_first}(null);
      </#if>
     </#list>

     }
     });
     }

     @Override
     public Boolean save(${className}Do ${className?uncap_first}Do) {
     return super.saveVo(sysConfigDo);
     }

     @Override
     public Boolean updateById(${className}Do ${className?uncap_first}Do) {
     return super.updateVoById(sysConfigDo);
     }

     @Override
     public ${className}Do getVoById(Long id) {
     return super.getVoById(id);
     }

     @Override
     public Boolean deleteByIds(List<Long> ids) {
      return super.deleteByIds(ids);
      }
}

