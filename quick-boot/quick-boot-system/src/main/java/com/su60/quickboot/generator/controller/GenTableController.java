package com.su60.quickboot.generator.controller;

import cn.hutool.core.map.MapUtil;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.generator.dos.GenTableDo;
import com.su60.quickboot.generator.dos.GenTablePreviewVo;
import com.su60.quickboot.generator.entity.GenTableEntity;
import com.su60.quickboot.generator.service.IGenTableService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 代码生成业务表
 *
 * @author luyanan
 * @since 2024/10/15
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/generator/gentable")

public class GenTableController {


	private final IGenTableService genTableService;

	/**
	 * 分页查询
	 *
	 * @param genTableDo 分页参数
	 * @return 分页信息
	 * @since 2024/10/15
	 */
	@SaCheckPermission("generator:gen:list")
	@GetMapping("list")
	public PageInfo<GenTableDo> page(GenTableDo genTableDo) {


		return  genTableService.page(genTableDo);
	}


	/**
	 * 保存
	 *
	 * @param genTableDo 代码生成业务表
	 * @return 是否成功
	 * @since 2024/10/15
	 */
	@SaCheckPermission("generator:gen:add")
	@PostMapping("save")
	public Boolean save(@RequestBody @Validated(AddGroup.class) GenTableDo genTableDo) {
		return genTableService.saveVo(genTableDo);
	}


	/**
	 * 根据id修改
	 *
	 * @param genTableDo 代码生成业务表
	 * @return 是否成功
	 * @since 2024/06/29
	 */
	@SaCheckPermission("generator:gen:edit")
	@PutMapping
	public Boolean updateById(@RequestBody @Validated(UpdateGroup.class) GenTableDo genTableDo) {
		return genTableService.updateVoById(genTableDo);
	}


	/**
	 * 根据id查询
	 *
	 * @param id id
	 * @return 代码生成信息
	 * @since 2024/10/15
	 */
	@SaCheckPermission("generator:gen:query")
	@GetMapping("/{id}")
	public GenTableDo getById(@PathVariable("id") Long id) {
		return genTableService.getVoById(id);
	}


	/**
	 * 根据ids 删除
	 *
	 * @param ids 多个以英文逗号(,)分割
	 * @return 是否成功
	 * @since 2024/10/15
	 */
	@SaCheckPermission("generator:gen:remove")
	@DeleteMapping()

	public Boolean deleteByIds(@RequestBody List<Long> ids) {
		return genTableService.deleteByIds(ids);
	}


	/**
	 * 根据ids 删除
	 *
	 * @param id 多个以英文逗号(,)分割
	 * @return 是否成功
	 * @since 2024/10/15
	 */
	@SaCheckPermission("generator:gen:remove")
	@DeleteMapping("/{id}")
	public Boolean deleteById(@PathVariable("id") Long id) {
		genTableService.deleteByIds(Arrays.asList(id));
		return true;
	}

	/**
	 * 根据sql创建表
	 *
	 * @param map sql内容
	 * @return 是否成功
	 * @since 2024/10/23
	 */
	@SaCheckPermission("generator:gen:import")
	@PostMapping("createTableSave")
	public Boolean createTableSave(@RequestBody Map<String, Object> map) {
		String content = MapUtil.getStr(map, "content");

		return genTableService.createTable(content);
	}


	/**
	 * 从数据库导入表
	 *
	 * @param tableDo 参数
	 * @return 分页信息
	 * @since 2024/10/24
	 */
	@SaCheckPermission("generator:gen:import")
	@GetMapping("dbTables")
	public PageInfo<GenTableDo> dbTables(GenTableDo tableDo) {
		return genTableService.dbTables(tableDo);
	}


	/**
	 * 导入表
	 *
	 * @param tableNames 表名称
	 * @return 是否成功
	 * @since 2024/10/24
	 */
	@SaCheckPermission("generator:gen:import")
	@PostMapping("importTable")
	public Boolean importTable(@RequestBody List<String> tableNames) {
		genTableService.save(tableNames);
		return true;
	}


	/**
	 * 表信息同步
	 *
	 * @param tableId 表信息
	 * @return 是否成功
	 * @since 2024/10/27
	 */
	@SaCheckPermission("generator:gen:edit")
	@GetMapping("tableSyn/{tableId}")
	public Boolean tableSyn(@PathVariable("tableId") Long tableId) {
		genTableService.tableSyn(tableId);
		return true;
	}

	/**
	 * 代码生成
	 *
	 * @param tableIds 表id
	 * @param response 响应体
	 * @return 是否成功
	 * @since 2024/10/27
	 */
	@SaCheckPermission("generator:gen:code")
	@GetMapping("generator")
	public Boolean generator(String tableIds, HttpServletResponse response) throws IOException {
		genTableService.generator(Arrays.stream(tableIds.split(",")).map(Long::valueOf).toList(), response, "1");
		return true;
	}

	/**
	 * 代码生成(下载)
	 *
	 * @param tableIds 表id
	 * @param response 响应
	 * @since 2024/10/27
	 */
	@GetMapping("generator/download")
	public void generatorDownload(String tableIds, HttpServletResponse response) throws IOException {
		genTableService.generator(Arrays.stream(tableIds.split(",")).map(Long::valueOf).toList(), response, "0");
	}


	/**
	 * 预览
	 *
	 * @param tableId 表id
	 * @return 预览信息
	 * @since 2024/10/30
	 */
	@SaCheckPermission("generator:gen:preview")
	@GetMapping("preview/{tableId}")
	public List<GenTablePreviewVo> preview(@PathVariable("tableId") Long tableId) {
		return genTableService.preview(tableId);
	}

}