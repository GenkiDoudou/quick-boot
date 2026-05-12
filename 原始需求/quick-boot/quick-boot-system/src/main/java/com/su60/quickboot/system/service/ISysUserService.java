package com.su60.quickboot.system.service;

import com.su60.quickboot.common.core.ImportResult;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.system.entity.SysUserEntity;
import com.su60.quickboot.system.dos.SysUserDo;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */

public interface ISysUserService {

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
	void exportExcel(HttpServletResponse response, SysUserDo sysUserDo) throws Exception;

	/**
	 * 用户导入
	 * @since 2025/11/29
	 * @param file Excel文件
	 * @param updateSupport 是否更新已存在的数据
	 * @return 导入结果
	 */
	ImportResult importExcel(org.springframework.web.multipart.MultipartFile file, Boolean updateSupport) throws IOException;

	/**
	 * 根据id查询
	 * @since 2025/12/30
	 * @param id  id
	 * @return
	 */
	SysUserDo getVoById(Long id);

	PageInfo<SysUserDo> page(SysUserDo sysUserDo);

	/**
	 * 根据id集合查询
	 * @since 2025/12/30
	 * @param ids  id集合
	 * @return
	 */
	Boolean deleteByIds(List<Long> ids);
}
