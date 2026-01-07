package com.su60.quickboot.system.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.system.dos.SysOperLogDo;
import com.su60.quickboot.system.entity.SysOperLogEntity;
import com.su60.quickboot.system.service.ISysOperLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作日志记录
 *
 * @author luyanan
 * @since 2024/11/15
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/sysoperlog")

public class SysOperLogController {


	private final ISysOperLogService sysOperLogService;

	/**
	 * 分页查询
	 *
	 * @param sysOperLogDo 分页参数
	 * @return 分页结果
	 * @since 2024/11/15
	 */
	@SaCheckPermission("system:operlog:list")
	@GetMapping("list")
	public PageInfo<SysOperLogDo> page(SysOperLogDo sysOperLogDo) {
		return sysOperLogService.page(sysOperLogDo);


	}


	/**
	 * 根据id查询
	 *
	 * @param id id
	 * @return 操作日志记录
	 * @since 2024/11/15
	 */
	@SaCheckPermission("system:operlog:query")
	@GetMapping("/{id}")
	public SysOperLogDo getById(@PathVariable("id") Long id) {
		return sysOperLogService.getVoById(id);
	}


}