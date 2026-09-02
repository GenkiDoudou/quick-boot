package ${packageName}.internal.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.mybatisplus.CrudServiceImpl;
import ${packageName}.internal.entity.${className};
import ${packageName}.internal.mapper.${className}Mapper;
import ${packageName}.internal.service.I${className}Service;
import ${packageName}.internal.vo.${className}Vo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ${tableComment!} 服务实现：继承 {@link CrudServiceImpl}，Entity 仅在内部流转。
 */
@Service
public class ${className}ServiceImpl extends CrudServiceImpl<${className}Mapper, ${className}, ${className}Vo>
  implements I${className}Service {

  @Override
  protected Class<${className}Vo> voClass() {
    return ${className}Vo.class;
  }

  @Override
  public void applyQuery(LambdaQueryWrapper<${className}> q, ${className}Vo param) {
    if (param == null) {
      return;
    }
<#list queryColumns as col>
  <#if col.javaType == "String">
    if (StrUtil.isNotBlank(param.get${col.javaField?cap_first}())) {
      q.like(${className}::get${col.javaField?cap_first}, param.get${col.javaField?cap_first}().trim());
    }
  <#else>
    if (param.get${col.javaField?cap_first}() != null) {
      q.eq(${className}::get${col.javaField?cap_first}, param.get${col.javaField?cap_first}());
    }
  </#if>
</#list>
  }

  @Override
  public PageInfo<${className}Vo> page(PageRequest<${className}Vo> pageRequest) {
    return crudPage(pageRequest);
  }

  @Override
  public ${className}Vo getDetail(<#if pkColumn??>${pkColumn.javaType}<#else>Long</#if> id) {
    return crudGetDetail(id, "${tableComment!}不存在");
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long add(${className}Vo vo) {
    ${className} entity = toEntity(vo);
<#if pkColumn??>
    entity.set${pkColumn.javaField?cap_first}(null);
</#if>
    this.save(entity);
<#if pkColumn?? && pkColumn.javaType == "Long">
    return entity.get${pkColumn.javaField?cap_first}();
<#else>
    return null;
</#if>
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean update(${className}Vo vo) {
    ${className} entity = toEntity(vo);
    return this.updateById(entity);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void remove(Collection<<#if pkColumn??>${pkColumn.javaType}<#else>Long</#if>> ids) {
    if (ids == null || ids.isEmpty()) {
      return;
    }
    List<<#if pkColumn??>${pkColumn.javaType}<#else>Long</#if>> distinct = ids.stream()
      .filter(Objects::nonNull)
      .distinct()
      .collect(Collectors.toList());
    if (!distinct.isEmpty()) {
      this.removeByIds(distinct);
    }
  }

  @Override
  public List<${className}Vo> export(${className}Vo query) {
    List<${className}> rows = crudListForQuery(query, ${className}Vo::getIds);
    return rows.stream().map(e -> toVo(e, ${className}Vo.class)).collect(Collectors.toList());
  }
}
