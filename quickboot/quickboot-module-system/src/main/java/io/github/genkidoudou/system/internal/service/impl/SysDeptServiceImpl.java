package io.github.genkidoudou.system.internal.service.impl;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.mybatisplus.BaseServiceImpl;
import io.github.genkidoudou.system.internal.entity.*;
import io.github.genkidoudou.system.internal.mapper.*;
import io.github.genkidoudou.system.internal.service.ISysDeptService;
import io.github.genkidoudou.system.internal.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门管理实现：树形组装、删除前校验子部门与用户绑定。
 */
@Service @RequiredArgsConstructor
public class SysDeptServiceImpl extends BaseServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {
  private final SysUserMapper userMapper;
  /** {@inheritDoc} */
  public List<SysDeptVo> list(String name,String status){ List<SysDept> all=list(new LambdaQueryWrapper<SysDept>().like(StrUtil.isNotBlank(name),SysDept::getDeptName,name).eq(StrUtil.isNotBlank(status),SysDept::getStatus,status).orderByAsc(SysDept::getOrderNum)); return tree(all,0L); }
  /** {@inheritDoc} */
  public List<SysDeptVo> treeSelect(){ return tree(list(new LambdaQueryWrapper<SysDept>().eq(SysDept::getStatus,"0").orderByAsc(SysDept::getOrderNum)),0L); }
  private List<SysDeptVo> tree(List<SysDept> all,Long pid){ return all.stream().filter(x->Objects.equals(x.getParentId()==null?0L:x.getParentId(),pid)).map(x->{SysDeptVo v=toVo(x,SysDeptVo.class);v.setChildren(tree(all,x.getDeptId()));return v;}).toList(); }
  /** {@inheritDoc} */
  public SysDeptVo getDetail(Long id){ SysDept d=getById(id);if(d==null)throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM,"部门不存在");return toVo(d,SysDeptVo.class); }
  /** {@inheritDoc} */
  public Long add(SysDeptVo v){ SysDept d=toEntity(v);d.setDeptId(null);d.setParentId(v.getParentId()==null?0L:v.getParentId());d.setDeptName(v.getDeptName().trim());d.setOrderNum(v.getOrderNum()==null?0:v.getOrderNum());d.setStatus(StrUtil.blankToDefault(v.getStatus(),"0"));save(d);return d.getDeptId(); }
  /** {@inheritDoc} */
  public boolean update(SysDeptVo v){ if(Objects.equals(v.getDeptId(),v.getParentId()))throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM,"上级部门不能是自身");SysDept old=getById(v.getDeptId());if(old==null)throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM,"部门不存在");SysDept d=toEntity(v);d.setDeptId(old.getDeptId());d.setParentId(v.getParentId()==null?0L:v.getParentId());d.setDeptName(v.getDeptName().trim());d.setStatus(StrUtil.blankToDefault(v.getStatus(),old.getStatus()));return updateById(d); }
  /** 存在子部门或绑定用户时拒绝删除。 */
  public void remove(Collection<Long> ids){for(Long id:ids){if(count(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId,id))>0)throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM,"存在子部门");if(userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId,id))>0)throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM,"部门存在绑定用户");removeById(id);}}
  /** {@inheritDoc} */
  public List<SysDeptVo> export(SysDeptVo q){List<SysDept> es=q!=null&&q.getIds()!=null&&!q.getIds().isEmpty()?listByIds(q.getIds()):list();if(es.size()>5000)throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM,"导出条数超过上限 5000");return es.stream().map(x->toVo(x,SysDeptVo.class)).collect(Collectors.toList());}
  /** {@inheritDoc} */
  public ExcelResult<SysDeptImportRow> importExcel(MultipartFile f,boolean update) throws IOException {ExcelResult<SysDeptImportRow> r=ExcelUtils.importExcel(f.getInputStream(),SysDeptImportRow.class,(row,c)->{SysDept d=new SysDept();d.setParentId(row.getParentId()==null?0L:row.getParentId());d.setDeptName(row.getDeptName());d.setOrderNum(row.getOrderNum());d.setLeader(row.getLeader());d.setPhone(row.getPhone());d.setEmail(row.getEmail());d.setStatus(StrUtil.blankToDefault(row.getStatus(),"0"));save(d);},(b,c)->{});r.writeErrorFile();return r;}
}
