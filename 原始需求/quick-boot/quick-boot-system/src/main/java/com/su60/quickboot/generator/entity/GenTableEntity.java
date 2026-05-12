package com.su60.quickboot.generator.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;

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
@TableName("gen_table")
public class GenTableEntity implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 编号
	 *
	 * @since 2024/10/15
	 */

	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;


	/**
	 * 表名称
	 *
	 * @since 2024/10/15
	 */
	@TableField("table_name")
	private String tableName;


	/**
	 * 表描述
	 *
	 * @since 2024/10/15
	 */
	@TableField("table_comment")
	private String tableComment;


	/**
	 * 实体类名称
	 *
	 * @since 2024/10/15
	 */
	@TableField("class_name")
	private String className;


	/**
	 * 生成包路径
	 *
	 * @since 2024/10/15
	 */
	@TableField("package_name")
	private String packageName;


	/**
	 * 生成模块名
	 *
	 * @since 2024/10/15
	 */
	@TableField("module_name")
	private String moduleName;


	/**
	 * 生成业务名
	 *
	 * @since 2024/10/15
	 */
	@TableField("business_name")
	private String businessName;


	/**
	 * 生成功能名
	 *
	 * @since 2024/10/15
	 */
	@TableField("function_name")
	private String functionName;


	/**
	 * 生成功能作者
	 *
	 * @since 2024/10/15
	 */
	@TableField("function_author")
	private String functionAuthor;


	/**
	 * 生成代码方式（0zip压缩包 1自定义路径）
	 *
	 * @since 2024/10/15
	 */
	@TableField("gen_type")
	private String genType;


	/**
	 * 生成路径（不填默认项目路径）
	 *
	 * @since 2024/10/15
	 */
	@TableField("gen_path")
	private String genPath;


	/**
	 * 创建者
	 *
	 * @since 2024/10/15
	 */
	@TableField(value = "create_by", fill = FieldFill.INSERT)
	private Long createBy;


	/**
	 * 创建时间
	 *
	 * @since 2024/10/15
	 */
	@TableField(value = "create_time", fill = FieldFill.INSERT)
	private Date createTime;


	/**
	 * 更新者
	 *
	 * @since 2024/10/15
	 */
	@TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
	private Long updateBy;


	/**
	 * 更新时间
	 *
	 * @since 2024/10/15
	 */
	@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
	private Date updateTime;


	/**
	 * 备注
	 *
	 * @since 2024/10/15
	 */
	@TableField("remark")
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
