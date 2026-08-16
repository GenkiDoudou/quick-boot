package io.github.genkidoudou.common.mybatisplus;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 项目 Mapper 标记接口：继承 MyBatis-Plus {@link BaseMapper}，便于统一扫描与扩展。
 *
 * @param <T> 实体类型
 */
public interface BaseBaseMapper<T> extends BaseMapper<T> {
}
