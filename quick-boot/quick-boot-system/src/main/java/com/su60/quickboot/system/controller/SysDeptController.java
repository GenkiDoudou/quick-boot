package com.su60.quickboot.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.tree.Tree;
import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import com.su60.quickboot.system.dos.SysDeptDo;
import com.su60.quickboot.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 *部门表
 *
 *
 * @author luyanan
 * @since 2025/11/27
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/sysdept")

public class SysDeptController {


	private final ISysDeptService sysDeptService;

	/**
	 * 分页查询
	 *
	 * @param sysDeptDo 分页参数
	 * @return 分页结果
	 * @since 2025/11/27
	 */
	@SaCheckPermission("system:sysdept:list")
	@GetMapping("list")
	public List<SysDeptDo> page(SysDeptDo sysDeptDo) {
		return this.sysDeptService.listAll(sysDeptDo);

	}


	/**
	 * 分页查询
	 *
	 * @param sysDeptDo 分页参数
	 * @return 分页结果
	 * @since 2025/11/27
	 */
	@SaCheckPermission("system:sysdept:list")
	@GetMapping("treeList")
	public List<Tree<Long>> treeList(SysDeptDo sysDeptDo) {
		return this.sysDeptService.treeList(sysDeptDo);

	}

	/**
	 * 保存
	 *
	 * @param sysDeptDo 部门表
	 * @return 是否成功
	 * @since 2025/11/27
	 */
	@SaCheckPermission("system:sysdept:add")
	@PostMapping()
	public Boolean save(@RequestBody @Validated(AddGroup.class) SysDeptDo sysDeptDo) {
		return sysDeptService.saveVo(sysDeptDo);
	}


	/**
	 * 根据id修改
	 *
	 * @param sysDeptDo 部门表
	 * @return 是否成功
	 * @since 2024/06/29
	 */
	@SaCheckPermission("system:sysdept:edit")
	@PutMapping
	public Boolean updateById(@RequestBody @Validated(UpdateGroup.class) SysDeptDo sysDeptDo) {
		return sysDeptService.updateVoById(sysDeptDo);
	}


	/**
	 * 根据id查询
	 *
	 * @param id id
	 * @return 部门表
	 * @since 2025/11/27
	 */
	@SaCheckPermission("system:sysdept:query")
	@GetMapping("/{id}")
	public SysDeptDo getById(@PathVariable("id") Long id) {
		return sysDeptService.getVoById(id);
	}


	/**
	 * 根据id 删除
	 *
	 * @param id  id
	 * @return 是否成功
	 * @since 2025/11/27
	 */
	@SaCheckPermission("system:sysdept:remove")
	@DeleteMapping("/{id}")
	public Boolean deleteByIds(@PathVariable("id") Long id) {
		return sysDeptService.deleteById(id);
	}
}