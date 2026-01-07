package com.su60.quickboot.system.controller;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.map.MapBuilder;
import com.su60.quickboot.common.core.R;
import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import com.su60.quickboot.core.security.LoginUserUtils;
import com.su60.quickboot.system.dos.SysMenuDo;
import com.su60.quickboot.system.dos.TreeSelect;
import com.su60.quickboot.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 系统菜单
 *
 * @author luyanan
 * @since 2024/8/20
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("system/menu")
public class SysMenuController {


	private final ISysMenuService sysMenuService;

	/**
	 * 获取菜单下拉树列表
	 *
	 * @param sysMenuDo 菜单信息
	 * @return 菜单下拉树列表
	 * @since 2024/8/20
	 */
	@SaCheckPermission("system:menu:list")
	@GetMapping("treeselect")
	public List<TreeSelect> treeselect(SysMenuDo sysMenuDo) {
		return sysMenuService.treeselect(sysMenuDo, LoginUserUtils.getUser().getId());
	}

	/**
	 * 根据角色id查询对应的菜单树列表
	 *
	 * @param roleId 角色id
	 * @return 菜单树列表
	 * @since 2024/10/10
	 */
	@SaCheckPermission("system:role:query")
	@GetMapping("roleMenuTreeselect/{roleId}")
	public Map<String, Object> roleMenuTreeselect(@PathVariable("roleId") Long roleId) {

		Long userId = LoginUserUtils.getUser().getId();
		List<SysMenuDo> sysMenuDos = sysMenuService.listByUserId(userId, null);
		List<Long> menuIds = sysMenuService.selectMenuListByRoleId(roleId);

		return MapBuilder.create(new HashMap<String, Object>())
				.put("checkedKeys", menuIds)
				.put("menus", sysMenuService.buildMenuTreeSelect(sysMenuDos)).build();
	}

	/**
	 * 查询菜单列表
	 *
	 * @param sysMenuDo 菜单信息
	 * @return 菜单列表
	 * @since 2024/11/6
	 */
	@SaCheckPermission("system:menu:list")
	@GetMapping("list")
	public List<SysMenuDo> list(SysMenuDo sysMenuDo) {
		return sysMenuService.listByUserId(LoginUserUtils.getUser().getId(), sysMenuDo);
	}

	/**
	 * 查询菜单列表(树形)
	 *
	 * @param sysMenuDo 菜单信息
	 * @return 菜单列表
	 * @since 2024/11/6
	 */
	@SaCheckPermission("system:menu:list")
	@GetMapping("treeList")
	public List<Tree<Long>> treeList(SysMenuDo sysMenuDo) {

		return sysMenuService.treeList(sysMenuDo, LoginUserUtils.getUser().getId());
	}


	/**
	 * 根据id查询
	 *
	 * @param id id
	 * @return 菜单信息
	 * @since 2024/11/6
	 */
	@SaCheckPermission("system:menu:query")
	@GetMapping("/{id}")
	public SysMenuDo info(@PathVariable("id") Long id) {

		return sysMenuService.getVoById(id);
	}


	/**
	 * 保存菜单
	 *
	 * @param sysMenuDo 菜单信息
	 * @return 是否成功
	 * @since 2024/11/6
	 */
	@SaCheckPermission("system:menu:add")
	@PostMapping("")
	public Boolean save(@RequestBody @Validated(AddGroup.class) SysMenuDo sysMenuDo) {

		return sysMenuService.saveVo(sysMenuDo);
	}

	/**
	 * 根据id修改
	 *
	 * @param sysMenuDo 菜单信息
	 * @return 是否成功
	 * @since 2024/11/6
	 */
	@SaCheckPermission("system:menu:edit")
	@PutMapping
	public Boolean update(@RequestBody @Validated(UpdateGroup.class) SysMenuDo sysMenuDo) {

		return sysMenuService.updateVoById(sysMenuDo);
	}


	/**
	 * 根据id删除
	 *
	 * @param id 菜单id
	 * @return 是否成功
	 * @since 2024/11/6
	 */
	@SaCheckPermission("system:menu:remove")
	@DeleteMapping("/{id}")
	public Boolean delete(@PathVariable("id") Long id) {
		return sysMenuService.deleteById(id);
	}


	/**
	 * 树形下拉框
	 *
	 * @param value id
	 * @return 树形结果
	 * @since 2024/11/19
	 */
	@SaCheckPermission("system:menu:list")
	@PostMapping("tree")
	public List<Tree<Long>> tree(@RequestParam(value = "id", required = false) Long value) {

		List<SysMenuDo> sysMenuDos = sysMenuService.listByUserId(LoginUserUtils.getUser().getId(), new SysMenuDo()
				.setParentId(value));

		List<TreeNode<Long>> treeNodes = sysMenuDos.stream().map(getNodeFunction()).collect(Collectors.toList());
		return TreeUtil.build(treeNodes, 0L);
	}

	private Function<SysMenuDo, TreeNode<Long>> getNodeFunction() {
		return sysMenuDo -> {

			TreeNode<Long> treeNode = new TreeNode<>();
			treeNode.setId(sysMenuDo.getId());
			treeNode.setParentId(sysMenuDo.getParentId());
			treeNode.setName(sysMenuDo.getMenuName());
			treeNode.setWeight(sysMenuDo.getOrderNum());
			Map<String, Object> ext = new HashMap<>();
			ext.put("value", sysMenuDo.getId());
			ext.put("label", sysMenuDo.getMenuName());
			treeNode.setExtra(ext);
			return treeNode;
		};
	}

	/**
	 * 查询所有的父级
	 *
	 * @param id id
	 * @return 父级id集合
	 * @since 2024/11/19
	 */
	@SaCheckPermission("system:menu:query")
	@GetMapping("tree/parent/{id}")
	public List<Long> findParent(@PathVariable("id") Long id) {

		return sysMenuService.findParent(id);
	}

}
