package io.github.genkidoudou.system.internal.api;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.system.api.SysUserQueryFacade;
import io.github.genkidoudou.system.api.SysUserView;
import io.github.genkidoudou.system.internal.entity.SysUser;
import io.github.genkidoudou.system.internal.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {@link SysUserQueryFacade} 实现：经 Mapper 查实体并映射为跨模块 View（不经公开 Service 暴露 Entity）。
 */
@Service
@RequiredArgsConstructor
public class SysUserQueryFacadeImpl implements SysUserQueryFacade {

  private final SysUserMapper userMapper;

  @Override
  public SysUserView findByUserName(String username) {
    if (StrUtil.isBlank(username)) {
      return null;
    }
    SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
      .eq(SysUser::getUserName, username.trim()), false);
    return toView(user);
  }

  @Override
  public SysUserView findByUserId(Long userId) {
    if (userId == null) {
      return null;
    }
    return toView(userMapper.selectById(userId));
  }

  @Override
  public long countActiveUsers() {
    Long n = userMapper.selectCount(null);
    return n == null ? 0L : n;
  }

  private static SysUserView toView(SysUser user) {
    if (user == null) {
      return null;
    }
    return new SysUserView(
      user.getUserId(),
      user.getUserName(),
      user.getNickName(),
      user.getStatus(),
      user.getDeptId()
    );
  }
}
