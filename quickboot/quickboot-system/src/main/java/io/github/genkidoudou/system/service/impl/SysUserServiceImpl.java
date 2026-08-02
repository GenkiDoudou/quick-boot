package io.github.genkidoudou.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.genkidoudou.system.entity.SysUser;
import io.github.genkidoudou.system.mapper.SysUserMapper;
import io.github.genkidoudou.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {


  @Override
  public SysUser findByUserName(String username) {
    if (StrUtil.isBlank(username)) {
      return null;
    }
    return this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username), false);
  }

  @Override
  public SysUser findByUserId(Long userId) {
    if (userId == null) {
      return null;
    }
    return this.getById(userId);
  }
}
