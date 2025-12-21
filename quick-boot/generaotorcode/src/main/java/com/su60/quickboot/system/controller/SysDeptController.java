package com.su60.quickboot.system.controller;

import cn.t200.quickboot.common.core.PageInfo;
import cn.t200.quickboot.common.core.PageRequest;
import cn.t200.quickboot.data.mybatisplus.PageVoHandler;
import cn.t200.quickboot.common.validation.AddGroup;
import cn.t200.quickboot.common.validation.UpdateGroup;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.su60.quickboot.system.dos.SysDeptDo;
import com.su60.quickboot.system.entity.SysDeptEntity;
import com.su60.quickboot.system.service.ISysDeptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
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
    * @since  2025/11/27
    */
    @SaCheckPermission("system:sysdept:list")
    @GetMapping("list")
    public PageInfo<SysDeptDo> page(SysDeptDo sysDeptDo) {

        return sysDeptService.page(sysDeptDo, new PageVoHandler<SysDeptEntity, SysDeptDo>() {
        @Override
        public void queryWrapperHandler(SysDeptDo vo, SysDeptEntity sysDeptEntity, LambdaQueryWrapper<SysDeptEntity> queryWrapper) {


}



        });

        }

                    /**
                    * 保存
                    *
                    * @param sysDeptDo 部门表
                    * @return 是否成功
                    * @since  2025/11/27
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
                    * @since  2025/11/27
                    */
                    @SaCheckPermission("system:sysdept:query")
                    @GetMapping("/{id}")
                    public SysDeptDo getById(@PathVariable("id") Long id) {
                    return sysDeptService.getVoById(id);
                    }


                    /**
                    * 根据ids 删除
                    *
                    * @param ids 多个以英文逗号(,)分割
                    * @return 是否成功
                    * @since  2025/11/27
                    */
                    @SaCheckPermission("system:sysdept:remove")
                    @DeleteMapping()
                    public Boolean deleteByIds(@RequestBody List<Long> ids) {
                    return sysDeptService.deleteByIds(ids);
                    }
}