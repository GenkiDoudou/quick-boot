package io.github.genkidoudou.system.vo;

import lombok.Data;

import java.util.List;

/**
 * 批量保存菜单排序请求体。
 */
@Data
public class MenuSortVo {

  private List<Long> menuIds;

  private List<Integer> orderNums;
}
