package io.github.genkidoudou.web.system.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.system.notice.domain.SysNotice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知公告 Mapper。
 */
@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {
}
