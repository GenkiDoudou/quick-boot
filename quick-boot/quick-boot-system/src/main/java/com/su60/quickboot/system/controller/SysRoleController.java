package com.su60.quickboot.system.controller;

import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.common.bean.BeanConvertUtils;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import com.su60.quickboot.data.excel.ExcelUtils;
import com.su60.quickboot.data.excel.ExcelUtils2;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.system.dos.SysRoleDo;
import com.su60.quickboot.system.entity.SysRoleEntity;
import com.su60.quickboot.system.excel.SysRoleExcel;
import com.su60.quickboot.system.service.ISysRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 角色
 *
 * @author luyanan
 * @since 2024/8/16
 */
@RestController
@RequestMapping("system/role")
@RequiredArgsConstructor
public class SysRoleController {

	private final ISysRoleService sysRoleService;


	/**
	 * 分页查询
	 *
	 * @param sysRoleDo 参数
	 * @param request   请求参数
	 * @return 分页结果
	 * @since 2024/8/16
	 */
	@SaCheckPermission("system:role:list")
	@GetMapping("list")
	public PageInfo<SysRoleDo> page(SysRoleDo sysRoleDo, HttpServletRequest request) {

		return sysRoleService.page(sysRoleDo, request, new PageVoHandler<SysRoleEntity, SysRoleDo>() {
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

	/**
	 * 导出
	 *
	 * @param response  响应体
	 * @param sysRoleDo 角色信息
	 * @since 2024/8/17
	 */
	@SaCheckPermission("system:role:export")
	@PostMapping("export")
	public void export(HttpServletResponse response, SysRoleDo sysRoleDo) throws Exception {
		sysRoleService.export(response, sysRoleDo);
	}

	/**
	 * 新增角色
	 *
	 * @param sysRoleDo 角色信息
	 * @return 是否成功
	 * @since 2024/8/21
	 */
	@SaCheckPermission("system:role:add")
	@PostMapping()
	public boolean save(@RequestBody @Validated(AddGroup.class) SysRoleDo sysRoleDo) {
		return sysRoleService.saveRole(sysRoleDo);
	}


	/**
	 * 修改角色信息
	 *
	 * @param sysRoleDo 角色信息
	 * @return 是否成功
	 * @since 2024/10/10
	 */
	@SaCheckPermission("system:role:edit")
	@PutMapping
	public boolean update(@RequestBody @Validated(UpdateGroup.class) SysRoleDo sysRoleDo) {
		return sysRoleService.updateRole(sysRoleDo);
	}

	/**
	 * 根据id查询
	 *
	 * @param id id
	 * @return 角色信息
	 * @since 2024/08/24
	 */
	@SaCheckPermission("system:role:query")
	@GetMapping("/{id}")
	public SysRoleDo info(@PathVariable("id") Long id) {
		return sysRoleService.getVoById(id);
	}

	/**
	 * 根据id集合删除
	 *
	 * @param ids id集合
	 * @return 是否成功
	 * @since 2024/08/24
	 */
	@SaCheckPermission("system:role:remove")
	@DeleteMapping
	public boolean delete(@RequestBody List<Long> ids) {

		return sysRoleService.delete(ids);
	}


	/**
	 * 该角色选中的菜单id
	 *
	 * @param roleId 角色id
	 * @return 选中的菜单id
	 * @since 2024/08/24
	 */
	@SaCheckPermission("system:role:query")
	@GetMapping("checkedKeys/{roleId}")
	public List<Long> checkedKeys(@PathVariable("roleId") Long roleId) {
		return sysRoleService.checkedKeys(roleId);
	}


	/**
	 * 所有的角色
	 *
	 * @param sysRoleDo 角色信息
	 * @return 角色列表
	 * @since 2024/11/12
	 */
	@SaCheckPermission("system:role:list")
	@GetMapping("lists")
	public List<SysRoleDo> listAll(SysRoleDo sysRoleDo) {

		return sysRoleService.listAll(sysRoleDo);

	}
}
