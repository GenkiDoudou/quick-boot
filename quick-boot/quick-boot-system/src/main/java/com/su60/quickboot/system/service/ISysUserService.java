package com.su60.quickboot.system.service;

import com.su60.quickboot.system.entity.SysUserEntity;
import com.su60.quickboot.system.dos.SysUserDo;
import com.su60.quickboot.data.mybatisplus.IBaseService2;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */

public interface ISysUserService extends IBaseService2<SysUserEntity, SysUserDo> {

	/**
	 * 根据用户名查询
	 *
	 * @param username 用户名
	 * @return 用户信息
	 * @since 2024/8/7
	 */
	SysUserDo findByUserName(String username);

	/**
	 * 用户信息保存
	 *
	 * @param sysUserDo 用户信息
	 * @return 是否成功
	 * @since 2024/10/14
	 */
	Boolean saveUser(SysUserDo sysUserDo);


	/**
	 * 修改用户信息
	 *
	 * @param sysUserDo 用户信息
	 * @return 是否成功
	 * @since 2024/10/14
	 */
	Boolean updateUser(SysUserDo sysUserDo);

	/**
	 * 重置密码
	 * @since 2025/11/18
	 * @param userId 用户id
	 * @return
	 */
	void resetPwd(Long userId);

	/**
	 * 修改状态
	 * @since 2025/11/19
	 * @param userId 用户id
	 * @param status  状态
	 * @return
	 */
	void updateStatus(Long userId, String status);

	/**
	 * 用户导出
	 * @since 2025/11/21
	 * @param response
	 * @param sysUserDo
	 * @return
	 */
	void exportExcel(HttpServletResponse response,SysUserDo sysUserDo) throws Exception;

	/**
	 * 用户导入
	 * @since 2025/11/29
	 * @param file Excel文件
	 * @param updateSupport 是否更新已存在的数据
	 * @return 导入结果
	 */
	com.su60.quickboot.common.core.ImportResult importExcel(org.springframework.web.multipart.MultipartFile file, Boolean updateSupport) throws IOException;
}
