package ${packageName}.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import ${packageName}.internal.dto.${className}Bo;
import ${packageName}.internal.dto.${className}QueryBo;
import ${packageName}.internal.dto.${className}Vo;

import java.util.List;

/**
 * ${tableComment!} 服务。
 */
public interface ${className}Service {

    PageInfo<${className}Vo> page(${className}QueryBo query);

    ${className}Vo getById(<#if pkColumn??>${pkColumn.javaType}<#else>Long</#if> id);

    void add(${className}Bo req);

    void update(${className}Bo req);

    void removeBatch(List<<#if pkColumn??>${pkColumn.javaType}<#else>Long</#if>> ids);
}
