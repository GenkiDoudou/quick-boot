package com.su60.quickboot.system.service.impl;

import com.su60.quickboot.system.entity.SysDeptEntity;
import com.su60.quickboot.system.dos.SysDeptDo;
import com.su60.quickboot.system.mapper.SysDeptMapper;
import com.su60.quickboot.system.service.ISysDeptService;
import cn.t200.quickboot.data.mybatisplus.BaseServiceImpl2;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
/**
 * <p>
 * 部门表 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2025/11/27
 */
@RequiredArgsConstructor
@Service
public class SysDeptServiceImpl extends BaseServiceImpl2<SysDeptMapper, SysDeptEntity, SysDeptDo> implements ISysDeptService {

}

