package ${packageName}.${moduleName}.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ${packageName}.${moduleName}.domain.${className};
import org.apache.ibatis.annotations.Mapper;

/**
 * ${tableComment!} Mapper。
 */
@Mapper
public interface ${className}Mapper extends BaseMapper<${className}> {
}
