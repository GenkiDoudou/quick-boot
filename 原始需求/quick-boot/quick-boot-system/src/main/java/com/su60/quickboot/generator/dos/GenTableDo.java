package com.su60.quickboot.generator.dos;

import com.su60.quickboot.common.validation.UpdateGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 代码生成业务表
 * </p>
 *
 * @author luyanan
 * @since 2024/10/15
 */
@Data
@Accessors(chain = true)
public class GenTableDo implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * 编号
	 *
	 * @since 2024/10/15
	 */
	@NotNull(groups = UpdateGroup.class, message = "编号 不能为空")
	private Long id;


	/**
	 * 表名称
	 *
	 * @since 2024/10/15
	 */
	private String tableName;


	/**
	 * 表描述
	 *
	 * @since 2024/10/15
	 */
	private String tableComment;


	/**
	 * 实体类名称
	 *
	 * @since 2024/10/15
	 */
	private String className;


	/**
	 * 生成包路径
	 *
	 * @since 2024/10/15
	 */
	private String packageName;


	/**
	 * 生成模块名
	 *
	 * @since 2024/10/15
	 */
	private String moduleName;


	/**
	 * 生成业务名
	 *
	 * @since 2024/10/15
	 */
	private String businessName;


	/**
	 * 生成功能名
	 *
	 * @since 2024/10/15
	 */
	private String functionName;


	/**
	 * 生成功能作者
	 *
	 * @since 2024/10/15
	 */
	private String functionAuthor;


	/**
	 * 生成代码方式（0zip压缩包 1自定义路径）
	 *
	 * @since 2024/10/15
	 */
	private String genType;


	/**
	 * 生成路径（不填默认项目路径）
	 *
	 * @since 2024/10/15
	 */
	private String genPath;


	/**
	 * 创建者
	 *
	 * @since 2024/10/15
	 */
	@JsonIgnore
	private Long createBy;


	/**
	 * 创建时间
	 *
	 * @since 2024/10/15
	 */
	private Date createTime;


	/**
	 * 更新者
	 *
	 * @since 2024/10/15
	 */
	@JsonIgnore
	private Long updateBy;


	/**
	 * 更新时间
	 *
	 * @since 2024/10/15
	 */
	@JsonIgnore
	private Date updateTime;


	/**
	 * 备注
	 *
	 * @since 2024/10/15
	 */
	private String remark;


	/**
	 * 是否校验权限(sys_yes_no)
	 *
	 * @since 2024/12/18
	 */
	private String verifyPermission;


	/**
	 * 父菜单id
	 *
	 * @since 2024/12/18
	 */

	private Long parentId;
}
