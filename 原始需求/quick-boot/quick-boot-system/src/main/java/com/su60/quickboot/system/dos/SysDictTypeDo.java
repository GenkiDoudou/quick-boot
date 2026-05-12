package com.su60.quickboot.system.dos;

import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 字典类型表
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */
@Data
@Accessors(chain = true)
public class SysDictTypeDo implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * 字典主键
	 *
	 * @since 2024/08/07
	 */
	@NotNull(groups = UpdateGroup.class, message = "字典主键 不能为空")
	private Long id;


	/**
	 * 字典名称
	 *
	 * @since 2024/08/07
	 */
	@NotBlank(message = "字典名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
	private String dictName;


	/**
	 * 字典类型
	 *
	 * @since 2024/08/07
	 */
	@NotBlank(message = "字典类型不能为空", groups = {AddGroup.class, UpdateGroup.class})
	private String dictType;


	/**
	 * 状态（0正常 1停用）
	 *
	 * @since 2024/08/07
	 */
	@NotBlank(message = "字典状态不能为空", groups = {AddGroup.class, UpdateGroup.class})
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
