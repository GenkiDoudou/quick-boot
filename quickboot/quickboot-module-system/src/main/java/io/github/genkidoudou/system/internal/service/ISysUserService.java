package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.system.internal.entity.SysUser;
import io.github.genkidoudou.system.internal.vo.SysUserAuthRoleVo;
import io.github.genkidoudou.system.internal.vo.SysUserImportRow;
import io.github.genkidoudou.system.internal.vo.SysUserVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface ISysUserService {

  SysUser findByUserName(String username);

  SysUser findByUserId(Long userId);

  PageInfo<SysUserVo> page(PageRequest<SysUserVo> pageRequest);

  SysUserVo getDetail(Long userId);

  Long add(SysUserVo vo);

  boolean update(SysUserVo vo);

  void remove(Collection<Long> userIds);

  void changeStatus(Long userId, String status);

  void resetPwd(Long userId, String password);

  SysUserAuthRoleVo authRole(Long userId);

  void saveAuthRole(Long userId, List<Long> roleIds);

  List<SysUserVo> export(SysUserVo query);

  ExcelResult<SysUserImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException;
}
