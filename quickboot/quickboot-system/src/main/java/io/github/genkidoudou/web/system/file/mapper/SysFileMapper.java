package io.github.genkidoudou.web.system.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.system.file.domain.SysFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统文件元数据 Mapper。
 */
@Mapper
public interface SysFileMapper extends BaseMapper<SysFile> {
}

