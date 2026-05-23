package com.su60.quickboot.demo.dos;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 测试实体类
 *
 * @since 2024/11/16
 */


@Data
@Accessors(chain = true)
public class DemoCrudDos implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 *
	 * @since 2024/11/16
	 */

	private Long id;


	/**
	 * input
	 *
	 * @since 2024/11/16
	 */

	private String input;

	/**
	 * 数字
	 *
	 * @since 2024/11/16
	 */

	private Integer inputNumber;

	/**
	 * 单选框
	 *
	 * @since 2024/11/16
	 */

	private String radio;

	/**
	 * 下拉选
	 *
	 * @since 2024/11/16
	 */

	private String select;

	/**
	 * 远程下拉
	 *
	 * @since 2024/11/16
	 */

	private String remoteSelect;

	/**
	 * 树形菜单
	 *
	 * @since 2024/11/16
	 */

	private String tree;

	/**
	 * 图片
	 *
	 * @since 2024/11/16
	 */

	private String image;


	/**
	 * 文件
	 *
	 * @since 2024/11/16
	 */

	private String file;


	/**
	 * 日期
	 *
	 * @since 2024/11/16
	 */

	@DateTimeFormat(pattern = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
	@JsonFormat(pattern = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
	private Date datetime;


	/**
	 * 多选框
	 *
	 * @since 2024/11/16
	 */

	private String checkBox;

	/**
	 * 日期选择器
	 *
	 * @since 2024/11/16
	 */

	private String dataPicker;


	/**
	 * 日期时间选择器
	 *
	 * @since 2024/11/16
	 */

	private String dataTimePicker;
}
