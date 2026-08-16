package io.github.genkidoudou.system.internal.vo;

import cn.hutool.core.date.DatePattern;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单管理 VO（树节点可带 children）。
 */
@ExcelIgnoreUnannotated
@Data
public class SysMenuVo implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /** 菜单主键。 */
  @NotNull(message = "菜单ID不能为空", groups = UpdateGroup.class)
  @Null(message = "新增时菜单ID必须为空", groups = AddGroup.class)
  @ExcelProperty(value = "菜单ID", index = 0)
  private Long menuId;

  /** 上级菜单 ID。 */
  @NotNull(message = "上级菜单不能为空", groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty(value = "上级菜单ID", index = 1)
  private Long parentId;

  /** 菜单名称。 */
  @NotBlank(message = "菜单名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty(value = "菜单名称", index = 2)
  private String menuName;

  /** 菜单类型(sys_menu_menu_type)。 */
  @NotBlank(message = "菜单类型不能为空", groups = {AddGroup.class, UpdateGroup.class})
  @ExcelDictFormat(dictType = "sys_menu_menu_type")
  @ExcelProperty(value = "菜单类型", index = 3)
  private String menuType;

  /** 路由地址。 */
  @ExcelProperty(value = "路由地址", index = 4)
  private String path;

  /** 组件路径。 */
  @ExcelProperty(value = "组件路径", index = 5)
  private String component;

  /** 路由名称。 */
  @ExcelProperty(value = "路由名称", index = 6)
  private String routeName;

  /** 权限标识。 */
  @ExcelProperty(value = "权限标识", index = 7)
  private String perms;

  /** 菜单图标。 */
  @ExcelProperty(value = "图标", index = 8)
  private String icon;

  /** 显示顺序。 */
  @ExcelProperty(value = "显示顺序", index = 9)
  private Integer orderNum;

  /** 路由 query 参数。 */
  @ExcelProperty(value = "路由参数", index = 10)
  private String query;

  /** 0=否，1=外链 (sys_yes_no) */
  @ExcelDictFormat(dictType = "sys_yes_no")
  @ExcelProperty(value = "是否外链", index = 11)
  private String isFrame;

  /** 0=缓存，1=不缓存 (sys_menu_is_cache) */
  @ExcelDictFormat(dictType = "sys_menu_is_cache")
  @ExcelProperty(value = "是否缓存", index = 12)
  private String isCache;

  /** 0=显示，1=隐藏 (sys_show_hide) */
  @ExcelDictFormat(dictType = "sys_show_hide")
  @ExcelProperty(value = "显示状态", index = 13)
  private String visible;

  /** 0=正常，1=停用 */
  @NotBlank(message = "状态不能为空", groups = AddGroup.class)
  @ExcelDictFormat(dictType = "sys_normal_disable")
  @ExcelProperty(value = "状态", index = 14)
  private String status;

  /** 备注。 */
  @ExcelProperty(value = "备注", index = 15)
  private String remark;

  /** 创建时间。 */
  @ExcelProperty(value = "创建时间", index = 16)
  @com.alibaba.excel.annotation.format.DateTimeFormat(value = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
  @JsonFormat(pattern = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
  @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
  private LocalDateTime createTime;

  /** 子菜单节点。 */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<SysMenuVo> children = new ArrayList<>();

  /**
   * 勾选主键；非空时忽略搜索条件
   */
  private List<Long> ids;
}
