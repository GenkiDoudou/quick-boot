package com.su60.quickboot.quartz.controller;

import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.quartz.dos.SysJobLogDo;
import com.su60.quickboot.quartz.entity.SysJobLogEntity;
import com.su60.quickboot.quartz.service.ISysJobLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

/**
 * 定时任务调度日志表
 *
 * @author luyanan
 * @since 2024/11/14
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/quartz/sysjoblog")

public class SysJobLogController {


	private final ISysJobLogService sysJobLogService;

	/**
	 * 分页查询
	 *
	 * @param sysJobLogDo 分页参数
	 * @return 分页结果
	 * @since 2024/11/14
	 */
	@SaCheckPermission("quartz:sysjoblog:list")
	@GetMapping("list")
	public PageInfo<SysJobLogDo> page(SysJobLogDo sysJobLogDo) {


		return  sysJobLogService.page(sysJobLogDo);

	}


	/**
	 * 根据id查询
	 *
	 * @param id id
	 * @return 定时任务调度日志表
	 * @since 2024/11/14
	 */
	@SaCheckPermission("quartz:sysjoblog:query")
	@GetMapping("/{id}")
	public SysJobLogDo getById(@PathVariable("id") Long id) {
		return sysJobLogService.getVoById(id);
	}

	/**
	 * 清空日志
	 *
	 * @param jobId 任务id
	 * @return 是否成功
	 * @since 2024/11/14
	 */
	@SaCheckPermission("quartz:sysjoblog:remove")
	@DeleteMapping("clean")
	public Boolean clean(Long jobId) {

		return sysJobLogService.clean(jobId);
	}

}