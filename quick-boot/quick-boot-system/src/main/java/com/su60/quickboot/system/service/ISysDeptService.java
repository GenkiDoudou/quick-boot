package com.su60.quickboot.system.service;

import cn.hutool.core.lang.tree.Tree;
import com.su60.quickboot.core.security.LoginUser;
import com.su60.quickboot.system.dos.SysDeptDo;
import com.su60.quickboot.system.entity.SysDeptEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 部门表 服务类
 * </p>
 *
 * @author luyanan
 * @since 2025/11/27
 */

public interface ISysDeptService {

	/**
	 * 树形列表展示
	 * @since 2025/12/22
	 * @param sysDeptDo 参数
	 * @return
	 */
	List<SysDeptDo> listAll(SysDeptDo sysDeptDo);


	List<SysDeptEntity> listAll();

	/**
	 * 树形展示
	 * @since 2025/12/23
	 * @param sysDeptDo
	 * @return
	 */
	List<Tree<Long>> treeList(SysDeptDo sysDeptDo);

	/**
	 * 根据id集合查询
	 * @since 2025/12/25
	 * @param deptIds  id集合
	 * @return
	 */
	Map<Long, SysDeptDo> listByIds(List<Long> deptIds);


	/**
	 * 查询本部门以及所有的下级部门
	 * @since 2025/12/27
	 * @param deptId
	 * @return
	 */
	List<Long> listDeptAndChild(Long deptId);



	/**
	 * 保存
	 * @since 2025/12/27
	 * @param sysDeptDo
	 * @return
	 */
	Boolean saveVo(SysDeptDo sysDeptDo);

	/**
	 * 根据修改
	 * @since 2025/12/27
	 * @param sysDeptDo
	 * @return
	 */
	Boolean updateVoById(SysDeptDo sysDeptDo);

	/**
	 * 根据id查询
	 * @since 2025/12/27
	 * @param id  id
	 * @return
	 */
	SysDeptDo getVoById(Long id);

	/**
	 * 根据id删除
	 * @since 2025/12/27
	 * @param id  id
	 * @return
	 */
	Boolean deleteById(Long id);
}
