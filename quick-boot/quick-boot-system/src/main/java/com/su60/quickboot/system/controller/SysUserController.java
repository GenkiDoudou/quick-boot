package com.su60.quickboot.system.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.core.R;
import com.su60.quickboot.common.core.ImportResult;
import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.data.spring.restful.annotation.NoRestFul;
import com.su60.quickboot.system.dos.SysUserDo;
import com.su60.quickboot.system.entity.SysUserEntity;
import com.su60.quickboot.system.service.ISysRoleService;
import com.su60.quickboot.system.service.ISysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统用户
 *
 * @author luyanan
 * @since 2024/8/7
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("sys/user")
@RestController
public class SysUserController {


	private final ISysUserService sysUserService;


	private final ISysRoleService sysRoleService;

	/**
	 * 用户分页
	 *
	 * @param sysUserDo 系统用户
	 * @return 分页信息
	 * @since 2024/10/14
	 */
	@SaCheckPermission("system:user:list")
	@GetMapping("page")
	public PageInfo<SysUserDo> page(SysUserDo sysUserDo) {

		return sysUserService.page(sysUserDo, new PageVoHandler<SysUserEntity, SysUserDo>() {
			@Override
			public void queryWrapperHandler(SysUserDo vo, SysUserEntity sysUserEntity, LambdaQueryWrapper<SysUserEntity> queryWrapper) {
				queryWrapper.orderByDesc(SysUserEntity::getCreateTime);
				//  用户名搜索

				queryWrapper.like(StrUtil.isNotBlank(vo.getUserName()), SysUserEntity::getUserName, vo.getUserName());
				sysUserEntity.setUserName(null);
				// 姓名搜索
				queryWrapper.like(StrUtil.isNotBlank(vo.getNickName()), SysUserEntity::getNickName, vo.getNickName());
				sysUserEntity.setNickName(null);


			}

			@Override
			public void recordsHandler(List<SysUserEntity> sysUserEntities, List<SysUserDo> sysUserDos) {
				Map<Long, List<Long>> userRoleIdsMap = new HashMap<>();
				Set<Long> allRoleIds = new HashSet<>();
				for (SysUserDo userDo : sysUserDos) {
					userDo.setPassword(null);
					// 查询关联的角色
					List<Long> roleIds = sysRoleService.listRoleIdUserId(userDo.getId());
					allRoleIds.addAll(roleIds);
					userRoleIdsMap.put(userDo.getId(), roleIds);
				}

				Map<Long, String> roleMap = sysRoleService.getVoByIds(allRoleIds).stream().collect(Collectors.toMap(k -> k.getId(), v -> v.getRoleName()));
				for (SysUserDo userDo : sysUserDos) {
					List<Long> roleIds = userRoleIdsMap.get(userDo.getId());
					// 设置角色名称
					userDo.setRoleIds(roleIds);
					userDo.setRoleNames(roleIds.stream().map(roleMap::get).collect(Collectors.joining(",")));
				}

			}
		});

	}


	/**
	 * 用户信息保存
	 *
	 * @param sysUserDo 用户 信息
	 * @return 是否成功
	 * @since 2024/10/14
	 */
	@SaCheckPermission("system:user:add")
	@PostMapping()
	public Boolean save(@RequestBody @Validated(AddGroup.class) SysUserDo sysUserDo) {

		return sysUserService.saveUser(sysUserDo);
	}

	/**
	 * 根据id修改
	 *
	 * @param sysUserDo 用户信息表
	 * @return 是否成功
	 * @since 2024/06/29
	 */
	@SaCheckPermission("system:user:edit")
	@PutMapping
	public Boolean updateById(@RequestBody @Validated(UpdateGroup.class) SysUserDo sysUserDo) {
		return sysUserService.updateUser(sysUserDo);
	}


	/**
	 * 根据id查询
	 *
	 * @param id id
	 * @return 用户信息
	 * @since 2024/10/31
	 */
	@SaCheckPermission("system:user:query")
	@GetMapping("/{id}")
	public SysUserDo getById(@PathVariable("id") Long id) {
		return sysUserService.getVoById(id);
	}


	/**
	 * 根据ids 删除
	 *
	 * @param ids 多个以英文逗号(,)分割
	 * @return 是否成功
	 * @since 2024/10/31
	 */
	@SaCheckPermission("system:user:remove")
	@PostMapping("/delete")
	public Boolean deleteByIds(@RequestBody List<Long> ids) {
		return sysUserService.deleteByIds(ids);
	}


	/**
	 * 重置密码
	 * @since 2025/11/18
	 * @param userId 用户id
	 * @return
	 */
	@PostMapping("resetPwd/{userId}")
	public R<Void> resetPwd(@PathVariable("userId") Long userId) {


		sysUserService.resetPwd(userId);
		return R.ok();
	}

	/**
	 * 修改状态
	 * @since 2025/11/19
	 * @param userId 用户id
	 * @param status  状态
	 * @return
	 */
	@PostMapping("updateStatus/{userId}/{status}")
	public R<Void> updateStatus(@PathVariable("userId") Long userId, @PathVariable("status") String status) {
		sysUserService.updateStatus(userId, status);
		return R.ok();
	}


	/**
	 * 导出excel
	 * @since 2025/11/21
	 * @param request
	 * @param response
	 * @param sysUserDo
	 * @return
	 */
	@ApiOperation(value = "导出excel")
//	@PostMapping("exportExcel")
	@NoRestFul
	@PostMapping("exportExcel")
	public void exportExcel(HttpServletRequest request, HttpServletResponse response, SysUserDo sysUserDo) throws Exception {
		sysUserService.exportExcel(response, sysUserDo);
		// 不需要返回值，因为响应已经在exportExcel方法中处理了
	}

	/**
	 * 导入excel
	 * @since 2025/11/29
	 * @param file Excel文件
	 * @param updateSupport 是否更新已存在的数据
	 * @return 导入结果
	 */
	@ApiOperation(value = "导入excel")
	@SaCheckPermission("system:user:import")
	@PostMapping("importExcel")
	public R<ImportResult> importExcel(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws IOException {
		ImportResult importResult = sysUserService.importExcel(file, updateSupport);
		return R.ok(importResult);
	}
}
