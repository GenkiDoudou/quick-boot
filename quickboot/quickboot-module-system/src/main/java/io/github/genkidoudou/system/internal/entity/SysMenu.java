package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.genkidoudou.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统菜单，表 {@code sys_menu}。
 * <p>
 * menu_type：M=目录，C=菜单，F=按钮；status：0=正常，1=停用；
 * visible：0=显示，1=隐藏；is_frame：0=否，1=外链；is_cache：0=缓存，1=不缓存。
 * </p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_menu")
public class SysMenu extends BaseEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @TableId(value = "menu_id", type = IdType.ASSIGN_ID)
  private Long menuId;

  private Long parentId;

  private String menuName;

  /** M / C / F */
  private String menuType;

  private String path;

  private String component;

  private String routeName;

  private String perms;

  private String icon;

  private Integer orderNum;

  /** 路由 query 串 */
  private String query;

  /** 0=否，1=外链 */
  private String isFrame;

  /** 0=缓存，1=不缓存 */
  private String isCache;

  /** 0=显示，1=隐藏 */
  private String visible;

  /** 0=正常，1=停用 */
  private String status;
}
