package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.system.internal.vo.H5WorkbenchItemVo;

import java.util.List;

/**
 * H5 首页快捷：候选池、最终宫格解析与个人偏好保存。
 */
public interface ISysH5HomeShortcutService {

  /** 首页快捷数量上限 */
  int MAX_SHORTCUTS = 8;

  /**
   * 当前用户最终宫格（偏好或默认 ∩ 授权候选，≤8）。
   *
   * @param userId 用户主键字符串
   * @return 有序入口
   */
  List<H5WorkbenchItemVo> listFinalShortcuts(String userId);

  /**
   * 当前用户可选候选池（与工作台 H5 C 叶子同源）。
   *
   * @param userId 用户主键字符串
   * @return 扁平入口
   */
  List<H5WorkbenchItemVo> listCandidates(String userId);

  /**
   * 全量覆盖个人偏好。
   * <p>空列表清除偏好行（恢复默认）；非空须 ⊆ 候选且 ≤ {@link #MAX_SHORTCUTS}。</p>
   *
   * @param userId  用户主键字符串
   * @param menuIds 有序菜单 id（可为字符串数字）
   */
  void saveShortcuts(String userId, List<String> menuIds);
}
