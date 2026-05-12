package com.su60.quickboot.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su60.quickboot.common.bean.BeanConvertUtils;
import com.su60.quickboot.common.exception.Assert;
import com.su60.quickboot.core.security.LoginUserUtils;
import com.su60.quickboot.data.mybatisplus.BaseVoServiceImpl;
import com.su60.quickboot.system.dos.*;
import com.su60.quickboot.system.entity.SysMenuEntity;
import com.su60.quickboot.system.entity.SysRoleMenuEntity;
import com.su60.quickboot.system.enums.SysMenuEnums;
import com.su60.quickboot.system.mapper.SysMenuMapper;
import com.su60.quickboot.system.service.ISysMenuService;
import com.su60.quickboot.system.service.ISysRoleMenuService;
import com.su60.quickboot.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单权限表 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class SysMenuServiceImpl extends BaseVoServiceImpl<SysMenuMapper, SysMenuEntity, SysMenuDo> implements ISysMenuService {

	private final ISysRoleMenuService sysRoleMenuService;

	private final ISysRoleService sysRoleService;

	@Override
	public List<String> listPermsByRoleIds(List<Long> roleIds) {
		List<SysMenuDo> sysMenuDos = this.listByRoleIds(roleIds);
		return sysMenuDos
				.stream()
//				.filter(a -> StrUtil.isNotBlank(a.getPerms()) && a.getMenuType().equals(SysMenuEnums.MENU_TYPE_BUTTON.getValue()) )
				.map(SysMenuDo::getApiPerms)
				.filter(StrUtil::isNotBlank)
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public List<SysMenuDo> listByRoleIds(List<Long> roleIds) {
		if (CollectionUtil.isEmpty(roleIds)) {
			return new ArrayList<>();
		}
		List<SysRoleMenuEntity> list = sysRoleMenuService.list(roleIds);
		if (CollectionUtil.isEmpty(list)) {
			return new ArrayList<>();
		}
		List<Long> menuIds = list.stream().map(SysRoleMenuEntity::getMenuId).distinct().toList();
		return BeanConvertUtils.convertListTo(this.listByIds(menuIds), SysMenuDo::new);
	}

	@Override
	public List<RouterVo> getRouters(Long userId) {
		List<SysMenuDo> sysMenuDos = null;
//		if (LoginUserUtils.isAdmin(userId)) {
//			// 查询所有
//			sysMenuDos = BeanConvertUtils.convertListTo(this.list(), SysMenuDo::new);
//		} else {
		List<Long> roleIds = sysRoleService.listByUserId(userId).stream().map(SysRoleDo::getId).toList();

		sysMenuDos = this.listByRoleIds(roleIds);
//		}

		List<SysMenuDo> sysMenuDos1 = buildChildren(sysMenuDos, 0L);
		return buildMenus(sysMenuDos1);
	}

	@Override
	public List<TreeSelect> treeselect(SysMenuDo sysMenuDo, Long userId) {

		List<SysMenuDo> sysMenuDos = this.listByUserId(userId, sysMenuDo);
		return this.buildMenuTreeSelect(sysMenuDos);
	}

	@Override
	public List<SysMenuDo> listByUserId(Long userId, SysMenuDo sysMenuDo) {
		List<SysMenuDo> sysMenuDos = null;
		if (LoginUserUtils.isAdmin(userId)) {
			sysMenuDos = BeanConvertUtils.convertListTo(this.list(new LambdaQueryWrapper<>(BeanConvertUtils.convertTo(sysMenuDo, SysMenuEntity::new))), SysMenuDo::new);
		} else {
			//  根据用户id查询角色
			List<Long> roleIds = sysRoleService.listByUserId(userId).stream().map(SysRoleDo::getId).toList();
			sysMenuDos = this.listByRoleIds(roleIds);
		}
		return sysMenuDos;
	}

	@Override
	public List<TreeSelect> buildMenuTreeSelect(List<SysMenuDo> sysMenuDos) {

		List<SysMenuDo> menuTrees =
				buildMenuTree(sysMenuDos);
		return menuTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
	}

	@Override
	public List<Long> selectMenuListByRoleId(Long roleId) {
		SysRoleDo sysRoleDo = sysRoleService.getVoById(roleId);

		return this.baseMapper.selectMenuListByRoleId(roleId, sysRoleDo.getMenuCheckStrictly());
	}

	@Override
	public List<Long> findParent(Long id) {
		LinkedList<Long> ids = new LinkedList<>();
		ids.addLast(id);
		SysMenuEntity sysMenu = super.getById(id);
		if (sysMenu.getParentId() != 0L) {
			findParent(ids, sysMenu);
		}
		return ids;
	}

	@Override
	public List<Tree<Long>> treeList(SysMenuDo sysMenuDo, Long userId) {

		//  根据用户id查询角色
//		List<Long> roleIds = sysRoleService.listByUserId(userId).stream().map(SysRoleDo::getId).toList();
//		List<SysMenuDo> sysMenuDos = this.listByRoleIds(roleIds);
		List<SysMenuDo> sysMenuDos = BeanConvertUtils.convertListTo(super.list(new LambdaQueryWrapper<>()), SysMenuDo::new);
		if (CollectionUtil.isEmpty(sysMenuDos)) {
			return new ArrayList<>();
		}
		List<TreeNode<Long>> nodeList = sysMenuDos.stream().map(a -> {
			TreeNode<Long> node = new TreeNode<>();
			node.setParentId(a.getParentId());
			node.setId(a.getId());
			node.setName(a.getMenuName());
			node.setWeight(a.getOrderNum());
			Map<String, Object> ext = new HashMap<>();
			ext.put("path", a.getPath());
			ext.put("menuName", a.getMenuName());
			ext.put("component", a.getComponent());
			ext.put("query", a.getQuery());
			ext.put("routeName", a.getRouteName());
			ext.put("isFrame", a.getIsFrame());
			ext.put("isCache", a.getIsCache());
			ext.put("menuType", a.getMenuType());
			ext.put("visible", a.getVisible());
			ext.put("status", a.getStatus());
			ext.put("buttonPerms", a.getButtonPerms());
			ext.put("apiPerms", a.getApiPerms());
			ext.put("icon", a.getIcon());
			ext.put("createBy", a.getCreateBy());
			ext.put("createTime", a.getCreateTime());

			node.setExtra(ext);
			return node;
		}).collect(Collectors.toList());

		return TreeUtil.build(nodeList, 0L);
	}

	@Override
	public SysMenuDo getVoById(Long id) {
		return super.getVoById(id);
	}

	public void findParent(LinkedList<Long> ids, SysMenuEntity sysMenu) {
		//查询父节点
		SysMenuEntity parent = super.getById(sysMenu.getParentId());
		ids.addFirst(parent.getId());
		if (parent.getParentId() != 0L) {
			findParent(ids, parent);
		}
	}

	/**
	 * 构建菜单树信息
	 *
	 * @param sysMenuDos 菜单列表
	 * @return 菜单树集合
	 * @since 2024/08/24
	 */
	private List<SysMenuDo> buildMenuTree(List<SysMenuDo> sysMenuDos) {
		List<SysMenuDo> returnList = new ArrayList<>();
		List<Long> menuIds = sysMenuDos.stream().map(SysMenuDo::getId).collect(Collectors.toList());

		for (Iterator<SysMenuDo> iterator = sysMenuDos.iterator(); iterator.hasNext(); ) {
			SysMenuDo next = iterator.next();
			// 如果是顶级节点,便利该父节点所有的子节点
			if (!menuIds.contains(next.getParentId())) {
				recursionFn(sysMenuDos, next);
				returnList.add(next);
			}
		}
		if (returnList.isEmpty()) {
			returnList = sysMenuDos;
		}
		return returnList;
	}

	/**
	 * 构建菜单路由信息
	 *
	 * @param sysMenuDos 菜单信息
	 * @return 菜单路由信息
	 * @since 2024/08/24
	 */
	public List<RouterVo> buildMenus(List<SysMenuDo> sysMenuDos) {

		List<RouterVo> routers = new LinkedList<>();
		for (SysMenuDo sysMenuDo : sysMenuDos) {
			RouterVo routerVo = new RouterVo()
					.setHidden(sysMenuDo.getVisible().equals("1"))
					.setName(getRouteName(sysMenuDo))
					.setPath(getRouterPath(sysMenuDo))
					.setComponent(getComponent(sysMenuDo))
					.setQuery(sysMenuDo.getQuery())
					.setMeta(new MetaVo(sysMenuDo.getMenuName(), sysMenuDo.getIcon(), sysMenuDo.getIsCache().equals("1"),
							sysMenuDo.getPath()));

			List<SysMenuDo> children = sysMenuDo.getChildren();
			if (CollectionUtil.isNotEmpty(children) && SysMenuEnums.MENU_TYPE_DIR.getValue().equals(sysMenuDo.getMenuType())) {
				routerVo.setChildren(buildMenus(children));
				routerVo.setRedirect("noRedirect");
				routerVo.setAlwaysShow(true);
			} else if (isMenuFrame(sysMenuDo)) {
				routerVo.setMeta(null);
				List<RouterVo> childrentList = new ArrayList<>();
				RouterVo child = new RouterVo();
				child.setPath(sysMenuDo.getPath());
				child.setComponent(sysMenuDo.getComponent());
				child.setName(getRouteName(sysMenuDo.getRouteName(), sysMenuDo.getPath()));
				child.setComponent(sysMenuDo.getComponent());
				child.setQuery(sysMenuDo.getQuery());
				child.setMeta(new MetaVo(sysMenuDo.getMenuName(), sysMenuDo.getIcon(), sysMenuDo.getIsCache().equals("1"), sysMenuDo.getPath()));

				childrentList.add(child);

				routerVo.setChildren(childrentList);
			} else if (sysMenuDo.getParentId().intValue() == 0 && isInnerLink(sysMenuDo)) {
				routerVo.setMeta(new MetaVo(sysMenuDo.getMenuName(), sysMenuDo.getIcon()));
				routerVo.setPath("/");
				List<RouterVo> childrentList = new ArrayList<>();
				RouterVo child = new RouterVo();
				String routhPath = innerLinkReplaceEach(sysMenuDo.getPath());
				child.setPath(routhPath);
				child.setComponent("InnerLink");
				child.setName(getRouteName(sysMenuDo.getRouteName(), sysMenuDo.getPath()));
				child.setMeta(new MetaVo(sysMenuDo.getMenuName(), sysMenuDo.getIcon(), sysMenuDo.getPath()));
				childrentList.add(child);
				routerVo.setChildren(childrentList);
			}
			routers.add(routerVo);
		}
		return routers;
	}

	/**
	 * 构建子菜单
	 *
	 * @param sysMenuDos 所有菜单列表
	 * @param parentId   父节点id
	 * @return 子菜单
	 * @since 2024/8/14
	 */
	public List<SysMenuDo> buildChildren(List<SysMenuDo> sysMenuDos, Long parentId) {

		List<SysMenuDo> returnList = new ArrayList<>();
		for (SysMenuDo sysMenuDo : sysMenuDos) {
			if (sysMenuDo.getParentId().equals(parentId)) {
				sysMenuDo.setChildren(buildChildren(sysMenuDos, sysMenuDo.getId()));
				recursionFn(sysMenuDos, sysMenuDo);
				returnList.add(sysMenuDo);
			}
		}
		return returnList;
	}

	/**
	 * 递归列表
	 *
	 * @param sysMenuDos 菜单列表
	 * @param sysMenuDo  菜单
	 * @since 2024/8/14
	 */
	private void recursionFn(List<SysMenuDo> sysMenuDos, SysMenuDo sysMenuDo) {
		// 得到子节点列表

		List<SysMenuDo> children = getChildren(sysMenuDos, sysMenuDo);
		sysMenuDo.setChildren(children);
		for (SysMenuDo child : children) {
			if (hasChild(sysMenuDos, child)) {
				recursionFn(sysMenuDos, child);
			}
		}
	}

	/**
	 * 是否包含子节点
	 *
	 * @param sysMenuDos 菜单信息列表
	 * @param t          菜单信息
	 * @return 是否成功
	 * @since 2024/8/14
	 */
	private boolean hasChild(List<SysMenuDo> sysMenuDos, SysMenuDo t) {
		return getChildren(sysMenuDos, t).size() > 0;
	}

	/**
	 * 获取子节点
	 *
	 * @param sysMenuDos 菜单列表
	 * @param sysMenuDo  当前节点
	 * @return 子节点
	 * @since 2024/08/24
	 */
	private List<SysMenuDo> getChildren(List<SysMenuDo> sysMenuDos, SysMenuDo sysMenuDo) {
		List<SysMenuDo> tlist = new ArrayList<>();
		Iterator<SysMenuDo> iterator = sysMenuDos.iterator();
		while (iterator.hasNext()) {
			SysMenuDo next = iterator.next();
			if (next.getParentId().equals(sysMenuDo.getId())) {
				tlist.add(next);
			}
		}
		return tlist;
	}

	/**
	 * 是否为菜单内部跳转
	 *
	 * @param sysMenuDo 菜单信息
	 * @return 是/否
	 * @since 2024/8/10
	 */

	public boolean isMenuFrame(SysMenuDo sysMenuDo) {
		return sysMenuDo
				.getParentId()
				.intValue() == 0 && SysMenuEnums.MENU_TYPE_MENU.getValue()
				.equals(sysMenuDo.getMenuType()) && sysMenuDo.getIsFrame()
				.equals(SysMenuEnums.IS_FRAME_NO.getValue());
	}

	/**
	 * 是否为内链组件
	 *
	 * @param sysMenuDo 菜单信息
	 * @return 是否为内链组件
	 * @since 2024/8/10
	 */
	public boolean isInnerLink(SysMenuDo sysMenuDo) {

		return sysMenuDo.getIsFrame().equals(SysMenuEnums.IS_FRAME_NO.getValue()) && StrUtil.isNotBlank(sysMenuDo.getPath()) && sysMenuDo.getPath().startsWith("http");
	}

	/**
	 * 是否为parent_view组件
	 *
	 * @param sysMenuDo 菜单信息
	 * @return 是否为parent_view组件
	 * @since 2024/8/10
	 */
	public boolean isParentView(SysMenuDo sysMenuDo) {
		return sysMenuDo.getParentId().intValue() != 0 && SysMenuEnums.MENU_TYPE_DIR.getValue().equals(sysMenuDo.getMenuType());
	}

	/**
	 * 内链域名特殊字符替换吧
	 *
	 * @param path 路径
	 * @return 替换后的内链域名
	 * @since 2024/8/10
	 */
	public String innerLinkReplaceEach(String path) {
		return path.replace("http://", "").replace("https://", "").replace("www.", "").replace(".", "/").replace(":", "/");
	}


	/**
	 * 获取
	 *
	 * @param sysMenuDo 菜单信息
	 * @return 组件信息
	 * @since 2024/8/10
	 */
	public String getComponent(SysMenuDo sysMenuDo) {
		String component = SysMenuEnums.COMPONENT_LAYOUT.getValue();
		if (StrUtil.isNotEmpty(sysMenuDo.getComponent()) && !isMenuFrame(sysMenuDo)) {
			component = sysMenuDo.getComponent();
		} else if (StrUtil.isEmpty(sysMenuDo.getComponent()) && sysMenuDo.getParentId().intValue() != 0 && isInnerLink(sysMenuDo)) {
			component = SysMenuEnums.COMPONENT_INNER_LINK.getValue();
		} else if (StrUtil.isEmpty(sysMenuDo.getComponent()) && isParentView(sysMenuDo)) {
			component = SysMenuEnums.COMPONENT_PARENTVIEW.getValue();
		}
		return component;
	}

	/**
	 * 获取路由地址
	 *
	 * @param sysMenuDo 菜单信息
	 * @return 路由地址
	 * @since 2024/8/11
	 */
	public String getRouterPath(SysMenuDo sysMenuDo) {
		String routhPath = sysMenuDo.getPath();
		// 内链打开外网方式
		if (sysMenuDo.getParentId().intValue() != 0 && isInnerLink(sysMenuDo)) {
			routhPath = innerLinkReplaceEach(routhPath);
		}
		// 非外链并且是一级目录(类型为目录)
		if (sysMenuDo.getMenuType()
				.equals(SysMenuEnums.MENU_TYPE_DIR.getValue()) && 0 == sysMenuDo.getParentId().intValue() && sysMenuDo.getIsFrame()
				.equals(SysMenuEnums.IS_FRAME_NO.getValue())) {
			return "/" + sysMenuDo.getPath();
		}

		// 非外链并且是一级目录(类型为菜单)
		else if (isMenuFrame(sysMenuDo)) {
			routhPath = "/";
		}
		return routhPath;

	}

	/**
	 * 获取路由名称，如没有配置路由名称则取路由地址
	 *
	 * @param name 路由名称
	 * @param path 路由地址
	 * @return 路由名称(驼峰格式)
	 * @since 2024/8/11
	 */
	public String getRouteName(String name, String path) {
		String routerName = StrUtil.isNotEmpty(name) ? name : path;
		return StrUtil.upperFirst(routerName);
	}


	/**
	 * 获取路由名称
	 *
	 * @param sysMenuDo 菜单信息
	 * @return 路由名称
	 * @since 2024/8/14
	 */
	public String getRouteName(SysMenuDo sysMenuDo) {
		// 非外链并且是一级目录(类型为目录)
		if (isMenuFrame(sysMenuDo)) {
			return "";
		}
		return getRouteName(sysMenuDo.getRouteName(), sysMenuDo.getPath());
	}

	@Override
	public Boolean saveVo(SysMenuDo sysMenuDo) {
		// 保证菜单名称唯一
		Assert.isTrue(this.count(new LambdaQueryWrapper<SysMenuEntity>()
				.eq(SysMenuEntity::getMenuName, sysMenuDo.getMenuName())) == 0, 200013);
		// 保证 当为外链的时候必须是以http(s)开头
		Assert.isFalse(sysMenuDo.getIsFrame()
				.equals(SysMenuEnums.IS_FRAME_YES.getValue())
				&& (StrUtil.startWith(sysMenuDo.getPath(), "http")
				|| StrUtil.startWith(sysMenuDo.getPath(), "https")), 200014);

		return super.saveVo(sysMenuDo);
	}

	@Override
	public Boolean updateVoById(SysMenuDo sysMenuDo) {
		// 保证菜单名称唯一
		Assert.isTrue(this.count(new LambdaQueryWrapper<SysMenuEntity>()
				.eq(SysMenuEntity::getMenuName, sysMenuDo.getMenuName())
				.ne(SysMenuEntity::getId, sysMenuDo.getId())) == 0, 200013);
		// 保证 当为外链的时候必须是以http(s)开头
		Assert.isFalse(sysMenuDo.getIsFrame().equals(SysMenuEnums.IS_FRAME_YES.getValue())
				&& (StrUtil.startWith(sysMenuDo.getPath(), "http")
				|| StrUtil.startWith(sysMenuDo.getPath(), "https")), 200014);
		// 判断父菜单不能是自己
		Assert.isFalse(sysMenuDo.getId().equals(sysMenuDo.getParentId()), 200015);
		return super.updateVoById(sysMenuDo);
	}

	@Override
	public Boolean deleteById(Long id) {
		return super.deleteById(id);
	}

	@Override
	public Boolean deleteById(Serializable id) {
		// 检查当前菜单是否包含子菜单
		Assert.isFalse(super.count(new LambdaQueryWrapper<SysMenuEntity>()
				.eq(SysMenuEntity::getParentId, id)) > 0, 200016);
		// 检查当前菜单是否绑定角色
		Assert.isFalse(sysRoleMenuService.checkMenuExistRole((Long) id), 200017);
		return super.deleteById(id);
	}
}

