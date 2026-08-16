package io.github.genkidoudou.system.internal.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;

import io.github.genkidoudou.system.internal.entity.SysOauthClient;
import org.apache.ibatis.annotations.Mapper;

/**
 * OAuth 客户端表 Mapper。
 */
@Mapper
public interface SysOauthClientMapper extends BaseBaseMapper<SysOauthClient> {
}
