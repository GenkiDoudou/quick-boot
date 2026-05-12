package com.su60.quickboot.system.dos;

import com.su60.quickboot.common.validation.UpdateGroup;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 系统访问记录
 * </p>
 *
 * @author luyanan
 * @since 2024/11/15
 */
@Data
@Accessors(chain = true)
public class SysLogininforDo implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * 访问ID
	 *
	 * @since 2024/11/15
	 */
	@NotNull(groups = UpdateGroup.class, message = "访问ID 不能为空")
	private Long id;


	/**
	 * 用户账号
	 *
	 * @since 2024/11/15
	 */
	private String userName;


	/**
	 * 登录IP地址
	 *
	 * @since 2024/11/15
	 */
	private String ipaddr;


	/**
	 * 登录地点
	 *
	 * @since 2024/11/15
	 */
	private String loginLocation;


	/**
	 * 浏览器类型
	 *
	 * @since 2024/11/15
	 */
	private String browser;


	/**
	 * 操作系统
	 *
	 * @since 2024/11/15
	 */
	private String os;


	/**
	 * 访问时间
	 *
	 * @since 2024/11/15
	 */
	private Date loginTime;


	/**
	 * 用户id
	 *
	 * @since 2024/11/15
	 */

	private Long userId;


	/**
	 * 登录/注销
	 * @since 2025/12/21
	 */

	private String loginType;
	/**
	 * 结果(成功/失败)
	 * @since 2025/12/21
	 */

	private String loginResult;
	/**
	 * 客户端id
	 *
	 * @since 2024/11/15
	 */

	private String clientId;


	/**
	 * 登录时间
	 *
	 * @since 2024/11/15
	 */

	private List<String> loginTimes;
}
