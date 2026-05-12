package com.su60.quickboot.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su60.quickboot.common.bean.BeanConvertUtils;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.exception.Assert;
import com.su60.quickboot.data.excel.ExcelUtils2;
import com.su60.quickboot.data.mybatisplus.BaseVoServiceImpl;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.system.dos.SysRoleDo;
import com.su60.quickboot.system.entity.SysRoleEntity;
import com.su60.quickboot.system.entity.SysUserRoleEntity;
import com.su60.quickboot.system.excel.SysRoleExcel;
import com.su60.quickboot.system.mapper.SysRoleMapper;
import com.su60.quickboot.system.service.ISysRoleDeptService;
import com.su60.quickboot.system.service.ISysRoleMenuService;
import com.su60.quickboot.system.service.ISysRoleService;
import com.su60.quickboot.system.service.ISysUserRoleService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 角色信息表 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysRoleServiceImpl extends BaseVoServiceImpl<SysRoleMapper, SysRoleEntity, SysRoleDo> implements ISysRoleService {

	private final ISysUserRoleService sysUserRoleService;
	private final ISysRoleMenuService sysRoleMenuService;

	private final ISysRoleDeptService sysRoleDeptService;

	@Override
	public List<SysRoleDo> listByUserId(Long userId) {

		List<SysUserRoleEntity> userRoleEntities = sysUserRoleService.list(new SysUserRoleEntity().setUserId(userId));
		if (CollectionUtil.isEmpty(userRoleEntities)) {
			return new ArrayList<>();
		}
		// 角色id集合
		return BeanConvertUtils.convertListTo(this.listByIds(userRoleEntities.stream().map(SysUserRoleEntity::getRoleId).toList()), SysRoleDo::new);
	}

	@Override
	public boolean saveRole(SysRoleDo sysRoleDo) {
		// 角色名称唯一
		Assert.isTrue(!this.exists(new LambdaQueryWrapper<SysRoleEntity>().eq(SysRoleEntity::getRoleName, sysRoleDo.getRoleName())), 100007);
		// 角色权限字符串唯一
		Assert.isTrue(!this.exists(new LambdaQueryWrapper<SysRoleEntity>().eq(SysRoleEntity::getRoleKey, sysRoleDo.getRoleKey())), 100008);

		// 保存角色信息
		SysRoleEntity sysRoleEntity = BeanConvertUtils.convertTo(sysRoleDo, SysRoleEntity::new);

		super.save(sysRoleEntity);
		// 保存角色菜单关联关系

		sysRoleMenuService.save(sysRoleEntity.getId(), sysRoleDo.getMenuIds());

		// 保存角色和自定义部门的关联关系
		if (!sysRoleDo.getDataScope().equals("2")) {
			sysRoleDo.setDeptIds(new ArrayList<>());
		}
		sysRoleDeptService.save(sysRoleEntity.getId(), sysRoleDo.getDeptIds());
		return true;
	}

	@Override
	public boolean delete(List<Long> ids) {
		// 删除角色跟菜单的关联关系
		sysRoleMenuService.delete(ids);
		// 删除角色与部门的关联关系
		sysRoleDeptService.deleteByRoleIds(ids);
		return this.removeByIds(ids);
	}

	@Override
	public List<SysRoleEntity> list(SysRoleEntity sysRoleEntity) {
		return this.list(new LambdaQueryWrapper<>(sysRoleEntity));
	}

	@Override
	public List<Long> checkedKeys(Long roleId) {
		SysRoleEntity sysRole = super.getById(roleId);

		return this.baseMapper.selectMenuListByRoleId(sysRole.getId(), sysRole.getMenuCheckStrictly());
	}

	@Override
	public boolean updateRole(SysRoleDo sysRoleDo) {
		// 判断角色名称唯一

		Assert.isTrue(!this.exists(new LambdaQueryWrapper<SysRoleEntity>().eq(SysRoleEntity::getRoleName, sysRoleDo.getRoleName()).ne(SysRoleEntity::getId, sysRoleDo.getId())), 100007);
		// 判断角色权限唯一
		Assert.isTrue(!this.exists(new LambdaQueryWrapper<SysRoleEntity>().eq(SysRoleEntity::getRoleKey, sysRoleDo.getRoleKey()).ne(SysRoleEntity::getId, sysRoleDo.getId())), 100008);

		SysRoleEntity sysRoleEntity = BeanConvertUtils.convertTo(sysRoleDo, SysRoleEntity::new);
		// 角色关联的菜单
		sysRoleMenuService.save(sysRoleEntity.getId(), sysRoleDo.getMenuIds());

		// 保存角色和自定义部门的关联关系
		if (!sysRoleDo.getDataScope().equals("2")) {
			sysRoleDo.setDeptIds(new ArrayList<>());
		}
		sysRoleDeptService.save(sysRoleEntity.getId(), sysRoleDo.getDeptIds());
		return super.updateById(sysRoleEntity);

	}

	@Override
	public List<Long> listRoleIdUserId(Long userId) {
		return sysUserRoleService.list(new SysUserRoleEntity().setUserId(userId)).stream().map(a -> a.getRoleId()).distinct().toList();
	}

	@Override
	public void saveUserRoles(Long userId, List<Long> roleIds) {
		sysUserRoleService.saveUserRoles(userId, roleIds);
	}

	@Override
	public List<SysRoleDo> listAll(SysRoleDo sysRoleDo) {
		List<SysRoleEntity> list = super.list(new LambdaQueryWrapper<SysRoleEntity>(BeanConvertUtils.convertTo(sysRoleDo, SysRoleEntity.class)));
		return BeanConvertUtils.convertListTo(list, SysRoleDo::new);
	}

	@Override
	public void export(HttpServletResponse response, SysRoleDo sysRoleDo) throws Exception {

		LambdaQueryWrapper<SysRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.like(StrUtil.isNotBlank(sysRoleDo.getRoleName()), SysRoleEntity::getRoleName, sysRoleDo.getRoleName());
		queryWrapper.eq(StrUtil.isNotBlank(sysRoleDo.getRoleKey()), SysRoleEntity::getRoleKey, sysRoleDo.getRoleKey());
		ExcelUtils2.builder(response)
				.addSheet(ExcelUtils2.sheet(SysRoleExcel.class)
						.name("角色列表")
						.page(current -> {

							IPage<SysRoleEntity> page = new Page(current, 100);
							page = this.baseMapper.selectPage(page, queryWrapper);
							List<SysRoleEntity> records = page.getRecords();
							return BeanConvertUtils.convertListTo(records, SysRoleExcel::new);
						}).build())
				.export("角色列表");
	}

	@Override
	public SysRoleDo getVoById(Long roleId) {
		SysRoleDo sysRoleDo = super.getVoById(roleId);
		if (sysRoleDo.getDataScope().equals("2")) {
			List<Long> deptIds = sysRoleDeptService.listDeptByRoleId(roleId);
			sysRoleDo.setDeptIds(deptIds);
		} else {
			sysRoleDo.setDeptIds(new ArrayList<>());
		}
		return sysRoleDo;
	}

	@Override
	public PageInfo<SysRoleDo> page(SysRoleDo sysRoleDo) {
		return super.page(sysRoleDo, new PageVoHandler<SysRoleEntity, SysRoleDo>() {
			@Override
			public void queryWrapperHandler(SysRoleDo vo, SysRoleEntity sysRoleEntity, LambdaQueryWrapper<SysRoleEntity> queryWrapper) {
				queryWrapper.orderByDesc(SysRoleEntity::getCreateTime);
				queryWrapper.like(StrUtil.isNotBlank(sysRoleEntity.getRoleName()), SysRoleEntity::getRoleName, sysRoleEntity.getRoleName());
				sysRoleEntity.setRoleName(null);

				queryWrapper.like(StrUtil.isNotBlank(sysRoleEntity.getRoleKey()), SysRoleEntity::getRoleKey, sysRoleEntity.getRoleKey());
				sysRoleEntity.setRoleKey(null);
			}
		});
	}

	@Override
	public List<SysRoleEntity> getVoByIds(Set<Long> ids) {
		if (CollectionUtil.isEmpty(ids)) {
			return new ArrayList<>();
		}
		return super.listByIds(ids);
	}
}

