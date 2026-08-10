package io.github.genkidoudou.system.api;

/**
 * 跨模块用户只读视图（不含密码等敏感字段）。
 *
 * @param userId   用户 ID
 * @param userName 用户名
 * @param nickName 昵称
 * @param status   状态（{@code 0}/{@code 1}）
 * @param deptId   部门 ID，可为 {@code null}
 */
public record SysUserView(
  Long userId,
  String userName,
  String nickName,
  String status,
  Long deptId
) {
}
