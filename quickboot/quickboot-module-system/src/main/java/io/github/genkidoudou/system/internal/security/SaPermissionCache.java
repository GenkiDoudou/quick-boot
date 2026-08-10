package io.github.genkidoudou.system.internal.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * sa-token Account-Session 中的权限/角色缓存键与失效工具。
 * <p>框架默认不缓存 {@link cn.dev33.satoken.stp.StpInterface} 结果；写入 Session 后，
 * 业务变更角色菜单或用户角色时须调用本类清理，避免鉴权读到过期数据。</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SaPermissionCache {

  /** Account-Session：权限字符列表 */
  public static final String KEY_PERMISSION_LIST = "qb:Permission_List";

  /** Account-Session：角色标识列表 */
  public static final String KEY_ROLE_LIST = "qb:Role_List";

  /**
   * 清除指定登录账号的权限/角色缓存（未登录则忽略）。
   *
   * @param loginId sa-token 登录 id（与 {@code StpUtil.login} 写入的类型一致，常见为 Long）
   */
  public static void clearByLoginId(Object loginId) {
    if (loginId == null) {
      return;
    }
    if (!StpUtil.isLogin(loginId)) {
      return;
    }
    SaSession session = StpUtil.getSessionByLoginId(loginId, false);
    if (session == null) {
      return;
    }
    session.delete(KEY_PERMISSION_LIST);
    session.delete(KEY_ROLE_LIST);
  }

  /**
   * 按业务用户主键批量清理（兼容 String / 可解析 Long，因登录 id 多为 Long）。
   *
   * @param userIds {@code sys_user.user_id} 列表
   */
  public static void clearByUserIds(Collection<String> userIds) {
    if (CollUtil.isEmpty(userIds)) {
      return;
    }
    for (String userId : userIds) {
      if (StrUtil.isBlank(userId)) {
        continue;
      }
      String trimmed = userId.trim();
      clearByLoginId(trimmed);
      try {
        clearByLoginId(Long.parseLong(trimmed));
      } catch (NumberFormatException ignored) {
        // 非数字主键仅按字符串尝试
      }
    }
  }
}
