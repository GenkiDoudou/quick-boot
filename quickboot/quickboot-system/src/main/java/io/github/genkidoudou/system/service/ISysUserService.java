package io.github.genkidoudou.system.service;

import io.github.genkidoudou.system.entity.SysUser;

public interface ISysUserService {


  /**
   * 根据用户名查询用户
   *
   * @param username 用户名
   * @return 用户；不存在则 null
   * @since 2026/7/27
   */
  SysUser findByUserName(String username);

  /**
   * 根据用户 ID 查询。
   *
   * @param userId 用户主键
   * @return 用户；不存在则 null
   */
  SysUser findByUserId(Long userId);

}
