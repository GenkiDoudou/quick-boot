package io.github.genkidoudou.web.system.menu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点（管理端列表）。
 */
@Data
@Schema(description = "菜单树节点")
public class SysMenuTreeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long menuId;

    private Long parentId;

    private String menuType;

    private String menuName;

    private Integer orderNum;

    private String path;

    private String component;

    private String routeName;

    private String perms;

    private String icon;

    private String visible;

    private String status;

    private String isFrame;

    private String isCache;

    private String remark;

    private String createBy;

    private LocalDateTime createTime;

    @Schema(description = "子节点")
    private List<SysMenuTreeVo> children = new ArrayList<>();
}
