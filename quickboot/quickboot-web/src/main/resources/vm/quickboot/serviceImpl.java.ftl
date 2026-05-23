package ${packageName}.${moduleName}.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import ${packageName}.${moduleName}.domain.${className};
import ${packageName}.${moduleName}.dto.${className}Bo;
import ${packageName}.${moduleName}.dto.${className}QueryBo;
import ${packageName}.${moduleName}.dto.${className}Vo;
import ${packageName}.${moduleName}.mapper.${className}Mapper;
import ${packageName}.${moduleName}.service.${className}Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ${tableComment!} 服务实现。
 */
@Service
public class ${className}ServiceImpl implements ${className}Service {

    private final ${className}Mapper mapper;

    public ${className}ServiceImpl(${className}Mapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PageInfo<${className}Vo> page(${className}QueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        LambdaQueryWrapper<${className}> w = Wrappers.lambdaQuery();
<#list queryColumns as col>
    <#if col.javaType == "String">
        w.like(StrUtil.isNotBlank(query.get${col.javaField?cap_first}()), ${className}::get${col.javaField?cap_first}, query.get${col.javaField?cap_first}());
    <#else>
        w.eq(query.get${col.javaField?cap_first}() != null, ${className}::get${col.javaField?cap_first}, query.get${col.javaField?cap_first}());
    </#if>
</#list>
        Page<${className}> mp = mapper.selectPage(new Page<>(pageNum, pageSize), w);
        List<${className}Vo> rows = new ArrayList<>(mp.getRecords().size());
        for (${className} row : mp.getRecords()) {
            rows.add(BeanUtil.copyProperties(row, ${className}Vo.class));
        }
        Page<${className}Vo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public ${className}Vo getById(<#if pkColumn??>${pkColumn.javaType}<#else>Long</#if> id) {
        ${className} row = mapper.selectById(id);
        return row == null ? null : BeanUtil.copyProperties(row, ${className}Vo.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(${className}Bo req) {
        ${className} entity = BeanUtil.copyProperties(req, ${className}.class);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        mapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(${className}Bo req) {
        ${className} entity = BeanUtil.copyProperties(req, ${className}.class);
        entity.setUpdateTime(LocalDateTime.now());
        mapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<<#if pkColumn??>${pkColumn.javaType}<#else>Long</#if>> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        mapper.deleteBatchIds(ids);
    }
}
