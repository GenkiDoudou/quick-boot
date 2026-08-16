package io.github.genkidoudou.system.internal.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * H5 工作台分组（目录 M → 九宫格子项）。
 */
@Data
public class H5WorkbenchGroupVo {

  /** 分组菜单 id */
  private String id;

  /** 分组标题（menu_name） */
  private String title;

  /** 排序 */
  private Integer orderNum;

  private List<H5WorkbenchItemVo> items = new ArrayList<>();
}
