package io.github.genkidoudou.system.internal.vo;

import lombok.Data;

/**
 * H5 工作台入口项（菜单 C 且 path 以 /pages/ 开头）。
 */
@Data
public class H5WorkbenchItemVo {

  /** 菜单 id */
  private String id;

  /** 展示名称 */
  private String label;

  /** uni 页面路径，如 /pages/system/user/index */
  private String path;

  /** 菜单图标（可选） */
  private String icon;

  /** 排序 */
  private Integer orderNum;
}
