package com.su60.quickboot.system.controller;

import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.util.IdUtil;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.core.PageRequest;
import com.su60.quickboot.common.core.R;
import com.su60.quickboot.common.utils.IpUtils;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.su60.quickboot.system.dos.SysOauthClientDo;
import com.su60.quickboot.system.entity.SysOauthClientEntity;
import com.su60.quickboot.system.service.ISysOauthClientService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

import org.springframework.validation.annotation.Validated;

/**
 * 客户端管理
 *
 * @author luyanan
 * @since 2026/01/21
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/sysoauthclient")

public class SysOauthClientController {


	private final ISysOauthClientService sysOauthClientService;

	/**
	 * 分页查询
	 *
	 * @param sysOauthClientDo 分页参数
	 * @return 分页结果
	 * @since 2026/01/21
	 */
	@SaCheckPermission("system:sysoauthclient:list")
	@GetMapping("list")
	public PageInfo<SysOauthClientDo> page(SysOauthClientDo sysOauthClientDo) {

		return sysOauthClientService.page(sysOauthClientDo);

	}

	/**
	 * 保存
	 *
	 * @param sysOauthClientDo 客户端管理
	 * @return 是否成功
	 * @since 2026/01/21
	 */
	@SneakyThrows
	@SaCheckPermission("system:sysoauthclient:add")
	@PostMapping()
	public Boolean save(@RequestBody @Validated(AddGroup.class) SysOauthClientDo sysOauthClientDo) {


		return sysOauthClientService.save(sysOauthClientDo);
	}


	/**
	 * 根据id修改
	 *
	 * @param sysOauthClientDo 客户端管理
	 * @return 是否成功
	 * @since 2024/06/29
	 */
	@SaCheckPermission("system:sysoauthclient:edit")
	@PostMapping("/update")
	public Boolean updateById(@RequestBody @Validated(UpdateGroup.class) SysOauthClientDo sysOauthClientDo) {
		return sysOauthClientService.updateById(sysOauthClientDo);
	}


	/**
	 * 根据id查询
	 *
	 * @param id id
	 * @return 客户端管理
	 * @since 2026/01/21
	 */
	@SaCheckPermission("system:sysoauthclient:query")
	@GetMapping("/{id}")
	public SysOauthClientDo getById(@PathVariable("id") Long id) {
		return sysOauthClientService.getVoById(id);
	}


	/**
	 * 根据ids 删除
	 *
	 * @param ids 集合
	 * @return 是否成功
	 * @since 2026/01/21
	 */
	@SaCheckPermission("system:sysoauthclient:remove")
	@PostMapping("/delete")
	public Boolean deleteByIds(@RequestBody List<Long> ids) {
		return sysOauthClientService.deleteByIds(ids);
	}


	/**
	 * 修改状态
	 *
	 * @param id     id
	 * @param status 状态
	 * @return
	 * @since 2026/2/8
	 */
	@PostMapping("updateStatus")
	public R updateStatus(@RequestParam(value = "id", required = true) Long id,
						  @RequestParam(value = "status", required = true) String status) {

		sysOauthClientService.updateStatus(id, status);
		return R.ok();
	}

	/**
	 * 生成加密解密的公私要
	 *
	 * @param id id
	 * @return
	 * @since 2026/2/8
	 */
	@PostMapping("generateEncryptionKey/{id}")
	public R generateEncryptionKey(@PathVariable("id") Long id) {

		sysOauthClientService.generateEncryptionKey(id);
		return R.ok();
	}

}