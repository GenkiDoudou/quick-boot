package com.su60.quickboot.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su60.quickboot.data.mybatisplus.BaseVoServiceImpl;
import com.su60.quickboot.system.dos.SysRoleMenuDo;
import com.su60.quickboot.system.entity.SysRoleMenuEntity;
import com.su60.quickboot.system.mapper.SysRoleMenuMapper;
import com.su60.quickboot.system.service.ISysRoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色和菜单关联表 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */
@RequiredArgsConstructor
@Service

public class SysRoleMenuServiceImpl extends BaseVoServiceImpl<SysRoleMenuMapper, SysRoleMenuEntity, SysRoleMenuDo> implements ISysRoleMenuService {

	@Override
	public void save(Long roleId, List<Long> menuIds) {

		this.remove(new LambdaQueryWrapper<SysRoleMenuEntity>().eq(SysRoleMenuEntity::getRoleId, roleId));
		if (CollectionUtil.isEmpty(menuIds)) {
			return;
		}
		List<SysRoleMenuEntity> entityList = menuIds.stream().map(a -> {
			SysRoleMenuEntity sysRoleMenuEntity = new SysRoleMenuEntity();
			sysRoleMenuEntity.setRoleId(roleId);
			sysRoleMenuEntity.setMenuId(a);
			return sysRoleMenuEntity;
		}).collect(Collectors.toList());
		this.saveBatch(entityList);
	}

	@Override
	public void delete(List<Long> roleIds) {
		if (CollectionUtil.isEmpty(roleIds)) {
			return;
		}
		this.remove(new LambdaQueryWrapper<SysRoleMenuEntity>().in(SysRoleMenuEntity::getRoleId, roleIds));
	}

	@Override
	public List<SysRoleMenuEntity> list(List<Long> roleIds) {
		List<SysRoleMenuEntity> list = super.list(new LambdaQueryWrapper<SysRoleMenuEntity>().in(SysRoleMenuEntity::getRoleId, roleIds));

		return list;
	}

	@Override
	public boolean checkMenuExistRole(Long menuId) {
		long count = super.count(new LambdaQueryWrapper<SysRoleMenuEntity>().eq(SysRoleMenuEntity::getMenuId, menuId));
		return count > 0;
	}
}

