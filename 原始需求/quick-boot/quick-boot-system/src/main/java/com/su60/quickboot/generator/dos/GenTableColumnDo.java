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
 * 代码生成业务表字段
 * </p>
 *
 * @author luyanan
 * @since 2024/10/24
 */
@Data
@Accessors(chain = true)
public class GenTableColumnDo implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * 编号
	 *
	 * @since 2024/10/24
	 */
	@NotNull(groups = UpdateGroup.class, message = "编号 不能为空")
	private Long id;


	/**
	 * 归属表编号
	 *
	 * @since 2024/10/24
	 */
	private Long tableId;


	/**
	 * 列名称
	 *
	 * @since 2024/10/24
	 */
	private String columnName;


	/**
	 * 列描述
	 *
	 * @since 2024/10/24
	 */
	private String columnComment;


	/**
	 * 列类型
	 *
	 * @since 2024/10/24
	 */
	private String columnType;


	/**
	 * JAVA类型
	 *
	 * @since 2024/10/24
	 */
	private String javaType;


	/**
	 * JAVA字段名
	 *
	 * @since 2024/10/24
	 */
	private String javaField;


	/**
	 * 是否主键（1是）
	 *
	 * @since 2024/10/24
	 */
	private String isPk;


	/**
	 * 是否自增（1是）
	 *
	 * @since 2024/10/24
	 */
	private String isIncrement;


	/**
	 * 是否必填（1是）
	 *
	 * @since 2024/10/24
	 */
	private String isRequired;


	/**
	 * 是否为插入字段（1是）
	 *
	 * @since 2024/10/24
	 */
	private String isInsert;


	/**
	 * 是否编辑字段（1是）
	 *
	 * @since 2024/10/24
	 */
	private String isEdit;


	/**
	 * 是否列表字段（1是）
	 *
	 * @since 2024/10/24
	 */
	private String isList;


	/**
	 * 是否查询字段（1是）
	 *
	 * @since 2024/10/24
	 */
	private String isQuery;


	/**
	 * 查询方式（等于、不等于、大于、小于、范围）
	 *
	 * @since 2024/10/24
	 */
	private String queryType;


	/**
	 * 显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）
	 *
	 * @since 2024/10/24
	 */
	private String htmlType;


	/**
	 * 字典类型
	 *
	 * @since 2024/10/24
	 */
	private String dictType;


	/**
	 * 排序
	 *
	 * @since 2024/10/24
	 */
	private Integer sort;


	/**
	 * 创建者
	 *
	 * @since 2024/10/24
	 */
	@JsonIgnore
	private String createBy;


	/**
	 * 创建时间
	 *
	 * @since 2024/10/24
	 */
	private Date createTime;


	/**
	 * 更新者
	 *
	 * @since 2024/10/24
	 */
	@JsonIgnore
	private String updateBy;


	/**
	 * 更新时间
	 *
	 * @since 2024/10/24
	 */
	@JsonIgnore
	private Date updateTime;


}
