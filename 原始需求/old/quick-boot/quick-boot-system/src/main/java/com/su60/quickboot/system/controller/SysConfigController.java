package com.su60.quickboot.system.controller;

import  com.su60.quickboot.common.core.PageInfo;
import  com.su60.quickboot.common.core.PageRequest;
import  com.su60.quickboot.data.mybatisplus.PageVoHandler;
import  com.su60.quickboot.common.validation.AddGroup;
import  com.su60.quickboot.common.validation.UpdateGroup;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.su60.quickboot.system.dos.SysConfigDo;
import com.su60.quickboot.system.entity.SysConfigEntity;
import com.su60.quickboot.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import java.util.List;
import org.springframework.validation.annotation.Validated;
/**
*
*参数配置表
*
*
* @author luyanan
* @since 2026/01/11
*/
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/sysconfig")

 public class SysConfigController {


  private final ISysConfigService sysConfigService;

    /**
    * 分页查询
    *
    * @param sysConfigDo 分页参数
    * @return 分页结果
    * @since  2026/01/11
    */
    @SaCheckPermission("system:sysconfig:list")
    @GetMapping("list")
    public PageInfo<SysConfigDo> page(SysConfigDo sysConfigDo) {

                 return sysConfigService.page(sysConfigDo);

        }

                    /**
                    * 保存
                    *
                    * @param sysConfigDo 参数配置表
                    * @return 是否成功
                    * @since  2026/01/11
                    */
                    @SaCheckPermission("system:sysconfig:add")
                    @PostMapping()
                    public Boolean save(@RequestBody @Validated(AddGroup.class) SysConfigDo sysConfigDo) {
                    return sysConfigService.save(sysConfigDo);
                    }


                    /**
                    * 根据id修改
                    *
                    * @param sysConfigDo 参数配置表
                    * @return 是否成功
                    * @since 2024/06/29
                    */
                    @SaCheckPermission("system:sysconfig:edit")
                    @PostMapping("/update")
                    public Boolean updateById(@RequestBody @Validated(UpdateGroup.class) SysConfigDo sysConfigDo) {
                    return sysConfigService.updateById(sysConfigDo);
                    }


                    /**
                    * 根据id查询
                    *
                    * @param id id
                    * @return 参数配置表
                    * @since  2026/01/11
                    */
                    @SaCheckPermission("system:sysconfig:query")
                    @GetMapping("/{id}")
                    public SysConfigDo getById(@PathVariable("id") Long id) {
                    return sysConfigService.getVoById(id);
                    }


                    /**
                    * 根据ids 删除
                    *
                    * @param ids 集合
                    * @return 是否成功
                    * @since  2026/01/11
                    */
                            @SaCheckPermission("system:sysconfig:remove")
                    @PostMapping("/delete")
                    public Boolean deleteByIds(@RequestBody List<Long> ids) {
                    return sysConfigService.deleteByIds(ids);
                    }
}