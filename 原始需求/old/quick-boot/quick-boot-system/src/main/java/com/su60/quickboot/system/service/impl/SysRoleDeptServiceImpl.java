package com.su60.quickboot.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su60.quickboot.data.mybatisplus.BaseServiceImpl;
import com.su60.quickboot.system.entity.SysRoleDeptEntity;
import com.su60.quickboot.system.mapper.SysRoleDeptMapper;
import com.su60.quickboot.system.service.ISysRoleDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色关联部门表 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2025/12/27
 */
@RequiredArgsConstructor
@Service
public class SysRoleDeptServiceImpl extends BaseServiceImpl<SysRoleDeptMapper, SysRoleDeptEntity> implements ISysRoleDeptService {

	@Override
	public List<Long> listDeptByRoleIds(List<Long> roleIds) {
		if (CollectionUtil.isEmpty(roleIds)) {
			return new ArrayList<>();
		}
		return this.list(new LambdaQueryWrapper<SysRoleDeptEntity>()
						.in(SysRoleDeptEntity::getRoleId, roleIds))
				.stream().map(SysRoleDeptEntity::getDeptId).distinct().collect(Collectors.toList());
	}

	@Override
	public List<Long> listDeptByRoleId(Long roleId) {

		return this.list(new LambdaQueryWrapper<SysRoleDeptEntity>()
						.eq(SysRoleDeptEntity::getRoleId, roleId))
				.stream().map(SysRoleDeptEntity::getDeptId).distinct().collect(Collectors.toList());
	}

	@Override
	public void save(Long roleId, List<Long> deptIds) {
		this.baseMapper.delete(new LambdaQueryWrapper<SysRoleDeptEntity>()
				.eq(SysRoleDeptEntity::getRoleId, roleId));
		if (CollectionUtil.isNotEmpty(deptIds)) {
			deptIds = deptIds.stream().distinct().collect(Collectors.toList());
			List<SysRoleDeptEntity> entityList = deptIds.stream().map(a -> {
				SysRoleDeptEntity sysRoleDeptEntity = new SysRoleDeptEntity();
				sysRoleDeptEntity.setDeptId(a);
				sysRoleDeptEntity.setRoleId(roleId);
				return sysRoleDeptEntity;
			}).collect(Collectors.toList());
			this.saveBatch(entityList);
		}
	}

	@Override
	public void deleteByRoleIds(List<Long> roleIds) {
		if (CollectionUtil.isEmpty(roleIds)) {
			return;
		}
		this.baseMapper.delete(new LambdaQueryWrapper<SysRoleDeptEntity>()
				.in(SysRoleDeptEntity::getRoleId, roleIds));
	}
}

