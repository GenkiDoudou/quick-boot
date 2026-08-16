package io.github.genkidoudou.system.api;

/**
 * 用户只读查询门面（跨模块消费入口；不暴露持久化实体）。
 */
public interface SysUserQueryFacade {

  /**
   * 按用户名查询用户视图。
   *
   * @param username 用户名
   * @return 用户视图；不存在时返回 {@code null}
   */
  SysUserView findByUserName(String username);

  /**
   * 按用户 ID 查询用户视图。
   *
   * @param userId 用户 ID
   * @return 用户视图；不存在时返回 {@code null}
   */
  SysUserView findByUserId(Long userId);

  /**
   * 未删除用户总数（逻辑删除已生效，对齐态势总览「总用户」口径）。
   *
   * @return 用户数，永不 {@code null}
   */
  long countActiveUsers();
}
