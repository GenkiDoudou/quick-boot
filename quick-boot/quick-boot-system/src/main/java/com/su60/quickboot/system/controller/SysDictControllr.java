package com.su60.quickboot.system.controller;

import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import com.su60.quickboot.common.vo.DictVo;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.system.dos.SysDictDataDo;
import com.su60.quickboot.system.dos.SysDictTypeDo;
import com.su60.quickboot.system.entity.SysDictDataEntity;
import com.su60.quickboot.system.entity.SysDictTypeEntity;
import com.su60.quickboot.system.service.ISysDictDataService;
import com.su60.quickboot.system.service.ISysDictTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典项
 *
 * @author luyanan
 * @since 2024/8/18
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("system/dict")
public class SysDictControllr {

	private final ISysDictDataService sysDictDataService;

	private final ISysDictTypeService sysDictTypeService;

	/**
	 * 根据字段类型查询字典项的信息
	 *
	 * @param dictType 字典类型
	 * @return 字典项目列表
	 * @since 2024/8/18
	 */
	@SaCheckPermission("system:dict:list")
	@GetMapping("type/{dictType}")
	public List<SysDictDataDo> dictType(@PathVariable("dictType") String dictType) {
		return sysDictDataService.listByDictType(dictType);
	}

	/**
	 * 字典类型分页接口
	 *
	 * @param sysDictTypeDo 字典类型参数
	 * @return 分页数据
	 * @since 2024/10/11
	 */
	@SaCheckPermission("system:dict:list")
	@GetMapping("type/page")
	public PageInfo<SysDictTypeDo> typePage(SysDictTypeDo sysDictTypeDo) {
		return sysDictTypeService.page(sysDictTypeDo);


	}


	/**
	 * 根据id查询
	 *
	 * @param id id
	 * @return 字典信息
	 * @since 2024/10/12
	 */
	@SaCheckPermission("system:dict:query")
	@GetMapping("type/info/{id}")
	public SysDictTypeDo dictTypeInfo(@PathVariable("id") Long id) {
		return sysDictTypeService.getVoById(id);
	}

	/**
	 * 保存字典类型
	 *
	 * @param sysDictTypeDo 对象
	 * @return 是否成功
	 * @since 2024/10/12
	 */
	@SaCheckPermission("system:dict:add")
	@PostMapping("type")
	public boolean saveDictType(@RequestBody @Validated(AddGroup.class) SysDictTypeDo sysDictTypeDo) {
		return sysDictTypeService.saveVo(sysDictTypeDo);
	}


	/**
	 * 修改字典类型
	 *
	 * @param sysDictTypeDo 对象
	 * @return 是否成功
	 * @since 2024/10/12
	 */
	@SaCheckPermission("system:dict:edit")
	@PutMapping("type")
	public boolean updateDictType(@RequestBody @Validated SysDictTypeDo sysDictTypeDo) {
		return sysDictTypeService.updateVoById(sysDictTypeDo);
	}

	/**
	 * 删除字典类型
	 *
	 * @param ids id集合
	 * @return 是否成功
	 * @since 2024/10/12
	 */
	@SaCheckPermission("system:dict:remove")
	@DeleteMapping("type")
	public boolean deleteDictType(@RequestBody List<Long> ids) {
		return sysDictTypeService.deleteByIds(ids);
	}


	/**
	 * 字典数据分页
	 *
	 * @param sysDictDataDo 字典项
	 * @return 分页
	 * @since 2024/10/13
	 */
	@SaCheckPermission("system:dict:list")
	@GetMapping("/data/page")
	public PageInfo<SysDictDataDo> dataPage(SysDictDataDo sysDictDataDo) {
		return sysDictDataService.page(sysDictDataDo);

	}

	/**
	 * 字典项保存
	 *
	 * @param sysDictDataDo 字典项
	 * @return 是否成功
	 * @since 2024/10/13
	 */
	@SaCheckPermission("system:dict:add")
	@PostMapping("data")
	public Boolean dataSave(@RequestBody @Validated(AddGroup.class) SysDictDataDo sysDictDataDo) {
		return sysDictDataService.saveVo(sysDictDataDo);
	}

	/**
	 * 字典项修改
	 *
	 * @param sysDictDataDo 字典项
	 * @return 是否成功
	 * @since 2024/11/12
	 */
	@SaCheckPermission("system:dict:edit")
	@PutMapping("data")
	public Boolean dataUpdate(@RequestBody @Validated(UpdateGroup.class) SysDictDataDo sysDictDataDo) {
		return sysDictDataService.updateVoById(sysDictDataDo);
	}


	/**
	 * 字典项删除
	 *
	 * @param ids id集合
	 * @return 是否成功
	 * @since 2024/10/13
	 */
	@SaCheckPermission("system:dict:remove")
	@DeleteMapping("data")
	public Boolean dataDelete(@RequestBody List<Long> ids) {
		return sysDictDataService.deleteByIds(ids);
	}

	/**
	 * 根据id查询字典项
	 *
	 * @param id id
	 * @return 字典项
	 * @since 2024/10/13
	 */
	@SaCheckPermission("system:dict:query")
	@GetMapping("data/{id}")
	public SysDictDataDo getDictData(@PathVariable("id") Long id) {
		return sysDictDataService.getVoById(id);
	}


	/**
	 * 查询所有的字典项
	 *
	 * @return 字典项列表
	 * @since 2024/10/26
	 */
	@SaCheckPermission("system:dict:list")
	@GetMapping("types")
	public List<DictVo> types() {
		return sysDictTypeService.listAll().stream().map(item -> new DictVo()
				.setValue(item.getDictType())
				.setLabel(item.getDictName())).toList();
	}


	/**
	 * excel导出
	 * @since 2025/12/20 
	 * @param dictTypeDo
	 * @param response
	 * @return
	 */

	@PostMapping("export")
	public void export(SysDictTypeDo dictTypeDo, HttpServletResponse response) throws Exception {

		sysDictTypeService.export(dictTypeDo, response);
	}
}
