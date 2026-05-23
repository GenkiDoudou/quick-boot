package com.su60.quickboot.demo.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.core.PageRequest;
import com.su60.quickboot.common.vo.DictVo;
import com.su60.quickboot.core.security.LoginUserUtils;
import com.su60.quickboot.data.mybatisplus.PageUtils;
import com.su60.quickboot.demo.dos.DemoCrudDos;
import com.su60.quickboot.system.dos.SysMenuDo;
import com.su60.quickboot.system.service.ISysMenuService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * crud
 *
 * @author luyanan
 * @since 2024/11/16
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("demo/crud")
public class DemoCurdController {

	private final ISysMenuService sysMenuService;

	/**
	 * 列表
	 */
	private static final List<DemoCrudDos> LIST = new ArrayList<>();

	static {
		for (int i = 0; i < 5; i++) {
			LIST.add(new DemoCrudDos()
					.setId((long) i)
					.setInput("input-" + i)
					.setInputNumber(i)
					.setSelect("0")
					.setRadio(RandomUtil.randomInt(2) + "")
					.setRemoteSelect("5" + i)
					.setTree("1000")
					.setFile("http://127.0.0.1:12000/file/view/demofile/20241121/0e5dfddd1b2cfc59db55868e90e5f544.doc,"
							+ "http://127.0.0.1:12000/file/view/demofile/20241121/0e5dfddd1b2cfc59db55868e90e5f544.doc")
					.setImage("http://127.0.0.1:12000/file/view/demo/20241121/e9c6b6cb6f7c26bdf4d707dbab9bf887.png,"
							+ "http://127.0.0.1:12000/file/view/demo/20241121/e9c6b6cb6f7c26bdf4d707dbab9bf887.png")
					.setCheckBox("0")
					.setDatetime(new Date())
					.setDataPicker("2024-11-20,2024-12-19")
					.setDataTimePicker("2024-11-13 23:00"));

		}
	}


	/**
	 * 页面
	 *
	 * @param demoCrudDos 演示crud dos
	 * @param request     请求
	 * @return 页面信息<演示crud dos>
	 */
	@SaCheckPermission("demo:crud:list")
	@GetMapping("list")
	public PageInfo<DemoCrudDos> page(DemoCrudDos demoCrudDos, HttpServletRequest request) {
		PageRequest<DemoCrudDos> page = PageUtils.getPage(demoCrudDos, request);
		return PageUtils.ljPage(LIST, page.getCurrent(), page.getSize());
	}


	/**
	 * 远程选择
	 *
	 * @param query 查询
	 * @param value value值
	 * @return 列表
	 */
	@GetMapping("remoteSelect")
	public List<DictVo> remoteSelect(String query, String value) {
		List<DictVo> list = new ArrayList<>();
		for (int i = 0; i < 100; i++) {

			list.add(new DictVo<String>()
					.setValue(i + "")
					.setLabel("remote-" + i));
		}
		if (StrUtil.isNotBlank(query)) {
			list = list.stream().filter(item -> item.getLabel().contains(query)).toList();
		}
		if (StrUtil.isNotBlank(value)) {
			list = list.stream().filter(item -> item.getValue().equals(value)).toList();
		}
		return CollectionUtil.sub(list, 0, 10);
	}


	/**
	 * 按id获取
	 *
	 * @param id 身份证件
	 * @return 测试实体类
	 */
	@SaCheckPermission("demo:crud:query")
	@GetMapping("/{id}")
	public DemoCrudDos getById(@PathVariable("id") Long id) {
		return LIST.stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
	}

	/**
	 * 树形下拉框
	 *
	 * @param id id
	 * @return 树形结果
	 * @since 2024/11/19
	 */
	@GetMapping("tree")
	public List<Tree<Long>> tree(@RequestParam(value = "id", required = false) Long id) {

		List<SysMenuDo> sysMenuDos = sysMenuService.listByUserId(LoginUserUtils.getUser().getId(), new SysMenuDo()
				.setParentId(id));

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

	@GetMapping("tree/parent/{id}")
	public List<Long> findParent(@PathVariable("id") Long id) {

		return sysMenuService.findParent(id);
	}


	/**
	 * 新增
	 *
	 * @param demoCrudDos demo
	 * @return 是否成功
	 * @since 2024/11/20
	 */
	@SaCheckPermission("demo:crud:add")
	@PostMapping()
	public Boolean save(@RequestBody DemoCrudDos demoCrudDos) {
		//取出来list中最大的id
		Long id = LIST.stream().mapToLong(DemoCrudDos::getId).max().orElse(0L);
		demoCrudDos.setId(id + 1);
		LIST.add(demoCrudDos);
		return true;
	}

	/**
	 * 修改
	 *
	 * @param demoCrudDos demo
	 * @return 是否成功
	 * @since 2024/11/23
	 */
	@SaCheckPermission("demo:crud:edit")
	@PostMapping("/update")
	public Boolean update(@RequestBody DemoCrudDos demoCrudDos) {
		Integer index = findIndex(demoCrudDos.getId());
		if (index != null) {
			LIST.set(index, demoCrudDos);
		}
		return true;
	}


	/**
	 * 批量删除
	 *
	 * @param ids id集合
	 * @return 是否成功
	 * @since 2024/11/23
	 */
	@SaCheckPermission("demo:crud:remove")
	@PostMapping("/delete")
	public Boolean delete(@RequestBody List<Long> ids) {
		for (Long id : ids) {
			int index = findIndex(id);

			LIST.remove(index);
		}
		return true;
	}

	/**
	 * 根据id查询集合的下标
	 *
	 * @param id id
	 * @return 集合下标
	 * @since 2024/11/23
	 */
	private int findIndex(Long id) {
		for (int i = 0; i < LIST.size(); i++) {
			DemoCrudDos demoCrudDos1 = LIST.get(i);
			if (demoCrudDos1.getId().equals(id)) {
				return i;
			}
		}
		return -1;
	}

}
