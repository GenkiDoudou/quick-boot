package com.su60.quickboot.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.su60.quickboot.data.mybatisplus.BaseVoServiceImpl;
import com.su60.quickboot.system.entity.SysUserRoleEntity;
import com.su60.quickboot.system.dos.SysUserRoleDo;
import com.su60.quickboot.system.mapper.SysUserRoleMapper;
import com.su60.quickboot.system.service.ISysUserRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户和角色关联表 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */
@RequiredArgsConstructor
@Service

public class SysUserRoleServiceImpl extends BaseVoServiceImpl<SysUserRoleMapper, SysUserRoleEntity, SysUserRoleDo> implements ISysUserRoleService {

	@Override
	public List<SysUserRoleEntity> list(SysUserRoleEntity sysUserRoleEntity) {
		return super.list(new LambdaQueryWrapper<SysUserRoleEntity>(sysUserRoleEntity));
	}

	@Override
	public void saveUserRoles(Long userId, List<Long> roleIds) {
		this.remove(new LambdaQueryWrapper<SysUserRoleEntity>()
				.eq(SysUserRoleEntity::getUserId, userId));
		if (CollectionUtil.isNotEmpty(roleIds)) {
			List<SysUserRoleEntity> entityList = roleIds.stream().map(a -> new SysUserRoleEntity().setUserId(userId).setRoleId(a))
					.collect(Collectors.toList());
			this.saveBatch(entityList);
		}
	}

	@Override
	public void deleteByUserIds(Collection<? extends Serializable> userIds) {
		if (CollectionUtil.isEmpty(userIds)) {
			return;
		}
		this.remove(new LambdaQueryWrapper<SysUserRoleEntity>()
				.in(SysUserRoleEntity::getUserId, userIds));
	}

	@Override
	public List<SysUserRoleEntity> listByUserId(Serializable userId) {
		return super.list(new LambdaQueryWrapper<SysUserRoleEntity>()
				.eq(SysUserRoleEntity::getUserId, userId));
	}
}

