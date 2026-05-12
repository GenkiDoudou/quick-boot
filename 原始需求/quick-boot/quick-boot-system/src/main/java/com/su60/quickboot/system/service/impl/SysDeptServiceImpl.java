package com.su60.quickboot.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su60.quickboot.common.bean.BeanConvertUtils;
import com.su60.quickboot.core.security.LoginUser;
import com.su60.quickboot.data.datascope.DataScopeType;
import com.su60.quickboot.data.mybatisplus.BaseVoServiceImpl;
import com.su60.quickboot.system.dos.SysDeptDo;
import com.su60.quickboot.system.entity.SysDeptEntity;
import com.su60.quickboot.system.mapper.SysDeptMapper;
import com.su60.quickboot.system.service.ISysDeptService;
import com.su60.quickboot.system.service.ISysRoleDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门表 服务实现类
 *
 * 方案说明：
 * 1. 只缓存 selectAll
 * 2. 下级部门关系在内存中实时计算
 * 3. 部门变更时直接清缓存
 *
 * @author luyanan
 */
@RequiredArgsConstructor
@Service
public class SysDeptServiceImpl
		extends BaseVoServiceImpl<SysDeptMapper, SysDeptEntity, SysDeptDo>
		implements ISysDeptService {


	private final ISysRoleDeptService sysRoleDeptService;

	/**
	 * 查询全部部门（缓存）
	 */
	@Override
	@Cacheable(cacheNames = "sys:dept:all", key = "'list'")
	public List<SysDeptEntity> listAll() {
		return super.list(new LambdaQueryWrapper<>());
	}

	/**
	 * 查询部门列表（Do）
	 */
	@Override
	public List<SysDeptDo> listAll(SysDeptDo sysDeptDo) {
		LambdaQueryWrapper<SysDeptEntity> queryWrapper =
				new LambdaQueryWrapper<>(BeanConvertUtils.convertTo(sysDeptDo, SysDeptEntity::new));
		List<SysDeptEntity> list = super.list(queryWrapper);
		return BeanConvertUtils.convertListTo(list, SysDeptDo::new);
	}

	/**
	 * 构建部门树
	 */
	@Override
	public List<Tree<Long>> treeList(SysDeptDo sysDeptDo) {

		List<SysDeptEntity> list = listAll();
		if (CollectionUtil.isEmpty(list)) {
			return new ArrayList<>();
		}

		List<TreeNode<Long>> nodeList = list.stream().map(a -> {
			TreeNode<Long> node = new TreeNode<>();
			node.setId(a.getDeptId());
			node.setParentId(a.getParentId());
			node.setName(a.getDeptName());
			node.setWeight(a.getOrderNum());

			Map<String, Object> ext = new HashMap<>();
			ext.put("deptId", a.getDeptId());
			ext.put("deptName", a.getDeptName());
			ext.put("leader", a.getLeader());
			ext.put("phone", a.getPhone());
			ext.put("email", a.getEmail());
			ext.put("status", a.getStatus());
			ext.put("createBy", a.getCreateBy());
			ext.put("createTime", a.getCreateTime());
			node.setExtra(ext);

			return node;
		}).collect(Collectors.toList());

		return TreeUtil.build(nodeList, 0L);
	}

	/**
	 * 根据 ID 查询部门
	 */
	@Override
	public Map<Long, SysDeptDo> listByIds(List<Long> deptIds) {
		if (CollectionUtil.isEmpty(deptIds)) {
			return new HashMap<>();
		}

		List<SysDeptEntity> list = super.listByIds(deptIds);
		return list.stream()
				.map(a -> BeanConvertUtils.convertTo(a, SysDeptDo::new))
				.collect(Collectors.toMap(SysDeptDo::getDeptId, a -> a));
	}

	/**
	 * 查询当前部门及所有下级部门
	 */
	@Override
	public List<Long> listDeptAndChild(Long deptId) {

		SysDeptServiceImpl proxy = (SysDeptServiceImpl) AopContext.currentProxy();
		List<SysDeptEntity> all = proxy.listAll();
		if (CollectionUtil.isEmpty(all)) {
			return new ArrayList<>();
		}

		// parentId -> childrenDeptIds
		Map<Long, List<Long>> parentChildMap = new HashMap<>();

		for (SysDeptEntity dept : all) {
			parentChildMap
					.computeIfAbsent(dept.getParentId(), k -> new ArrayList<>())
					.add(dept.getDeptId());
		}

		List<Long> result = new ArrayList<>();
		dfsDept(deptId, parentChildMap, result);
		return result;
	}



	@Override
	public Boolean saveVo(SysDeptDo sysDeptDo) {
		return super.saveVo(sysDeptDo);
	}

	@Override
	public Boolean updateVoById(SysDeptDo sysDeptDo) {
		return super.updateVoById(sysDeptDo);
	}

	@Override
	public SysDeptDo getVoById(Long id) {
		return super.getVoById(id);
	}

	@Override
	public Boolean deleteById(Long id) {
		return super.deleteById(id);
	}

	/**
	 * DFS 递归查询下级部门
	 */
	private void dfsDept(Long deptId,
						 Map<Long, List<Long>> parentChildMap,
						 List<Long> result) {

		result.add(deptId);
		List<Long> children = parentChildMap.get(deptId);
		if (CollectionUtil.isEmpty(children)) {
			return;
		}

		for (Long childId : children) {
			dfsDept(childId, parentChildMap, result);
		}
	}

	/* ================== 部门变更，清缓存 ================== */

	@Override
	@CacheEvict(cacheNames = "sys:dept:all", allEntries = true)
	public boolean save(SysDeptEntity entity) {
		return super.save(entity);
	}

	@Override
	@CacheEvict(cacheNames = "sys:dept:all", allEntries = true)
	public boolean updateById(SysDeptEntity entity) {
		return super.updateById(entity);

	}


}
