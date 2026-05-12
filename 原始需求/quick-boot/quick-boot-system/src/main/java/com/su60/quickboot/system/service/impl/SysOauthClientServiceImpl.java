package com.su60.quickboot.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su60.quickboot.common.bean.BeanConvertUtils;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.utils.SmUtils;
import com.su60.quickboot.system.entity.SysOauthClientEntity;
import com.su60.quickboot.system.dos.SysOauthClientDo;
import com.su60.quickboot.system.mapper.SysOauthClientMapper;
import com.su60.quickboot.system.service.ISysOauthClientService;
import com.su60.quickboot.data.mybatisplus.BaseVoServiceImpl;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;

import java.util.List;

/**
 * <p>
 * 客户端管理 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2026/01/21
 */
@CacheConfig(cacheNames = "sysOauthClient")
@RequiredArgsConstructor
@Service
public class SysOauthClientServiceImpl extends BaseVoServiceImpl<SysOauthClientMapper, SysOauthClientEntity, SysOauthClientDo> implements ISysOauthClientService {


	@Override
	public PageInfo<SysOauthClientDo> page(SysOauthClientDo sysOauthClientDo) {
		return super.page(sysOauthClientDo, new PageVoHandler<SysOauthClientEntity, SysOauthClientDo>() {
			@Override
			public void queryWrapperHandler(SysOauthClientDo vo, SysOauthClientEntity sysOauthClientEntity, LambdaQueryWrapper<SysOauthClientEntity> queryWrapper) {
				queryWrapper.like(StrUtil.isNotBlank(vo.getClientId()), SysOauthClientEntity::getClientId, vo.getClientId());
				sysOauthClientEntity.setClientId(null);
				queryWrapper.like(StrUtil.isNotBlank(vo.getClientSecret()), SysOauthClientEntity::getClientSecret, vo.getClientSecret());
				sysOauthClientEntity.setClientSecret(null);
				queryWrapper.like(StrUtil.isNotBlank(vo.getScope()), SysOauthClientEntity::getScope, vo.getScope());
				sysOauthClientEntity.setScope(null);
				queryWrapper.like(StrUtil.isNotBlank(vo.getAuthorities()), SysOauthClientEntity::getAuthorities, vo.getAuthorities());
				sysOauthClientEntity.setAuthorities(null);
				queryWrapper.like(StrUtil.isNotBlank(vo.getWhitelistIp()), SysOauthClientEntity::getWhitelistIp, vo.getWhitelistIp());
				sysOauthClientEntity.setWhitelistIp(null);
				queryWrapper.like(StrUtil.isNotBlank(vo.getVerifyType()), SysOauthClientEntity::getVerifyType, vo.getVerifyType());
				sysOauthClientEntity.setVerifyType(null);

			}
		});
	}


	@CacheEvict(allEntries = true)
	@Override
	public Boolean save(SysOauthClientDo sysOauthClientDo) throws Exception {
		// 生成客户端id 客户端密钥
		String clientId = IdUtil.fastSimpleUUID();
		sysOauthClientDo.setClientId(clientId);
		// 私钥
		String clientSecret = IdUtil.fastSimpleUUID();
		sysOauthClientDo.setClientSecret(clientSecret);
		// 如果类型是加密或者加密 则生成公私要
		String verifyType = sysOauthClientDo.getVerifyType();
		if (verifyType.contains("1") || verifyType.contains("2")) {
			SmUtils.KeyPair keyPair = SmUtils.generateCompatibleKeyPair();
			sysOauthClientDo.setPrivateKey(keyPair.getPrivateKey());
			sysOauthClientDo.setPublicKey(keyPair.getPublicKey());
		}
		return super.saveVo(sysOauthClientDo);
	}

	@CacheEvict(allEntries = true)
	@SneakyThrows
	@Override
	public Boolean updateById(SysOauthClientDo sysOauthClientDo) {
		SysOauthClientEntity oauthClient = this.getById(sysOauthClientDo.getId());
		if (null == oauthClient) {
			throw new RuntimeException("未找到该数据");
		}
		if (oauthClient.getVerifyType().contains("1") || oauthClient.getVerifyType().contains("2")) {
			if (StrUtil.isBlank(oauthClient.getPrivateKey()) || StrUtil.isBlank(oauthClient.getPublicKey())) {
				SmUtils.KeyPair keyPair = SmUtils.generateCompatibleKeyPair();
				sysOauthClientDo.setPrivateKey(keyPair.getPrivateKey());
				sysOauthClientDo.setPublicKey(keyPair.getPublicKey());
			}
		}
		return super.updateVoById(sysOauthClientDo);
	}


	@Override
	public SysOauthClientDo getVoById(Long id) {
		return super.getVoById(id);
	}

	@CacheEvict(allEntries = true)
	@Override
	public Boolean deleteByIds(List<Long> ids) {
		return super.deleteByIds(ids);
	}

	@CacheEvict(allEntries = true)
	@Override
	public void updateStatus(Long id, String status) {

		SysOauthClientEntity oauthClient = super.getById(id);
		if (null == oauthClient) {
			throw new RuntimeException("未找到该数据");
		}
		SysOauthClientEntity sysOauthClient = new SysOauthClientEntity();
		sysOauthClient.setId(id);
		sysOauthClient.setStatus(status);
		super.updateById(sysOauthClient);
	}

	@CacheEvict(allEntries = true)
	@SneakyThrows
	@Override
	public void generateEncryptionKey(Long id) {
		SysOauthClientEntity oauthClient = super.getById(id);
		if (null == oauthClient) {
			throw new RuntimeException("未找到该数据");
		}
		SysOauthClientEntity sysOauthClient = new SysOauthClientEntity();
		SmUtils.KeyPair keyPair = SmUtils.generateCompatibleKeyPair();
		sysOauthClient.setId(id);
		sysOauthClient.setPrivateKey(keyPair.getPrivateKey());
		sysOauthClient.setPublicKey(keyPair.getPublicKey());
		super.updateById(sysOauthClient);
	}

	@Override
	public SysOauthClientDo getEnableByClientId(String clientId) {
		if (StrUtil.isBlank(clientId)) {
			return null;
		}
		SysOauthClientEntity sysOauthClient = super.getOne(new LambdaQueryWrapper<SysOauthClientEntity>()
				.eq(SysOauthClientEntity::getClientId, clientId)
				.eq(SysOauthClientEntity::getStatus, "0"));
		return BeanConvertUtils.convertTo(sysOauthClient, SysOauthClientDo::new);
	}
}

