package com.su60.quickboot.generator.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 代码生成业务表字段
 * </p>
 *
 * @author luyanan
 * @since 2024/10/24
 */
@Data
@Accessors(chain = true)
@TableName("gen_table_column")
public class GenTableColumnEntity implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 编号
	 *
	 * @since 2024/10/24
	 */

	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;


	/**
	 * 归属表编号
	 *
	 * @since 2024/10/24
	 */
	@TableField("table_id")
	private Long tableId;


	/**
	 * 列名称
	 *
	 * @since 2024/10/24
	 */
	@TableField("column_name")
	private String columnName;


	/**
	 * 列描述
	 *
	 * @since 2024/10/24
	 */
	@TableField("column_comment")
	private String columnComment;


	/**
	 * 列类型
	 *
	 * @since 2024/10/24
	 */
	@TableField("column_type")
	private String columnType;


	/**
	 * JAVA类型
	 *
	 * @since 2024/10/24
	 */
	@TableField("java_type")
	private String javaType;


	/**
	 * JAVA字段名
	 *
	 * @since 2024/10/24
	 */
	@TableField("java_field")
	private String javaField;


	/**
	 * 是否主键（1是）
	 *
	 * @since 2024/10/24
	 */
	@TableField("is_pk")
	private String isPk;


	/**
	 * 是否自增（1是）
	 *
	 * @since 2024/10/24
	 */
	@TableField("is_increment")
	private String isIncrement;


	/**
	 * 是否必填（1是）
	 *
	 * @since 2024/10/24
	 */
	@TableField("is_required")
	private String isRequired;


	/**
	 * 是否为插入字段（1是）
	 *
	 * @since 2024/10/24
	 */
	@TableField("is_insert")
	private String isInsert;


	/**
	 * 是否编辑字段（1是）
	 *
	 * @since 2024/10/24
	 */
	@TableField("is_edit")
	private String isEdit;


	/**
	 * 是否列表字段（1是）
	 *
	 * @since 2024/10/24
	 */
	@TableField("is_list")
	private String isList;


	/**
	 * 是否查询字段（1是）
	 *
	 * @since 2024/10/24
	 */
	@TableField("is_query")
	private String isQuery;


	/**
	 * 查询方式（等于、不等于、大于、小于、范围）
	 *
	 * @since 2024/10/24
	 */
	@TableField("query_type")
	private String queryType;


	/**
	 * 显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）
	 *
	 * @since 2024/10/24
	 */
	@TableField("html_type")
	private String htmlType;


	/**
	 * 字典类型
	 *
	 * @since 2024/10/24
	 */
	@TableField("dict_type")
	private String dictType;


	/**
	 * 排序
	 *
	 * @since 2024/10/24
	 */
	@TableField("sort")
	private Integer sort;


	/**
	 * 创建者
	 *
	 * @since 2024/10/24
	 */
	@TableField(value = "create_by", fill = FieldFill.INSERT)
	private String createBy;


	/**
	 * 创建时间
	 *
	 * @since 2024/10/24
	 */
	@TableField(value = "create_time", fill = FieldFill.INSERT)
	private Date createTime;


	/**
	 * 更新者
	 *
	 * @since 2024/10/24
	 */
	@TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
	private String updateBy;


	/**
	 * 更新时间
	 *
	 * @since 2024/10/24
	 */
	@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
	private Date updateTime;


}
