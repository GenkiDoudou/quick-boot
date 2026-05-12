package com.su60.quickboot.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su60.quickboot.common.bean.BeanConvertUtils;
import com.su60.quickboot.common.core.ImportResult;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.exception.WarningException;
import com.su60.quickboot.common.security.PasswordEncoder;
import com.su60.quickboot.core.security.LoginUserUtils;
import com.su60.quickboot.data.datascope.DataPermission;
import com.su60.quickboot.data.excel.ExcelUtils2;
import com.su60.quickboot.data.mybatisplus.BaseVoServiceImpl;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;
import com.su60.quickboot.system.dos.SysDeptDo;
import com.su60.quickboot.system.dos.SysUserDo;
import com.su60.quickboot.system.entity.SysRoleEntity;
import com.su60.quickboot.system.entity.SysUserEntity;
import com.su60.quickboot.system.entity.SysUserRoleEntity;
import com.su60.quickboot.system.mapper.SysUserMapper;
import com.su60.quickboot.system.service.ISysDeptService;
import com.su60.quickboot.system.service.ISysRoleService;
import com.su60.quickboot.system.service.ISysUserRoleService;
import com.su60.quickboot.system.service.ISysUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */
@Service
public class SysUserServiceImpl extends BaseVoServiceImpl<SysUserMapper, SysUserEntity, SysUserDo> implements ISysUserService {

	private final ISysRoleService sysRoleService;
	private final ISysUserRoleService sysUserRoleService;
	private final PasswordEncoder passwordEncoder;
	private final SqlSessionFactory sqlSessionFactory;
	private final ISysDeptService sysDeptService;

	/**
	 * 构造器注入（支持 @Lazy 延迟加载）
	 */
	public SysUserServiceImpl(
			@Lazy ISysRoleService sysRoleService,
			ISysUserRoleService sysUserRoleService,
			PasswordEncoder passwordEncoder,
			SqlSessionFactory sqlSessionFactory,
			ISysDeptService sysDeptService) {
		this.sysRoleService = sysRoleService;
		this.sysUserRoleService = sysUserRoleService;
		this.passwordEncoder = passwordEncoder;
		this.sqlSessionFactory = sqlSessionFactory;
		this.sysDeptService = sysDeptService;
	}

	@Override
	public SysUserDo findByUserName(String username) {
		if (StrUtil.isBlank(username)) {
			return null;
		}
		SysUserEntity sysUserEntity = super.getOne(new LambdaQueryWrapper<SysUserEntity>()
				.eq(SysUserEntity::getUserName, username));
		return BeanConvertUtils.convertTo(sysUserEntity, SysUserDo.class);
	}

	@Override
	public Boolean saveUser(SysUserDo sysUserDo) {
		// 检验userName 唯一
		if (this.count(new LambdaQueryWrapper<SysUserEntity>()
				.eq(SysUserEntity::getUserName, sysUserDo.getUserName())) > 0) {
			throw new WarningException(100000, sysUserDo.getUserName());
		}
		// 密码加密
		sysUserDo.setPassword(passwordEncoder.encode(sysUserDo.getPassword()));
		Boolean b = super.saveVo(sysUserDo);
		// 保存用户和角色关联关系
		List<Long> roleIds =
				sysUserDo.getRoleIds();
		if (CollectionUtil.isNotEmpty(roleIds)) {
			sysRoleService.saveUserRoles(sysUserDo.getId(), sysUserDo.getRoleIds());
		}
		return b;
	}

	@Override
	public Boolean updateUser(SysUserDo sysUserDo) {
		// 检验userName 唯一
		if (this.count(new LambdaQueryWrapper<SysUserEntity>()
				.eq(SysUserEntity::getUserName, sysUserDo.getUserName())
				.ne(SysUserEntity::getId, sysUserDo.getId())) > 0) {
			throw new WarningException(100000, sysUserDo.getUserName());
		}
		// 密码加密
		if (StrUtil.isNotBlank(sysUserDo.getPassword())) {
			sysUserDo.setPassword(passwordEncoder.encode(sysUserDo.getPassword()));
		}
		Boolean b = super.updateVoById(sysUserDo);
		// 保存用户和角色关联关系
		List<Long> roleIds =
				sysUserDo.getRoleIds();
		if (CollectionUtil.isNotEmpty(roleIds)) {
			sysRoleService.saveUserRoles(sysUserDo.getId(), sysUserDo.getRoleIds());
		}
		return b;
	}

	@Override
	public void resetPwd(Long userId) {
		SysUserEntity sysUserEntity = this.baseMapper.selectById(userId);
		if (null == sysUserEntity) {
			throw new WarningException(100000, "用户不存在");
		}
		if (LoginUserUtils.isAdmin(userId)) {
			throw new WarningException(100000, "管理员用户不允许重置密码");
		}
		String defaultPwd = "123456";
		sysUserEntity.setPassword(passwordEncoder.encode(defaultPwd));
		this.updateById(sysUserEntity);
	}

	@Override
	public void updateStatus(Long userId, String status) {
		SysUserEntity sysUserEntity = this.baseMapper.selectById(userId);
		if (null == sysUserEntity) {
			throw new WarningException(100000, "用户不存在");
		}
		SysUserEntity params = new SysUserEntity();
		params.setId(userId);
		params.setStatus(status);
		this.updateById(params);
	}

	@Override
	public void exportExcel(HttpServletResponse response, SysUserDo sysUserDo) throws Exception {
		LambdaQueryWrapper<SysUserEntity> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysUserEntity::getStatus, "0");
		List<SysRoleEntity> roleEntities = sysRoleService.list(new SysRoleEntity());

		ExcelUtils2.builder(response)
				.addSheet(ExcelUtils2.sheet(SysUserDo.class)
						.name("用户信息")
						.page(page -> {
							IPage<SysUserEntity> page1 = new Page(page, 100);
							page1 = this.baseMapper.selectPage(page1, queryWrapper);
							List<SysUserEntity> records = page1.getRecords();

							List<SysUserDo> sysUserDos = BeanConvertUtils.convertListTo(records, SysUserDo.class);
							for (SysUserDo userDo : sysUserDos) {
								List<SysUserRoleEntity> sysUserRoleEntities = sysUserRoleService.listByUserId(userDo.getId());
								// 匹配角色名称
								userDo.setRoleNames(sysUserRoleEntities.stream()
										.map(sysUserRoleEntity -> roleEntities
												.stream()
												.filter(roleEntity -> roleEntity.getId().equals(sysUserRoleEntity.getRoleId()))
												.map(a -> a.getRoleName())
												.findFirst()
												.orElse(null))
										.findFirst()
										.orElse(null));
							}

							return sysUserDos;
						}).build()).export("用户信息");
		;


	}

	@Override
	public Boolean deleteByIds(List<Long> ids) {
		// 删除用户与角色的关联关系
		sysUserRoleService.deleteByUserIds(ids);
		return super.deleteByIds(ids);
	}

	@Override
	public SysUserDo getVoById(Long id) {
		SysUserDo aDo = super.getVoById(id);
		aDo.setPassword(null);
		// 查询关联关系
		List<SysUserRoleEntity> sysUserRoleEntities = sysUserRoleService.listByUserId(id);
		aDo.setRoleIds(sysUserRoleEntities.stream().map(SysUserRoleEntity::getRoleId).collect(Collectors.toList()));
		return aDo;
	}

	@DataPermission(tables = {"sys_user"}, userField = "create_by")
	@Override
	public PageInfo<SysUserDo> page(SysUserDo sysUserDo) {
		return super.page(sysUserDo, new PageVoHandler<SysUserEntity, SysUserDo>() {
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
				List<Long> deptIds = sysUserDos.stream().map(SysUserDo::getDeptId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
				Map<Long, SysDeptDo> sysDeptDoMap = new HashMap<>();
				if (CollectionUtil.isNotEmpty(deptIds)) {
					sysDeptDoMap = sysDeptService.listByIds(deptIds);
				}
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

					// 设置部门
					userDo.setDeptName(sysDeptDoMap.get(userDo.getDeptId()) == null ? null : sysDeptDoMap.get(userDo.getDeptId()).getDeptName());
				}

			}
		});
	}

	@Override
	public ImportResult importExcel(MultipartFile file, Boolean updateSupport) throws IOException {
		ImportResult importResult = new ImportResult();

		if (file == null || file.isEmpty()) {
			importResult.addFailure(1, "上传文件不能为空");
			return importResult;
		}

		// 使用 EasyExcel 读取文件
		EasyExcel.read(file.getInputStream(), SysUserDo.class, new ReadListener<SysUserDo>() {
			@Override
			public void invoke(SysUserDo userDo, AnalysisContext context) {
				// EasyExcel 的 getRowIndex() 返回数据行索引（从0开始，不包括表头）
				// Excel行号 = 数据行索引 + 2（因为第1行是表头，数据从第2行开始）
				int currentRowNum = context.readRowHolder().getRowIndex() + 2;

				try {
					// 验证必填字段
					if (StrUtil.isBlank(userDo.getUserName())) {
						importResult.addFailure(currentRowNum, "用户账号不能为空");
						return;
					}
					if (StrUtil.isBlank(userDo.getNickName())) {
						importResult.addFailure(currentRowNum, "用户昵称不能为空");
						return;
					}

					// 检查用户是否已存在
					SysUserEntity existingUser = getOne(new LambdaQueryWrapper<SysUserEntity>()
							.eq(SysUserEntity::getUserName, userDo.getUserName()));

					if (existingUser != null) {
						// 用户已存在
						if (Boolean.TRUE.equals(updateSupport)) {
							// 更新模式：更新用户信息
							userDo.setId(existingUser.getId());
							// 如果密码为空，不更新密码
							if (StrUtil.isBlank(userDo.getPassword())) {
								userDo.setPassword(null);
							} else {
								// 密码加密
								userDo.setPassword(passwordEncoder.encode(userDo.getPassword()));
							}

							try {
								updateUser(userDo);
								importResult.incrementSuccess();
							} catch (Exception e) {
								importResult.addFailure(currentRowNum, "更新用户失败: " + e.getMessage());
							}
						} else {
							// 不更新模式：跳过
							importResult.addFailure(currentRowNum, "用户账号已存在");
						}
					} else {
						// 新增用户
						// 设置默认密码（如果未提供）
						if (StrUtil.isBlank(userDo.getPassword())) {
							userDo.setPassword("123456"); // 默认密码
						}
						// 设置默认状态（如果未提供）
						if (StrUtil.isBlank(userDo.getStatus())) {
							userDo.setStatus("0"); // 默认正常状态
						}

						try {
							saveUser(userDo);
							importResult.incrementSuccess();
						} catch (Exception e) {
							String errorMsg = e.getMessage();
							if (e instanceof WarningException) {
								errorMsg = ((WarningException) e).getMsg();
							}
							importResult.addFailure(currentRowNum, "保存用户失败: " + errorMsg);
						}
					}
				} catch (Exception e) {
					String errorMsg = e.getMessage();
					if (e instanceof WarningException) {
						errorMsg = ((WarningException) e).getMsg();
					}
					importResult.addFailure(currentRowNum, "处理失败: " + errorMsg);
				}
			}

			@Override
			public void doAfterAllAnalysed(AnalysisContext context) {
				// 所有数据读取完成后的处理
			}
		}).sheet().doRead();

		return importResult;
	}


}

