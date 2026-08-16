package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * H5 首页个人快捷偏好，表 {@code sys_user_h5_home_shortcut}。
 * <p>无行表示未个性化，走系统默认 menu_id 列表。</p>
 */
@Data
@TableName("sys_user_h5_home_shortcut")
public class SysUserH5HomeShortcut implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /** 主键。 */
  @TableId(value = "id", type = IdType.ASSIGN_ID)
  private Long id;

  /** 用户 ID，对齐 {@code sys_user.user_id}。 */
  private String userId;

  /** 菜单 ID（H5 页面 C 节点）。 */
  private Long menuId;

  /** 展示顺序，升序。 */
  private Integer orderNum;

  /** 创建者。 */
  @TableField(fill = FieldFill.INSERT)
  private String createBy;

  /** 创建时间。 */
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  /** 更新者。 */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private String updateBy;

  /** 更新时间。 */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
}
