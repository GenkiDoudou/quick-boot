package com.su60.quickboot.common.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel导入结果
 *
 * @author luyanan
 * @since 2025/11/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult {

	/**
	 * 成功条数
	 */
	private Integer successNum = 0;

	/**
	 * 失败条数
	 */
	private Integer failureNum = 0;

	/**
	 * 失败数据列表
	 */
	private List<FailureData> failureList = new ArrayList<>();

	/**
	 * 失败数据
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FailureData {
		/**
		 * 行号（Excel中的行号，从1开始）
		 */
		private Integer rowNum;

		/**
		 * 失败原因
		 */
		private String errorMsg;
	}

	/**
	 * 添加失败数据
	 *
	 * @param rowNum   行号
	 * @param errorMsg 错误信息
	 */
	public void addFailure(Integer rowNum, String errorMsg) {
		this.failureNum++;
		this.failureList.add(new FailureData(rowNum, errorMsg));
	}

	/**
	 * 增加成功条数
	 */
	public void incrementSuccess() {
		this.successNum++;
	}
}

