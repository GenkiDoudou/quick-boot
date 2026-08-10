package io.github.genkidoudou.system.internal.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单下拉树节点（id/label 契约，供前端 TreeSelect）。
 * <p>{@code id} 由全局 Jackson Long→String 序列化，与前端 valueType=string 对齐。</p>
 */
@Data
public class MenuTreeSelectVo {

  private Long id;

  private String label;

  private List<MenuTreeSelectVo> children = new ArrayList<>();
}
