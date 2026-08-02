package io.github.genkidoudou.system.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单导入行（扁平行；可选 menuId）。
 */
@ExcelIgnoreUnannotated
@Data
public class SysMenuImportRow implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @ExcelProperty(value = "菜单ID", index = 0)
  private Long menuId;

  @ExcelProperty(value = "上级菜单ID", index = 1)
  private Long parentId;

  @ExcelProperty(value = "菜单名称", index = 2)
  private String menuName;

  @ExcelProperty(value = "菜单类型", index = 3)
  private String menuType;

  @ExcelProperty(value = "路由地址", index = 4)
  private String path;

  @ExcelProperty(value = "组件路径", index = 5)
  private String component;

  @ExcelProperty(value = "路由名称", index = 6)
  private String routeName;

  @ExcelProperty(value = "权限标识", index = 7)
  private String perms;

  @ExcelProperty(value = "图标", index = 8)
  private String icon;

  @ExcelProperty(value = "显示顺序", index = 9)
  private Integer orderNum;

  @ExcelProperty(value = "路由参数", index = 10)
  private String query;

  @ExcelProperty(value = "是否外链", index = 11)
  private String isFrame;

  @ExcelProperty(value = "是否缓存", index = 12)
  private String isCache;

  @ExcelProperty(value = "显示状态", index = 13)
  private String visible;

  @ExcelProperty(value = "状态", index = 14)
  private String status;

  @ExcelProperty(value = "备注", index = 15)
  private String remark;
}
