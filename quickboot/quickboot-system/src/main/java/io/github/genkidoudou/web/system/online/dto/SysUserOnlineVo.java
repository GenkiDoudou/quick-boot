package io.github.genkidoudou.web.system.online.dto;

import lombok.Data;

/**
 * 在线会话视图。
 */
@Data
public class SysUserOnlineVo {

    /** 会话编号（token 值）。 */
    private String tokenId;

    /** 用户主键。 */
    private Long userId;

    /** 登录名称。 */
    private String userName;

    /** 部门名称。 */
    private String deptName;

    /** 主机 IP。 */
    private String ipaddr;

    /** 登录地点。 */
    private String loginLocation;

    /** 浏览器。 */
    private String browser;

    /** 操作系统。 */
    private String os;

    /** 登录时间（ISO-8601 本地时间字符串）。 */
    private String loginTime;
}
