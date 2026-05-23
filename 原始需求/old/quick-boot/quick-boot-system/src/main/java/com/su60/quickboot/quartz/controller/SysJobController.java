package com.su60.quickboot.quartz.controller;

import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.quartz.dos.SysJobDo;
import com.su60.quickboot.quartz.entity.SysJobEntity;
import com.su60.quickboot.quartz.service.ISysJobService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 定时任务调度表
 *
 * @author luyanan
 * @since 2024/11/07
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/quartz/sysjob")

public class SysJobController {


	private final ISysJobService sysJobService;

	/**
	 * 分页查询
	 *
	 * @param sysJobDo 分页参数
	 * @return 分页结果
	 * @since 2024/11/07
	 */
	@SaCheckPermission("quartz:sysjob:list")
	@GetMapping("list")
	public PageInfo<SysJobDo> page(SysJobDo sysJobDo) {


		return  sysJobService.page(sysJobDo);

	}

	/**
	 * 保存
	 *
	 * @param sysJobDo 定时任务调度表
	 * @return 是否成功
	 * @since 2024/11/07
	 */
	@SaCheckPermission("quartz:sysjob:add")
	@PostMapping()
	public Boolean save(@RequestBody @Validated(AddGroup.class) SysJobDo sysJobDo) {
		return sysJobService.saveVo(sysJobDo);
	}


	/**
	 * 根据id修改
	 *
	 * @param sysJobDo 定时任务调度表
	 * @return 是否成功
	 * @since 2024/06/29
	 */
	@SaCheckPermission("quartz:sysjob:edit")
	@PostMapping("/update")
	public Boolean updateById(@RequestBody @Validated(UpdateGroup.class) SysJobDo sysJobDo) {
		return sysJobService.updateVoById(sysJobDo);
	}


	/**
	 * 根据id查询
	 *
	 * @param id id
	 * @return 任务信息
	 * @since 2024/11/07
	 */
	@SaCheckPermission("quartz:sysjob:query")
	@GetMapping("/{id}")
	public SysJobDo getById(@PathVariable("id") Long id) {
		return sysJobService.getVoById(id);
	}


	/**
	 * 根据ids 删除
	 *
	 * @param ids 多个以英文逗号(,)分割
	 * @return 是否成功
	 * @since 2024/11/07
	 */
	@SaCheckPermission("quartz:sysjob:remove")
	@PostMapping("/delete")
	public Boolean deleteByIds(@RequestBody List<Long> ids) {
		return sysJobService.deleteByIds(ids);
	}


	/**
	 * 修改状态
	 *
	 * @param id     任务id
	 * @param status 状态
	 * @return 是否成功
	 * @since 2024/11/13
	 */
	@SaCheckPermission("quartz:sysjob:changeStatus")
	@GetMapping("changeStatus/{id}/{status}")
	public Boolean changeStatus(@PathVariable("id") Long id, @PathVariable("status") String status) {
		return sysJobService.changeStatus(id, status);
	}

	/**
	 * 立即执行
	 *
	 * @param id 任务id
	 * @return 是否成功
	 * @since 2024/11/13
	 */
	@SaCheckPermission("quartz:sysjob:run")
	@GetMapping("/run/{id}")
	public Boolean run(@PathVariable("id") Long id) {

		return sysJobService.run(id);
	}
}