package io.github.genkidoudou.web.system.menu.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单实体，与表 {@code sys_menu} 对应。
 * <p>
 * {@code parentId = -1} 表示顶级；{@code menuType}：M 目录、C 菜单、F 按钮；{@code delFlag} 与全局逻辑删除一致。
 */
@Data
@TableName("sys_menu")
public class SysMenu implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "menu_id", type = IdType.ASSIGN_ID)
    private Long menuId;

    private Long parentId;

    /** M 目录 / C 菜单 / F 按钮 */
    private String menuType;

    private String menuName;

    private Integer orderNum;

    private String path;

    private String component;

    private String query;

    /** 路由 name，供前端 keep-alive 与路由表使用 */
    private String routeName;

    /** 是否外链：0 否 1 是 */
    private String isFrame;

    /** 是否缓存：0 缓存 1 不缓存（与若依 is_cache 一致） */
    private String isCache;

    /** 显示状态：0 显示 1 隐藏 */
    private String visible;

    /** 菜单状态：0 正常 1 停用 */
    private String status;

    /** 权限标识；多个以英文逗号分隔入库，如 {@code a:b:c,x:y:z} */
    private String perms;

    private String icon;

    private String remark;

    @TableLogic
    private String delFlag;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
