package io.github.genkidoudou.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单下拉树节点（id/label 契约，供前端 TreeSelect）。
 * <p>id 序列化为字符串，避免与前端 valueType=string 类型不一致导致不反显。</p>
 */
@Data
public class MenuTreeSelectVo {

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Long id;

  private String label;

  private List<MenuTreeSelectVo> children = new ArrayList<>();
}
