package com.su60.quickboot.system.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SysDictExcelVo implements Serializable {
	/**
	 * 字典名称
	 *
	 * @since 2024/08/07
	 */
	@ExcelProperty(value = "字典名称")
	private String dictName;


	/**
	 * 字典类型
	 *
	 * @since 2024/08/07
	 */
	@ExcelProperty(value = "字典类型")
	private String dictType;


	/**
	 * 字典标签
	 *
	 * @since 2024/08/07
	 */
	@ExcelProperty("字典标签")
	private String dictLabel;


	/**
	 * 字典键值
	 *
	 * @since 2024/08/07
	 */
	@ExcelProperty(value = "字典键值")
	private String dictValue;


	/**
	 * 字典排序
	 *
	 * @since 2024/08/07
	 */

	@ExcelProperty(value = "字典排序")
	private Integer dictSort;
}
