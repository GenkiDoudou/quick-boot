package io.github.genkidoudou.web.system.dept.service;

import io.github.genkidoudou.web.system.dept.domain.SysDept;
import io.github.genkidoudou.web.system.dept.dto.SysDeptSaveRequest;
import io.github.genkidoudou.web.system.dept.vo.SysDeptTreeSelectVo;
import io.github.genkidoudou.web.system.dept.vo.SysDeptTreeVo;

import java.util.List;

/**
 * 部门业务：内存建树、列表剪枝筛选、下拉树、CRUD 与删除/改父校验。
 */
public interface DeptService {

    /**
     * 部门列表（嵌套树）。无筛选参数时返回全量未删树；有任一非空筛选时按 delta spec 剪枝。
     *
     * @param deptName 部门名称（可选，非空时子串匹配，忽略大小写）
     * @param leader     负责人（可选，非空时子串匹配，忽略大小写）
     * @param status     状态（可选，非空时精确匹配）
     * @return 根节点数组
     */
    List<SysDeptTreeVo> listTree(String deptName, String leader, String status);

    /**
     * 全量部门下拉树（不受列表筛选影响），节点为 {@code id}/{@code label}/{@code children}。
     *
     * @return 根节点数组
     */
    List<SysDeptTreeSelectVo> treeselect();

    /**
     * 按主键查询未逻辑删除的部门。
     *
     * @param deptId 部门 id
     * @return 实体；不存在或已删时返回 {@code null}
     */
    SysDept getById(Long deptId);

    /**
     * 新增部门。
     *
     * @param dept 载荷（{@code deptId} 一般由框架生成）
     */
    void add(SysDeptSaveRequest req);

    /**
     * 修改部门。
     *
     * @param dept 载荷（须含 {@code deptId}）
     */
    void update(SysDeptSaveRequest req);

    /**
     * 逻辑删除部门；存在未删子部门时拒绝。
     * <p>
     * 用户占用校验见实现类预留说明（当前迭代不查用户表）。
     *
     * @param deptId 部门 id
     */
    void remove(Long deptId);
}
