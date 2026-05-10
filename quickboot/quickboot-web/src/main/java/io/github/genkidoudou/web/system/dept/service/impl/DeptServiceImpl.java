package io.github.genkidoudou.web.system.dept.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.dept.domain.SysDept;
import io.github.genkidoudou.web.system.dept.dto.SysDeptSaveRequest;
import io.github.genkidoudou.web.system.dept.mapper.SysDeptMapper;
import io.github.genkidoudou.web.system.dept.service.DeptService;
import io.github.genkidoudou.web.system.dept.vo.SysDeptTreeSelectVo;
import io.github.genkidoudou.web.system.dept.vo.SysDeptTreeVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 部门服务实现：全表加载后在内存建树；写操作含父级存在、自指、成环及删前子部门计数校验。
 */
@Service
public class DeptServiceImpl implements DeptService {

    private static final Long ROOT_PARENT_ID = -1L;

    private final SysDeptMapper sysDeptMapper;

    public DeptServiceImpl(SysDeptMapper sysDeptMapper) {
        this.sysDeptMapper = sysDeptMapper;
    }

    @Override
    public List<SysDeptTreeVo> listTree(String deptName, String leader, String status) {
        List<SysDept> all = loadAllDepts();
        if (!hasAnyFilter(deptName, leader, status)) {
            return buildTreeVos(all);
        }
        Set<Long> keep = computeFilteredNodeIds(all, deptName, leader, status);
        List<SysDept> pruned = all.stream().filter(d -> keep.contains(d.getDeptId())).toList();
        return buildTreeVos(pruned);
    }

    @Override
    public List<SysDeptTreeSelectVo> treeselect() {
        List<SysDept> all = loadAllDepts();
        return buildSelectVos(all);
    }

    @Override
    public SysDept getById(Long deptId) {
        return sysDeptMapper.selectById(deptId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysDeptSaveRequest req) {
        Objects.requireNonNull(req, "req");
        SysDept dept = toEntity(req);
        normalizeNew(dept);
        validateParentExists(dept.getParentId(), null);
        validateStatus(dept.getStatus());
        validateContactLoose(dept.getPhone(), dept.getEmail());
        sysDeptMapper.insert(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysDeptSaveRequest req) {
        Objects.requireNonNull(req, "req");
        if (req.getDeptId() == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "修改部门须携带 deptId");
        }
        SysDept dept = toEntity(req);
        SysDept existing = sysDeptMapper.selectById(dept.getDeptId());
        if (existing == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "部门不存在或已删除");
        }
        validateStatus(dept.getStatus());
        validateContactLoose(dept.getPhone(), dept.getEmail());
        Long parentId = dept.getParentId() != null ? dept.getParentId() : existing.getParentId();
        validateParentExists(parentId, dept.getDeptId());
        if (parentId.equals(dept.getDeptId())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "上级部门不能为自身");
        }
        assertNoCycle(dept.getDeptId(), parentId);
        dept.setParentId(parentId);
        sysDeptMapper.updateById(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long deptId) {
        SysDept existing = sysDeptMapper.selectById(deptId);
        if (existing == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "部门不存在或已删除");
        }
        long childCount = sysDeptMapper.selectCount(
                Wrappers.<SysDept>lambdaQuery().eq(SysDept::getParentId, deptId));
        if (childCount > 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "存在未删除的子部门，无法删除");
        }
        sysDeptMapper.deleteById(deptId);
    }

    private List<SysDept> loadAllDepts() {
        return sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getOrderNum));
    }

    private static boolean hasAnyFilter(String deptName, String leader, String status) {
        return StrUtil.isNotBlank(deptName) || StrUtil.isNotBlank(leader) || StrUtil.isNotBlank(status);
    }

    static Set<Long> computeFilteredNodeIds(List<SysDept> all, String deptName, String leader, String status) {
        Map<Long, SysDept> byId = new HashMap<>(all.size() * 2);
        for (SysDept d : all) {
            byId.put(d.getDeptId(), d);
        }
        Set<Long> matched = new HashSet<>();
        for (SysDept d : all) {
            if (matchesFilter(d, deptName, leader, status)) {
                matched.add(d.getDeptId());
            }
        }
        if (matched.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Long> keep = new HashSet<>();
        for (Long id : matched) {
            Long cur = id;
            while (cur != null) {
                if (!keep.add(cur)) {
                    break;
                }
                SysDept node = byId.get(cur);
                if (node == null) {
                    break;
                }
                Long p = node.getParentId();
                if (p == null || ROOT_PARENT_ID.equals(p)) {
                    break;
                }
                cur = p;
            }
        }
        return keep;
    }

    static boolean matchesFilter(SysDept d, String deptName, String leader, String status) {
        if (StrUtil.isNotBlank(deptName)) {
            String needle = deptName.trim();
            if (d.getDeptName() == null || !d.getDeptName().toLowerCase().contains(needle.toLowerCase())) {
                return false;
            }
        }
        if (StrUtil.isNotBlank(leader)) {
            String needle = leader.trim();
            if (d.getLeader() == null || !d.getLeader().toLowerCase().contains(needle.toLowerCase())) {
                return false;
            }
        }
        if (StrUtil.isNotBlank(status)) {
            if (!status.trim().equals(d.getStatus())) {
                return false;
            }
        }
        return true;
    }

    private List<SysDeptTreeVo> buildTreeVos(List<SysDept> rows) {
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        Comparator<SysDept> order = Comparator
                .comparing(SysDept::getOrderNum, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SysDept::getDeptName, Comparator.nullsLast(String::compareTo));
        Map<Long, List<SysDept>> childrenMap = new LinkedHashMap<>();
        for (SysDept d : rows) {
            Long pid = d.getParentId() != null ? d.getParentId() : ROOT_PARENT_ID;
            childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(d);
        }
        for (List<SysDept> list : childrenMap.values()) {
            list.sort(order);
        }
        List<SysDept> roots = childrenMap.getOrDefault(ROOT_PARENT_ID, Collections.emptyList());
        List<SysDeptTreeVo> out = new ArrayList<>();
        for (SysDept r : roots) {
            out.add(toTreeVo(r, childrenMap));
        }
        return out;
    }

    private SysDeptTreeVo toTreeVo(SysDept d, Map<Long, List<SysDept>> childrenMap) {
        SysDeptTreeVo vo = copyToTreeVo(d);
        List<SysDept> ch = childrenMap.getOrDefault(d.getDeptId(), Collections.emptyList());
        List<SysDeptTreeVo> vos = new ArrayList<>();
        for (SysDept c : ch) {
            vos.add(toTreeVo(c, childrenMap));
        }
        vo.setChildren(vos);
        return vo;
    }

    private static SysDeptTreeVo copyToTreeVo(SysDept d) {
        SysDeptTreeVo vo = new SysDeptTreeVo();
        vo.setDeptId(d.getDeptId());
        vo.setParentId(d.getParentId());
        vo.setDeptName(d.getDeptName());
        vo.setOrderNum(d.getOrderNum());
        vo.setLeader(d.getLeader());
        vo.setPhone(d.getPhone());
        vo.setEmail(d.getEmail());
        vo.setStatus(d.getStatus());
        vo.setRemark(d.getRemark());
        vo.setCreateBy(d.getCreateBy());
        vo.setCreateTime(d.getCreateTime());
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    private List<SysDeptTreeSelectVo> buildSelectVos(List<SysDept> rows) {
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        Comparator<SysDept> order = Comparator
                .comparing(SysDept::getOrderNum, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SysDept::getDeptName, Comparator.nullsLast(String::compareTo));
        Map<Long, List<SysDept>> childrenMap = new LinkedHashMap<>();
        for (SysDept d : rows) {
            Long pid = d.getParentId() != null ? d.getParentId() : ROOT_PARENT_ID;
            childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(d);
        }
        for (List<SysDept> list : childrenMap.values()) {
            list.sort(order);
        }
        List<SysDept> roots = childrenMap.getOrDefault(ROOT_PARENT_ID, Collections.emptyList());
        List<SysDeptTreeSelectVo> out = new ArrayList<>();
        for (SysDept r : roots) {
            out.add(toSelectVo(r, childrenMap));
        }
        return out;
    }

    private SysDeptTreeSelectVo toSelectVo(SysDept d, Map<Long, List<SysDept>> childrenMap) {
        SysDeptTreeSelectVo vo = new SysDeptTreeSelectVo();
        vo.setId(d.getDeptId());
        vo.setLabel(d.getDeptName());
        List<SysDept> ch = childrenMap.getOrDefault(d.getDeptId(), Collections.emptyList());
        List<SysDeptTreeSelectVo> vos = new ArrayList<>();
        for (SysDept c : ch) {
            vos.add(toSelectVo(c, childrenMap));
        }
        vo.setChildren(vos);
        return vo;
    }

    private void normalizeNew(SysDept dept) {
        if (dept.getParentId() == null) {
            dept.setParentId(ROOT_PARENT_ID);
        }
        if (StrUtil.isBlank(dept.getDelFlag())) {
            dept.setDelFlag("0");
        }
        if (StrUtil.isBlank(dept.getStatus())) {
            dept.setStatus("0");
        }
        if (dept.getOrderNum() == null) {
            dept.setOrderNum(0);
        }
    }

    private void validateParentExists(Long parentId, Long excludeDeptIdWhenCheckingSelf) {
        if (ROOT_PARENT_ID.equals(parentId)) {
            return;
        }
        SysDept p = sysDeptMapper.selectById(parentId);
        if (p == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "上级部门不存在或已删除");
        }
        if (excludeDeptIdWhenCheckingSelf != null && excludeDeptIdWhenCheckingSelf.equals(parentId)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "上级部门不能为自身");
        }
    }

    private void validateStatus(String status) {
        if (StrUtil.isBlank(status)) {
            return;
        }
        if (!"0".equals(status) && !"1".equals(status)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "状态取值须为 0（正常）或 1（停用），与字典 sys_normal_disable 一致");
        }
    }

    private void validateContactLoose(String phone, String email) {
        if (StrUtil.isNotBlank(phone)) {
            String p = phone.trim();
            if (p.length() > 20 || !p.matches("[\\d\\-+\\s()]{5,20}")) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "联系电话格式不正确");
            }
        }
        if (StrUtil.isNotBlank(email)) {
            String e = email.trim();
            if (e.length() > 100 || !e.contains("@") || e.indexOf('@') != e.lastIndexOf('@')) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "邮箱格式不正确");
            }
        }
    }

    private void assertNoCycle(Long deptId, Long newParentId) {
        if (ROOT_PARENT_ID.equals(newParentId)) {
            return;
        }
        Set<Long> descendants = collectDescendantIds(deptId);
        if (descendants.contains(newParentId)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "不能将上级部门设为自己或子孙节点，否则会形成环");
        }
    }

    private Set<Long> collectDescendantIds(Long rootId) {
        List<SysDept> all = loadAllDepts();
        Map<Long, List<Long>> children = new HashMap<>();
        for (SysDept d : all) {
            Long pid = d.getParentId() != null ? d.getParentId() : ROOT_PARENT_ID;
            children.computeIfAbsent(pid, k -> new ArrayList<>()).add(d.getDeptId());
        }
        Set<Long> out = new HashSet<>();
        ArrayDeque<Long> dq = new ArrayDeque<>();
        dq.add(rootId);
        while (!dq.isEmpty()) {
            Long id = dq.removeFirst();
            if (!out.add(id)) {
                continue;
            }
            List<Long> ch = children.getOrDefault(id, Collections.emptyList());
            dq.addAll(ch);
        }
        return out;
    }

    private SysDept toEntity(SysDeptSaveRequest req) {
        SysDept entity = new SysDept();
        entity.setDeptId(req.getDeptId());
        entity.setParentId(req.getParentId());
        entity.setDeptName(req.getDeptName());
        entity.setOrderNum(req.getOrderNum());
        entity.setLeader(req.getLeader());
        entity.setPhone(req.getPhone());
        entity.setEmail(req.getEmail());
        entity.setStatus(req.getStatus());
        entity.setRemark(req.getRemark());
        return entity;
    }
}
