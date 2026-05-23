package io.github.genkidoudou.web.monitor.logininfor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.monitor.logininfor.domain.SysLogininfor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录访问日志 Mapper。
 */
@Mapper
public interface SysLogininforMapper extends BaseMapper<SysLogininfor> {
}
