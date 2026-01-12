package ${package}.service.impl;

import ${package}.entity.${className}Entity;
import ${package}.dos.${className}Do;
import ${package}.mapper.${className}Mapper;
import ${package}.service.I${className}Service;
import com.su60.quickboot.data.mybatisplus.BaseServiceImpl2;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
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

public class ${className}ServiceImpl extends BaseServiceImpl2<${className}Mapper, ${className}Entity, ${className}Do> implements I${className}Service {

}

