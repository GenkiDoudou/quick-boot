package io.github.genkidoudou.system.vo;

import cn.hutool.core.date.DatePattern;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
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

  @NotNull(message = "菜单ID不能为空", groups = UpdateGroup.class)
  @Null(message = "新增时菜单ID必须为空", groups = AddGroup.class)
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  @ExcelProperty(value = "菜单ID", index = 0)
  private Long menuId;

  @NotNull(message = "上级菜单不能为空", groups = {AddGroup.class, UpdateGroup.class})
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  @ExcelProperty(value = "上级菜单ID", index = 1)
  private Long parentId;

  @NotBlank(message = "菜单名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty(value = "菜单名称", index = 2)
  private String menuName;

  @NotBlank(message = "菜单类型不能为空", groups = {AddGroup.class, UpdateGroup.class})
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

  /** 0=否，1=外链 */
  @ExcelProperty(value = "是否外链", index = 11)
  private String isFrame;

  /** 0=缓存，1=不缓存 */
  @ExcelProperty(value = "是否缓存", index = 12)
  private String isCache;

  /** 0=显示，1=隐藏 */
  @ExcelProperty(value = "显示状态", index = 13)
  private String visible;

  /** 0=正常，1=停用 */
  @NotBlank(message = "状态不能为空", groups = AddGroup.class)
  @ExcelProperty(value = "状态", index = 14)
  private String status;

  @ExcelProperty(value = "备注", index = 15)
  private String remark;

  @ExcelProperty(value = "创建时间", index = 16)
  @com.alibaba.excel.annotation.format.DateTimeFormat(value = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
  @JsonFormat(pattern = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
  @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
  private LocalDateTime createTime;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<SysMenuVo> children = new ArrayList<>();

  /**
   * 勾选主键；非空时忽略搜索条件
   */
  private List<Long> ids;
}
