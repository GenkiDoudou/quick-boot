package io.github.genkidoudou.web.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.knowledge.domain.KbDocLibraryFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档库文件 Mapper。
 */
@Mapper
public interface KbDocLibraryFileMapper extends BaseMapper<KbDocLibraryFile> {
}
