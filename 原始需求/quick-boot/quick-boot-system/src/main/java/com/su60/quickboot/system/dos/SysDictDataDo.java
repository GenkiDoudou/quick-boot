package com.su60.quickboot.system.dos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 字典数据表
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */
@Data
@Accessors(chain = true)
public class SysDictDataDo implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * 字典编码
	 *
	 * @since 2024/08/07
	 */
	@NotNull(groups = UpdateGroup.class, message = "字典编码 不能为空")
	private Long id;


	/**
	 * 字典排序
	 *
	 * @since 2024/08/07
	 */
	@NotNull(groups = {AddGroup.class, UpdateGroup.class}, message = "字典排序不能为空")
	private Integer dictSort;


	/**
	 * 字典标签
	 *
	 * @since 2024/08/07
	 */
	@NotBlank(message = "字典标签不能为空", groups = {AddGroup.class, UpdateGroup.class})
	private String dictLabel;


	/**
	 * 字典键值
	 *
	 * @since 2024/08/07
	 */
	@NotBlank(message = "字典键值不能为空", groups = {AddGroup.class, UpdateGroup.class})
	private String dictValue;


	/**
	 * 字典类型
	 *
	 * @since 2024/08/07
	 */
	@NotBlank(message = "字典类型不能为空", groups = {AddGroup.class, UpdateGroup.class})
	private String dictType;


	/**
	 * 样式属性（其他样式扩展）
	 *
	 * @since 2024/08/07
	 */
	private String cssClass;


	/**
	 * 表格回显样式
	 *
	 * @since 2024/08/07
	 */
	private String listClass;


	/**
	 * 是否默认（Y是 N否）
	 *
	 * @since 2024/08/07
	 */
	private String isDefault;


	/**
	 * 状态（0正常 1停用）
	 *
	 * @since 2024/08/07
	 */
	@NotBlank(message = "状态不能为空", groups = {AddGroup.class, UpdateGroup.class})
	private String status;


	/**
	 * 创建者
	 *
	 * @since 2024/08/07
	 */
	@JsonIgnore
	private Long createBy;


	/**
	 * 创建时间
	 *
	 * @since 2024/08/07
	 */
	private Date createTime;


	/**
	 * 更新者
	 *
	 * @since 2024/08/07
	 */
	@JsonIgnore
	private Long updateBy;


	/**
	 * 更新时间
	 *
	 * @since 2024/08/07
	 */
	@JsonIgnore
	private Date updateTime;


	/**
	 * 备注
	 *
	 * @since 2024/08/07
	 */
	private String remark;


}
