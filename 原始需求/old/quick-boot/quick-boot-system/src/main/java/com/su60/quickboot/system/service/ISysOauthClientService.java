package com.su60.quickboot.system.service;

import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.system.dos.SysConfigDo;
import com.su60.quickboot.system.dos.SysOauthClientDo;

import java.util.List;

/**
 * <p>
 * 客户端管理 服务类
 * </p>
 *
 * @author luyanan
 * @since 2026/01/21
 */

public interface ISysOauthClientService {


	/**
	 * 分页查询
	 *
	 * @param sysOauthClientDo 参数
	 * @return
	 * @since 2026/01/21
	 */
	PageInfo<SysOauthClientDo> page(SysOauthClientDo sysOauthClientDo);

	/**
	 * 保存
	 *
	 * @param sysOauthClientDo 参数
	 * @return
	 * @since 2026/01/21
	 */
	Boolean save(SysOauthClientDo sysOauthClientDo) throws Exception;

	/**
	 * 根据id修改
	 *
	 * @param sysOauthClientDo 参数
	 * @return
	 * @since 2026/1/8
	 */
	Boolean updateById(SysOauthClientDo sysOauthClientDo);

	/**
	 * 根据id查询
	 *
	 * @param id id
	 * @return
	 * @since 2026/01/21
	 */
	SysOauthClientDo getVoById(Long id);

	/**
	 * 根据id集合查询
	 *
	 * @param ids id集合
	 * @return
	 * @since 2026/01/21
	 */
	Boolean deleteByIds(List<Long> ids);

	/**
	 * 修改状态
	 *
	 * @param id     id
	 * @param status 状态
	 * @return
	 * @since 2026/2/8
	 */
	void updateStatus(Long id, String status);

	/**
	 * 生成加密解密的公私钥
	 *
	 * @param id id
	 * @return
	 * @since 2026/2/8
	 */
	void generateEncryptionKey(Long id);

	/**
	 * 根据客户端id查询
	 *
	 * @param clientId 客户端id
	 * @return
	 * @since 2026/2/9
	 */
	SysOauthClientDo getEnableByClientId(String clientId);

}
