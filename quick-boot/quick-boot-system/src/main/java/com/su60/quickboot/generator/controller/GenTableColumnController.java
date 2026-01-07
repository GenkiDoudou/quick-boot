package com.su60.quickboot.generator.controller;

import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.generator.dos.GenTableColumnDo;
import com.su60.quickboot.generator.entity.GenTableColumnEntity;
import com.su60.quickboot.generator.service.IGenTableColumnService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代码生成业务表字段
 *
 * @author luyanan
 * @since 2024/10/24
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/generator/gentablecolumn")

public class GenTableColumnController {


	private final IGenTableColumnService genTableColumnService;

	/**
	 * 分页查询
	 *
	 * @param genTableColumnDo 分页参数
	 * @return 分页信息
	 * @since 2024/10/24
	 */
	@SaCheckPermission("generator:gentablecolumn:list")
	@GetMapping("list")
	public PageInfo<GenTableColumnDo> page(GenTableColumnDo genTableColumnDo) {


		return  genTableColumnService.page(genTableColumnDo);


	}


	/**
	 * 批量修改
	 *
	 * @param genTableColumnDos 字段信息
	 * @return 是否成功
	 * @since 2024/10/27
	 */
	@SaCheckPermission("generator:gentablecolumn:edit")
	@PostMapping("updateBatch")
	public Boolean updateBatch(@RequestBody List<GenTableColumnDo> genTableColumnDos) {
		genTableColumnService.updateBatch(genTableColumnDos);
		return true;
	}
}